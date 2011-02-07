---
layout: main
title: clock
---

Simple clock chip with any number of triggerable outputs. Whenever an input pin is set to high, the corresponding output pin will start sending clock pulses. Only one clock is actually used in each chip causing the outputs to be synchronised.
Pulse width is currently 50%. 

The clock frequency is set in the sign argument. Default value is 1000 (1 second).
The frequency argument can be either the duration in milliseconds or it can use a large number of [units](https://github.com/eisental/RedstoneChips/blob/master/src/main/java/org/tal/redstonechips/parsing/UnitParser.java). 
Some examples include 1000ms 1sec 60bpm 2hz and even 1year.

The clock's pulse width is set to 0.5 by default which means that the clock output would be on half of the time and off half of the time. You can change the pulse width by adding a second argument to the chip sign. The argument should be a fraction between 0 and 1. A lower number would cause the clock to change its output back to off faster. Use a pulse width of 0 for better stability and performance.
 
The clock can't run faster than once per 100 ms. 

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/clock.java)

* * *


#### I/O setup 
* Should have the same number of inputs and outputs.

#### Sign text
1. `   clock   `
2. ` [frequency] `
3. ` [pulse-width] ` (default is 0.5)

![Clock circuits](/RedstoneChips/images/clock.png "Clock circuits")

__Version history:__ Added to BasicCircuits 0.1