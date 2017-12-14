import machine

led_green = machine.Pin(22, machine.Pin.OUT)
led_red = machine.Pin(21, machine.Pin.OUT)

def LED(status):
    if status == "green":
        led_green.value(1)
        led_red.value(0)
    elif status == "orange":
        led_green.value(1)
        led_red.value(1)
    elif status == "red":
        led_green.value(0)
        led_red.value(1)
    else:
        led_green.value(0)
        led_red.value(0)
