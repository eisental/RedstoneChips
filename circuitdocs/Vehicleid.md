---
layout: main
title: vehicleid
---
The vehicleid chip captures the unique identifier of any vehicle that passes over one of it's interface blocks, and toggles a clock pin to indicate a vehicle has passed over it.

This requires a minimum of two output pins. The first is the clock output which toggles whenever a vehicle passed over an interface, and the rest output the vehicles unique identifier.

Optionally this chip may have one or two input pins. The first input pin is a disable pin. If this pin is on, vehicles will be ignored and all outputs will be turned off. The second is a reset pin, which when toggled will reset all outputs to an off state.

Note, the unique id of a vehicle is only valid while the vehicle exists. If the vehicle is destroyed for any reason, for example a minecart teleport plugin, the new vehicle will have a different unique id.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/vehicleid.java)

* * *

#### I/O setup 
* Requires at least two output pins.
* Requires at least one Interface block.
* May optionally have up to two input pins. A disable pin and a reset pin.
* Capturing the full ID of a vehicle requires 33 output pins. One for the clock, and 32 for the vehicleid.

#### Sign text
1. `   vehicleid   `

__Version history:__ Added to SensorLibrary 0.3
