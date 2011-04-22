---
layout: main
title: multiplier
---

Performs an arithmetic multiplication on any number of input sets and outputs the result as a binary number.
The 1st sign argument should note how many bits each input number is using.
If an optional constant argument is added to the sign text, the IC will multiply the result by the constant.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/multiplier.java)

For a detailed example of building a chip that uses a number of input bit sets read the [AND gate documentation](And).
* * *


#### I/O setup 
* Can have any number of outputs. 
* The number of input pins can be any multiple of the wordlength.

#### Sign text
1. `   multiplier   `
2. ` [wordlength]   `  
3. ` [constant number]  ` (optional)

__Version history:__ Added to BasicCircuits 0.1