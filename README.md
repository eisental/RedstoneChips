RedstoneChips 0.2
==================

This plugin makes it possible to build integrated redstone circuits with any number of inputs
and outputs. 
Currently there are 17 kinds of digital integrated circuits that can handle binary numbers of any length (though currently mostly limited to java 32bit integers). 
These include multi-bit logic gates (or, and and xor), binary arithmetic (adder, multiplier, divider), registers (flipflop, pisoregister, shiftregister) for serial communication, wireless communication (receiver, transmitter) and some more.

Install:
--------
   * Copy jar file to the plugins folder of your craftbukkit installation.
   
After running craftbukkit with the plugin for the first time, a redstonechips.properties file is created in the server root folder containing the default plugin settings.

You can change the block types used to construct a circuit by editing this file. Default values are gold block for outputs, iron block for inputs, and sandstone for the circuit body.

Type is NOT case-sensitive and can be any bukkit block material name.

NOTE: any already activated circuit will stay activated even after a change in the block types.

How to build an IC:
-------------------
   Every IC is made of a line of blocks (set with blockType in the properties file) with input and output blocks on the left or right. 
Each output block must have a redstone wire attached to it in order to activate the circuit and counts as part of the circuit. You don't have to add redstone wire to the inputs and this can be done after activating the circuit. A wall sign must be attached to the first main block of the circuit with the circuit class name on the first line, and any additional arguments on the next lines.

   Once the IC is built, right-click the sign to activate the circuit.

* Input and output numbering starts from the sign onwards. i.e. input/output 0 will be the closest to the sign.
* The main line can be of any length and input and output blocks can be placed anywhere along this line, on both sides. 

Here's an example (looking from above, each letter represents one block):  
* b - main block (sandstone by default) * i - input block (iron block by default) * o - output block (gold block by default) * r - redstone wire * s - wall sign attached to the main block


	    s	    
	r o b i
	    b
	r o b i


sign text:
	clock
	2sec

This will create a clock circuit with 2 outputs and 2 inputs. When the block left to one of the input blocks receives a high redstone current the corresponding output will start to blink, turning on every 2 seconds.

An image of the circuit:
![clock example circuit](/example.png)

Commands:  
---------
   * /redchips-list	Message the player with a list of every currently activated circuit in the server.

Available circuit classes:
-------------------------
* adder: 
   
* and:

* clock:

* counter:

* demultiplexer:

* divider:

* flipflop:

* multiplexer:

* multiply:

* or:

* pisoregister:

* print:

* random:

* receiver:

* shiftregister:

* transmitter:

* xor:






