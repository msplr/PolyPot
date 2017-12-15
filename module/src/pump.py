import machine

class Pump():
    """Water Pump class"""
    def __init__(self, pin):
        self.pin = pin
        self.pwm = None
        self.off()

    def on(self, duty=1.0):
        duty = int(1023 * duty)
        if self.pwm is None:
            self.pwm = machine.PWM(self.pin, freq=1000, duty=duty)
        else:
            self.pwm.duty(duty)

    def off(self):
        if self.pwm is not None:
            self.pwm.deinit()

        self.pin.init(machine.Pin.OUT)
        self.pin.value(0)
