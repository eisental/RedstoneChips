---
layout: main
title: display
---
Creates a wool display with individually addressable pixels.

This chip may be wireless, or use Input Blocks. To make a wireless version, do not attach any Input Blocks and specify a channel Identifier as the lat parameter on the sign.

This chip requires two Interface Blocks placed at opposite corners of the display, and a layer of wool making up the display. The display must be flat and can not be built at an angle. If the physical display is larger than the size specified on the sign, the size of the pixels will be adjusted accordingly. For example, if the sign states that the display is 10x10, and the physical display is 40x40, each pixel will be 4x4 wool blocks.

The display chip requires at least one argument, specifying the size of the display in the format `<Width>x<Height>`, for example "10x20".

Additional optional arguments are a color index as detailed in the [pixel](Pixel) chip, and a channel identifier if the display will be wireless.

The number of inputs required will vary based on the size of the display and the number of colors specified, if any. The first input is the clock, and when turned on will cause the selected pixel to update. The next set of inputs is the X coordinate, and there must be enough input pins to express the X coordinate, for example 2 pins for a width of 4, 3 pins for a width of 8, 8 pins for a width of 256, etc. The next set of inputs is for the Y coordinate, and follow the same rules as the X cordinate pins. The final set of inputs are for the color. If a color index is used there must be enough inputs to represent the index, otherwise 4 pins are required.

The channel name can specify a start bit using the syntax `<channel name>:<start bit>`.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/display.java)

* * *

![a 2x2 display](/RedstoneChips/images/display1.png "a 2x2 display")

#### Color names and ids (Color names are case insensitive)
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
* input 0 is clock pin.
* X inputs for the X coordinate.
* Y inputs for the Y coordinate.
* C inputs for color.
* Requires two Interface Blocks at opposite corners of the display area.

#### Sign text
1. `   display   `
2. `  <width>x<height>  `
2. `   colors   ` (optional)
3. ` <broadcast channel>[:<start bit>] ` (optional)

![a 40x24 wireless display running random colors.](/RedstoneChips/images/display7.png "a 40x24 wireless display running random colors.")

__Version history:__ Added to BasicCircuits 0.92
