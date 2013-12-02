---
layout: main
title: Plugin Commands
---

* * *

###/redstonechips: General plugin information.

permissions:
- redstonechips.command.redstonechips - Grants the redstonechips command.
- redstonechips.command.redstonechips.deny - Denies the redstonechips command.

* * *

###/rclist: Prints a list of active chips.

`/rclist [world-name|all|this] [<filter-type>: <arg>,<arg>,<arg>;...]`

- Use `/rclist` to list all chips in the current world or, when used from the server console, all chips on the server.
- Use `/rclist all` as a player to see a list of all chips on the server
- Use `/rclist <world-name>` to list all circuits in a specific world.

To show only certain kinds of chips you can use a filter. There are several filter types:

- `location: <x>,<y>,<z>,[radius]` - Show only circuits in a radius around the location.
- `location: this, [radius]` - Uses your current location.
- `chunk: <x>,<z>` - Show only circuits in the specified chunk. `chunk: this` will use your current chunk
- `class: <class-name>` - Show only circuits of the specified class. You can use partial class names.

You can combine different filters by separating them with a semicolon (;). You can also use partial filter type names.

Examples:

- `/rclist this ch: this; cl: pix` - List every pixel circuit in your current world and chunk.
- `/rclist this loc: this, 10; class: decoder` - List every decoder in a 10 block radius around you.

permissions:
- redstonechips.command.rclist - Grants the rclist command.
- redstonechips.command.rclist.deny - Denies the rclist command.

* * *

###/rcclasses: Prints a list of installed chip types.

`/rcclasses`

aliases: `/rccl`

permissions:
- redstonechips.command.rcclasses - Grants the rcclasses command.
- redstonechips.command.rcclasses.deny - Denies the rcclasses command.

* * *

###/rcprefs: Allows to see and change the plugin's preferences.

`/rcprefs [pref key] [new value]`

Use the command with no arguments to list all preferences values.
To change a preference value use `/rcprefs <pref key> <new value>`.

permissions:
- redstonechips.command.rcprefs - Grants the rcprefs command.
- redstonechips.command.rcprefs.set - Grants using the rcprefs command to set preferences.
- redstonechips.command.rcprefs.deny - Denies the rcprefs command.
- redstonechips.command.rcprefs.set.deny - Denies using the rcprefs command to set preferences.

* * *

###/rcdebug: Register yourself as a chip debugger.

`/rcdebug [id] [io|.|clear|list]`

- Pointing at a chip and running the command will toggle debugging the chip.
- To debug or stop debugging a chip remotely use `/rcdebug <chip id>`.
- Pause debugging for all chips by running `/rcdebug`.
- List all debugged chips by running `/rcdebug list`.
- Clear your debug list by running `/rcdebug clear`.
- To receive a debug message whenever a chip pin changes its state use `/rcdebug [chip id] io`. 

You can use `/rcdb` as a shortcut or type only part of the command. 
For ex. `/rcdb l` is equivalent to `/rcdebug list`.

aliases: `/rcdb`

permissions:
- redstonechips.command.rcdebug - Grants the rcdebug command.
- redstonechips.command.rcdebug.deny - Denies the rcdebug command.

* * *

###/rcpin: Prints information about a chip pin.

Point towards an output lever or input redstone source.
Prints the pin number (starting at 0), pin type (input/output), and current state (on/off) of the pin.

permissions:
- redstonechips.command.rcpin - Grants the rcpin command.
- redstonechips.command.rcpin.deny - Denies the rcpin command.

* * *

###/rcactivate: Activates a chip.

`/rcactivate [-v|-v1|-v2]`

Point towards the chip sign and execute the command.
To activate a circuit built with different input, output or interface block types then set in the preferences use: `/rcactivate <inputBlockType> <outputBlockType> <interfaceBlockType>`.

Use `/rcactivate <-v|-v1|-v2>` to receive debug messages from the chip scanner.

aliases: `/rca`

permissions:
- redstonechips.command.rcactivate - Grants the rcactivate command.
- redstonechips.command.rcactivate.deny - Denies the rcactivate command.

* * *

###/rcdestroy: Destroys a chip and removes its blocks.

Point towards a block of the chip you wish to destroy and enter the command.
This command is disabled by default. To enable it use `/rcprefs enableDestroyCommand true`.

permissions:
- redstonechips.command.rcdestroy - Grants the rcdestroy command.
- redstonechips.command.rcdestroy.deny - Denies the rcdestroy command.

* * *

###/rcbreak: Deactivates a chip without removing its blocks.

`/rcbreak [id]`

Point towards a block of the chip or enter the its id as an argument.

aliases: `/rcb`

permissions:
- redstonechips.command.rcbreak - Grants the rcbreak command.
- redstonechips.command.rcbreak.id - Grants the rcbreak command with a circuit id.
- redstonechips.command.rcbreak.deny - Denies the rcbreak command.
- redstonechips.command.rcbreak.id.deny - Denies the rcbreak command with a circuit id.

* * *

###/rctype: General command for sending input to a chip.
`/rctype [...]`

Use by pointing at a block and enter the command. Each chip reacts to it differently.
Check the relevant chip documentation for more information about usage.

aliases: `/rct`

permissions:
- redstonechips.command.rctype - Grants the rctype command.
- redstonechips.command.rctype.deny - Denies the rctype command.

* * *

###/rcsend: Transmit data over a wireless channel.
`/rcsend <channel> [start-bit:]data ... [start-bit:]data`

data can be either an integer number, ascii character, or the letter 'b' followed by a binary number.
ascii characters may optionally be surrounded by quotes ''. For example, `/rcs chan '8'`

aliases: `/rcs`

permissions:
- redstonechips.command.rctransmit - Grants the rctransmit command.
- redstonechips.command.rctransmit.deny - Denies the rctransmit command.

* * *

###/rcreset: Rescans and reactivates a chip.
`/rcreset [id|all]`

Use by pointing towards the chip or by using the its id as an argument.
Reset a circuit to scan for new i/o blocks or sign arguments and apply any changes.

Running `/rcreset all` will reset all active chips. Use with caution!

aliases: `/rcr`

permissions:
- redstonechips.command.rcreset - Grants the rcreset command.
- redstonechips.command.rcreset.id - Grants the rcreset command with a circuit id.
- redstonechips.command.rcreset.all - Grants the rcreset all command.
- redstonechips.command.rcreset.deny - Denies the rcreset command.
- redstonechips.command.rcreset.id.deny - Denies the rcreset command with a circuit id.
- redstonechips.command.rcreset.all.deny - Denies the rcreset all command

* * *

###/rcenable: Enables a disabled chip.
`rcenable [id]`

Use by pointing towards the chip or by using the its id as an argument.

permissions:
- redstonechips.command.rcenable - Grants the rcenable command.
- redstonechips.command.rcenable.id - Grants the rcenable command with a circuit id.
- redstonechips.command.rcenable.deny - Denies the rcenable command.
- redstonechips.command.rcenable.id.deny - Denies the rcenable command with a circuit id.

* * *

###/rcdisable: Disables a chip.
`/rcdisable [circuit-id]`

A disabled chip is still active but does not respond to any input. 
Use by pointing towards the chip or by using its id as an argument.
To reenable the chip use /rcenable. Disabled chip sign color changes to gray.

permissions:
- redstonechips.command.rcdisable - Grants the rcdisable command.
- redstonechips.command.rcdisable.id - Grants the rcdisable command with a circuit id.
- redstonechips.command.rcdisable.deny - Denies the rcdisable command.
- redstonechips.command.rcdisable.id.deny - Denies the rcdisable command with a circuit id.

* * *

###/rcchannels: Prints a list of all wireless broadcast channels.
`/rcchannels [channel-name]`

Use a channel name as an argument to print info about that channel.

aliases: `/rcch`

permissions:
- redstonechips.command.rcchannels - Grants the rcchannels command.
- redstonechips.command.rcchannels.deny - Denies the rcchannels command.

* * *

###/rcinfo: Prints a lot of useful information about a chip.
`/rcinfo [circuit-id]`

Use by pointing at a block of the chip you want to get info about or use it's id as an argument.

aliases: `/rci`

permissions:
- redstonechips.command.rcinfo - Grants the rcinfo command.
- redstonechips.command.rcinfo.deny - Denies the rcinfo command.

* * *

###/rcfixioblocks: Replaces i/o blocks that use incorrect materials.
`/rcfixioblocks [circuit-id]`

Use by pointing at the chip you want to fix, or use its id as an argument.
Any i/o blocks using a different block type than those set in the plugin's preferences are replaced to the correct material.

permissions:
- redstonechips.command.rcfixioblocks - Grants the rcfixioblocks command.
- redstonechips.command.rcfixioblocks.id - Grants the rcfixioblocks command with a circuit id.
- redstonechips.command.rcfixioblocks.deny - Denies the rcfixioblocks command.
- redstonechips.command.rcfixioblocks.id.deny - Denies the rcfixioblocks command with a circuit id.

* * *

###/rcsel: Select and edit a group of chips.
To add/remove chips from selection:

- `/rcsel` - Switch selection mode on/off. When on, right-clicking a chip block adds it to selection.
- `/rcsel cuboid` - Select chips in a WorldEdit cuboid or start to define a cuboid.
- `/rcsel id <chip-id>...<chip-id>` - Select chips by a list of ids.
- `/rcsel list` - Lists all chips in the selection
- `/rcsel clear` - Clears the current selection.

To run a command on all chips in selection:
The following work the same as their single chip versions.
  
- `/rcsel activate [<input type> <output type> <interface block type>]`, 
- `/rcsel break`
- `/rcsel destroy`
- `/rcsel reset`
- `/rcsel enable`
- `/rcsel disable` 
- `/rcsel fixioblocks`

`/rcsel activate` requires a defined cuboid (either a WorldEdit cuboid or a RedstoneChips one). 

permissions:
- redstonechips.command.rcsel - Grants the rcsel command.
- redstonechips.command.rcsel.deny - Denies the rcsel command.

* * *

###/rcarg: Replace, add or clear chip sign arguments.
`/rcarg <arg-number|add|clear> <arg-value>...<arg-number|add|clear> <arg-value>`

Use by pointing at the circuit you want to edit and enter the command.
To change an existing argument value use `/rcarg <arg-number> <arg-value>`, where arg-number is the
sign argument number (starting from 1), and arg-value is the new value for this argument.

To add an argument as the last use `/rcarg add <arg-value>`.
To remove an argument use `/rcarg clear <arg-number>`.

You can make changes to more than 1 argument by typing additional command arguments. For ex. `/rcarg 1 val clear 2`
will set the 1st argument to val and remove the 2nd argument.

permissions:
- redstonechips.command.rcarg - Grants the rcarg command.
- redstonechips.command.rcarg.deny - Denies the rcarg command.

* * *

###/rcsave: Saves all chip data to file.
Makes sure all chips are intact and saves their current state.
You don't need to use it unless there's a problem with the automatic data save.

permissions:
- redstonechips.command.rcsave - Grants the rcsave command.
- redstonechips.command.rcsave.deny - Denies the rcsave command.

* * *

###/rcload: Reloads chip data from file.
This will reset any changes made to the chips state since the last save occured.

permissions:
- redstonechips.command.rcload - Grants the rcload command.
- redstonechips.command.rcload.deny - Denies the rcload command.

* * *

###/rchelp: Prints a list or description of all RC commands.
Use `/rchelp <command name>` to get help about a specific command.
Use `/rchelp all` to get a printout of all help content available.

permissions:
- redstonechips.command.rchelp - Grants the rchelp command.
- redstonechips.command.rchelp.deny - Denies the rchelp command.

* * *

###/rcp: Moves to a different page when entered after using a command with paging.
`/rcp [page #|prev|next|last]`
Running the command without arguments will cause it to move to the next page or go back to the first if the last page was reached.

* * *

###/rcprotect: Creates, removes, and configures protected wireless channels.
`/rcprotect [channel|listchannels] [protect|unprotect|add|remove] [users:names] [admins:names]`

- Use the command with `listchannels` to list all protected channels.
- Use the command with only a channel name to list users and admins of that channel.
- Use the command with a channel name and `protect` to protect a channel, you will automatically be added as an admin of the channel. You may also pass a list of users and/or admins.
- Use the command with a channel name and `unprotect` to remove protection for a channel.
- Use the command with a channel name and `add` or `remove` to add or remove admins and users of a channel. These commands require that you pass a list of users and/or admins.

Users and admins should be passed in the form of `users:User1,User2,User3` and `admins:Admin1,Admin2,Admin3`.

permissions:
- redstonechips.command.rcprotect.deny - Denies the rcprotect command.

* * *

###/rctool: Set an item as a chip probe.
`/rctool [clear|<item>]`

Run the command w/o arguments to set the item in your hand as the chip probe. 
Run `/rctool clear` to clear the tool item setting.
Run `/rctool <item>` to set the chip probe to the specified item.

- Use the chip probe on a chip i/o block to print information about the pin state.
- Use it on the chip sign to print info about the chip.
- Using it on any other chip block turns chip debug on and off.
	
permissions:
- redstonechips.command.rctool - Grants the rctool command.
- redstonechips.command.rctool.deny - Denies the rctool command.

* * *

###/rcname: Name or unname a circuit for easier debugging.
`/rcname [id] <new-name>` - change the chip name.
`/rcname [id] remove` - unname the chip.

Point towards a chip and enter the command, or use the its id (including its existing name).

permissions:
- redstonechips.command.rcname - Grants the rcname command.
- redstonechips.command.rcname.deny - Denies the rcname command.
- redstonechips.command.rcname.id - Grants the rcname command with a circuit id.
- redstonechips.command.rcname.id.deny - Denies the rcname command with a circuit id.