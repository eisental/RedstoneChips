---
layout: main
title: comparator
---

A digital comparator circuit that compares two input numbers of any size and outputs the result.

There are 2 modes of operation:
- Identity comparator, when the chip is built with 1 output. The chip sets its output on when both numbers are equal.
- Magnitude comparator, When it's built with 3 outputs. It will set its 1st output on when the 1st input is less than the 2nd input, its 2nd output on when the 1st input is equal to the 2nd input, and its 3rd output on when the 1st input is greater than the 2nd input.

The 2nd input number can be a constant number set in the sign arguments. In that case the 1st number is the value of all the input pins and the 2nd number is the argument constant. When the constant argument is not used, the 1st half of input bits are treated as the 1st number and the 2nd half of input bits are treated as the 2nd number.

on [Wikipedia](http://en.wikipedia.org/wiki/Digital_comparator)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/comparator.java)
    
* * *


#### I/O setup 
* Can have any number of inputs. When a constant is used, the set of input pins is treated as one number. If a constant is not used, the circuit must have an even number of input pins.
* Can have either 1 output pin for the identity mode, or 3 output pins for the magnitude mode.

#### Sign text
1. `   comparator   `
2. `   [constant]   ` (optional)

__Version history:__ Added to BasicCircuits 0.77