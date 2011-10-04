---
layout: main
title: display
---

what it does?


how to build?

size arg - doesn't have to match actual size. 
indexed/non indexed color
wireless mode


[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/display.java)

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
* Inputs: 1 clock pin (input 0), a number of x-coordinate inputs, a number of y-coordinate inputs, and up to 4 color input pins.
* No outputs.
* 2 interface blocks. one in each opposite corners of the screen.
* When using a broadcast channel for receiving data the chip can have no inputs at all.

#### Sign text
1. `   display   `
2. `  <width>x<height>   `
2. `  [color] ... [color] ` (optional)
4. ` [broadcast-channel] (optional. must be the last argument)


__Version history:__ Added to BasicCircuits 0.92