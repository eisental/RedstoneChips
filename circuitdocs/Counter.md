counter
----------
 A counter chip of any bit-size (limited to 32bits at the moment). Outputs a binary number representing the current count value. 
The bit-size of the counter is set by the number of output pins. When the counter value overflows the number of output bits it goes back to 0.

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/counter.java)

#### I/O setup 
* Should have 2 inputs and any number of outputs.
* input 0 (inc): When set to high the counter output is incremented by 1.
* input 1 (reset): Sets the output of the counter to its initial state.

#### Sign text
1. `   counter   `

***
back to [Circuit Classes](Home)
