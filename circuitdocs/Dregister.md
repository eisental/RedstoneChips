---
layout: main
title: dregister
---
Any number of D-flipflops sharing a clock pin and a reset pin.
When the clock pin (input 0) is triggered, the data pins values (from input 2 onwards) are copied into the register and its output pins are set accordingly.
When the reset pin (input 1) is triggered, the register is cleared and all of the circuits outputs are set to off.

Using this circuit it's possible to make any chip's asynchronous inputs to be clock synchronized.

D-flipflop on [Wikipeda](http://en.wikipedia.org/wiki/Flip-flop_(electronics)#D_flip-flop)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/dregister.java)
    
* * *

#### I/O setup 
* Can have any number of outputs.
* Requires 2 more inputs than outputs. 1 clock pin, 1 reset pin, and the same number of data pins as there are outputs.

#### Sign text
1. `   dregister   `

__Version history:__ Added to BasicCircuits 0.87