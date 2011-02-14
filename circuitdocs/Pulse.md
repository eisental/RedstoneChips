---
layout: main
title: pulse
---

A pulse generator. When one of the inputs changes from low to high, the corresponding output changes to on, waits a certain duration and then goes back to off. The pulse duration is set with a sign argument. Possible values are any duration in the same format as [clock](Clock): 120bpm, 10hz, 1sec, 2years are all possible.

It's possible to set the 2nd, optional, edge-trigger argument to change the triggering behaviour:
- `positive` (default value) - The pulse will trigger when its input goes from low to high.
- `negative` - The pulse will trigger when its input goes from high to low.
- `doubleEdge` - The pulse will trigger whenever the input changes, from low to high or from high to low.

For maximum performance pulse of 0ms duration should be used. This will make the circuit output to go from low to high and then immediately back, creating an invisible pulse.
Though it's not possible to see the current change the pulse is there and can be used to control any circuit's clock input.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/pulse.java)
    
* * *


#### I/O setup 
* Can have any number of input, output pairs. At least 1 pair is required.

#### Sign text
1. `   pulse   `
2. `  [duration] ` (default value is 1sec)
3. `  [edge-trigger] ` (default value is positive)

__Version history:__ Added to BasicCircuits 0.6