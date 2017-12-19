import ujson as json
import os

# Writing a dictionnary in a file
def write_in_flash(filename, dict):
    #Check the file extension
    if filename.find(".txt", len(filename)-4,len(filename))<0:
        filename += ".txt"
    file = open(filename, "w")
    text = json.dumps(dict)
    file.write(text)
    file.close()
    return filename

#Reading an existing file in flash memory
def read_in_flash(filename):
    try:
        file=open(filename,"r")
    except OSError:
        print ("ERROR: could not read file, please check that the file exists\n")
        raise
    else:
        text=file.read()
        file.close()
        dict=json.loads(text)
    return dict

#Deleting an existing file
def delete_file(filename):
    try:
        os.remove(filename)
    except:
        print("ERROR: could not delete file, please check that the file exists\n")
        raise

def create_dict(wakeup_count, data, commands, received_cmd, url, wifi_param, config):
    master_dict = {}
    master_dict["wakeup_count"] = wakeup_count
    master_dict["data"]         = data
    master_dict["commands"]     = commands
    master_dict["received_cmd"] = received_cmd
    master_dict["url"]          = url
    master_dict["wifi_param"]   = wifi_param
    master_dict["config"]       = config
    return master_dict

def open_dict(master_dict):
    wakeup_count = master_dict["wakeup_count"]
    data         = master_dict["data"]
    commands     = master_dict["commands"]
    received_cmd = master_dict["received_cmd"]
    url          = master_dict["url"]
    wifi_param   = master_dict["wifi_param"]
    config       = master_dict["config"]
    return wakeup_count, data, commands, received_cmd, url, wifi_param, config
