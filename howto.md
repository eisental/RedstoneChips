---
layout: main
title: RedstoneChips Usage Guide
---

Building a Chip
---------------
- Start by building the chip's __structure__ out of any block you want. You can't use sand or gravel and the block type must be different than the input/output/interface blocks type (see below).
- Place a __wall sign__ attached to one of the chip blocks. This is the starting point of the circuit and affects the order of input and output pins. Set the sign text according to the circuit you're trying to build. See the [circuitdocs](/RedstoneChips/circuitdocs) for more info.
- Add __output blocks__ (gold block by default) and __input blocks__ (iron block by default). Each block has to be attached to a chip block. Changes in redstone current will be sensed on any side or on top of the input blocks. 
- The circuit outputs its bits by setting levers on and off. Attach __levers__ to any side or on top of each output block.
- Some circuits, such as the [synth](/RedstoneChips/circuitdocs/Synth.html) circuit require you to add __interface blocks__ (lapis blocks by default). Again, make sure that each interface block is attached to a chip block. The circuit will then use blocks on any side, on top, or below as a point of interaction with the "physical" world or the player. In the case of the synth circuit, it will play a note on any noteblock attached to its interface blocks.
- Last but not least, go back to your circuit sign and __right-click__ it to activate the circuit. If all went well, you should receive a message saying 'Circuit X activated: > a input(s), b output(s), and c interface block(s)'. 

The order of your input and output blocks is very important as different pin numbers have different functions. In simple circuit structures the pin numbering starts at the chip sign onwards. See "Chip detection scanning rules" below, for information on predicting the order of pins in more complex structures.

If you're having problems or just want to see more information, you can use the debugging commands `/rc-debug`, `/rc-info` and `/rc-pin`. See the description below.


Simple [adder](/RedstoneChips/circuitdocs/Adder.html) circuit. Input and output 0 lies next to the circuit sign.

![adder circuit](/RedstoneChips/images/adder.jpg)


A [pixel](/RedstoneChips/circuitdocs/Pixel.html) circuit with a more complex structure. Input and output order is a bit harder to find out.  
Interface blocks are the lapis blocks placed on the "roof".

![pixel circuit](/RedstoneChips/images/pixel2.png)

Destroying a Chip
-----------------
You can destroy a chip by breaking any of its structure blocks, including the output levers or by running the /redchips-deactivate command
while pointing at a circuit block. Use `/redchips-destroy` to destroy the chip and remove all of its blocks. 
__Creepers__, __TNT__ explosions and __fire__ (in case you're using flammable blocks) will also cause the IC to stop functioning.
__Power tools__, however, will not cause it to decativate and can result in "phantom" circuits still taking place in memory without an actual chip to make them of any use.

Plugin commands
----------------
- `/rc-list` - Prints a list of active chips including their id numbers and locations.
- `/rc-classes` - Prints a list of installed circuit classes.
- `/rc-prefs` - Allows to see and change the plugin's preferences. Use the command with no arguments to list all preferences values.
      To change a specific value use `/rc-prefs <pref key> <new value>`.
- `/rc-debug` - Register yourself to receive debug messages from a chip. Use by either pointing towards the circuit you wish to debug or by using `/rc-debug <chip id>` if you have admin priviliges. To stop receiving debug messages from the chip use `/rc-debug off` or `/rc-debug <chip id> off`. To stop receiving debug messages from any circuit use `/rc-debug alloff`
- `/rc-pin` - Prints information about a chip pin - it's pin number, type and current state. Point towards an output lever or input redstone
      source to use.
- `/rc-destroy` - Destroys a circuit and turns all of its blocks into air. Point at a block of the circuit you wish to destroy and enter the command. This command is disabled by default. To enable it use `/rc-prefs enableDestroyCommand true`.
- `/rc-break` - Deactivates a circuit without removing its blocks. Point at a block of the circuit or enter the chip's id number as an argument if you have admin priviliges.
- `/rc-type` - Used for sending text or numbers to supporting circuits. Check the relevant circuit documentation for more information.
- `/rc-reset` - Reactivates a circuit, applying any changes made to the sign with sign edit commands. Use by pointing towards the circuit or by using the chip's id number as an argument if you have admin priviliges.
- `/rc-channels` - Prints a list of currently used wireless broadcast channels.
- `/rc-info` - Prints a lot of useful information about a chip. Point at a block of the chip you wish to get info about or use the chip's id number as an argument.
- `/rc-help` - Prints a list or description of all RedstoneChips commands. Use `/rc-help <command name>` to get help about a specific command.
- `/rc-save` - Saves all circuit data to file. Should not be used unless there's a problem with the automatic data save. Can only be used by ops.
- `/rc-load` - Reloads circuit data from file. Will reset any changes since last save. Can only be used by ops.


Chip detection scanning rules (for the advanced RC user...)
------------------------------
To be able to understand the pin numbering of more complex structures you need to understand how the plugin detects and scans the structure once you right-click the circuit sign. It scans the circuit block by block starting at the sign. The pins are numbered as the circuit structure is scanned, therefore when the structure is a straight line the counting starts at the sign and onwards. When more than one dimension is used the plugin will scan according to the following rules:
1. The sign block is added to the structure and then the plugin moves to the chip block the sign is attached to. 
2. It will try to find input, output or interface blocks at any of the other sides of the chip block and above or below. The important part is the order in which different sides are scanned. First, it will look to the right (relative to the current scan direction), next it will look to the left. After that it will look at the next block in the original direction and finally it will look back, opposite to the scan direction. Finally it checks the block above and below. If going backwards seem like a waste of time see the next point. 
3. Now the plugin will go to the next chip block. The scan order is 
	1. The block to the right, relative to the current scan direction.
	2. The block to the left, relative to the current scan direction.
	3. The block in the same direction as before.
	4. The block in the opposite direction.
	5. One block up. If the block above is a chip block the scan will continue in the same direction as before only one level above.
	6. One block below. Will also continue in the same direction.
	
One very important thing to note is that the recursive scanning process work by branches. It will continue going from block to block in one direction even when chip blocks are found in other directions. Only when it reaches a dead end it will go back to try the other branches.

The exact algorithm can be found at the [CircuitManager](http://github.com/eisental/RedstoneChips/blob/master/src/main/java/org/tal/redstonechips/CircuitManager.java) class. Specifically `CircuitManager.checkForCircuit()`

Preference keys
---------------

##### Block types - these can be any material name or id.
- `inputBlockType` - Sets the input indicator block material (`IRON_BLOCK` by default).
- `outputBlockType` - Sets the output indicator block material (`GOLD_BLOCK` by default).
- `interfaceBlockType` - Sets the interface indicator block material (`LAPIS_BLOCK` by default).

##### Message colors - these can be any chat color name.
- `infoColor` - Color of info messages (`GREEN` by default)
- `errorColor` - Color of error messages (`RED` by default)
- `debugColor` - Color of debug messages (`AQUA` by default)

##### Other 
- `enableDestroyCommand` - Enable or disable /redchips-destroy command. Possible values are `true` or `false` (`false` by default).
