---
layout: main
title: random
---

When one of the input pins becomes high its corresponding output pin is randomly set (50% chance) to either high or low.
The chip can have any number of matching inputs and outputs. It's also possible to use 1 input pin to trigger any number of outputs.
 
[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/random.java)

* * *

#### I/O setup 
* Must have either the same number of outputs and inputs or only 1 input.

#### Sign text
1. `   random   `

![random in 1 input mode](/RedstoneChips/images/random.png "random in 1 input mode")

__Version history:__ Added to BasicCircuits 0.1