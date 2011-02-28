---
layout: main
title: daytime
---

A circuit for getting the current either real or game time. 
Use 'earthtime' as 1st sign argument to sync with real world time or use 'gametime' to sync with game ticks. 
The 2nd sign arguments determines the time field that will be sent to the chip's outputs.

These are valid time field values for earthtime:
- `hour` - Real day hour. Between 0-23.
- `minute` - Minute of real hour. Between 0-59.
- `second` - Second of real minute. Between 0-59.
- `secondofday` - Second of real day. Between 0-86399.

These are the valid time field values for gametime:
- `hour` - Current game time in hours. Between 0-23. Each hour lasts a 1000 ticks. 
- `tick` - Game tick of current hour. Between 0-1000.
- `tickofday` - Current game time in ticks. Between 0-23999.

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