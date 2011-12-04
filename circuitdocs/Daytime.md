---
layout: main
title: daytime
---

A chip for sending the current real, or game time. 
Use 'earthtime' as 1st sign argument to sync with real world time or use 'gametime' to sync with game ticks. 
The 2nd sign argument determines the time field that will be sent to the chip's outputs.

Possible time field argument values:
- `tick` - In earthtime, equivalent to secondofday. In gametime, the current game tick, between 0-23999.
- `second` - In earthtime, current second between 0-59. In gametime it's equivalent to `tick`.
- `secondofday` - In earthtime, current second of day, between 0-86399. In gametime, equivalent to `tick`.
- `minute` - Current minute, between 0-59.
- `minuteofday` - Current minute of day, between 0-1439.
- `hour` - Current hour, between 0-23.
- `second10` - In earthtime, the current second 10s digit between 0-5.
- `second1` - In earthtime, the current second 1s digit between 0-9.
- `minute10` - Current minute 10s digit, between 0-5.
- `minute1` - Current minute 1s digit, between 0-9.
- `hour10` - Current hour 10s digit, between 0-5.
- `hour1` - Current hour 1s digit, between 0-9.

A 3rd optional sign argument determines the world the chip will get the current time from. Enter any world name to use that world's time instead of the time in the world the 
chip is built in. 

The chip will update its outputs once the clock pin is triggered according to the current time.

If the chip is not built with enough output pins to output the whole range of values for the time field being used, the output is mapped accordingly.
For example, If you build a daytime chip that uses the hour time field with only 1 output bit, hours 0-11 will output 0 while hours 12-23 will output 1.
Using 5 outputs leaves the chip enough bits to output the unmapped value ranging between 0-23.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/daytime.java)
    
* * *


#### I/O setup 
* 1 clock input pin.
* Any number of output pins.

#### Sign text
1. `   daytime   `
2. `   [earthtime/gametime]   ` (optional, defaults to gametime)
3. `   [time field]   ` (optional, gametime defaults to tick, earthtime defaults to second)
4. `   [world]    ` (optional, defaults to the world the chip is built in) 

__Version history:__ Added to SensorLibrary 0.2