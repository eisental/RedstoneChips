---
layout: main
title: daytime
---

A circuit for getting the current either real or game time. 
Use 'earthtime' as 1st sign argument to sync with real world time or use 'gametime' to sync with game ticks. 
The 2nd sign arguments determines the time field that will be sent to the chip's outputs.

Possible time field argument values:
- `tick` - In earthtime, equivalent to secondofday. In gametime, the current game tick, between 0-23999.
- `second` - In earthtime, current second between 0-60. In gametime it's equivalent to `tick`.
- `secondofday` - In earthtime, current second of day, between 0-86399. In gametime, equivalent to `tick`.
- `minute` - Current minute, between 0-60.
- `minuteofday` - Current minute of day, between 0-1439.
- `hour` - Current hour, between 0-23.

The chip will update its outputs once the clock pin is triggered according to the current time.

If the chip is not built with enough output pins to output the whole range of values for the time field it's using, the output is mapped accordingly.
For example, If you build a daytime chip that uses the hour time field with only 1 output bit, hours 0-11 will output 0 while hours 12-23 will cause it to output 1.
Using 5 outputs leaves the chip enough bits to output the unmapped value ranging between 0-24.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/daytime.java)
    
* * *


#### I/O setup 
* 1 clock input pin.
* Any number of output pins.

#### Sign text
1. `   daytime   `
2. `   [earthtime/gametime]   ` (optional, defaults to gametime)
3. `   [time field]   ` (optional, gametime defaults to tick, earthtime defaults to second)

__Version history:__ Added to SensorLibrary 0.2