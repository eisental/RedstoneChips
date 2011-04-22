---
layout: main
title: flipflop
---

A flipflop register of any number of bits. When an input is turned on, its corresponding output changes its state.
If the chip has one extra input pin, the first input becomes a reset pin. 
When it's triggered it will turn every flipflop on the chip off.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/flipflop.java)

* * *

#### I/O setup 
* Can have any number of input/output pairs. 
* If the circuit has one extra input pin, the first input pin will become a reset pin.

#### Sign text
1. `   flipflop   `

![4-bit flipflop register](/RedstoneChips/images/flipflop.png "4-bit flipflop register")

__Version history:__ Added to BasicCircuits 0.1