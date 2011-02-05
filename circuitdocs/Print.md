---
layout: main
title: print
---

Prints the data pins (inputs 1 and above) as text on a sign block when the clock pin (input 0) is set to high. 
Any wall sign, attached to an interface block will be updated.

The first sign argument sets the way the data is printed to the sign.
* num - prints input bits as unsigned integer.
* signed - prints input bits as signed integer.
* unsigned - same as 'num' 
* ascii - prints an ascii character based on the integer value of the input bits.
* hex - same as 'unsigned' with hexadecimal representation.
* oct - same as 'unsigned' with octal representation.
* bin - prints the incoming bits as a series of 0s and 1s.

A second sign argument with the word 'add' would cause the chip to add any new printed text to what's already printed.
If the clock signal fails to update the signs, you can try using a redstone repeater (two redstone inverters in a row) between the 
clock source (this can be also a lever or button of course) and the clock pin.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/print.java)

* * *


#### I/O setup 
* At least 2 inputs and no outputs. 

#### Sign text
1. `   print   `
2. `  [type] ` (num by default)
3. ` [add] `

__Version history:__ Added to BasicCircuits 0.1