---
layout: main
title: pulse
---

A pulse generator. When one of the inputs changes from off to on, the corresponding output changes to on, waits a certain duration and then goes back to off. The pulse duration is set with a sign argument. Possible values are any duration in the same format as [clock](Clock): 120bpm, 10hz, 1sec, 2years are all possible.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/pulse.java)
    
* * *


#### I/O setup 
* Can have any number of input, output pairs. At least 1 pair is required.

#### Sign text
1. `   pixel   `
2. `  [duration] ` (default value is 1sec)

__Version history:__ Added to BasicCircuits 0.6