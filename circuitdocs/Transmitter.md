---
layout: main
title: transmitter
---

Broadcasts its data inputs wirelessly over a specific broadcast channel. The data is can be received by any [receiver](Receiver) chip tuned to the same channel.

Set your message in the input data pins, then set the send pin (input 0) on to send the message.
A transmitter with one input pin doesn't have a send pin and it will send a message whenever that pin's state is changed.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/transmitter.java)

#### I/O setup 
* input 0 is the send pin unless only the transmitter uses only 1 input pin.
* inputs 1 and above are the data pins.
* Can have any number of inputs and no outputs.

#### Sign text
1. `   transmitter   `
2. ` <broadcast channel> ` (NOT optional)
***
back to [Circuit Classes](Home)
