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
recived_cmd = []

single_data={}
single_command={}

# # Establishing the first connection
while True:
     ap=communication.AP_activation()
     wifi_param=communication.setup()
     wlan=communication.wifi_init()
     status=communication.wifi_connect(ap,wifi_param, wlan)
     if status:
         break

# Initialising the moodule
url_send=wifi_param["server"]+suffix_send+wifi_param["uuid"]
response=communication.send_datas(url_send)
config = response['configuration']
ntptime.settime() # TODO: solve the 30 years offset
communication.wifi_disconnect(wlan)

while True:
    #Reinitialise if the user presses the button
    if machine.wake_reason()==machine.PIN_WAKE:
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
    if True:
        communication.wifi_connect(ap, wifi_param, wlan)
        response=communication.get_config(url_send)
        config = response["configuration"]
        recived_cmd = response["commands"]
        print(recived_cmd)
        #communication.wifi_disconnect(wlan)
        data=[]
        commands=[]

    if wakeup_count*config["logging_interval"] >= config["sending_interval"]:
        send_datas=True
        wakeup_count=0

    sensors.start()
    # Reading sensors
    single_data = sensors.read_all()
    sensors.stop()

    # Updating time
    time=utime.localtime()
    time_iso='{}-{}-{}T{}:{}:{}Z'.format(time[0], time[1], time[2], time[3], time[4], time[5])
    single_data["datetime"]=time_iso


    # Saving the datas
    data.append(single_data)

    # TODO: Pump if needed and update the command object
    print("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n")
    print(len(recived_cmd))
    if len(recived_cmd)>0:
        print("treating command")
        for cmd in recived_cmd:
            board.water_pump.on()
            utime.sleep(5)
            board.water_pump.off()
            cmd["status"]="executed"
            cmd["datetime"]=time_iso
            commands.append(cmd)




    # if send_datas:
    if True:
        #communication.wifi_connect(ap, wifi_param, wlan)
        response=communication.send_datas(data,commands, url_send)
        config = response["configuration"]
        recived_cmd = response["commands"]
        print(recived_cmd)
        communication.wifi_disconnect(wlan)
        data=[]
        commands=[]

    # Returning to sleep
    wakeup_count+=1
    bed_time=utime.ticks_ms()
    utime.sleep(5)

