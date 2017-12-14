import struct

class VL6180X():
    """VL6180X TOF distance sensor driver """
    ADDR = 0x29
    # Register configuration as in application note AN4545 : VL6180X basic ranging
    REG_CONFIG = [
        # Mandatory: Private registers.
        (0x0207, 0x01),(0x0208, 0x01),(0x0096, 0x00),(0x0097, 0xfd),
        (0x00e3, 0x00),(0x00e4, 0x04),(0x00e5, 0x02),(0x00e6, 0x01),
        (0x00e7, 0x03),(0x00f5, 0x02),(0x00d9, 0x05),(0x00db, 0xce),
        (0x00dc, 0x03),(0x00dd, 0xf8),(0x009f, 0x00),(0x00a3, 0x3c),
        (0x00b7, 0x00),(0x00bb, 0x3c),(0x00b2, 0x09),(0x00ca, 0x09),
        (0x0198, 0x01),(0x01b0, 0x17),(0x01ad, 0x00),(0x00ff, 0x05),
        (0x0100, 0x05),(0x0199, 0x05),(0x01a6, 0x1b),(0x01ac, 0x3e),
        (0x01a7, 0x1f),(0x0030, 0x00),
        # Recommended : Public registers - See data sheet for more detail */
        (0x0011, 0x10), # Enables polling for New Sample ready when measurement completes
        (0x010a, 0x30), # Set the averaging sample period (compromise between lower noise and increased execution time)
        (0x003f, 0x46), # Sets the light and dark gain (upper nibble). Dark gain should not be changed.
        (0x0031, 0xFF), # Sets the # of range measurements after which auto calibration of system is performed
        (0x0040, 0x63), # Set ALS integration time to 100ms
        (0x002e, 0x01), # perform a single temperature calibration of the ranging sensor
        (0x0014, 0x24), # Configure interrupt on new sample ready. Required for polling to work.
    ]
    VL6180X_SYSTEM_INTERRUPT_CLEAR = 0x15
    VL6180X_SYSRANGE_START = 0x18
    VL6180X_RESULT_RANGE_STATUS = 0x4d
    VL6180X_RESULT_INTERRUPT_STATUS_GPIO = 0x4f
    VL6180X_RESULT_RANGE_VAL = 0x62

    def __init__(self, i2c, reset_pin):
        super(VL6180X, self).__init__()
        self.i2c = i2c
        self.reset = reset_pin

    def start(self):
        # enable chip
        self.reset.value(1)

        while self.read_reg(0x16) != 0x01:
            pass

        # clear SYSTEM__FRESH_OUT_OF_RESET
        self.write_reg(0x016, 0x00)

        for reg in self.REG_CONFIG:
            self.write_reg(*reg)

    def stop(self):
        self.reset.value(0)

    def write_reg(self, reg, byte):
        byte = struct.pack('B', byte)
        self.i2c.writeto_mem(self.ADDR, reg, byte, addrsize=16)

    def read_reg(self, reg):
        data = self.i2c.readfrom_mem(self.ADDR, reg, 1, addrsize=16)
        (data, ) = struct.unpack('B', data)
        return data

    def wait_ready(self, timeout=100000):
        while timeout > 0 and self.read_reg(self.VL6180X_RESULT_RANGE_STATUS) & 1 == 0:
            timeout -= 1
        if timeout <= 0:
            return False
        else:
            return True

    def start_range(self):
        self.write_reg(self.VL6180X_SYSRANGE_START, 0x01)

    def poll_range(self):
        status = self.read_reg(self.VL6180X_RESULT_INTERRUPT_STATUS_GPIO)
        while status & 0x07 != 4:
            status = self.read_reg(self.VL6180X_RESULT_INTERRUPT_STATUS_GPIO)

    def read_range(self):
        return self.read_reg(self.VL6180X_RESULT_RANGE_VAL)

    def clear_interrupts(self):
        # Clear interrupt flags
        self.write_reg(self.VL6180X_SYSTEM_INTERRUPT_CLEAR, 0x07)

    def distance(self):
        self.wait_ready()
        self.start_range()
        self.poll_range()
        mm = self.read_range()
        self.clear_interrupts()
        return mm
