---
layout: main
title: counter
---

A counter chip of any bit-size up to 32bits. When input 0 (inc) is set to high the chip increments or decrements its internal count variable and outputs a binary number representing the current value. 
The bit-size of the counter is set by the number of output pins. When the counter value overflows the number of output bits it goes back to 0.
There are 3 possible sign arguments (See below for the correct ordering), max, min and direction. 

- `max` sets the maximum number the counter can reach. The default value is the maximum number that can be represented by the output pins. 
- `min` sets the minimum number the counter can reach. The defatlt value is '0'.
- `direction` can be either 'up', 'down' or 'updown'. 
	- 'up' - the counter will increment its count, going back to 'min' once it reaches 'max'. Resetting will cause the counter to go back to 'min'. This is the default 'direction' value.
	- 'down' - the counter will decrement its count, going back to 'max' once it reaches 'min'. Resetting will cause it to go back to 'max'. 
	- 'updown' - the counter starts with direction set to 'up' and switches direction each time it reaches 'max' and 'min'. Resetting will cause it to go back to 'min' regardless of its current direction.
	  
[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/counter.java)

* * *


#### I/O setup 
- Should have 2 inputs and any number of outputs.
- input 0 (inc): When set to high the counter output is incremented or decremented by 1.
- input 1 (reset): Sets the output of the counter to its initial state.
- input 2 (direction): Controls the counter direction. When on, the counter will count up. When off, the counter will count down.
All inputs are optional.

#### Sign text
1. `   counter   `
2. ` <max> `

or 
1. `   counter   `
2. ` <min> <max> `

or
1. `   counter   `
2. ` <min> <max> <direction> `

__Version history:__ Added to BasicCircuits 0.1