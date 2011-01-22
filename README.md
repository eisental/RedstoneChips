RedstoneChips 0.5
==================

A Bukkit plugin for building custom integrated redstone circuits with any number of inputs and outputs.
This is the core RedstoneChips plugin consisting of only the essentials.
To actually build ICs you need to download and install the [BasicCircuits](http://github.com/eisental/BasicCircuits) plugin.

__If you like this, please__

[![Donate](/eisental/RedstoneChips/raw/master/images/btn_donate_LG.gif")](http://sites.google.com/site/eisental/home/donate)

![Note block sequencer](/eisental/RedstoneChips/raw/master/images/above.jpg "Note block sequencer")

Install
--------
   * Download [jar file](/eisental/RedstoneChips/RedstoneChips-0.5.jar).
   * Copy jar file to the plugins folder of your craftbukkit installation.
   * Install at least one circuit package plugin.

On the first time craftbukkit is started with the plugin installed, it will create a folder is inside the craftbukkit plugins folder
and a preferences.yml file. The file redstonechip.dat is created after the activating a circuit for the first time.
You can change the block types used to construct a circuit by editing preferences.yml or by using the /redchips-prefs command (see below).
Default values are gold block for outputs, iron block for inputs, and sandstone for the chip itself.
Type is NOT case-sensitive and can be any bukkit block material name or id.
any already activated circuit will stay activated even after it's block types are no longer valid.

How to build an IC
-------------------
First build a line of chip blocks and add input and output blocks to the left or to the right of the line.
Place a lever on the block next to each output block and a wall sign attached to the first main block of the circuit.
On the first line of text write the circuit class name, any additional arguments must use the next lines.
Once the IC is built, right-click the sign to activate it. You should receive a green confirmation message that lets you know
that the circuit is now ready for use.

* Any block that can send a redstone current (redstone wire, redstone torch, button or lever) can be placed on the block next to an input.
  The direction of a redstone wire is irrelevant as long as its in the right place.
* Input and output numbering starts from the sign onwards. i.e. input/output 0 will be the ones closest to the sign.
  Blocks south or west to the chip block will have a lower index that the blocks on the other side.
* The main line can be of any length and input and output blocks can be placed anywhere along this line, on both sides.

Here's an example 4-bit counter chip with 2 inputs and 4 outputs (looking from above, each letter represents one block):
* b - main block (sandstone by default) * i - input block (iron block by default) * o - output block (gold block by default) * l - lever on any block face * s - wall sign attached to the main block

	       s
	0  l o b i  0
	       b
	1  l o b i  1
	       b
	2  l o b
	       b
	3  l o b


sign text (on first line):
	counter

The first input pin is one line after the sign. Each time the block on its right receives a redstone current the chip will increment
its internal variable by 1 and output it as a binary number by changing the state of the output levers. The least significant bit 
is the first output (i.e. the one closest to the sign). For example, when the counter reaches 5, it will switch on outputs 0 and 2 
giving out the binary number 1010. 

The second input pin is the reset pin. When it receives redstone current the counter will go back to 0.

The 4-bit counter:
![counter example circuit](/eisental/RedstoneChips/raw/master/images/counter.jpg)

For more info check the [BasicCircuits](https://github.com/eisental/BasicCircuits/wiki/BasicCircuits-) Wiki.

Destroying an IC
-----------------
You can destroy an IC by breaking any of its structure blocks, including the output levers.
Creepers and TNT explosions will also cause the IC to stop functioning.

Commands
---------
   * __/redchips-active__ Lists every currently active circuit on the server.
   * __/redchips-classes__ Prints a list of installed circuit classes.
   * __/redchips-prefs__ Prints preferences.
   * __/redchips-prefs key__ Prints one preference key: value pair.
   * __/redchips-preds key value__ Changes the value of a preference key and saves the file. Only admins are allowed to change the preferences values.
     - Example: typing <code>/redchips-prefs chipBlockType GLASS</code> will change the preferences value and make the plugin
            immediately expect new chips to have their body made of glass.

Changelog
----------
#### RedstoneChips 0.5 (22/01/11)
* improved circuit detection algorithm.
* improved performance of circuit destruction.
* circuits are now destroyed when one of their blocks get exploded.
* new /redchips-classes command.
* renamed active circuits list command to /redchips-active
* new /redchips-prefs command, for viewing and editing the preferences file from within the game.    
* moved plugin files to the plugin folder.
* preferences are now a yaml file.
* added color to player messages.
* fixed error message when creating a bit-set circuit with no outputs.
* catching concurrent modification exception when setting lever data

#### RedstoneChips 0.4 (20/01/11)
* temporary fix for the button bug.
* better performance when working with a large number of circuits.



