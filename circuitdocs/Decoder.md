---
layout: main
title: decoder
---

Reverse operation of the [encoder](Encoder) chip.
 
on [Wikipedia](http://en.wikipedia.org/wiki/Decoder)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/encoder.java)

#### I/O setup 
* Must have at least 2 inputs.
* input pin 0 (write):  When set to high, the input state is decoded and the result is sent to the output pins.
* input pins 1 and above (data): The bit-set to decode.
* Number of output pins must be 2 to the power of number of inputs plus 1. i.e. 2in - 2out, 3in - 4out, 4in - 8out, 5in - 16out, and so forth.

#### Sign text
1. `   decoder   `

##### Version history: Added to BasicCircuits 0.1