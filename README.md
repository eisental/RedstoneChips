RedstoneChips 
=============

RedstoneChips is a Minecraft Bukkit plugin that allows you to build custom integrated circuits on your minecraft server. 
Chips can be constructed with any number of inputs and outputs and in almost any imaginable structure. RedstoneChips currently consists of the following plugins:

- RedstoneChips - The core plugin (required). 

- [BasicCircuits](http://github.com/eisental/BasicCircuits) - The basic chip library containing most chip types you might need (required).

- [SensorLibrary](http://github.com/eisental/SensorLibrary) - A chip library for various sensor chips (optional).

Check out the [circuitdocs](http://eisental.github.com/RedstoneChips/circuitdocs) for more info on available RedstoneChips circuits.

__For much more information, visit the [RedstoneChips](http://eisental.github.com/RedstoneChips) site.__

Installation
------------
* [Download](http://eisental.github.com/RedstoneChips) the latest version bundle.
* Copy all jar files to the plugins folder of your craftbukkit installation. 

Changelog
----------

#### RedstoneChips 0.97 (Apr 30th, 2013)
- Fixed the saving bug on cb 1.5.1.
- Added an option to disable update checking.

#### RedstoneChips 0.96 (Dec 1st, 2012)
- Fix for too frequent saving on bukkit 1.3.1+

#### RedstoneChips 0.95 (Apr 8th, 2012)
- Updated for MC1.2.5
- Rewrote /rcsel. Added /rcsel target for selecting chips by pointing at them and /rcsel world <world> … <world> for selecting all chips in specified worlds.
- Added /rctool <material> - sets the chip probe tool material and /rctool clear for removing all tools.
- Added many command aliases. 
- Memory data that's no longer used by any other chip is properly released.
- Split some functionality into etCommon library. 
- Uses new bukkit event system.
- Player data, including the chip probe material is saved on player quit and loaded on player join.
- /rcactivate - renamed verbose option to -v. -v1 and -v2 print different amount of debug messages.
- Chips can be built with one io block and a sign attached to the io block.
- /rctransmit renamed to /rcsend. 
- Added support for saving circuit state data on a reset.
- Fixed memory file support for integers with more than 64bits. 
- Fixed bit set and big integer conversion methods. 
- Fixed redstone torch output CB1.1 bug.
- Fixed lever update doesn't apply physics around the output block.
- Fixed an update bug in redstone torch output devices.

#### RedstoneChips 0.94 (Jan 9th, 2012)
- Note blocks can be used as output devices.
- removed rightClickToActivate pref. It's now always right-click to activate.
- Added verbose argument to /rcactivate for finding scan problems. 
- Rewrote wireless system. Allows one chip to use any number of receivers and transmitters. This could BREAK chip libraries.
- Improved the feedback loop detector. 
- Added command aliases to /rchelp
- /rc command new update check doesn't block the server.
- Fixed sram /rctype dump crashing the server (http://github.com/eisental/BasicCircuits/issues/9).
- Many other small bug fixes and changes.

### RedstoneChips 0.93 (19/12/11)

Features:

- Redstone torches, doors, trapdoors and powered rails can be directly controlled by an output block in addition to levers.
- A chip will activate also when one of its output pin has no devices (levers, torches, etc.) connected to it.
- Multiple output devices can be connected to one output block.
- An output block that has no output devices will try to send a signal directly to adjacent input blocks (this includes blocks above or below the output block). This doesn't use redstone at all and is considerably faster.  
- Chips are not allowed to share blocks any more.
- new /rctransmit (alias /rct) command for transmitting data over wireless channels.
- New chip probe tool. define an item as a probe using /rctool. Right-click on a chip pin to display pin info, right-click on a chip block to display chip info, right-click on the chip sign to enable/disable debug. You can't change lever state or activate circuits when using the tool.
- Added ability to name chips using /rcname. Any command that worked with a chip id also works with a chip name. rcdebug uses the chip name in messages.
- World circuit files are not created unless the world has chips on it. World files that no longer contain any chips are deleted.
- Added /rcenable and /rcdisable commands. Disabled chips will stay disabled after a server restart.
- Added permission list for each command in /rchelp.
- Added /rchelp all, mostly so I could copy paste it to the website.
- Added a check for plugin updates on plugin startup and in /rc command.
- /rcpin now works with interface blocks, input blocks (pin or source) and output blocks (pin or output device).
- The last message on all protected channels is transmitted to the channel receivers on server startup.
- Block place event is cancelled when trying to activate a chip so that right-clicking a sign with a block in hand won't place the block.
- /rcclasses displays classes by libraries. 
- Changes to command messages and help texts.

Bugfixes:

- Fixed the /reload bug
- Fixed remote chip break permission.
- Fixed NPE that occurred when an output lever had an invalid data value. Added a check for invalid output levers in circuit integrity check.
- Fixed line wrapping and some other things in page maker.
- Chips are properly shutdown after unloading a world.
- The plugin will disable on startup if an invalid preference value is found in preferences.yml file
- Chunks properly load when necessary. This should solve various chunk related bugs.
- The plugin doesn't save twice on server shutdown.

#### RedstoneChips 0.92 (4/12/11)
- Compatible with craftbukkit build 1532+- and Minecraft 1.0.
- Different wool colors act as different materials when used as a chip block (by @AterIgnis). 
- Finally, permissions support! By @Mordenkainen
- Better support for multiworld: Each world has it's own circuits file. chips are activated and deactivated when their world is loading/unloading allowing for hotswapping worlds without any ghost chips. This should also solve any startup problems you might have. by @AterIgnis and @eisental
- Special characters are allowed for sign args. Circuits file is saved in UTF-8 encoding.
- Changed the yaml dump format.
- new `/redstonechips` (or `/rc`) command for general plugin info.
- disabled chips have gray class names on their sign.
- A debugged disabled chip will broadcast a MUCH lower number of messages.
- When using `/rcdebug list` the title notifies whether debug is paused or not.
- `/rcinfo` internal state keys will appear on 1 line.
- Rewrote /rcdebug. Added /rcdb alias. 
- /rcp command will not re-execute the last paging command each time it's typed.
- Lever state is not updated unless its chunk is loaded. 
- Checking if player interact event and entity explode event is cancelled.
- New pref key - maxInputChangesPerTick. When a pin receives this many input changes the feedback loop detector will fire and disable the chip's inputs. Set to 20000 by default.
- Receiving circuit clock output will pulse even when the message received is identical to the last one.
- `/rclist` filter parsing errors are now properly handled.
- levers must be actually attached to an output block in order to function. Bugged 0-data levers will still work.
- when the players gamemode is creative, only right-click can activate a chip.
- A fix to solve the "broken counter" bug. Thanks @Mordenkainen !

#### RedstoneChips 0.9 (23/04/11)
- Circuit input state will now update when a redstone source block is placed or removed from around the input block.
- Redstone torches can also be used as an input source.
- `/rcsel` now tries to use WorldEdit's selection when there's no `/rcsel` selection defined. 
- `/rcarg` will undo the changes if they cause an error.
- It's possible to change more than 1 argument using `/rcarg <arg index/add/clear> <arg value>…<arg index/add/clear> <arg value>`.
- New `/rcsel list` command prints list of all active circuits in the selection.
- `/rchelp` now uses pages and the command description looks a bit better. Many changes to the help content.
- Fixed wrong number of lines used in `/rcchannels`.
- Added filters to `/rclist` (see `/rchelp rclist`)
- New page turning system. Using `/rcp [page#|prev|next|last]` after running a command with paging will run the command again displaying a new page.
- `/rcclasses` uses the page maker.
- Added `/reset all` command. Use with caution.
- Many new lines of code making sure chunks don't get loaded unless necessary.
- The coordinates of all chunks used by a circuit are saved in the circuits file and displayed in `/rcinfo`.
- When saving circuit states each circuit is scanned to make sure it's structure is intact and deactivated if otherwise.
- When trying to activate a circuit made of invalid block types a nice error message is displayed.
- Binary numbers are displayed in text messages in words of 4 bit with a space between each.
- Internal circuit state bitsets are now saved in little endian order (Should require a reset to every kind of register and some other circuits classes).
- Circuit load console messages give some more details.
- Circuit loading errors will now cause a stack trace print.

bug fixes:

- Circuit classes with a name longer than 13 characters are not loaded to work with MC1.5.
- Fixed bug that caused an exception when trying to activate a sign with an empty 1st line
- Fixed a bunch of /rc-* leftovers by removing the dash.
- Fixed bug in /rcdebug by circuit id.
- Fixed a bug where left-clicking an empty sign would throw an exception.
- When deregistering a transmitter it makes sure the channel exists before trying to remove it.
- Activation signs are only updated if their chunk is already loaded.

internal changes (for library authors):

- Renamed Circuit.updateOutputLevers() to Circuit.circuitChunkLoaded() and CircuitManager.checkUpdateOuptutLevers(Chunk chunk) to CircuitManager.updateOnChunkLoad(Chunk chunk).
- Renamed RedstoneChips.getPrefsManager() to getPrefs().
- Moved helper methods to their own classes.
- Rewrote command handling to use a separate class for each command.
- New field Circuit.chunkLoaded set to true when the circuit's chunk is loaded and set to false when it's unloaded.
- Added method Circuit.circuitChunkUnloaded() that's called when the circuit's chunk is unloaded.


#### RedstoneChips 0.88 (07/04/11)
- New /rcsel command for mass editing circuits in a cuboid. It's possible to use it for activating, destroying reseting or fixing i/o block types of circuits.
- New /rcactivate command for activating circuits possibly with different i/o block types than currently set.
- New /rcarg command for editing/adding/clearing sign arguments of a circuit.
- New /rcfixioblocks command for changing input, output and interface block types of a circuit to match the current setting. 
- Active circuits will have their class name colored (in red by default). It's possible to change the color using the signColor preference key.
- Removed dashes from command names. /rc-debug becomes /rcdebug, /rc-info becomes /rcinfo, etc.
- Circuit activation is now done by LEFT-clicking the circuit's sign. A preference key can be set to revert back to right-click.
- Any kind of loading error will now trigger a circuits file backup.
- Added hex representation of input and output states in /rcinfo.
- Fixed bug where receivers with 0 outputs were allowed and caused an exception.

#### RedstoneChips 0.87 (30/03/11)
- Complete rewrite of the broadcast channels system.
- transmitters and receivers can work with a selected part of the broadcast channel. Use <channel name>:<start bit> as the channel sign arg to set the 1st bit in the channel 
the circuit will work on.
- `/rc-channels` now displays the number of bits used by each channel.
- `/rc-channels <channel name>` prints additional information about a specific channel.
- Using plugin name and version on circuit library load message
- Sepearated between circuit shutdown, which happens each time the plugin is disabled, and circuit destroy - when the circuit is permanently removed from the circuit list.
- Updated for craftbukkit #602.

#### RedstoneChips 0.86 (19/03/11)
- Wool color or any other block with data value can be used as an indicator block (input/output/interface). For example: /rc-prefs inputBlockType wool:silver will now work.
- `/rc-list` will now show only the circuits in the current world by default. Using `/rc-list all` would show circuits in all worlds and display each circuit's world.
- `/rc-list` shows each circuits sign arguments (or at least part of them).
- `/rc-channels` shows how many receivers and transmitters are communicating on each broadcast channel.
- Using /rc-debug on a circuit you're already debugging will turn debug for the circuit off.
- Added hex values and changed the message format of input-change debug messages
- Changed the circuit id system. Circuits will keep their id numbers after rc-reset or server restart.
- New backup redstonechips.circuits files won't overwrite older ones.
- Invalid pref values entered using /rc-prefs are now caught properly.
- Some command format changes and bug fixes.
- Fixed a little bug when removing rc-type circuits.

#### RedstoneChips 0.84 (10/03/11)
- New circuit library interface mechanism (again). Plugin jar file can be renamed to anything now. 
- The plugin will now backup the circuits file when it encounters a loading error.

#### RedstoneChips 0.83 (07/03/11)
- Changed maven version to beta so I won't need to recompile every circuit library whenever there's a new RedstoneChips version. This means that the jar filename is changed to RedstoneChips-beta.jar and it will stay that way on the next versions as well.
- Works with multiworld plugins. Using same fix as ControllerBlock, thanks @Hell_Fire
- Saves circuits also when the plugin is disabled. You can use the reload command without losing changes.
- New rc-save and rc-load commands.

#### RedstoneChips 0.82 (28/02/11)
- Every chip can now use a different block as a chip block. The chip's block type is determined by the block type the sign is attached to. 
- /rc-debug can be used with a circuit id number and /rc-debug alloff can be used to stop receiving debug messages from all circuits.
- Uses the new Bukkit Player.getTargetBlock().
- Supports /rc-reset with circuit id as an argument; New /rc-help command for some info about RC commands.
-  Temporary fix for yet another circuit library load order problem. Many changes to the way circuit libraries behave. Check [LibraryTemplate](https://github.com/eisental/LibraryTemplate) for info.
- Changed Circuit.init() to use CommandSender instead of player. If you're working on a circuit library you need to change your circuits to use `public void init(CommandSender sender, String[] args) {}`
- Don't need to use ChatFixUtil anymore.
- Doesn't use the old constructor. Good riddance.

#### RedstoneChips 0.8 (14/02/11)
- Many internal changes in the way input pins are handled and their lookup maps.
- Using ChatFixUtil by Olof Larsson prevent chat color client crashes.
- When scanning a chip, chip blocks are first looked for to the right and to the left instead of in the original direction. This means that branches closer to the sign will have lower pin numbers.
- any IO block (input, output or interaction) can now be placed on top or below a chip block.
- Circuits can now register themselves as receivers or transmitters on a specific broadcast channel to receive bits wirelessly. 
- Circuits can now register themselves as type receivers to receive text entered by using the /rc-type command.
- Moved /rc-channels command into RedstoneChips to work with the new ReceivingCircuit and TransmittingCircuit interfaces.
- Moved /rc-type command into RedstoneChips to work with the new rcTypeReceiver interface.
- Uses <craftbukkit>/plugins/RedstoneChips as the data folder. The version number is no longer part of the folder name.
- Saves the circuit state whenever the world is saved instead of doing it on every circuit activation/deactivation.
- Shorter command names: redchips-active -> rc-list, redchips-classes -> rc-classes, redchips-prefs -> rc-prefs, redchips-debug -> rc-debug, redchips-pin -> rc-pin, redchips-destroy -> rc-destroy, redchips-deactivate -> rc-break, redchips-channels -> rc-channels.
- New commands: /rc-reset to reactivate a circuit. Useful for applying changes to the circuit structure or sign arguments; /rc-info print's a lot of information about a circuit. Used by pointing at a circuit or with a circuit id.
- Material names in the preferences can be entered without spearating underscores.
- Added a check for infinite feedback loops, such as a not gate in which the output is connected straight to the input. This will no longer cause the server to crash and the offending circuit will now be disabled. To use it again it needs to be reactivated.
- Circuits will behave much more nicely on server restarts.

#### RedstoneChips 0.77 (7/02/11)
- Moved CircuitIndex and CircuitLibrary classes to package org.tal.redstonechips.circuit
- Using the new scheduler for turning off buttons that are directly connected to an input block.
- Some message text / color changes.
- Removed log message when storing circuits after circuit activate/deactivate 
- Added javadocs to most classes
- Circuit libraries can add their own preferences keys.

#### RedstoneChips 0.76 (and 0.75) (4/02/11)
- New circuit library loading mechanism for solving startup issues. Adding new circuit libraries should be a bit easier.
- Circuits can receive redstone current from the top or any side of an input block.
- Output levers can be attached to any side or on top of an output block.
- Controlled blocks can be attached to any side (including top and bottom) of an interface block.
- Circuits will now update their output levers when their chunk is loaded.
- After activating a circuit its outputs are now immediately updated according to the current state of its inputs.
- Fixed some bugs dealing with chunk load / unload and other stuff.

#### RedstoneChips 0.71 (31/01/11)
* Updated to work with new bukkit command api.
* New /redchips-deactivate command for deactivating circuits without destroying them, activated either by pointing at the circuit or remotely for admins, by using `/redchips-deactivate <circuit id>`.
* New /redchips-destroy command to destroy a circuit and all of its blocks. The command is disabled by default. Set preference key 'enableDestroyCircuit' to 'true' to enable.
* Circuit debuggers are now notified when the circuit is destroyed including info about who destroyed it and where.


#### RedstoneChips 0.7 (27/01/11)
* Completely new circuit detection algorithm. Circuits can now be built in almost any shape.
* New storage format for the active circuits file (now named redstonechips.circuits).
* Interface blocks replace the output block used by circuits such as pixel, print and synth. More than one interface block per chip is now possible. Interface blocks are placed the same way as input and output blocks and they're indicated by a block of material interfaceBlockType (also added to preferences).
* Fixed print order of binary numbers. LSB is now the rightmost character.
* Missing output levers will now cause an error message to be displayed.
* Output levers will now turn off after a circuit is destroyed.
* A circuit will now deactivate when one of its structure blocks burns down.
* Chat colors are added to the preferences file and can be modified using /redchips-prefs.

#### RedstoneChips 0.6 (24/01/11)
* new command /redchips-debug for getting debug messages from a circuit.
* new command /redchips-pin for getting info about a specific chip pin.


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



