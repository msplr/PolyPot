# Protocol specification

## Setup

The Pot registers with the Server through an HTTP GET request. The response is a JSON response containing the configuration and the Pot ID.

The URL to be called is /setup

The JSON response MUST have the following top-level members:
 - "configuration": an Configuration object, containing the configuration of the Pot. See "Configuration object" section for details.
 - "id": the ID the Pot will use for all subsequent requests.

## Communications paths

### Pot to Server and Server to Pot communication

The Pot send new data to the Server through an HTTP POST request containing a JSON-encoded payload. The response is a JSON response containing the configuration and the pending commands.

The URL to be called is /send-data/<int:pot_id>

The JSON request MAY have the following top-level members:
 - "data": a single Data object or an array of Data objects, representing the data recorded by the Pot. See "Data object" section for details.

The JSON response MUST have the following top-level members:
 - "configuration": an Configuration object, containing the Pot configuration. See "Configuration object" section for details.
 - "commands": an array of Command objects, containing the commands the Pot will execute. See "Command object" section for details.

### Server to Smartphone communication

The Smartphone ask for the data to the Server through an HTTP GET request. The response is a JSON response containing the data, the configuration of the Pot and notifications.

The URL to be called is /get-data/<int:pot_id>

The JSON response MUST have the following top-level members:
 - "data": an array of Data objects, representing all the data recorded by the Pot stored on the Server. See "Data object" section for details.
 - "notifications": an array of Notifications objects. See "Notification object" section for details.
 - "configuration": an Configuration object, containing the configuration of the Pot. See "Configuration object" section for details.

### Smartphone to Server communication

The Smartphone send new commands to the Server through an HTTP POST request containing a JSON-encoded payload.

The URL to be called is /send-commands-and-configuration/<int:pot_id>

The JSON request MAY have the following top-level members:
 - "configuration": an Configuration object, containing the configuration of the Pot. See "Configuration object" section for details.
 - "commands": a single Command object or an array of Command objects, containing the commands the Smartphone send. See "Command object" section for details.

## JSON Objects

### Data object

A Data object MUST have the following members:
 - "datetime": a string representing the date and the time (UTC) when the measures where made, following the ISO 8601 standard.
 - "soil_moisture": a float number representing the soil moisture, in percents.
 - "temperature": a float number representing the temperature, in degree Celsius.
 - "luminosity": an integer number representing the luminosity, in lux.
 - "water_level": an integer number representing the water level of the tank, in percents.
 - "plant_watered": a boolean set to true if the plant have been watered and to false otherwise.

### Configuration object

A Configuration object MAY have the following members:
 - "target_soil_moisture": a number representing the soil moisture when the Pot must water the plant, in percents.
 - "water_volume_pumped": the volume pumped when the Pot water the plant, in milliliter.
 - "logging_interval": the interval a which the Pot must wake up and take measures, in seconds.
 - "sending_interval": the interval a which the Pot must wake up and send the data accumulated, in seconds.

### Command object

A Command object MUST have the following members:
 - "type": a string representing the command type.

A Command object MAY have the following members:
 - Other members, depending on the command.

The following type are available:
 - "water": water the plant immediately.

### Notification object

A Notification object MUST have the following members:
 - "text": a string representing the text the Smartphone should show to the user.
 - "level": a string representing the notification level, can be: "info", "alert".
