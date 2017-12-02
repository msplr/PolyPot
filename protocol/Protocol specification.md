# Protocol specification

## Setup

### Pot and Smartphone

When the Pot is started for the first time, it MUST setup a Wi-Fi Access Point (AP) with the ssid `PolyPot` and the password `setupPolyPot`.

The Pot IP MUST be `192.168.1.1`.

The Smartphone configure the Pot through the AP and send an HTTP POST request containing a JSON-encoded payload.

The Smartphone MUST generate an UUID for the Pot.

The URL to be called is `/setup`.

The JSON request MUST have the following top-level members:
 - `ssid`: the ssid of the User's Wi-Fi.
 - `password`: the password of the User's Wi-Fi.
 - `uuid`: the UUID the Pot will use.
 - `server`: the server the Pot will use.

 The JSON response MUST be an empty JSON object.

### Pot and Server

The Pot registers with the Server through an HTTP GET request. The response is a JSON response containing the configuration and the Pot ID.

The URL to be called is `/setup/<uuid:pot_id>`.

If the Pot can't connect to the Server, the Pot MUST stay in the setup mode.

If the UUID already exist in the database, the Server MUST return an error and the Pot MUST stay in the setup mode.

The JSON response MUST have the following top-level members:
 - `configuration`: an Configuration object, containing the configuration of the Pot. See "Configuration object" section for details.

## Communications paths

### Pot to Server and Server to Pot communication

The Pot send new data to the Server through an HTTP POST request containing a JSON-encoded payload. The response is a JSON response containing the configuration and the pending commands.

The URL to be called is `/send-data/<uuid:pot_id>`.

The JSON request MAY have the following top-level members:
 - `data`: a single Data object or an array of Data objects, representing the data recorded by the Pot. See "Data object" section for details.
 - `commands`: a single Command object or an array of Command objects, representing the command(s) executed by the Pot. See "Command object" section for details.

The JSON response MUST have the following top-level members:
 - `configuration`: an Configuration object, containing the Pot configuration. See "Configuration object" section for details.
 - `commands`: an array of Command objects, containing the commands the Pot will execute. See "Command object" section for details.

### Server to Smartphone communication (latest information)

The Smartphone ask for the latest data information to the Server through an HTTP GET request. The response is a JSON response containing the last data point, the configuration of the Pot and the last command executed.

The URL to be called is `/get-latest/<uuid:pot_id>`.

The JSON response MUST have the following top-level members:
 - `data`: a Data object, representing the last data point recorded by the Pot stored on the Server. See "Data object" section for details.
 - `configuration`: an Configuration object, containing the configuration of the Pot. See "Configuration object" section for details.
 - `commands`: an array of Command objects, containing the last command of each type executed by the Pot. See "Command object" section for details.

### Server to Smartphone communication (data & commands)

The Smartphone ask for the data to the Server through an HTTP GET request. The response is a JSON response containing the data and the configuration of the Pot.

The URL to be called is `/get-data/<uuid:pot_id>?from=<from_datetime>&to=<to_datetime>`.

The `from` and `to` fields are optional, are used to limit the data returned by the Server and follow the ISO 8601 standard.

The JSON response MUST have the following top-level members:
 - `data`: an array of Data objects, representing all the data recorded by the Pot stored on the Server. See "Data object" section for details.
 - `commands`: an array of Command objects, representing all the commands stored on the Server. See "Configuration object" section for details.

### Smartphone to Server communication

The Smartphone send new commands to the Server through an HTTP POST request containing a JSON-encoded payload.

The URL to be called is `/send-c-and-c/<uuid:pot_id>`.

The JSON request MAY have the following top-level members:
 - `configuration`: an Configuration object, containing the configuration of the Pot. See "Configuration object" section for details.
 - `commands`: a single Command object or an array of Command objects, containing the commands the Smartphone send. See "Command object" section for details.

 The JSON response MUST be an empty JSON object.

## JSON Objects

### Data object

A Data object MUST have the following members:
 - `datetime`: a string representing the date and the time (UTC) when the measures where made, following the ISO 8601 standard.
 - `soil_moisture`: a float number representing the soil moisture, in percents.
 - `temperature`: a float number representing the temperature, in degree Celsius.
 - `luminosity`: an integer number representing the luminosity, in lux.
 - `water_level`: an integer number representing the water level of the tank, in percents.

### Configuration object

A Configuration object MAY have the following members:
 - `target_soil_moisture`: a number representing the soil moisture when the Pot must water the plant, in percents.
 - `water_volume_pumped`: the volume pumped when the Pot water the plant, in milliliter.
 - `logging_interval`: the interval a which the Pot must wake up and take measures, in seconds.
 - `sending_interval`: the interval a which the Pot must wake up and send the data accumulated, in seconds.

### Command object

A Command object MUST have the following members:
 - `type`: a string representing the command type.
 - `status`: a string representing the command status.
 - `datetime`: a string representing the date and the time (UTC) when the status last changed, following the ISO 8601 standard.

A Command object MAY have the following members:
 - `id`: id of the command. It MUST only be generated by the server. When a command is executed, it's `id` must be sent back in the Command object. If the command is a new command, the `id` field MUST NOT be present.
 - `args`: other sub-members, depending on the command.

The following types are available:
 - `water`: water the plant.

The following statuses are available:
 - `new`: new command from the Smartphone.
 - `sent`: command sent to the Pot.
 - `executed`: command executed by the pot.

## Date and Time

To avoid timezone problems, all the dates and time sent must be in UTC following the ISO 8601 standard.

They must follow the following format: `yyyy-MM-dd'T'HH:mm:ss'Z'` (Java) or `%Y-%m-%dT%H:%M:%SZ` (Python).

They will be converted to the local timezone of the User only when displayed.

## Security considerations

The most sensitive data handled by the system is the User's Wi-Fi password. Since a default password is set by the Pot when in AP mode, the connection between the Pot and the Smartphone during the setup phase is encrypted (using a unique randomly generated session key) and the password is not sent in clear. Thus, the User's Wi-Fi password is protected (even if the default password is known by everyone), which would not be the case using a passwordless AP.

More generally, the connection between the Pot and the Server and the connection between the Server and the Smartphone SHOULD be established in HTTPS mode to have an encrypted connection and avoid information leakage. In HTTPS, both the URL and data sent is encrypted.

Concerning the UUID, it is a 128-bit number randomly generated. The latest version of UUID (version 4) have 122 bits randomly generated, giving `2^122`, or `~5.3*10^36`, different possibilities. The number of random version 4 UUIDs which need to be generated in order to have a 50% probability of at least one collision is `~2.71*10^18`, which means that a collision is highly improbable. Thus, a randomly generated UUID is a good way to generate a unique and secret key/password.
