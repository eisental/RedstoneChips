RedstoneChips 0.3
==================

A Bukkit plugin for building custom integrated redstone circuits with any number of inputs and outputs.
This is the core RedstoneChips plugin consisting of only the essentials. 
To actually build ICs you need to download and install the [BasicCircuits](http://github.com/eisental/BasicCircuits) plugin. 

![counter example circuit](/eisental/RedstoneChips/raw/master/images/decoder.png "decoder circuit as a sequencer")


Install:
--------
   * Copy jar file to the plugins folder of your craftbukkit installation.
   
After running craftbukkit with the plugin for the first time, a redstonechips.properties file is created in the server root folder with default plugin settings.

You can change the block types used to construct a circuit by editing this file. Default values are gold block for outputs, iron block for inputs, and sandstone for the circuit body.
Type is NOT case-sensitive and can be any bukkit block material name. any already activated circuit will stay activated even after it's block types are invalid.

How to build an IC:
-------------------
   Every IC is made of a line of blocks with input and output blocks to the left or to the right of the line. 
Place a lever on the block next to each output block. Next place a wall sign attached to the first main block of the circuit with the circuit class name on the first line of text, and any additional arguments on the next lines.

   Once the IC is built, right-click the sign to activate it.

* Any block that can send a redstone current (redstone wire, redstone torch, button or lever) can be placed on the block next to an input.
* Input and output numbering starts from the sign onwards. i.e. input/output 0 will be the ones closest to the sign.
* The main line can be of any length and input and output blocks can be placed anywhere along this line, on both sides. 

Here's an example 4-bit counter chip with 2 inputs and 4 outputs (looking from above, each letter represents one block):  
* b - main block (sandstone by default) * i - input block (iron block by default) * o - output block (gold block by default) * l - lever on any block face * s - wall sign attached to the main block

	    s	    
	l o b i
	    b
	l o b i
	    b
	l o b
	    b
	l o b


sign text (on first line):
	counter


This will create a clock circuit with 2 outputs and 2 inputs. When the block left to one of the input blocks receives a high redstone current the corresponding output will start to blink, turning on every 2 seconds.

The 4-bit counter:
![counter example circuit](/eisental/RedstoneChips/raw/master/images/counter.png)

For more info check the [BasicCircuits](https://github.com/eisental/BasicCircuits/wiki/BasicCircuits-) Wiki.

Destroying an IC:
-----------------
You can destroy an IC by breaking one of its structure blocks, including the output levers.

Commands:  
---------
   * /redchips-list	Message the player with a list of every currently activated circuit in the server.






