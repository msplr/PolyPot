import machine
import struct

def sensor_pwr(enable):
    SENSOR_ENn = machine.Pin(25, machine.Pin.OUT)
    if enable:
        SENSOR_ENn.value(0)
    else:
        SENSOR_ENn.value(1)


pin_moist = machine.Pin(35, machine.Pin.IN)
adc_moist = machine.ADC(pin_moist) # ADC1 CH 7
adc_moist.atten(machine.ADC.ATTN_11DB)
adc_moist.read()

pin_battery = machine.Pin(32, machine.Pin.IN)
adc_battery = machine.ADC(pin_battery) # ADC1 CH 4

pin_luminosity = machine.Pin(33, machine.Pin.IN)
adc_luminosity = machine.ADC(pin_luminosity) # ADC1 CH 5

adc_luminosity.read()


i2c = machine.I2C(scl=machine.Pin(16), sda=machine.Pin(4), freq=100000)
i2c.scan()

# temperature sensor
i2c.writeto_mem(72, 0x01, b'\x00') # write control register
temp = i2c.readfrom_mem(72, 0x00, 2)
temp = struct.unpack('>h', temp) # MSByte first -> 16 bit big endian
(temp, ) = temp # upack tuple
temp = temp / 256

# TOF distance sensor
TOF_RESETn = machine.Pin(17, machine.Pin.OUT)
TOF_RESETn.value(1) # enable TOF sensor
