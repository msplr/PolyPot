import machine
import communication
import board
import sensors
import utime
import ntptime
import network

#Where to send the datas
suffix_send="/send-data/"

#Number of wakeups since the last wifi connection
wakeup_count=0
send_datas=False
data=[]
commands=[]

single_data={}
single_command={}

# # Establishing the first connection
# while True:
#     ap=communication.AP_activation()
#     wifi_param=communication.setup()
#     wlan=communication.wifi_init()
#     status=communication.wifi_connect(ap,wifi_param, wlan)
#     if status:
#         break

ap = network.WLAN(network.AP_IF)
ap.active(False)
wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wifi_param={
    'ssid': 'test',
    'password': 'PolyPot101',
    'server': 'https://polypot.0xf00.ch',
    'uuid': '01234567-89ab-cdef-0123-456789abcdef'
}
wlan.connect(wifi_param['ssid'],wifi_param['password'])
while not wlan.isconnected():
    utime.sleep(0.1)

# Initialising the moodule
url_send=wifi_param["server"]+suffix_send+wifi_param["uuid"]
config=communication.get_config(url_send)
communication.wifi_disconnect(wlan)
ntptime.setttime() #Should work. To test with a wifi connection


while True:
    wakeup_time=utime.ticks_ms()

    # Reinitialise if the user presses the button
    if utime.ticks_diff(wakeup_time, bed_time)< (config["logging_interval"]/1000): #TODO: add a bit of flexibility in this condition
        wakeup_count=0
        while True:
            communication.AP_activation()
            wifi_param = communication.setup()
            status = communication.wifi_connect(ap, wifi_param, wlan)
            if status:
                break
        url_send = wifi_param["server"] + suffix_send + wifi_param["uuid"]
        response = communication.get_config(url_send)
        config=response["configuration"]
        recived_cmd=response["commands"]
        communication.wifi_disconnect(wlan)

    # Check if Wifi shall be activated
    if wakeup_count*config["logging_interval"] >= config["sending_interval"]:
        send_datas=True
        wakeup_count=0

    sensors.start()
    # Reading sensors
    single_data = sensors.read_all()
    sensors.stop()

    # Updating time
    time=utime.localtime()
    time_iso=time[0]+"-"+time[1]+"-"+time[2]+"T"+time[3]+":"+time[4]+":"+time[5]+"Z"
    single_data["datetime"]=time_iso


    # Saving the datas
    data.append(single_data)

    # TODO: Pump if needed and update the command object
    if len(recived_cmd)>0:
        for cmd in recived_cmd:
            board.water_pump.on()
            utime.sleep(5)
            board.water_pump.off()
            recived_cmd[cmd]["status"]="executed"
            recived_cmd[cmd]["datetime"]=time_iso

    commands.append(recived_cmd)



    if send_datas:
        communication.wifi_connect(ap, wifi_param, wlan)
        response=communication.send_datas(data,commands, url)
        config = response["configuration"]
        recived_cmd = response["commands"]
        communication.wifi_disconnect(wlan)
        data=[]
        commands=[]

    # Returning to sleep
    wakeup_count+=1
    bed_time=utime.ticks_ms()
    lowpower.sleep(config["logging_interval"])








