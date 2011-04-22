---
layout: main
title: encoder
---

A priority encoder circuit of any size. Bit size is determined by the number of outputs.
The basic operation of the encoder is to output a binary number equal to the pin number of the input that's currently on.
For example, when the 3rd input of a 4input-2output encoder is on its outputs will be set to 10 or decimal 2.

on [Wikipedia](http://en.wikipedia.org/wiki/Priority_encoder)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/encoder.java)

* * *


#### I/O setup 
* Can have any number of outputs. 
* Number of input pins must be 2 to the power of the number of outputs. For example: 4 inputs - 2 outputs, 8 inputs - 3 outputs, 16 inputs - 4 outputs and so forth.

#### Sign text
1. `   encoder   `

__Version history:__ Added to BasicCircuits 0.1