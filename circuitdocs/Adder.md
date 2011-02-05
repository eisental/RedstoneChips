---
layout: main
title: adder
---

Performs an arithmetic addition on any number of bit sets and outputs the result as a binary number.
If an optional constant argument is added to the sign text, the IC will add it to the result as well. 

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/adder.java)

* * *

#### I/O setup 
* Can have any number of outputs. The number of output pins determines the number of bits in each input bit set.  
* The number of input pins can be any multiple of the number of outputs.

#### Sign text
1. `   adder   `

2. ` [constant number]  `

![4-bit adder](/RedstoneChips/images/adder.jpg "4-bit adder")

4-bit adder with 1 input bit set. Constant argument is set to 3. 5 goes in and 8 goes out.

__Version history:__ Added to BasicCircuits 0.1