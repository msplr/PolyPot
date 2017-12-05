import machine

def water_plant(time_ms, duty=100):
    # IO27 to pwm
    pump = machine.Pin(27);
    duty = int(1023 * duty / 100)
    pwm = machine.PWM(pump, freq=1000, duty=duty)
    # machine.sleep(time_ms);
    time.sleep(time_ms)
    pwm.deinit()
    pump.init(machine.Pin.OUT)
    pump.value(0)
