---
layout: main
title: photocell
---
Detects the average light level around the chip's interface blocks. Any number of interface blocks can be used in different locations.
When the clock input is triggered, the circuit will output the average light level relative to the number of output data bits used.
Treating no light as 0 and full daylight as the maximum number the data bits can output. When one of the interface blocks' faces are covered with another block, the light level will go down, unless that block is part of the photocell circuit.

on [Wikipedia](http://en.wikipedia.org/wiki/Photoresistor)

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/photocell.java)
    
* * *


#### I/O setup 
* 1 input clock pin.
* 1-4 output data pins.

#### Sign text
1. `   photocell   `

__Version history:__ Added to SensorLibrary 0.1