---
layout: main
title: RedstoneChips Usage Guide
---

Building a Chip
---------------
- Build a structure made of chip blocks (sandstone on default)

If you're having problems or just want to have more information, you can use the debugging commands `/redchips-debug` and `/redchips-pin`. See the description below.

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
