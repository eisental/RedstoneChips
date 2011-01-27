---
layout: main
title: ipreceiver
---

Receive messages from anywhere in the known universe. Works exactly the same as the [receiver](Receiver) circuit only it's set to listen for incoming messages on a UDP port from any internet address.
Any data that is sent to the ipreceiver's port number (using the server address) would cause the circuit to change its outputs accordingly.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/ipreceiver.java)

on [Wikipedia](http://en.wikipedia.org/wiki/Interplanetary_Internet) ;-)

* * *


#### I/O setup 
* Can have any number of outputs and 0 inputs

#### Sign text
1. `   receiver   `
2. `  <port> ` (NOT optional)

__Version history:__ Added to BasicCircuits 0.6