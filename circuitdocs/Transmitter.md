---
layout: main
title: transmitter
---

Broadcasts its data inputs wirelessly over a specific broadcast channel. The data can be received by any [receiver](Receiver) chip tuned to the same channel.

Set your message in the input data pins, then set the send pin (input 0) on to send the message.
A transmitter with one input pin doesn't have a send pin and will send a message whenever the pin changes state.
Once the send pin is on, any change to the data pins will cause the transmitter to broadcast.

The channel name can specify a start bit by using the syntax `<channel name>:<start bit>`. When used, the transmitter will set the broadcast channel data from the start bit on leaving the current value of channel bits that are out of range.
To set the transmitter's start bit dynamically, it's possible to use the chip in select mode. In this mode some of the chip's inputs set the start bit.
The argument should be written using the format of `select(<no. of select bits>)`. For example, `select(3)` will set inputs 1,2 and 3 as select inputs (input 0 is still the send pin).

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/transmitter.java)

* * *


#### I/O setup 
* input 0 is the send pin unless the transmitter uses only 1 input pin.
* in select mode, inputs 1 and above (according to the select length) are select pins.
* all remaining output pins are data pins.
* Can have any number of inputs and no outputs.

#### Sign text
1. `   transmitter   `
2. ` <broadcast channel>[:<start bit>] ` (NOT optional) (The channel name is not optional)
3. ` select(<select length>)` (optional)

![transmitter/receiver pair on channel A](/RedstoneChips/images/transmitterreceiver.png "transmitter/receiver pair on channel A")

__Version history:__ Added to BasicCircuits 0.1