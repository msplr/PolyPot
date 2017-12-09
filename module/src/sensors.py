import machine

def sensor_pwr(enable):
    SENSOR_ENn = machine.Pin(25, machine.Pin.OUT)
    if enable:
        SENSOR_ENn.value(0)
    else:
        SENSOR_ENn.value(1)

pin_moist = machine.Pin(35, machine.Pin.IN)
pin_battery = machine.Pin(32, machine.Pin.IN)
pin_luminosity = machine.Pin(33, machine.Pin.IN)

adc_moist = machine.ADC(pin_moist) # ADC1 CH 7
adc_battery = machine.ADC(pin_battery) # ADC1 CH 4
adc_luminosity = machine.ADC(pin_luminosity) # ADC1 CH 5

adc_luminosity.read()

