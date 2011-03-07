---
layout: main
title: and gate
---

An AND gate that supports any number of bit sets of any size.

For example, to AND 3 4-bit numbers together, build the chip with 12 inputs and 4 outputs. Inputs 0-3 represent the 1st number, inputs 4-7 represent the 2nd number and
inputs 8-11 represent the 3rd number. The chip will AND the corresponding bits of each one of the inputs and output the result to the corresponding output pin. 
i.e. The chip will AND inputs 0,4 and 8 and set output pin 0 accordingly it will then and inputs 1,5 and 9 and set output pin 1 and so forth.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/and.java)

* * *


#### I/O setup 
* Can have any number of outputs. The number of output pins determines the number of bits in each input bit set.  
* The number of input pins can be any multiple of the number of outputs.

#### Sign text
1. `   and   `

__Version history:__ Added to BasicCircuits 0.1


