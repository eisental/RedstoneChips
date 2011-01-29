---
layout: main
title: router
---

Routes any input signal into one or more outputs according to the routing table.
The routing table should be written on the sign using the following syntax: ` <input pin>:<output pin> ` any number of table entries 
can be used. 

Here's an example routing table for a 2 inputs, 3 outputs router circuit.

` 0:1 0:2 1:0 1:2 `

When input pin 0 changes state it will route the new state to outputs 1 and 2.
When input pin 1 changes state it will route to outputs 0 and 2.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/router.java)

* * *


#### I/O setup 
* Any number of inputs and outputs.

#### Sign text
1. `   router   `
2. ` [table entry]…[table entry] ` 


__Version history:__ Added to BasicCircuits 0.73