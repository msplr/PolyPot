import network
import urequests as requests
import ujson as json
import usocket as socket


def AP_activation(ap):
    ap.active(True)
    ap.config(essid="PolyPot", password="setupPolyPot", authmode=network.AUTH_WPA_WPA2_PSK)

#Activates an AP. A WLAN object containint the active AP
def AP_activation():

    ap = network.WLAN(network.AP_IF)
    ap.active(True)
    ap.config(essid="PolyPot", password="setupPolyPot", authmode=network.AUTH_WPA_WPA2_PSK)
    return ap

#Gets the wifi config in json format
def get_post():

    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]

    s = socket.socket()
    s.bind(addr)
    s.listen(1)

    cl, addr = s.accept()
    cl_file = cl.makefile('rwb', 0)
    config_ini=""
    while True:
        line = cl_file.readline()
        config_ini+=line
        if not line or line == b'\r\n':
            break
    response = json.dumps({})
    cl.send(response)
    cl.close()
    return config_ini

#Downloads the setups from the phone and reds them. Returns a dictionnary with wifi parameters and a config object
def setup():
    wifi_param={}

    # get the HTTP post
    wifi_param_json=get_post()

    wifi_param=json.loads(wifi_param_json)

    return wifi_param


def wifi_init():
    wlan = network.WLAN(network.STA_IF)
    return wlan

# Tries to connect to the wifi
def wifi_connect(ap,wifi_param, wlan):

    status=True

    ap.active(False)
    wlan.active(True)

    for count in range(1,10):
        wlan.connect(wifi_param['ssid'],wifi_param['password'])
        if wlan.isconnected():
            break

    if not wlan.isconnected():
        wlan.disconnect()
        status=False

    return wlan,status


def wifi_disconnect(wlan):
    wlan.disconnect()

#Get the config from the server
def get_config(url):
    config_json=requests.post(url, payload=json.dumps({}))
    config=json.loads(config_json)
    return config

#Sends datas to the server, returns the configuration
def send_datas(datas,url):
    json.loads(datas)
    response_json=requests.post(url, payload=json.dumps(datas))
    response=json.loads(response_json)
    return response
