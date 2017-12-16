import network
import urequests as requests
import ujson as json
import usocket as socket
import time
import embeded_ntptime as ntptime

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
    #Setup the socket
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]
    s    = socket.socket()
    s.bind(addr)

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
    cl.send(response)
    cl.close()
    s.close()
    return config_ini

# Downloads the setups from the phone and returns them as a dictionnary
def setup():
    wifi_param_json = get_post()
    wifi_param      = json.loads(wifi_param_json)
    return wifi_param

#Creates the wifi
def wifi_init():
    wlan = network.WLAN(network.STA_IF)
    return wlan

# Tries to connect to the wifi
def wifi_connect(ap, wifi_param, wlan):
    status = True
    ap.active(False)
    wlan.active(True)

    # Try connection
    wlan.connect(wifi_param['ssid'], wifi_param['password'])
    time.sleep(WIFI_TIMEOUT)
    if not wlan.isconnected():
        wlan.disconnect()
        status = False
    return status


def wifi_disconnect(wlan):
    wlan.disconnect()

#Sends datas to the server, returns the configuration
def send_data(url, data=None, commands=None):
    #Formating payload
    payload = {}
    if data and len(data) > 0:
        payload["data"] = data
    if commands and len(commands) > 0:
        payload["commands"] = commands

    #Server communication
    response_json = requests.post(url, json=payload)
    response      = json.loads(response_json.text)

    #Parsing answer
    config = response["configuration"]
    cmd    = response["commands"]
    return config, cmd

def new_connection(ap=None, wlan=None):
    while True:
        if ap:
            AP_activation(ap = ap)
        else:
            ap = AP_activation()
        wifi_param = setup()
        if not wlan:
            wlan = wifi_init()
        status = wifi_connect(ap, wifi_param, wlan)
        if status:
            break
        ntptime.settime()  # TODO: solve the 30 years offset
        time_tuple
    return ap, wlan, wifi_param