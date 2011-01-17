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
   You can change the block types used to construct a circuit by editing this file.
   Type is NOT case-sensitive and can be any bukkit block material name.

NOTE: any already activated circuit will stay activated even if it's blocks are no longer of the required block type.

How to use:
-----------  
   TBD

Commands:  
---------
   /redchips-list	Message the player with a list of every currently activated circuit in the server.

Available circuit classes:
-------------------------
* adder

* and

* clock

* counter

* demultiplexer

* divider

* flipflop

* multiplexer

* multiply

* or

* pisoregister

* print

* random

* receiver

* shiftregister

* transmitter

* xor






