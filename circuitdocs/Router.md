---
layout: main
title: router
---

Routes any input signal into one or more outputs according to the routing table.
The routing table should be written on the sign using the following format: ` <input pin>:<output pin> `. Any number of table entries 
can be used. The 1st input of the router is an input clock, when its state is set to high the router will update its outputs.
Routing table entries should use input 0 as the first input after the clock pin.

Here's an example routing table for a 2 inputs (plus 1 clock input), 3 outputs router circuit.

` 0:1 0:2 1:0 1:2 `

When data pin 0 (input pin 1) changes state it will route the new state to outputs 1 and 2.
When data pin 1 (input pin 2) changes state it will route to outputs 0 and 2.

To route an input to all of the routers outputs use 'all' as the output entry:

` 0:all ` for example, will act the same way as the repeater chip, routing the signal from the 1st input to all outputs.

If two or more inputs are routed to the same output they are ORed together.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/router.java)

* * *


#### I/O setup 
* At least 2 inputs, one clock input and any number of data inputs.
* Any number of outputs.

#### Sign text
1. `   router   `
2. ` [table entry] ... [table entry] `


__Version history:__ Added to BasicCircuits 0.73