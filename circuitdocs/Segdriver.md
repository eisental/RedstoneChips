---
layout: main
title: segdriver
---

A 7-segment led display driver for one digit. Translates an input binary number (up to 4-bits) into the proper segments that are necessary in order to display the digit. 
The chip needs to have 7 outputs. Connect each output to the corresponding segment. Output 0 to segment a, output 1 to segment b, etc. 
Uses standard segment configuration. Check the wikipedia link to see which letter goes to which segment. 
When the segdriver has more than 1 input its 1st input is a clock input. Only when the clock input is triggered the segdriver's outputs are updated according to its input bits.

An additional blanking pin can be added by writing `blankPin` as a sign argument. In this case, when the circuit has a clock input, the 2nd input is used as the blanking pin. When the circuit doesn't have a clock input, the 1st input is used as the blanking pin. When the pin is on the display is blanked by keeping each of the segdriver's outputs off. Once it's turned off, the circuit will again output the segments for the current input value.

In addition to building the segdriver it's still of course necessary to actually build the digit display and this can be done using anything you want. See below for an example using pixel chips.

The segdriver will print hex a,b,c,d,e and f letters for input values 10-15.

To easily divide a binary number into multiple decimal digits see the [bintobcd](Bintobcd) circuit.

on [Wikipeda](http://en.wikipedia.org/wiki/7_segment_display)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/segdriver.java)

![7-segment display using pixel circuits](/RedstoneChips/images/sevensegment.png "7-segment display using pixel circuits")

7-segment display using pixel circuits.
    
* * *


#### I/O setup 
* Can have up to 5 inputs or up to 6 inputs in blankPin mode. 
* When more than 1 inputs, or 2 inputs in blankPin mode, are used the 1st input is a clock input.
* Must have 7 outputs. 1 for each segment. 

#### Sign text
1. `   segdriver   `
2. `   [blankPin]  `

![Clock, counter, segdriver connected to 7 transmitters controlling the display digit in the back.](/RedstoneChips/images/sevensegment2.png "Clock, counter, segdriver connected to 7 transmitters controlling the display digit in the back.")

Clock, counter, segdriver connected to 7 transmitters controlling the display digit in the back.

__Version history:__ Added to BasicCircuits 0.83