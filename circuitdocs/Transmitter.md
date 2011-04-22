---
layout: main
title: transmitter
---

Broadcasts its data inputs wirelessly over a specific broadcast channel. The data can be received by any [receiver](Receiver) chip tuned to the same channel.

Set your message in the input data pins, then set the send pin (input 0) on to send the message.
A transmitter with one input pin doesn't have a send pin and will send a message whenever the pin changes state.

The channel name can specify a start bit using the syntax `<channel name>:<start bit>`. When used, the transmitter will set the broadcast channel data from the start bit on leaving the current value of channel bits that are out of range.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/transmitter.java)

* * *


#### I/O setup 
* input 0 is the send pin unless only the transmitter uses only 1 input pin.
* inputs 1 and above are the data pins.
* Can have any number of inputs and no outputs.

#### Sign text
1. `   transmitter   `
2. ` <broadcast channel>[:<start bit>] ` (NOT optional) (The channel name is not optional)

![transmitter/receiver pair on channel A](/RedstoneChips/images/transmitterreceiver.png "transmitter/receiver pair on channel A")

__Version history:__ Added to BasicCircuits 0.1