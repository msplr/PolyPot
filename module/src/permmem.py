import ujson as json
import os

# Writing a dictionnary in a file
def write_in_flash(filename, dict):
    #Check the file extension
    if filename.find(".txt", len(filename)-4,len(filename))<0:
        filename+=".txt"
    file=open(filename, "w")
    text=json.dumps(dict)
    file.write(text)
    file.close()
    return filename

#Reading an existing file in flash memory
def read_in_flash(filename):
    try:
        file=open(filename,"r")
    except:
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
