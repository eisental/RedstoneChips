---
layout: main
title: demultiplexer
---

The reverse operation of the [multiplexer](Multiplexer) chip. Sends one set of input data to a selected output set.

on [Wikipedia](http://en.wikipedia.org/wiki/Multiplexer)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/demultiplexer.java)

* * *


#### I/O setup 
* Number of outputs must be a multiple of the `<no. of output sets>` argument.
* bit-size is determined by number of outputs / `<no. of output sets>` argument.
* First `log2(<no. of input sets>)` pins are the select pins. 
* One input bit set should follow the select pins of the same size as one output set.

For example to demultiplex a 4-bit input into 4 4-bit output sets you would need 3 select input pins and additional 4 data inputs - total of 7 inputs
as well as 4 groups of 4-bit output pins - total of 16 outputs.

#### Sign text
1. `   multiplexer   `
2. ` <no. of output sets> ` (NOT optional)

__Version history:__ Added to BasicCircuits 0.1