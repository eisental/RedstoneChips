---
layout: main
title: sram
---
An emulation of a TTL74219 SRAM chip. 
Can store any amount of data words of any bit-length each. Allows for reading and writing the memory data by specifing a memory address.

The number of outputs determines the word-length of the memory chip. 
2 control inputs are required:
* Input 0 - read-write pin - sets the chip to either read mode or write mode.
* Input 1 - disable pin - When set to on the output pins are turned off and any input change is ignored.
After the control inputs are the address inputs. The address can be a number of any size using any number of inputs. 
The last inputs are the data pins. The number of data pins must be the same as the memory word-length.

For example, a memory of 16x4 bits (16 words of 4 bits each) requires 10 inputs - 2 control pins, 4 address pins and 4 data pins and 4 outputs. 

To read a word from memory:
* Make sure the disable pin is off.
* Set the read-write pin on.
* Set the address pins to the address you wish to read.
* The output pins are set to the memory value at the chosen address.

To write a word to memory:
* Make sure the disable pin is off.
* Set the data pins to the value you wish to write.
* Set the address pins to the address you wish to write to.
* Set the read-write pin from on to off if necessary, causing the data pins to be copied into the output pins.
* Set the read-write pin back on to write the data into memory and go back to read mode.

It's also possible to edit the memory contents directly using `/rc-type`.
To set the memory at a certain address to a new value use `/rc-type <address>:<value>`. More than one address can be changed with the
same command. If the address (and colon) is omitted the typed value is set to address 0 with any additional values set at the next addresses.
For example to set the 1st 5 words enter: `/rc-type 0 5 16 7 9`. 
Combining specific addresses is possible as well: `/rc-type 0 6:8 5 16 7 9` will set the 1st 6 bits to [0,5,16,7,9,8].

The memory data is stored in a separate file inside `<plugins folder>/RedstoneChips/` on circuit shutdown (when RedstoneChips is disabled). The values are reloaded when the circuit is initalized. Once the circuit is destroyed or reset the memory file is deleted.
A memory id sign argument can be used to specify a permanent id for this memory chip. 
When used the plugin will never delete its memory data file and try to use an existing memory data file if one is found. 
This means that using `/rc-reset` will not clear the memory data in this mode.
The filename format for the data file is `sram-<memory id>.data` while memory-id is either a free id generated on first circuit init or the memory id specified in the 
sign argument. 

Note that currently the memory contents are only saved when RedstoneChips is disabled or when the server shuts-down. 

on [Wikipeda](http://en.wikipedia.org/wiki/Static_random-access_memory)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/sram.java)
    
* * *

#### I/O setup 
* Can have any number of outputs. 
* Can have any number of inputs.
* Input 0 is the read-write pin.
* Input 1 is the disable pin.

#### Sign text
1. `   sram   `
2. `   [memory id]   ` (optional)

__Version history:__ Added to BasicCircuits 0.87