---
layout: main
title: multiplier
---

Performs an arithmetic multiplication on any number of input bit sets and outputs the result as a binary number.
If an optional constant argument is added to the sign text, the IC will multiply the result by the constant.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/multiplier.java)

* * *


#### I/O setup 
* Can have any number of outputs. The number of output pins determines the number of bits in each input bit set.  
* The number of input pins can be any multiple of the number of outputs.

#### Sign text
1. `   multiplier   `

2. ` [constant number]  `

__Version history:__ Added to BasicCircuits 0.1