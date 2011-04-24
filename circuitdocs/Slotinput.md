---
layout: main
title: slotinput
---

Input decimal numbers according to your currently selected inventory slot.
When left-clicking one of the chip's interface blocks a digit represented by that block is set to the number of the player's currently selected inventory slot.
Right-clicking the interface block will set its digit to 0. Once one of the digits is updated the chip's outputs are set to the binary value of the new decimal number.

Any number of interface blocks can be used each representing another decimal digit. An additional `<number of sets>` argument can be added to have more than one output number. In this case the interface blocks are divided into sets, each set controlling one output set.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/slotinput.java)
    
* * *

#### I/O setup 
* At least 1 interface block. Number of interface blocks must be a multiple of `<number of sets>` argument if set.
* At least 1 output pin. Number of outputs must also be a multiple of `<number of sets>` argument if set.

#### Sign text
1. `   slotinput   `
2. ` [number of sets] ` (optional. defaults to 1)

__Version history:__ Added to SensorLibrary 0.25, made by Shamebot.