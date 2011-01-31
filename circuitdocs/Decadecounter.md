---
layout: main
title: decadecounter
---

A decade counter. Whenever input pin 0 goes from low to high, the counter increments its value by 1 and sets the corresponding output
to high. Once the last output has been reached, the counter turns on the first output.
Setting the reset pin (input pin 1) from low to high, turns off all the counter outputs and sets the counter to turn the 1st output on the next trigger.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/decadecounter.java)

* * *


#### I/O setup
- Should have 2 inputs and any number of outputs.
- input 0 (inc): When set to high the counter output is incremented by 1.
- input 1 (reset): Turns off all the counter's outputs.


#### Sign text
1. `   decadecounter   `

__Version history:__ Added to BasicCircuits 0.74, commissioned by __I D__.