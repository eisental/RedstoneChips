---
layout: main
title: RedstoneChips Usage Guide
---

Building a Chip
---------------

If you're having problems or just want to have more information, you can use the debugging commands /redchips-debug and /redchips-pin. See the description below.

Chip detection scanning rules
------------------------------



Plugin commands
----------------

   * __/redchips-active__ Lists every currently active circuit on the server.
   * __/redchips-classes__ Prints a list of installed circuit classes.
   * __/redchips-pin__ Point your cross at one of the pins and enter the command to get the current state of the pin and its index. You have to point to the block where redstone current actually flows. That is, you need to point at levers, buttons or redstone wire and not at the output / input blocks.
   * __/redchips-debug__ Register a player as a debugger for a specific circuit. Point your cross at one of the circuit blocks and enter the command. The plugin will now send you debug messages from this circuit. Enter /redchips-debug off while pointing, to stop debugging.
   * __/redchips-prefs__ Prints preferences.
   * __/redchips-prefs key__ Prints one preference key: value pair.
   * __/redchips-prefs key value__ Changes the value of a preference key and saves the file. Only admins are allowed to use this command.
     - Example: typing <code>/redchips-prefs chipBlockType glass</code> will change the preferences value and make the plugin immediately expect new chips to have their body made of glass.
