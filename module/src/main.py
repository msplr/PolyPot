import machine
import communication
import board
import sensors
import utime
import permmem
import embeded_ntptime as ntptime

# Variables with no flash storage needed
PUMP_FLOW          = const(50)       # Average pump flow in [ml/s]
filename           = "log.txt"      # File storing the parameters in flash memory
reinit             = False          # If the module has been resinitilized
first_boot         = False          # If this is the first boot
server_connect     = False          # If a server connection is required at this point
contact            = False          # If the module successfully contacted the server
single_data        = {}             # Structure for a single data point
single_command     = {}             # Structure for a single command
master_dict        = {}
suffix_send        = "/send-data/"  # Extension of the url to send data to the server
SENSORS_START_TIME = 1

def variables_init():
    master_dict  = {} # A master dictionary to help writing and reading data in flash memory
    wakeup_count = 0  # Number of wake-ups since the last wifi connection
    data         = [] # Array destined to collect the data points
    commands     = [] # Array destined to collect the executed commands
    received_cmd = [] # Array destined to collect the commands sent by the server
    return master_dict, wakeup_count, data, commands, received_cmd

#____________________________________________________________________________________________________
#_____________________________________________MAIN_CODE______________________________________________
try:
    # Checking if there are already data in flash memory or if there's a reinitilization
    try:
        master_dict = permmem.read_in_flash(filename)
    except OSError:
        print('first boot')
        first_boot = True
    else:
        if not machine.wake_reason() == machine.PIN_WAKE:
            print('normal wakeup')
            print(master_dict)
            board.led_green()
            wakeup_count, data, commands, received_cmd, url_send, wifi_param, config = permmem.open_dict(master_dict)
        else:
            reinit = True

    # Initilization if needed
    if reinit or first_boot:
        print('initialize')
        board.led_red()
        master_dict, wakeup_count, data, commands, received_cmd = variables_init()
        # Module initialization
        while not contact:
            wlan, wifi_param = communication.new_connection()
            print("connection")
            url_send = wifi_param["server"] + suffix_send + wifi_param["uuid"]
            print("url done")
            #Tries to contact server
            try:
                config, received_cmd = communication.send_data(url_send)
            except:
                print("Couldn't reach server\n")
            else:
                contact = True
                print("GOT A NEW CONFIG AND COMMAND")
                print(received_cmd)
            communication.wifi_disconnect(wlan)

    # Check if Wifi shall be activated, and activates it if needed and possible
    if wakeup_count*config["logging_interval"] >= config["sending_interval"]:
        wlan = communication.wifi_init()
        print("Trying a normal connection")
        co_status = communication.wifi_connect(wifi_param, wlan)
        if co_status:
            print("Normal connaection OK")
            try:
                config, received_cmd = communication.send_data(url_send)
            except:
                board.led_red()
                communication.wifi_disconnect(wlan)
            else:
                server_connect = True
        else:
            print("Normal connection failed")
            board.led_red()

    # Reading sensors
    sensors.start()
    print("wait for sensor start")
    utime.sleep(SENSORS_START_TIME)
    print('read sensors')
    single_data = sensors.read_all()
    print("sensors red")
    sensors.stop()


    # Updating time
    print("Build time")
    time=utime.localtime()
    time_iso='{}-{}-{}T{}:{}:{}Z'.format(time[0], time[1], time[2], time[3], time[4], time[5])
    single_data["datetime"] = time_iso
    print("Time added")
    print(single_data)

    # Saving the data
    data.append(single_data)
    print("Data added")

    water_time = int(config["water_volume_pumped"] / PUMP_FLOW)
    #Trigering automatic watering
    #if data['soil_moisture']<config["target_soil_moisture"]:
    #    board.water_pump.on()
    #    utime.sleep(water_time)
    #    board.water_pump.off()
    #    single_command = {"type":"water","status":"executed","datetime":time_iso}
    #    commands.append(single_command)
    #    single_command = {}

    # Treating the commands
    if len(received_cmd)>0:
        for cmd in received_cmd:
            print('water plant')
            board.water_pump.on()
            utime.sleep(water_time)  # TODO: Empiracaly adjust the time
            board.water_pump.off()
            cmd["status"]   = "executed"
            cmd["datetime"] = time_iso
            print("Finished water")
            commands.append(cmd)
        received_cmd = []
        print("Commands cleared")

    # Sending the data and executed commands if required
    if server_connect:
        print("Second Connextion to sever")
        try:
            config, received_cmd = communication.send_data(url_send,data=data,commands=commands)
        except:
            board.led_red()
            print("Failed sending")
            utime.sleep(1)
        else:
            data = []
            commands = []
            wakeup_count = 0
            print("SENDING OK!!!!")
        finally:
            communication.wifi_disconnect(wlan)

    wakeup_count += 1
    print("Building the master dict")
    master_dict = permmem.create_dict(wakeup_count, data, commands, received_cmd, url_send, wifi_param, config)
    # Flash storage:
    try:
        permmem.delete_file(filename)
    except:
        print("FAILED DELETION")
        if not first_boot:
            print("WARNING: The log file couldn't be erased. This might lead to further errors\n")
    try:
        permmem.write_in_flash(filename, master_dict)
    except:
        print("ERROR: failure to write in flash")
        master_dict={}
        data=[]
        received_cmd={}
        master_dict=permmem.create_dict(wakeup_count, data, commands, received_cmd, url_send, wifi_param, config)
        try:
            permmem.write_in_flash(filename, master_dict)
        except:
            print("FATAL ERROR: REBOOT")

    # Returning to sleep
    print('go to deepseleep')
    board.led_off()
    board.sleep(config["logging_interval"])

# Fatal error mode
except:
    board.led_off()
    sensors.stop()
    try:
        permmem.delete_file(filename)
    except:
        print ("Crashed without permfile")
    finally:
        print("FATAL ERROR: REBOOTING")
        board.sleep(5)


