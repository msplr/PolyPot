import machine
import esp32

# wakeup from pin 23 not possible
# fix: wire from pin 23 to pin 15

def sleep(sec):
    button = machine.Pin(23, machine.Pine.IN)
    button = machine.Pin(15)
    button.init(machine.Pin.IN, machine.Pin.PULL_UP)
    esp32.wake_on_ext0(button, level=0)
    machine.deepsleep(1000*sec)


green_led_pin = machine.Pin(22, machine.Pin.OUT)
red_led_pin = machine.Pin(21, machine.Pin.OUT)

def led_green():
    green_led_pin.value(1)
    red_led_pin.value(0)

def led_orange():
    green_led_pin.value(1)
    red_led_pin.value(1)

def led_red():
    green_led_pin.value(0)
    red_led_pin.value(1)

def led_off():
    green_led_pin.value(0)
    red_led_pin.value(0)

button_pin = machine.Pin(15, machine.Pin.IN)

def button():
    if button_pin.value() == 0:
        return True
    else:
        return False

class Pump():
    """Water Pump class"""
    def __init__(self, pin):
        self.pin = pin
        self.pin.init(machine.Pin.OUT)
        self.off()

    def on(self):
        self.pin.value(1)

    def off(self):
        self.pin.value(0)

pump_pin = machine.Pin(27)
water_pump = Pump(pump_pin)

