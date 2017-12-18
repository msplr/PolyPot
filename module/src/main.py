import machine
import communication
import board
import sensors
import utime
import permmem

# Variables with no flash storage needed
filename           = "log.txt"      # File storing the parameters in flash memory
reinit             = False          # If the module has been resinitilized
first_boot         = False          # If this is the first boot
server_connect     = False          # If a server connection is required at this point
single_data        = {}             # Structure for a single data point
single_command     = {}             # Structure for a single command
suffix_send        = "/send-data/"  # Extension of the url to send data to the server
SENSORS_START_TIME = const(1)
# Checking if there are already data in flash memory or if there's a reinitilization
try:
    master_dict = permmem.read_in_flash(filename)
except OSError:
    first_boot = True
else:
    if not machine.wake_reason() == machine.PIN_WAKE:
        wakeup_count, data, commands, received_cmd, url, wifi_param, config = permmem.open_dict(master_dict)
    else:
        reinit = True

# Initilization if needed
if reinit or first_boot:
    master_dict, wakeup_count, data, commands, received_cmd = variables_init()
    # Module initialization
    wlan, wifi_param = communication.new_connection()
    url_send = wifi_param["server"] + suffix_send + wifi_param["uuid"]
    config, received_cmd = communication.send_data(url_send)
    communication.wifi_disconnect(wlan)

# Check if Wifi shall be activated, and activates it if needed and possible
if wakeup_count*config["logging_interval"] >= config["sending_interval"]:
    if communication.wifi_connect(wifi_param, wlan):
        server_connect = True
        wakeup_count   = 0
        config, received_cmd = communication.send_data(url_send)

# Reading sensors
sensors.start()
utime.sleep(SENSORS_START_TIME)
single_data = sensors.read_all()
sensors.stop()

# Updating time
time=utime.localtime()
time_iso='{}-{}-{}T{}:{}:{}Z'.format(time[0], time[1], time[2], time[3], time[4], time[5])
single_data["datetime"] = time_iso

# Saving the data
data.append(single_data)

# Treating the commands
if len(received_cmd)>0:
    for cmd in received_cmd:
        board.water_pump.on()
        utime.sleep(5)  # TODO: Empiracaly adjust the time
        board.water_pump.off()
        cmd["status"]   = "executed"
        cmd["datetime"] = time_iso
        commands.append(cmd)
    received_cmd = []

# Sending the data and executed commands if required
if server_connect:
    config, received_cmd = communication.send_data(url_send,data=data,commands=commands)
    communication.wifi_disconnect(wlan)
    data     = []
    commands = []

# Flash storage:
try:
    permmem.delete_file(filename)
except:
    print("WARNING: The log file couldn't be erased. This might lead to further errors\n")
master_dict=create_dict(wakeup_count, data, commands, received_cmd, url, wifi_param, config)
permmem.write_in_flash(filename, master_dict)

# Returning to sleep
wakeup_count += 1
utime.sleep(config["logging_interval"])

def variables_init():
    master_dict  = {} # A master dictionary to help writing and reading data in flash memory
    wakeup_count = 0  # Number of wake-ups since the last wifi connection
    data         = [] # Array destined to collect the data points
    commands     = [] # Array destined to collect the executed commands
    received_cmd = [] # Array destined to collect the commands sent by the server
    return master_dict, wakeup_count, data, commands, received_cmd