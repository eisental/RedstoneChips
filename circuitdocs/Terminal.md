---
layout: main
title: terminal
---

An input terminal for sending ascii characters or numbers over redstone wires. 
To use it, point towards one of its interface blocks and enter the chat command `/rctype <data>`
Once the command is executed, the terminal will send the command data through its outputs.

The first sign argument sets the way the data is encoded.

* `ascii` (default) - Sends one character's ascii value over the data outputs, triggers the clock output and then moves to the next character. When the chip has 10 outputs its 2nd pin is an end-of-transmission pin, triggered once the terminal is done sending all characters in its message. When an `eot` argument is added to the sign the terminal will send a 0x03 (or EOT ascii control character) out of its data outputs once all characters have been sent.
* `num` - Reads the whole line as an integer number, sends the integer over the data outputs and then triggers the clock output.

The chip can be built with an optional clear input pin. When the clear pin is triggered any data left on the chip's outputs is set to off.

This circuit can be directly connected to a [print](Print.html) circuit. 

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/terminal.java)

* * *

![terminal](/RedstoneChips/images/terminal.png "terminal")

#### I/O setup 
* ascii mode requires 1 clock output and 8 data output pins. An additional end-of-transmission 2nd output pin is used when the chip has 10 outputs.
* num mode requires 1 clock output and 1 or more data output pins.
* Requires at least 1 interface block.
* Can have 1 optional clear input pin.

#### Sign text
1. `   terminal   `
2. `  [type] ` (ascii by default)
3. `  [eot]  ` (optional)
__Version history:__ Added to BasicCircuits 0.73