---
layout: main
title: ramwatch
---

A chip that watches a memory object for changes.

The ramwatch chip requires 1 input and 1 output. The input functions as an
enable switch, and it must be on for the chip to function. The output generates
a 0-tick pulse whenever the specified memory object changes.

The memory object to watch is given as the first sign argument, in the form of
`$<memory-id>`. A second argument may be given, which specifies the address to
watch. (This address may be given in hexadecimal, as 0x1234.) If no address is
given, instead the entire memory object is watched for changes.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/ramwatch.java)
    
* * *

#### I/O setup 
* Must have at least 1 input and 1 output.

#### Sign text
1. `   ramwatch   `
2. ` $<memory-id> `
3. ` [memory-address] ` (optional)

__Version history:__ Added to BasicCircuits 0.96