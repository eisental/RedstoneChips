---
layout: main
title: ringcounter
---

A ring decimal counter chip. Whenever input pin 0 goes from off to on, the counter increments its value by 1 and sets the corresponding output on. Once the last output is reached, the counter turns on the first output.
Setting the reset pin (input pin 1) from off to on, turns off all the counter outputs and sets the counter to turn the 1st output on the next trigger.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/ringcounter.java)

* * *


#### I/O setup
- Should have 2 inputs and any number of outputs.
- input 0 (inc): When set to high the counter output is incremented by 1.
- input 1 (reset): Turns off all the counter's outputs.


#### Sign text
1. `   ringcounter   `

__Version history:__ Added to BasicCircuits 0.74, commissioned by __I D__.