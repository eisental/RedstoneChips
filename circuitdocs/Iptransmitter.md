---
layout: main
title: iptransmitter
---

Transmit your messages to anywhere in the known universe. Works exactly the same as the [transmitter](Transmitter) circuit except that the input bits are sent over UDP to any internet address.
You can use it together with ipreceiver to send and receive bits between minecraft servers.

Set your message in the input data pins, then set the send pin (input 0) on to send the message.
An iptransmitter with only 1 input is also possible. In that case whenever the input changes its state the new value is sent.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/iptransmitter.java)

on [Wikipedia](http://en.wikipedia.org/wiki/Interplanetary_Internet) ;-)

#### I/O setup 
* input 0 is the send pin.
* inputs 1 and above are the data pins.
* Can have any number of inputs and no outputs.

#### Sign text
1. `   iptransmitter   `
2. ` <address> <port> ` (NOT optional)

__New in BasicCircuits 0.6__

***
back to [Circuit Classes](Home)
