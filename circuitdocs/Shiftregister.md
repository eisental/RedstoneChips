---
layout: main
title: shiftregister
---

A SIPO shift register. Can be built with any wordlength determined by the number of output pins.
Each time the clock input is set to high, the register shifts one bit to the left and it's first bit is set to the current state of the data pin.
An optional reset pin can be added. When it's triggered the register is cleared.
 
on [Wikipedia](http://en.wikipedia.org/wiki/Shift_register)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/shiftregister.java)



* * *


#### I/O setup 
* Should have 2 or 3 inputs. Can have any number of outputs.
* Input pin 0: clock input.
* Input pin 1: data input.
* Input pin 2: optional reset pin.

#### Sign text
1. `   shiftregister   `

![a 4-bit shiftregister](/RedstoneChips/images/shiftregister.png "a 4-bit shiftregister")

__Version history:__ Added to BasicCircuits 0.1