---
layout: main
title: transmitter
---

Broadcasts its data inputs wirelessly over a specific broadcast channel. The data can be received by any [receiver](Receiver) chip tuned to the same channel.

Set your message in the input data pins, then set the send pin (input 0) on to send the message. If the send pin is left in an on state, the transitter will send data whenever any of the input data pins change.
A transmitter with one input pin doesn't have a send pin and will send a message whenever the pin changes state.

The channel name can specify a start bit using the syntax `<channel name>:<start bit>`. When used, the transmitter will set the broadcast channel data from the start bit on leaving the current value of channel bits that are out of range.

Using the optional `select(<# ranges>)` argument allows a single transmitter to send to multiple ranges of bits on the channel. Using this option requires enough additional input pins to represent the number of ranges, 1 for 2 ranges, 2 for 4 ranges, 4 for 16 ranges, etc. These additional pins will be right after the send pin. Data transmitted will be directed to a starting bit equal to the range selected * the number of data bits. For example, with an 8 bit transmitter and `select(3)`, 0 on the select pins will transmit to bits 0-7, 1 will transmit to bits 8-15, 2 will transmit to bits 16-23. This setting is in addition to any start bit specified in the channel identifier. Using the above example again, if the channel identifier specified bit 10 as the start bit, 0 on the select pins will transmit to bits 10-17, 1 will transmit to bits 18-25, 2 will transmit to bits 26-33.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/transmitter.java)

* * *


#### I/O setup 
* input 0 is the send pin unless only the transmitter uses only 1 input pin.
* inputs 1 and above are the data pins unless the `select` option is used. If the `select` option is used, pins 1 to the length needed by `select` will be used to select the bit range, and the remaining pins will be the data input pins.
* Can have any number of inputs and no outputs.

#### Sign text
1. `   transmitter   `
2. ` select(<# ranges>) ` Optional
3. ` <broadcast channel>[:<start bit>] ` (NOT optional) (The channel name is not optional)

![transmitter/receiver pair on channel A](/RedstoneChips/images/transmitterreceiver.png "transmitter/receiver pair on channel A")

__Version history:__ Added to BasicCircuits 0.1