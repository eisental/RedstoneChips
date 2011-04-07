---
layout: main
title: iptransmitter
---

Transmit your messages to anywhere in the known universe. Transmits its input bits over a UDP connection to any internet address in
the authorized port range.
You can use it together with ipreceiver to send and receive bits between minecraft servers.

Set your message in the input data pins, then set the send pin (input 0) on to send the message.
An iptransmitter with only 1 input is also possible. In that case whenever the input changes its state the new value is sent.

To change the range of authorized port numbers, use `/rcprefs iptransmitter.ports <range>`.

on [Wikipedia](http://en.wikipedia.org/wiki/Interplanetary_Internet) ;-)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/iptransmitter.java)

* * *


#### I/O setup 
* input 0 is the send pin.
* inputs 1 and above are the data pins.
* When only one input is used, the send pin can be ommited.
* Can have any number of inputs and 0 outputs.

#### Sign text
1. `   iptransmitter   `
2. ` <address> <port> ` (NOT optional)

__Version history:__ Added to BasicCircuits 0.6