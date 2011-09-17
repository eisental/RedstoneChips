---
layout: main
title: pulse
---

A pulse generator. When one of the inputs changes from off to on, its corresponding output changes to on, waits a certain duration and then goes back to off. The pulse duration is set with a sign argument. Possible values are any duration in the same format as [clock](Clock): 120bpm, 10hz, 1sec, 2years are all possible.

It's possible to set the 2nd, optional, edge-trigger argument to change the triggering behavior:
- `positive` (default value) - The pulse will trigger when its input goes from low to high.
- `negative` - The pulse will trigger when its input goes from high to low.
- `doubleEdge` - The pulse will trigger whenever the input changes, from low to high or from high to low.

For maximum performance a pulse of 0ms duration should be used. This will make the circuit output to go from off to on and then immediately back, creating an invisible pulse.
Though it's not possible to see the current change the pulse is there and can be used to control any circuit's clock input.

If the chip only has one input and multiple outputs, when the input changes from off to on, the first output will turn on, wait the duration, then turn off, then the next output will will do the same, until all outputs have toggled.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/pulse.java)
    
* * *


#### I/O setup 
* Can have any number of input, output pairs, or a single input any any number of outputs.

#### Sign text
1. `   pulse   `
2. `  [duration] ` (default value is 1sec)
3. `  [edge-trigger] ` (default value is positive)

__Version history:__ Added to BasicCircuits 0.6