---
layout: main
title: receiver
---

Outputs any data sent wirelessly by a [transmitter](Transmitter) chip or other transmitting circuits over the chosen broadcast channel. 
When the receiver has more than one output its 1st output is used as an output clock pin. Whenever the receiver receives any data the clock pin pulses shortly. The pulse is too short to actually see it.

The channel argument can specify a start bit using the format `<channel name>:<start bit>`. When used, the receiver will receive data in the channel from this bit on. Bits that are either too high to fit in the receivers outputs or are lower than the start bit are ignored.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/receiver.java)

* * *

#### I/O setup 
* Can have any number of outputs and no inputs. 
* When more than 1 output is used, the 1st output is a clock output pin.

#### Sign text
1. `   receiver   `
2. `  <broadcast channel>[:<start bit>] ` (The channel name is not optional)

![transmitter/receiver pair](/RedstoneChips/images/transmitterreceiver.png "transmitter/receiver pair")

__Version history:__ Added to BasicCircuits 0.1
