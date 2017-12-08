import network
import urequests as requests
import ujson as json


#Activates an AP. A WLAN object containint the active AP
def AP_activation():
    ap = network.WLAN(network.AP_IF)
    ap.config(essid='PolyPot')
    ap.config(password='setupPolyPot')
    ap.config(authmode=network.WPA2)
    ap.active(True)
    return ap

#Downloads the setups from the phione and reds them. Returns a dictionnary with wifi parameters and a config object
def setup():  #TODO: add a condition to check if the ap is active
    wifi_param={}
    config={}
    payload={}

    # GET request with answer
    r=requests.get('\setup', params=json.dumps(payload))

    # Sorting datas
    wifi_param=json.loads(r.json())
    config=wifi_param.pop('config')

    return wifi_param, config

# Desactivates ap and sets the wificonnect. Remember to check if the connection works with isconnected methode
def wifi_connect(ap,wifi_param):
    ap.active(False)
    wlan=network.WLAN(network.STA_IF)
    wlan.active(True)
    for count in range(1,10):
        wlan.connect(wifi_param['ssid'],wifi_param['password'])
        if wlan.isconnected():
            break
    return wlan

#
def send_datas(datas,url):
    json.loads(datas)
    r=requests.post(url, json.dumps(datas))
    response=json.loads(r)
    return response

