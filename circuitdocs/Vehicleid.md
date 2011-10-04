---
layout: main
title: vehicleid
---

A self-triggering vehicle (minecarts, boats, etc.) sensor. When a vehicle is detected on top of any of the chip's interface blocks, its data outputs
are set to the vehicle entity's id number (in binary) and its clock output (1st output) is triggered. 

An optional disable input pin can be used to disable the chip (when turned on), saving some processing power from the server. 
A 2nd input serves as a reset pin, clearing the chip's outputs when triggered.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/vehicleid.java)
    
* * *


#### I/O setup 
* 1 clock output and any number of data outputs.
* 1st input is an optional disable input.
* 2nd input is an optional reset input.

#### Sign text
1. `   vehicleid   `

__Version history:__ Added to SensorLibrary 0.3