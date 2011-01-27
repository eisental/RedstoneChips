---
layout: main
title: multiplexer
---

A multiplexer chip capable of multiplexing any number of input sets of any size. Number of outputs determine the bit-size.

For example, to build a 4 bit multiplexer that can select one input out of 3 you would need 2 select inputs, and 3 input sets of 4 pins each.
When the select pins value changes a new input set is sent to the chip outputs. 

on [Wikipedia](http://en.wikipedia.org/wiki/Multiplexer)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/multiplexer.java)

#### I/O setup 
* Number of outputs determine the bit-size.
* First log2(input sets count) pins are the select pins. 
* Any number of input sets (according to the count argument) should follow the select pins.
* Each input set must have the same number of pins as there are output pins.

#### Sign text
1. `   multiplexer   `
2. ` <input sets count> ` (NOT optional)

##### Version history: Added to BasicCircuits 0.1