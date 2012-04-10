---
layout: main
title: burst
---

A Burst chip will pulse its outputs a specified number of times.

The chip requires at least one input and can have any number of outputs.

The first input block is the trigger. When it goes from off to on, the chip begins pulsing the outputs.

The number of times to pulse the outputs can either be specified on the sign, or can be set via input blocks. 1 input block allows 0-1 pulses, 2 input blocks allow 0-2 pulses, 8 input blocks allow 0-15 pulses, etc.

An additional pulse rate argument can be added to specify a time interval between each pulse. Otherwise the pulse rate is 0.
The pulse rate must be specified with a [unit](https://github.com/eisental/RedstoneChips/blob/master/src/main/java/org/tal/redstonechips/util/UnitParser.java). 
For ex. 1sec, 2min, 10hz etc.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/burst.java)

* * *

#### I/O setup 
* Requires at least 1 input.
* Can have any number of outputs.

#### Sign text
1. `   burst   `
2. ` [pulses] ` (optional)
3. ` [pulse rate] ` (optional)

__Version history:__ Added to BasicCircuits 0.92
