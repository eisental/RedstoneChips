---
layout: main
title: delay
---

A fixed delay chip. When one of its inputs changes state, the corresponding output changes its state accordingly after a certain time duration. 
A sign argument is used to set the delay duration. Possible values are any duration in the same format as [clock](Clock): 120bpm, 10hz, 1sec, 2years are all possible.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/delay.java)
    
* * *


#### I/O setup 
* Can have any number of input/output pairs. At least 1 pair is required.

#### Sign text
1. `   delay   `
2. `  [duration] ` (default value is 1sec)

__Version history:__ Added to BasicCircuits 0.8