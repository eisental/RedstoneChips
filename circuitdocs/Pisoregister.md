---
layout: main
title: pisoregister
---

A PISO shift register. Can be built with any bit-length determined by the number of input pins.
When write/shift pin is set to HIGH, the data input values are stored in the register.
Once the write/shift pin is set to LOW, any pulse in clock input will set the output pin to the next register bit.
 
on [Wikipedia](http://en.wikipedia.org/wiki/Shift_register)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/pisoregister.java)

#### I/O setup 
* Should have at least 3 inputs and only one output.
* Input pin 0: clock input
* Input pin 1: write/shift pin
* Input pins 3 and above: data input
#### Sign text
1. `   shiftregister   `
***
back to [Circuit Classes](Home)
