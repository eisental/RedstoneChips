---
layout: main
title: synth
---

Sets the pitch of any note block connected to one of its interface blocks according to its data pins. 
You still need to trigger the note block somehow to make it play its note.

There are two ways to use this circuit. 

1. Use without any sign arguments (just write `synth` on the sign). The noteblock pitch is set according to the value of the data pins. Possible values are between 0 to f#1 to 24 for f#3. This mode requires 1-4 data pins and 1 clock pin. For example, send 00110 (decimal 6) to the data pins, and then trigger the clock input to update the noteblocks pitch to c2. 
2. Use a note index by adding a list of notes to the sign. These can be either note numbers or note names. Note names should be always be written with a note name and an octave number afterwards - for ex. f#1, eb2, g2, are all valid note names. 
The pitch is then set by setting the data pins to one of the note indexes. For example if your sign text (from the 2nd line) is 'f#2 a2 c#3', setting the pins to 0 and triggering the clock will set the block's pitch to f#2, setting the pins to 1 will set the pitch to a2 and setting them to 3 will set the pitch to c#3. You can have as many pitches as you like as long as they fit on the sign.

If all you want is to switch between two different pitches you can use the circuit with 1 input pin without a clock pin. In this mode every 
change of the data pin will update the note blocks.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/synth.java)

* * *

    
#### I/O setup 
* Inputs: Either 1 data pin without a clock pin, or 1 clock pin (input 0) and any number of data pins.
* No outputs.

#### Sign text
1. `   synth   `
2. `  [note] ... [note] ` 

__Version history:__ Added to BasicCircuits 0.6