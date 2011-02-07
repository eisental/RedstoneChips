---
layout: main
title: ipreceiver
---

Receive messages from anywhere in the known universe. Listens for UDP packets on the selected port number.
When a pulse is sent to the clock input pin, the receiver will fill its outputs with any available data in the socket buffer. 
If the data size is greater than the number of outputs, additional triggers to the clock pin will fill the outputs with new data.
Once the circuit finished updating its outputs a quick (and invisible) pulse is sent out the 1st output pin.

on [Wikipedia](http://en.wikipedia.org/wiki/Interplanetary_Internet) ;-)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/ipreceiver.java)

* * *


#### I/O setup 
* Can have any number of outputs. 1 output clock pin and any number of data pins.
* Must have 1 input clock pin.

#### Sign text
1. `   receiver   `
2. `  <port> ` (NOT optional)
3. ` <incoming address>...<incoming address> `

__Version history:__ Added to BasicCircuits 0.6