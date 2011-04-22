---
layout: main
title: srnor
---
A SR NOR latch.  When an input state changes from off to on, it's corresponding output pin is turned off while all other output pins are turned on. This SR NOR latch can support any number of input, output pairs.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/srnor.java)

* * *


#### I/O setup 
* Must have the same number of outputs and inputs.

#### Sign text
1. `   srnor   `

![4-bit SR NOR latch](/RedstoneChips/images/srnor.png "4-bit SR NOR latch")

__Version history:__ Added to BasicCircuits 0.72