---
layout: main
title: and gate
---

An AND gate. When used with 1 output, the output is on only when all inputs are on. 
When the chip has more than 1 output its inputs are grouped into bit sets. The size of each bit set is equal to the number of outputs.
The chip will AND each input with the corresponding inputs in all bit sets. 

For ex. to AND 3 numbers of 4 bits each together, build the chip with 12 inputs and 4 outputs. Inputs 0-3 represent the 1st number, inputs 4-7 represent the 2nd number and
inputs 8-11 represent the 3rd number. The chip will AND the corresponding bits of each one of the inputs and output the result to the corresponding output pin. 
i.e. The chip will AND inputs 0,4 and 8 and set output pin 0 accordingly it will then and inputs 1,5 and 9 and set output pin 1 and so forth.

on [Wikipedia](http://en.wikipedia.org/wiki/And_gate)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/and.java)

* * *


#### I/O setup 
* Can have any number of outputs. The number of output pins determines the number of bits in each input bit set.  
* The number of input pins can be any multiple of the number of outputs.

#### Sign text
1. `   and   `

__Version history:__ Added to BasicCircuits 0.1


