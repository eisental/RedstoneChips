---
layout: main
title: adder
---

Performs an addition of any number of input sets and outputs the result as a binary number.
The 1st sign argument should note how many bits each input number is using. The chip should have this number of input pins for each input number.
If an optional constant argument is added to the sign text, the IC will add it to the result as well. 

The adder can work in subtraction mode if its 3rd argument is `subtract`. In this mode each of the values including the constant are subtracted from the 1st value instead of added. You must specify a constant when using this mode although it can, of course, be zero.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/adder.java)

* * *

#### I/O setup 
* Can have any number of outputs. 
* The number of input pins can be any multiple of the input word length.

#### Sign text
1. `   adder   `
2. ` [wordlength] '
3. ` [constant number]  ` (optional)
4. ` [subtract] ` (optional)
__Version history:__ Added to BasicCircuits 0.1