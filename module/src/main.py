import machine
import communication
import board
import sensors
import utime
import ntptime

#Where to send the datas
suffix_send="/send-data/"

#Nuber of wakeups since the last wifi connection
wakeup_count=0
send_datas=False
data_array=()

data={}
command={}

# Establishing the first connection
while True:
    ap=communication.AP_activation()
    wifi_param=communication.setup()
    wlan=communication.wifi_init()
    status=communication.wifi_connect(ap,wifi_param, wlan)
    if status:
        break

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
        config = communication.get_config(url_send)
        communication.wifi_disconnect(wlan)

    # Check if Wifi shall be activated
    if wakeup_count*config["logging_interval"] >= config["sending_interval"]:
        send_datas=True
        wakeup_count=0

    sensors.sensors_pwr(True)
    # Reading sensors
    # MICHAEL: HERE GOES THE SENORS READING
    sensors.sensors_pwr(False)

    # Updating time
    time=utime.localtime()
    time_iso=time[0]+"-"+time[1]+"-"+time[2]+"T"+time[3]+":"+time[4]+":"+time[5]+"Z"
    data["datetime"]=time_iso

    # Saving the datas
    data_array.append(data)

    # TODO: Pump if needed and update the command object

    if send_datas:
        communication.wifi_connect(ap, wifi_param, wlan)
        config=communication.send_datas(data_array)
        communication.wifi_disconnect(wlan)
        data_array=[]

    # Returning to sleep
    wakeup_count+=1
    bed_time=utime.ticks_ms()
    lowpower.sleep(config["logging_interval"])








