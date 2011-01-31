---
layout: main
title: RedstoneChips Usage Guide
---

Building a Chip
---------------
- Start by build a structure made of __chip blocks__ (sandstone blocks by default). 
- Place a __wall sign__ attached to one of the chip blocks. This is the starting point of the circuit and affects the order of input and output pins. Set the sign text according to the circuit you're trying to build. See the [circuitdocs](/RedstoneChips/circuitdocs) for more info.
- Add __output blocks__ (gold block by default) and __input blocks__ (iron block by default). Make sure that each block has a chip block on one of its sides (not just above or below). Changes in redstone current will be sensed on the block attached to the input block on the opposite side from where the chip block is attached to the input block (see adder image below). 
- The circuit outputs its bits by setting levers on and off. Attach __levers__ to each output block on the opposite side from where the chip block is attached to the output blocks.
- Some circuits, such as the [print](/RedstoneChips/circuitdocs/Print.html) circuit require you to add __interface blocks__ (lapis blocks by default). Again, make sure that each interface block has a chip block on one of its horizontal sides. The circuit will then use the block on the opposite side as a point of interaction with the "physical" world. In the case of the print circuit, it will update the text of sign attached to the interface block (on the appropriate side).
- Last but not least, go back to your circuit sign and __right-click__ it, to activate the circuit. If all went well, you should receive a message saying 'Circuit X activated with y input(s) and z output(s)' 

The order of your input and output blocks is very important as different pin numbers have different functions. In simple circuit structures the pin numbering starts at the chip sign onwards. See "Chip detection scanning rules" for more information on predicting the order of pins in more complex structures.

If you're having problems or just want to see more information, you can use the debugging commands `/redchips-debug` and `/redchips-pin`. See the description below.


Simple [adder](/RedstoneChips/circuitdocs/Adder.html) circuit. Input and output 0 lies next to the circuit sign.

![adder circuit](/RedstoneChips/images/adder.jpg)



A [pixel](/RedstoneChips/circuitdocs/Pixel.html) circuit with a more complex structure. Input and output order is a bit harder to find out.  
Interface blocks are connected on the "roof".

![pixel circuit](/RedstoneChips/images/pixel2.png)

Chip detection scanning rules
------------------------------
To be able to understand the pin numbering of more complex structures you need to understand how the plugin detects and scans the structure once you right-click the circuit sign. It scans the circuit block by block starting at the sign. The pins are numbered as the circuit structure is scanned, therefore when the structure is a straight line the count starts at the sign. When more than one dimension is used the plugin will scan according to the following rules:
1. The sign block is added to the structure and then the plugin moves to the chip block the sign is attached to. 
2. It will try to find input, output or interface blocks at any of the other sides of the chip block, but not above or below. The important part is the order in which different sides are scanned. First, it will look to the right (relative to the current scan direction), next it will look to the left. After that it will look at the next block in the original direction and finally it will look back, opposite to the scan direction. If going backwards seem like a waste of time see the next point.
3. Now the plugin will go to the next chip block. The scan order is 
	1. The block in the same direction as before.
	2. The block in the opposite direction.
	3. One block up. If the block above is a chip block the scan will continue in the same direction as before only one level above.
	4. One block below. Will also continue in the same direction.
	5. The block to the right, relative to the current scan direction.
	6. The block to the left, relative to the current scan direction.
	
One very important thing to note is that the recursive scanning process work by branches. It will continue going from block to block even when chip blocks are found in more than one direction. Only when it reaches a dead end it will go back to try the other branches.

The exact algorithm can be found at the [CircuitManager](http://github.com/eisental/RedstoneChips/blob/master/src/main/java/org/tal/redstonechips/CircuitManager.java) class.

Destroying a Chip
-----------------
You can destroy a chip by breaking any of its structure blocks, including the output levers. Creepers, TNT explosions and fire (in case you're using flammable blocks) will also cause the IC to stop functioning.

Plugin commands
----------------

 - `/redchips-active` Lists every currently active circuit on the server.
 - `/redchips-classes` Prints a list of installed circuit classes.
 - `/redchips-channels` Lists currently used transmitter/receiver broadcast channels. 
 - `/redchips-pin` Point your cross at one of the pins and enter the command to get the current state of the pin and its index. You have to point to the block where redstone current actually flows. That is, you need to point at levers, buttons or redstone wire and not at the output / input blocks.
 - `/redchips-debug` Register a player as a debugger for a specific circuit. Point your cross at one of the circuit blocks and enter the command. The plugin will now send you debug messages from this circuit. Enter /redchips-debug off while pointing, to stop debugging.
 - `/redchips-destroy` Destroys a circuit and turns all of its blocks into air. Point at a block of the circuit you wish to destroy and enter the command. This command is disabled by default. To enable it use `/redchips-prefs enableDestroyCommand true`
 - `/redchips-deactivate` Deactivates a circuit w/o removing its blocks. Point at a block of the circuit or for admins, enter the circuit's id number as an argument.
 - `/redchips-prefs` Prints preferences. See below for a list of preference keys.
 - `/redchips-prefs key` Prints one preference key: value pair.
 - `/redchips-prefs key value` Changes the value of a preference key and saves the file. Only admins are allowed to use this command.
     - Example: typing <code>/redchips-prefs chipBlockType glass</code> will change the preferences value and make the plugin immediately expect new chips to have their body made of glass.

Preference keys
---------------
##### block types
These can be any material name or id.
`chipBlockType` - Sets the chip's structure block material (`SANDSTONE` by default).
`inputBlockType` - Sets the input indicator block material (`IRON_BLOCK` by default).
`outputBlockType` - Sets the output indicator block material (`GOLD_BLOCK` by default).
`interfaceBlockType` - Sets the interface indicator block material (`LAPIS_BLOCK` by default).

##### message colors
These can be any chat color name.
`infoColor` - Color of info messages (`GREEN` by default)
`errorColor` - Color of error messages (`RED` by default)
`debugColor` - Color of debug messages (`AQUA` by default)

##### permissions
`enableDestroyCommand` - Enable or disable /redchips-destroy command. Possible values or `true` or `false` (`false` by default).
