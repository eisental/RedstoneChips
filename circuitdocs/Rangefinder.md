---
layout: main
title: rangefinder
---

An ultrasonic range finder. When the clock pin is triggered the chip will output the distance to the closest object. Place an interface block and attach another interface block to one of its sides to set the direction of the range finder. The chip will fire a wave in the direction both interface blocks point to, and measure the distance according to the time it takes for the wave to return. 
The default detection range is 10m in an area of 3x3m (width and height). Any entity including players, creatures, dropped items and plain old blocks will be detected. 
When the distance value in meters can't fit the chip's outputs it is scaled. For example, when it has 4 outputs and a range of 10 meters, it will output 10 (in binary) for an object at 10 meters (or when no object is found) but if the chip has only 2 outputs, the output value will be scaled so a range of 10 meters will result in an output of 3. To force scaling the output when the maximum value is larger than the range (in order to get better resolution), use `scale` as 2nd or 3rd argument.
The cuboid width and height can be set by adding a 2nd cuboid-size argument, either in the format of WxH (i.e. 1x2 or 5x10) or just 1 number when both width and height are the same.

on [Wikipedia](http://en.wikipedia.org/wiki/Ultrasonic_sensor)

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/rangefinder.java)
    
* * *

![rangefinder connected to a clock](/RedstoneChips/images/rangefinder.png "rangefinder connected to a clock")

#### I/O setup 
* 1 input clock pin.
* Any number of output pins to output the range.
* 1 interface block connected to the chip with another interface block attached to one of its sides.

#### Sign text
1. `   rangefinder   `
2. `   [range]   ` (optional, defaults to 10m)
3. `   [cuboid-size]   ` (optional, defaults to 3x3)
4. `   [scale]   ` (optional)

__Version history:__ Added to SensorLibrary 0.2