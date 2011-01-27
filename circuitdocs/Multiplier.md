---
layout: main
title: multiplier
---

Performs an arithmetic multiplication on any number of bit sets and outputs the result as a binary number.
If an optional constant argument is added to the sign text, the IC will multiply the result by the constant.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/multiplier.java)

#### I/O setup 
* Number of output pins sets the word length of each bit set.  
* Number of input pins can be any multiple of the word length.

#### Sign text
1. `   multiplier   `

2. ` [constant number]  `

##### Version history: Added to BasicCircuits 0.1