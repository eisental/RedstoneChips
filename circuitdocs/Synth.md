---
layout: main
title: synth
---

Plays different note pitches on any number of note blocks connected to one of the chips interface blocks. The current pitch is set according to the state of its data pins. 
Any number of note blocks can be attached to each interface block.

There are two ways to use this circuit. 

* Use without any sign arguments - just write `synth` on the sign. The noteblock pitch is set according to the binary value of the data pins. Possible values are between 0 (f#1) to 24 (f#3). This mode requires 1-5 data pins and 1 clock pin. For example, to play the note c2, send 00110 (decimal 6) to the data pins, and then trigger the chip's clock input (1st input). 
* Use a note index by adding a list of notes to the sign. These can be either note numbers, note names, or 'r' for rests. Note names should always be written with a note name and an octave number afterwards - for ex. f#1, db2, g2, are all valid note names. Sharps can be used by typing the '#' sign and flats can be used with a 'b' letter after the note name. For ex. d#2 is D sharp in 2nd octave and eb2 is E flat in 2nd octave.
The pitch is then set by setting the data pins to one of the note indexes. For example if your sign's text (from the 2nd line) is 'f#2 a2 c#3', setting the pins to 0 and triggering the clock will make the blocks play a pitch of f#2, setting the pins to 1 will make the blocks play an a2 and setting the pins to 2 will make the note blocks play c#3. You can have as many pitches as you like as long as they somehow fit on the sign. 

If all you want is to switch between two different pitches you can use the circuit with 1 input pin without a clock pin. In this mode every change of the data pin will trigger the note blocks.

To build a the chip as a wireless receiver add a channel argument using the syntax `#name[:startbit]` 
The channel argument must be the last. In this mode synth requires no inputs or outputs. The play a note send either the note index or the note pitch value over the wireless channel. 

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/synth.java)

* * *

![synth](/RedstoneChips/images/synth.png "synth")
    
#### I/O setup 
* Inputs: Either 1 data pin without a clock pin, or 1 clock pin (input 0) and any number of data pins.
* No outputs.
* Wireless mode doesn't require any inputs.

#### Sign text
1. `   synth   `
2. `  [note] ... [note] ` 

__Version history:__ Added to BasicCircuits 0.6