---
layout: main
title: pixel
---

Changes dye color of any wool block placed in a 3x3x3 cube around the interface block. 

When the clock pin (input 0) is set to HIGH, the circuit will update wool color according to the value of the input pins.  

There are two ways to use this circuit. 

* Use without color arguments (just write `pixel` on the sign) - Wool color is set according to the color list below. This mode requires 4 data pins and 1 clock pin. For example, to change the wool color to Pink, send 0110 (decimal 6) to the data pins, and then send a pulse to the clock input.
* Use a color index by adding a list of colors to the sign (Either color IDs or names). Then set the color by sending its index to the data pins. For example, if the sign text from the 2nd line is set to `black white`, an input of 0 will change the wool color to black and an input of 1 will change it to white. Any number of colors can be used. The circuit requires enough data pins to support the largest index: 2 colors require 1 data pin,  3-4 require 2 pins, 5-8 require 3 pins, and so on. 

When the last sign argument is a broadcast channel name (i.e. not a color name). The pixel is set to receive new data from the selected channel.
In this operation mode it's possible to use a transmitter to set the pixel's color wirelessly. The transmitted bits should not include a clock value, starting at bit 0 with the pixel color or index. You can build the chip without any inputs in this mode. 
The channel name can specify a start bit using the syntax `<channel name>:<start bit>`. When used, the pixel will receive data from this bit in the channel.

The default wool painting range is 3 blocks away from the interface blocks. To use a different range add a d{<distance>} or dist{<distance>} argument to the chip sign. For example adding a dist{1} argument will make the pixel only paint blocks that are directly attached to its interface block.

It's possible to build the chip without a clock pin when only 1 data pin is used.  

Note that wool blocks that are not attached directly or indirectly (through other wool blocks) to an interface block will not be colored even if they are within range.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/pixel.java)

* * *


#### Colors and IDs (Color names are case insensitive)
- 0: White
- 1: Orange
- 2: Magenta
- 3: Light_blue
- 4: Yellow
- 5: Lime
- 6: Pink
- 7: Gray
- 8: Silver
- 9: Cyan
- 10: Purple
- 11: Blue
- 12: Brown
- 13: Green
- 14: Red
- 15: Black


#### I/O setup 
* Inputs: 1 clock pin (input 0) and 1-4 data pins. Clock pin can be omitted when only 1 data pin is used. 
* No outputs.
* When using a broadcast channel for receiving data the chip can have no inputs at all.

#### Sign text
1. `   pixel   `
2. `  [color] ... [color] ` 
3. ` [dist{<distance>}] ` (optional)
4. ` [broadcast-channel] (optional. must be the last argument)

![pixel circuit w/o sign arguments](/RedstoneChips/images/pixel3.png "pixel circuit w/o sign arguments")

__Version history:__ Added to BasicCircuits 0.6