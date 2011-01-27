---
layout: main
title: pixel
---

Changes dye color of any wool block placed in a 3x3x3 cube around the output block (one block after the last chip block).

When the clock pin (input 0) is set to HIGH, the circuit will update wool color according to the value of the input pins.  

There are two ways to use this circuit. 

* Use without arguments (just write `pixel` on the sign) - Wool color is set according to the color list below. This mode requires 4 data pins and 1 clock pin. For example, send 0110 (decimal 6) to the data pins, and then send current to the clock input to update the wool color to `Pink`.
* Use a color index by adding a list of colors to the sign (Either color IDs or names). Then set the color by sending its index to the data pins. For example, if the sign text from the 2nd line is set to `black white`, an input of 0 will change the wool color to black and an input of 1 will change it to white. Any number of colors can be used. The circuit requires enough data pins to support the largest index: 2 colors require 1 data pin,  3-4 require 2 pins, 5-8 require 3 pins, and so on.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/pixel.java)

#### Colors and IDs (Case insensitive)
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
* Inputs: 1 clock pin (input 0) and 1-4 data pins.
* No outputs.

#### Sign text
1. `   pixel   `
2. `  [color] ... [color] ` 

##### Version history: Added to BasicCircuits 0.6