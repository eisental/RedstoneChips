---
layout: main
title: divider
---

Performs an arithmetic division on any number of bit sets and outputs the result as a binary number.
The 1st sign argument should note how many bits each input number is using. If an optional constant argument is added to the sign text, the IC will divide the result by the constant.

Using a 3rd sign argument with the word `round` will cause the result to be rounded to the closest integer, otherwise the result is truncated, effectively always rounded down. 
Using a 3rd argument with the word `mod` makes the divider run in modulus mode. In this mode the division result is truncated as usual and sent out to the 1st wordlength number of output pins, while the modulus remainder is output to any extra output pins. 

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/divider.java)

For a detailed example of building a chip that uses a variable number of input bit sets read the [AND gate documentation](And).
 
* * *


#### I/O setup 
* Can have any number of outputs. 
* The number of input pins can be any multiple of the wordlength.

#### Sign text
1. `   divider   `
2. ` [wordlength] '
3. ` [constant number]  ` (optional)
4. ` [round/mod] ` (optional)
__Version history:__ Added to BasicCircuits 0.1