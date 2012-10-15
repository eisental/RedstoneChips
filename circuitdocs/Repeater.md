---
layout: main
title: repeater
---

A repeater chip. Will set the state of its outputs according to its inputs.
If the chip only has one input, that input will be repeated to all available
outputs. If the chip has more than one input, then the output pins will be
separated into sets, where each set has the same number of pins as the input.
(The output pin count must be a multiple of the input pin count.)

For example, with 3 inputs, and 9 outputs: input 1 will be repeated on outputs
1, 4, and 7; input 2 will be repeated on outputs 2, 5, and 8; and input 3 will
be repeated on outputs 3, 6, and 9.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/repeater.java)
    
* * *


#### I/O setup 
* Must have at least 1 input.
* Number of outputs must be a multiple of the number of inputs.

#### Sign text
1. `   repeater   `

__Version history:__ Added to BasicCircuits 0.83