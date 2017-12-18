import network
import urequests as requests
import ujson as json
import usocket as socket
import time
import embeded_ntptime as ntptime
import board

WIFI_TIMEOUT = 5

# Activates or reactivates the AP
def AP_activation(ap=None):
    if ap:
        ap.active(True)
        ap.config(essid="PolyPot", password="setupPolyPot", authmode=network.AUTH_WPA_WPA2_PSK)
    else:
        ap = network.WLAN(network.AP_IF)
        ap.active(True)
        ap.ifconfig(("192.168.1.1", "255.255.255.0", "192.168.4.1", "8.8.8.8"))
        ap.config(essid="PolyPot", password="setupPolyPot", authmode=network.AUTH_WPA_WPA2_PSK)
        return ap

# Gets the wifi config in json format when the pot is in AP
def get_post():
    Loop=False
    # Setup the socket
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]
    s    = socket.socket()

    # Some error might appear at this point if the antenna surrounded by too much material
    while True:
        try:
            s.bind(addr)
        except:
            print("ERROR: Check if the antenna is free\n")
            continue
        else:
            break

    # Reciving the post request
    s.listen(1)
    cl, addr = s.accept()
    cl_file  = cl.makefile('rwb', 0)

    # Start reading
    config_ini     = ""
    content_length = 0

    # Discard first line with different format
    line = cl_file.readline()

    # Reading the header to get content length
    while True:
        line = cl_file.readline()
        if not line or line == b'\r\n':
            break
        (header, value) = line.decode().split(":", 1)
        if header == "Content-Length":
            content_length = int(value)

    # Reading json file
    config_ini = cl_file.read(content_length).decode()

    # Answer and closing socket
    response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 2\r\n\r\n{}"
    while True:
        try:
            cl.send(response)
        except:
            print("ERROR while responding to the post request, check teh antenna exposure\n")
            continue
        else:
            break
    cl.close()
    s.close()
    return config_ini

# Downloads the setups from the phone and returns them as a dictionnary
def setup():
    wifi_param_json = get_post()
    wifi_param      = json.loads(wifi_param_json)
    return wifi_param

# Creates the wifi
def wifi_init():
    wlan = network.WLAN(network.STA_IF)
    return wlan

# Tries to connect to the wifi
def wifi_connect(wifi_param, wlan, ap=None):
    status = True
    if ap:
        ap.active(False)
        del(ap)
    wlan.active(True)

    # Try connection
    wlan.connect(wifi_param['ssid'], wifi_param['password'])
    time.sleep(WIFI_TIMEOUT)
    if not wlan.isconnected():
        wlan.disconnect()
        wlan.active(Flase)
        del(wlan)
        status = False
    return status


def wifi_disconnect(wlan):
    wlan.disconnect()
    wlan.active(False)
    del(wlan)

# Sends datas to the server, returns the configuration
def send_data(url, data=None, commands=None):
    # Formating payload
    payload = {}
    if data and len(data) > 0:
        payload["data"] = data
    if commands and len(commands) > 0:
        payload["commands"] = commands

    # Server communication
    response_json = requests.post(url, json=payload)
    response      = json.loads(response_json.text)

    # Parsing answer
    config = response["configuration"]
    cmd    = response["commands"]
    return config, cmd

def new_connection():

    while True:
        ap = AP_activation()
        wifi_param = setup()
        board.led_orange()
        time.sleep(2)
        wlan = wifi_init()
        status = wifi_connect(wifi_param, wlan)
        if status:
            board.led_green()
            ntptime.settime()
            break
        board.led_red()


    return wlan, wifi_param