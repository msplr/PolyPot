# Protocol specification

## Communications paths

### Pot to Server and Server to Pot communication

The Pot send new data to the Server through an HTTP POST request containing a JSON-encoded payload. The response is a JSON file containing the configuration and the pending commands.

The URL to be called is /send-data

The JSON payload follows the following convention:

The root JSON object has the following members:
 - "data": an array containing objects representing the data recorded by the Pot. See "Data object" section for details.
 
The JSON file follows the following convention:

The root JSON object has the following members:
 - "configuration": an object containing the configuration. See "Configuration object" section for details.
 - "commands": an array containing the commands the Pot will execute. See "Command object" section for details.

### Server to Smartphone communication

The Smartphone ask for the data to the Server through an HTTP GET request. The response is a JSON file containing the data.

The URL to be called is /get-data

The JSON file follows the following convention:

The root JSON object has the following members:
 - "data": an array containing objects representing all the data recorded by the Pot stored on the Server. See "Data object" section for details.
 - "notifications": an array containing notifications. See "Notification object" section for details.
 - "configuration": an object containing the configuration. See "Configuration object" section for details.
 
### Smartphone to Server communication

The Smartphone send new commands to the Server through an HTTP POST request containing a JSON-encoded payload.

The URL to be called is /send-commands-and-configuration

The JSON payload follows the following convention:

The root JSON object has the following members:
 - "configuration": an object containing the configuration. See "Configuration object" section for details.
 - "commands": an array containing the commands the Smartphone send. See "Command object" section for details.
 
## JSON Objects
 
### Data object

Each data object has the following members:
 - "datetime": a string representing the date and the time (UTC) when the measures where made, following the ISO 8601 standard.
 - "soil_moisture": a float number representing the soil moisture, in percents.
 - "temperature": a float number representing the temperature, in degree Celsius.
 - "luminosity": an integer number representing the luminosity, in lux.
 - "water_level": an integer number representing the water level of the tank, in percents.
 - "plant_watered": a boolean set to true if the plant have been watered and to false otherwise.
 
### Configuration object

The configuration object has the following members:
 - "target_soil_moisture": a number representing the soil moisture when the Pot must water the plant, in percents.
 - "water_volume_pumped": the volume pumped when the Pot water the plant, in milliliter.
 - "logging_interval": the interval a which the Pot must wake up and take measures, in seconds.
 - "sending_interval": the interval a which the Pot must wake up and send the data accumulated, in seconds.

### Command object
 
Each command object has the following members:
 - "type": a string representing the command type.
 - Other members, depending on the command.
 
The following type are available:
 - "water": water the plant immediately.
 
### Notification object

Each data object has the following members:
 - "text": a string representing the text the Smartphone should show to the user.
 - "level": a string representing the notification level, can be: "info", "alert".
