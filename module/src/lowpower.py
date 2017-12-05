import machine
import esp32

# wakeup from pin 23 not possible
# fix: wire from pin 23 to pin 15

def sleep(sec):
    button = machine.Pin(15)
    button.init(machine.Pin.IN, machine.Pin.PULL_UP)
    esp32.wake_on_ext0(button, level=0)
    machine.deepsleep(1000*sec)
