---
layout: main
title: pirsensor
---

An ultrasonic range finder. When the clock pin is triggered the chip will output the distance to the closest obstacle. Place an interface block and attach a noteblock to 1 of its sides to set the direction of the range finder. The chip will fire a wave in the opposite direction to the face of the noteblock that's attaches to the interface block.
The closest obstacle in a cuboid of 3x3xrange in the chosen direction will be detected. Any entity including players, creatures, dropped items and plain old blocks can be detected. The output value is scaled so that the full range of the chip fit the number of output pins. When it has 4-bits and a range of 10 meters, for example, it will output 15 for an obstacle at 10 meters (or none at all) and 7 for an obstacle of 5 meters. 

This circuit is still under development. The actual readout is still not completely accurate since the circuit needs to know the actual size of each obstacle.

on [Wikipedia](http://en.wikipedia.org/wiki/Ultrasonic_sensor)

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/rangefinder.java)
    
* * *


#### I/O setup 
* 1 input clock pin.
* Any number of output pins to output the range.
* 1 interface block with a noteblock attached to one of its sides. 

#### Sign text
1. `   rangefinder   `
2. `   [range]   ` (optional, defaults to 10 block operation range)

__Version history:__ Added to SensorLibrary 0.2