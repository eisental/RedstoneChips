---
layout: main
title: burst
---

A pulse generator that can send any number of pulses in a single instantaneous burst.
When the chip's 1st input pin is triggered each of it's outputs is set on and off successively according to the set pulse count. 
The pulse count is determined either by the 1st sign argument or by the binary value of any additional input pins (inputs 1 and above).

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/burst.java)
    
* * *


#### I/O setup 
* Must have 1 trigger pin.
* Can have any number of outputs.
* Can have any number of additional input pins to set the number of pulses on each burst.

#### Sign text
1. `   burst   `
2. `  [pulse count] ` (optional)

__Version history:__ Added to BasicCircuits 0.92