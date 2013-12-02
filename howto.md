---
layout: main
title: User Guide
---

- [Building a chip](#building_a_chip)
- [Debugging](#debugging)
- [Destroying a chip](#destroying_a_chip)
- [Chip detection scanning rules](#chip_detection_scanning_rules)
- [Plugin commands](#plugin_commands)
- [Preference keys](#preference_keys)
- [Permissions](#permissions)
- [Writing chip libraries](#writing_chip_libraries)


Debugging
---------
There are several commands that you can use to debug chips and find problems.

- [/rcdebug](#rcdebug_register_yourself_as_a_chip_debugger) - Sends debug messages from any number of chips.
- [/rcpin](#rcpin_prints_information_about_a_chip_pin) - Prints pin order index and current state.
- [/rcinfo](#rcinfo_prints_a_lot_of_useful_information_about_a_chip) - Prints info about the current state of the chip.
- [/rcname](#rcname_name_or_unname_a_circuit_for_easier_debugging) - Give names to chips to be able to identify them better.
- [/rcenable](#rcenable_enables_a_disabled_chip), [/rcdisable](#rcdisable_disables_a_chip) - Disable parts of you circuit to find problems easier.
- [/rctool](#rctool_turns_the_item_in_your_hand_into_a_chip_probe) - Define a chip probe that prints information when right-clicking chip blocks.

Destroying a Chip
-----------------
You can destroy a chip by breaking any of its structure blocks, or by running the /rcbreak command while pointing at a circuit block. Use `/rcdestroy` to destroy the chip and remove all of its blocks. 
__Creepers__, __TNT__ explosions and __fire__ (in case you're using flammable blocks) will also cause the chip to stop functioning.
Destroying a chip block with __Power tools__ or worldedit commands, however, will not cause the chip to decativate until the server shutdowns, and can result in "phantom" circuits still taking place in memory. Whenever the plugin saves data invalid chips should be deactivated. Running /rcsave to force saving.

Chip detection scanning rules
-----------------------------
To be able to guess the pin numbering of more complex structures you need to understand how the plugin detects and scans the structure once you activate it by clicking on the sign. It scans the circuit block by block starting at the sign block itself. The pins are numbered as the circuit structure is scanned, therefore when the structure is a straight line the count starts at the sign and onwards. When more than one dimension is used the plugin will scan according to the following rules:

- The sign block is added to the structure and then the plugin moves to the chip block the sign is attached to. 
- It will try to find input, output or interface blocks at any of the other sides of the chip block and above or below. The important part is the order in which different sides are scanned. First, it will look to the right (relative to the current scan direction), next it will look to the left. After that it will look at the next block in the original direction and finally it will look back, opposite to the scan direction. Finally it checks the block above and below. If going backwards seem like a waste of time see the next point. 
- Now the plugin will go to the next chip block. The scan order is 
	- The block to the right, relative to the current scan direction.
	- The block to the left, relative to the current scan direction.
	- The block in the same direction as before.
	- The block in the opposite direction.
	- One block up. If the block above is a chip block the scan will continue in the same direction as before only one level above.
	- One block below. Will also continue in the same direction.

	
One very important thing to note is that the recursive scanning process work by branches. It will continue going from block to block in one direction even when chip blocks are found in other directions. Only when it reaches a dead end it will go back to try the other branches.

The exact algorithm can be found at the [CircuitManager](http://github.com/eisental/RedstoneChips/blob/master/src/main/java/org/tal/redstonechips/ChipScanner.java) class. 

Plugin commands
----------------

Permissions
-----------
To use RedstoneChips permissions you must have a permissions plugin on your server. To activate permissions support enter: `/rcprefs usePermissions true`. Protected channels can be used also when usePermissions is set to false.

###Command permissions

`/rchelp <command name>` displays all permission node that are associated with a command.

All commands have an "allow" and "deny" permission. The "deny" permission overrides the "allow" permission. This can be used if a user belongs to a group which allows a command that you need to explicitly deny the user. 

###Chip permissions
All chips have create and destroy permissions. These also have an "allow" and "deny", which function identically as above. Destruction protection prevents a chip from being destroyed via RC commands or by breaking the blocks that make up the circuit. The following node types can be used:

- `redstonechips.circuit.destroy.\*` - allows destroying any circuit type.
- `redstonechips.circuit.destroy.deny` - denies destroying any circuit type.
- `redstonechips.circuit.<chip-name>;` - allows destroying a specific circuit type.
- `redstonechips.circuit.<chip-name>.deny` - denies destroying a specific circuit type.

###Protected channels
Protecting channels allow users to control who can create and destroy wireless chips on particular channels. This system is independent of the other permissions and functions even when permissions are disabled. Users can define admins for the channel, who can change who has access to the channel, and users, who can create chips using the channel, but can not change access. `/rcchannels` does not show a user any channels that they do not have access to, and protected channels the user does have access to are shown with a "P" to identify they are protected. `/rcchannels <channel name>` will show all authorized users and admins of the channel. Use the command `/rcprotect` to manage protected channels. 

###Top-level permission nodes
- `redstonechips.admin`: Grants access to all of RedstoneChips including admin commands.
- `redstonechips.*`: Grants access to all of RedstoneChips excluding admin commands.
- `redstonechips.deny`: Denys access to all of RedstoneChips.
- `redstonechips.command.*`: Grants access to all of RedstoneChips commands excluding admin commands.
- `redstonechips.command.admin`: Grants access to all of RedstoneChips commands including admin commands.
- `redstonechips.command.deny`: Deny all commands.
- `redstonechips.circuit.*`: Grants access to create and destroy all RedstoneChips circuits.
- `redstonechips.circuit.deny`: Deny all building or destroying circuits.

Writing chip libraries
-------------------------

Chip libraries are essentially bukkit plugins that provide RedstoneChips with new chip types. If you have a solid understanding of Java, writing your own chips should be easy enough. 

These should help you get started:

- [Library template](http://github.com/eisental/LibraryTemplate).
- [API documentation](/RedstoneChips/apidocs).

Don't hesitate to ask any question by mail or on the forum.