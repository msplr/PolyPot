mport struct
import machine
import VL6180X
import time

SENSOR_ENn = machine.Pin(25, machine.Pin.OUT)

def power_enable():
    SENSOR_ENn.value(0)

def power_disable():
    SENSOR_ENn.value(1)

i2c = machine.I2C(scl=machine.Pin(16), sda=machine.Pin(4), freq=100000)
i2c.scan()

moist_pin = machine.Pin(35, machine.Pin.IN)
moist_adc = machine.ADC(moist_pin) # ADC1 CH 7
moist_adc.atten(machine.ADC.ATTN_11DB)

def moisture():
    """Read the moisture sensor voltage"""
    return 3.3 * moist_adc.read()/1023

battery_pin = machine.Pin(32, machine.Pin.IN)
battery_adc = machine.ADC(battery_pin) # ADC1 CH 4
battery_adc.atten(machine.ADC.ATTN_11DB)

def battery_voltage():
    """Read the total battery voltage"""
    return 3 * 3.3 * battery_adc.read()/1023

luminosity_pin = machine.Pin(33, machine.Pin.IN)
luminosity_adc = machine.ADC(luminosity_pin) # ADC1 CH 5
luminosity_adc.atten(machine.ADC.ATTN_11DB)

LUMINOSITY_GAIN = 6/0.07 # [Lux/V]

def luminosity():
    """Read the luminosity sensor in Lux (approximatively)."""
    return 3.3 * luminosity_adc.read()/1023 * LUMINOSITY_GAIN

TOF_ADDR = 41
TOF_RESETn = machine.Pin(17, machine.Pin.OUT)
tof = VL6180X.VL6180X(i2c, TOF_RESETn)
DIST_FULL = 20
DIST_EMPTY = 118

def water_level():
    """Read water level in percent."""
    dist = tof.distance()
    level = 100*(dist - DIST_EMPTY)/(DIST_FULL - DIST_EMPTY)
    if level > 100.0:
        level = 100.0
    elif level < 0.0:
        level = 0.0
    return level

TEMP_ADDR = 72

def temperature():
    """Read temperature in deg Celsius."""
    i2c.writeto_mem(TEMP_ADDR, 0x01, b'\x00') # write control register, enable
    temp = i2c.readfrom_mem(TEMP_ADDR, 0x00, 2)
    temp = struct.unpack('>h', temp) # MSByte first -> 16 bit big endian
    (temp, ) = temp # upack tuple
    temp = temp / 256
    return temp

def start():
    power_enable()
    tof.start()

    addr = i2c.scan()
    # todo: check if all sensor present

def stop():
    tof.stop()
    power_disable()

def read_all():
    data = {}
    data['luminosity'] = luminosity()
    data['soil_moisture'] = moisture()
    #data['battery_level'] = battery_voltage()
    data['battery_level'] = 56
    data['water_level'] = water_level()
    data['temperature'] = temperature()
    return data