import network
import urequests as requests
import ujson as json
import usocket as socket
import time
#TODO: use **kwarg to merge the functions

#Re activates the AP
def AP_activation(ap):
    ap.active(True)
    ap.config(essid="PolyPot", password="setupPolyPot", authmode=network.AUTH_WPA_WPA2_PSK)

#Activates an AP. A WLAN object containig the active AP
def AP_activation():

    ap = network.WLAN(network.AP_IF)
    ap.active(True)
    ap.ifconfig(("192.168.1.1", "255.255.255.0", "192.168.4.1", "8.8.8.8"))
    ap.config(essid="PolyPot", password="setupPolyPot", authmode=network.AUTH_WPA_WPA2_PSK)
    return ap

#Gets the wifi config in json format when the pot is in AP
def get_post():
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]

    s = socket.socket()

    s.bind(addr)

    s.listen(1)

    cl, addr = s.accept()

    cl_file = cl.makefile('rwb', 0)

    config_ini=""
    content_length=0

    #Discard first line with different format
    line = cl_file.readline()

    while True:

        line = cl_file.readline()
        if not line or line == b'\r\n':
            break

        print(str(line))
        (header, value) = line.decode().split(":", 1)
        print(header)
        print(value)
        if header == "Content-Length":
            content_length=int(value)
            print(content_length)


    config_ini = cl_file.read(content_length).decode()
    print(config_ini)
    response="HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 2\r\n\r\n{}"
    print("reading over")
    cl.send(response)
    print("response sent")
    cl.close()
    print("tramission over\n")
    s.close()
    print("OVER\n")
    return config_ini

#Downloads the setups from the phone and returns them as a dictionnary
def setup():
    wifi_param={}

    # get the HTTP post
    wifi_param_json=get_post()

    wifi_param=json.loads(wifi_param_json)

    return wifi_param

#Creates the wifi
def wifi_init():
    wlan = network.WLAN(network.STA_IF)
    return wlan

# Tries to connect to the wifi
def wifi_connect(ap,wifi_param, wlan):

    status=True

    ap.active(False)
    wlan.active(True)

    wlan.connect(wifi_param['ssid'], wifi_param['password'])
    for i in range(1,10):
        if not wlan.isconnected():
            time.sleep(1)
        else:
            break


    if not wlan.isconnected():
        wlan.disconnect()
        status=False

    return status


def wifi_disconnect(wlan):
    wlan.disconnect()

#Get the config from the server
def get_config(url):
    config_json=requests.post(url, json=json.dumps({}))
    config=json.loads(config_json)
    return config

#Sends datas to the server, returns the configuration
def send_datas(datas,url):
    response_json=requests.post(url, json=json.dumps(datas))
    response=json.loads(response_json)
    return response

def test():
    ap=AP_activation()
    config=get_post()
    return  ap,config
