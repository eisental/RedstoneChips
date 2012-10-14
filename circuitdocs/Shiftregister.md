---
layout: main
title: shiftregister
---

A SIPO (Serial-In-Parallel-Out) shift register. Can be built with any word size determined by the number of output pins.
Each time the clock input is turned on, the register shifts one bit and inserts the current value of the data pin. The shiftregister will shift to the left by default. 
The 1st sign argument determines the shift direction - `left` or `right`.

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
2. `   [left|right]   ` (optional)

![a 4-bit shiftregister](/RedstoneChips/images/shiftregister.png "a 4-bit shiftregister")

__Version history:__ Added to BasicCircuits 0.1