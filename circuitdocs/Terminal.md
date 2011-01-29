---
layout: main
title: terminal
---

An input terminal for sending ascii characters or numbers over redstone wires. 
To use it, point towards one of its interface blocks and enter the chat command /rc-type <data>
Once the command is executed, the terminal will send the command data through its output according to the `type` sign argument.

The first sign argument sets the way the data is encoded.

* `ascii` (default) - Sends one character's ascii value over the data outputs, triggers the clock output and then moves to the next character.
* `num` - Reads the whole line as an integer number, sends the integer over the data outputs and then triggers the clock output.

This circuit can be directly connected to a [print](Print.html) circuit. 

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/terminal.java)

* * *


#### I/O setup 
* ascii mode requires 1 clock output and 8 data output pins.
* num mode requires 1 clock output and 1 or more data output pins.
* Requires at least 1 interface block.

#### Sign text
1. `   terminal   `
2. `  [type] ` (ascii by default)

__Version history:__ Added to BasicCircuits 0.73