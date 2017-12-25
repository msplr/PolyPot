import network
import urequests as requests
import ujson as json
import usocket as socket
import time
import embeded_ntptime as ntptime
import board

WIFI_TIMEOUT   = const(5)   # Time for which the module tries to connect to the wifi in seconds
TIME_LED_FLASH = const(2)   # The minimal time a LED is flashed in seconds
RESET_TIME     = const(1)   # The time the board stays in deep sleep when it has to reboot in seconds
NTP_ATTEMPTS   = const(100) # Number of try to get time through NTP

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
    # Setup the socket
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]
    s    = socket.socket()


    while True:
        try:
            s.bind(addr)
        except:
            print("ERROR: s.bind failed, rebooting the module.\n")
            raise
        else:
            break
    print("Ready to listen.\n")

    # Reciving the post request
    s.listen(1)
    cl, addr = s.accept()
    cl_file  = cl.makefile('rwb', 0)
    print("Ready to read the file.\n")

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
    print("Header received.\n")

    # Reading json file
    config_ini = cl_file.read(content_length).decode()
    print('Config received:\n')
    print(config_ini)

    # Answer and closing socket
    response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: 2\r\n\r\n{}"
    while True:
        try:
            cl.send(response)
        except:
            print("ERROR: Couldn't respond to the POST request.\n")
            continue
        else:
            print('Response sent.\n')
            break
    cl.close()
    s.close()
    return config_ini

# Downloads the setups from the phone and returns them as a dictionnary
def setup():
    print('AP mode: initialising server loop.\n')
    try:
        wifi_param_json = get_post()
    except:
        print("ERROR: AP mode failed. Rebboting to try agine.\n")
        raise
    else:
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
        wlan.active(False)
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
    timetuple=time.localtime()

    if timetuple[0]==2000:
        ntptime.settime()

    # Server communication
    try:
        response_json = requests.post(url, json=payload)
    except:
        raise
    response = json.loads(response_json.text)

    # Parsing answer
    config = response["configuration"]
    cmd    = response["commands"]
    return config, cmd

def new_connection():
    ntp_count = 0

    while True:
        ap = AP_activation()
        try:
            wifi_param = setup()
        except:
            print("ERROR: setup failed, rebooting.\n")
            raise
        else:
            board.led_orange()
            time.sleep(TIME_LED_FLASH)
            wlan = wifi_init()
            status = wifi_connect(wifi_param, wlan, ap)
            if status:
                board.led_green()
                while True:
                    try:
                        ntptime.settime()
                    except:
                        if ntp_count >= NTP_ATTEMPTS:
                            print("ERROR: failed ntp. Your data won't have the right time stamp."
                                  "Will retry during the next connection.\n")
                            break
                        else:
                            print("WARNING: failed ntp, retry.\n")
                            ntp_count += 1
                    else:
                        break
                break
            board.led_red()
            return wlan, wifi_param