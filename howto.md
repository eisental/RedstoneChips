---
layout: main
title: RedstoneChips Usage Guide
---

Building a Chip
---------------
- Start by build a structure made of __chip blocks__ (sandstone blocks by default). 
- Place a __wall sign__ attached to one of the chip blocks. This is the starting point of the circuit and affects the orders of input and output pins. Set the sign text according to the circuit you're trying to build. See the [circuitdocs](/RedstoneChips/circuitdocs) for more info.
- Add __output blocks__ (gold block by default) and __input blocks__ (iron block by default). Make sure that each block has a chip block on one of its sides (not just above or below). Changes in redstone current will be sensed on the block attached to the input block on the opposite side from where the chip block is attached to the input block. 
- The circuit outputs its bits by setting levers on and off. Attach __levers__ to each output block on the opposite side from where the chip block is attached to the output blocks.
- Some circuits, such as the [print](/RedstoneChips/circuitdocs/Print.html) circuit require you to add __interface blocks__ (lapis blocks by default). Again, make sure that each interface block has a chip block on one of its horizontal sides. The circuit will then use the block on the opposite side as a point of interaction with the "physical" world. In the case of the print circuit, it will update the text of sign attached to the interface block (on the appropriate side).
- Last but not least, go back to your circuit sign and __right-click__ it, to activate the circuit. If all went well, you should receive a message saying 'Circuit X activated with y input(s) and z output(s)' 

The order of your input and output blocks is very important as different pin numbers have different functions. In simple circuit structures the pin numbering starts at the chip sign onwards. See "Chip detection scanning rules" for more information on predicting the order of pins in more complex structures.

If you're having problems or just want to see more information, you can use the debugging commands `/redchips-debug` and `/redchips-pin`. See the description below.

![adder circuit](/RedstoneChips/images/adder.jpg)

Simple [adder](/RedstoneChips/circuitdocs/Adder.html) circuit. Input and output 0 lies next to the circuit sign.

![pixel circuit](/RedstoneChips/images/pixel2.png)

A pixel circuit with a more complex structure. Input and output order is a bit harder to find out. 


Chip detection scanning rules
------------------------------



Plugin commands
----------------

 - `/redchips-active` Lists every currently active circuit on the server.
 - `/redchips-classes` Prints a list of installed circuit classes.
 - `/redchips-pin` Point your cross at one of the pins and enter the command to get the current state of the pin and its index. You have to point to the block where redstone current actually flows. That is, you need to point at levers, buttons or redstone wire and not at the output / input blocks.
 - `/redchips-debug` Register a player as a debugger for a specific circuit. Point your cross at one of the circuit blocks and enter the command. The plugin will now send you debug messages from this circuit. Enter /redchips-debug off while pointing, to stop debugging.
 - `/redchips-prefs` Prints preferences.
 - `/redchips-prefs key` Prints one preference key: value pair.
 - `/redchips-prefs key value` Changes the value of a preference key and saves the file. Only admins are allowed to use this command.
     - Example: typing <code>/redchips-prefs chipBlockType glass</code> will change the preferences value and make the plugin immediately expect new chips to have their body made of glass.
