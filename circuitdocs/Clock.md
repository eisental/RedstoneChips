---
layout: main
title: clock
---

A clock chip that can tick in any frequency. The chip can have any number of outputs. It can have 1 input to control all outputs or have multiple inputs, 1 for each output. When a clock input is turned on it will start to toggle it's output lever at the specified rate.
Even when more than 1 output is used only one clock is actually running in each chip causing the outputs to be synchronised. 

The clock frequency is set in the sign argument. Default value is 1000 (1 second).
The frequency argument can be either the duration in milliseconds or it can use a large number of [units](https://github.com/eisental/RedstoneChips/blob/master/src/main/java/org/tal/redstonechips/util/UnitParser.java). 
Some examples include 100ms 1sec 60bpm 2hz and even 1year, 1week or 1day. All duration are in real time values and not game time.

The clock's pulse width is set to 0.5 by default which means that the clock output would be on half of the time and off half of the time. You can change the pulse width by adding a second argument to the chip sign. The argument should be a fraction between 0 and 1. A lower number would cause the clock to change its output back to off faster. Use a pulse width of 0 or 1 for maximum stability and performance.
 
The clock chip uses the system time to prevent it's output timing to drift keeping a steady pulse over long periods of time. 
The minimum duration of the clock's on or off states is 50ms (or 1 game tick).

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/clock.java)

* * *


#### I/O setup 
* Can have any number of outputs.
* Should have either 1 input or the same amount of inputs and outputs.

#### Sign text
1. `   clock   `
2. ` [frequency] `
3. ` [pulse-width] ` (default is 0.5)

![Clock circuits](/RedstoneChips/images/clock.png "Clock circuits")

__Version history:__ Added to BasicCircuits 0.1