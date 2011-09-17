---
layout: main
title: playerid
---
The playerid chip captures the unique identifier of any player who passes over one of it's interface blocks, and toggles a clock pin to indicate a player has passed over it.

This requires a minimum of two output pins. The first is the clock output which toggles whenever a player passed over an interface, and the rest output the players unique identifier.

Optionally this chip may have one or two input pins. The first input pin is a disable pin. If this pin is on, players will be ignored and all outputs will be turned off. The second is a reset pin, which when toggled will reset all outputs to an off state.

Note, the unique id of a player is only valid while the player is logged into the server. If the player logs off and back on, they will be assigned a new ID.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/playerid.java)

* * *


#### I/O setup 
* Requires at least two output pins.
* Requires at least one Interface block.
* May optionally have up to two input pins. A disable pin and a reset pin.
* Capturing the full ID of the player requires 33 output pins. One for the clock, and 32 for the playerid.

#### Sign text
1. `   playerid   `

__Version history:__ Added to SensorLibrary 0.26