import machine
import communication
import board
import sensors
import utime
import permmem
import embeded_ntptime as ntptime

# Variables with no flash storage needed
REBOOT_TIME        = const(5)       # Delay before waking up after a reboot in seconds
SENSORS_START_TIME = const(1)       # Delay needed by the sensors to available in seconds
PUMP_FLOW          = const(50)      # Average pump flow in [ml/s]
filename           = "log.txt"      # File storing the parameters in flash memory
reinit             = False          # If the module has been reinitialised
first_boot         = False          # If this is the first boot
server_connect     = False          # If a server connection is required at this point
contact            = False          # If the module successfully contacted the server
single_data        = {}             # Structure for a single data point
single_command     = {}             # Structure for a single command
master_dict        = {}             # A dictionnary to simplify witting data in a file
suffix_send        = "/send-data/"  # Extension of the url to send data to the server

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
        print('First boot.\n')
        first_boot = True
    else:
        if not machine.wake_reason() == machine.PIN_WAKE:
            print('Normal wakeup. Booting data:\n')
            print(master_dict)
            board.led_green()
            wakeup_count, data, commands, received_cmd, url_send, wifi_param, config = permmem.open_dict(master_dict)
        else:
            reinit = True

    # Initialisation if needed
    if reinit or first_boot:
        print('Initialise.\n')
        board.led_red()
        master_dict, wakeup_count, data, commands, received_cmd = variables_init()

        # Module initialisation
        while not contact:
            wlan, wifi_param = communication.new_connection()
            print("First connection\n")
            url_send = wifi_param["server"] + suffix_send + wifi_param["uuid"]
            print("URL generated\n")
            print(url_send)

            #Tries to contact server
            try:
                config, received_cmd = communication.send_data(url_send)
            except:
                print("WARNING: Couldn't reach server, will try again.\n")
            else:
                contact = True
                print("New config and command:\n")
                print(received_cmd)
                print(config)
            communication.wifi_disconnect(wlan)

    # Check if Wifi shall be activated, and activates it if needed and possible
    if wakeup_count*config["logging_interval"] >= config["sending_interval"]:
        wlan = communication.wifi_init()
        print("Trying a normal connection.\n")
        co_status = communication.wifi_connect(wifi_param, wlan)
        if co_status:
            print("Normal connection OK.\n")
            try:
                config, received_cmd = communication.send_data(url_send)
            except:
                board.led_red()
                communication.wifi_disconnect(wlan)
            else:
                server_connect = True
        else:
            print("Normal connection failed, reported the sendig to the next wakeup.\n")
            board.led_red()

    # Reading sensors
    sensors.start()
    print("Wait for sensor start\n")
    utime.sleep(SENSORS_START_TIME)
    print('Read sensors.\n')
    single_data = sensors.read_all()
    print("Reading done.\n")
    sensors.stop()


    # Updating time
    print("Formating time.\n")
    time=utime.localtime()
    time_iso='{}-{}-{}T{}:{}:{}Z'.format(time[0], time[1], time[2], time[3], time[4], time[5])
    single_data["datetime"] = time_iso
    print("Time done.\n")
    print(single_data)

    # Saving the data
    data.append(single_data)
    print("Data saved.\n")
    # Computing the watering time:
    water_time = int(config["water_volume_pumped"] / PUMP_FLOW)

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
    else:
        # Trigering automatic watering if needed and no commands done
        if data['soil_moisture']<config["target_soil_moisture"]:
            print("Starting automatic watering.\n")
            board.water_pump.on()
            utime.sleep(water_time)
            board.water_pump.off()
            print("Automatic watering done.\n")
            single_command = {"type":"water","status":"executed","datetime":time_iso}
            commands.append(single_command)
            single_command = {}
            print("Command saved.\n")

    # Sending the data and executed commands if required
    if server_connect:
        print("Second Connection to sever.\n")
        try:
            config, received_cmd = communication.send_data(url_send,data=data,commands=commands)
        except:
            board.led_red()
            print("Data sending failed, reporting the sending to next connection.\n")
            utime.sleep(communication.TIME_LED_FLASH)
        else:
            data = []
            commands = []
            wakeup_count = 0
            print("Data sending done.\n")
        finally:
            communication.wifi_disconnect(wlan)

    wakeup_count += 1
    print("Building the master dictionnary.\n")
    master_dict = permmem.create_dict(wakeup_count, data, commands, received_cmd, url_send, wifi_param, config)

    # Flash storage:
    print("Starting flash storage.\n")
    try:
        permmem.delete_file(filename)
    except:
        if not first_boot:
            print("WARNING: The log file couldn't be erased. This might lead to further errors\n")
    try:
        permmem.write_in_flash(filename, master_dict)
    except:
        print("ERROR: failure to write in flash, deleting data.\n")
        master_dict  = {}
        data         = []
        received_cmd = {}
        master_dict  = permmem.create_dict(wakeup_count, data, commands, received_cmd, url_send, wifi_param, config)
        try:
            print("Retrying to write in flash.\n")
            permmem.write_in_flash(filename, master_dict)
        except:
            print("FATAL ERROR: Couldn't write in flash. Full reboot.\n")

    # Returning to sleep
    print('Go to deepseleep.\n')
    board.led_off()
    board.sleep(config["logging_interval"])

# Fatal error mode
except:
    print("FATAL ERROR: Unexpected error. Rebooting the system.\n")
    board.led_off()
    sensors.stop()
    try:
        permmem.delete_file(filename)
    except:
        print ("No log file, ready to reboot.\n")
    else:
        print ("Deleted log file, ready to reboot.\n")
    finally:
        print("REBOOTING.\n")
        board.sleep(REBOOT_TIME)


