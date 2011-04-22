---
layout: main
title: bintobcd
---

A binary to BCD (binary-coded-decimals) encoder. 
Converts it's input binary number into any number of BCD digits. First the input binary number is converted into a decimal number, then
each digit is represented by 4 bits and sent to the chips outputs.

Example:
To convert a 6 bit binary number into 2 digits build a bintobcd chip with 6 inputs and 8 outputs. The 1st 4 outputs will output the least significant digit (the 1s)
and the other 4 outputs will output the most significant digit (the 10s). 
If the chip receives 100110 (38) it will output 0011 1000 - 2 digits, binary 3 and binary 8.

The bintobcd chip is very useful for sending numbers into a decimal display such as a 7 segment digit display. Each 4-bit digit output can be directly connected to a
[segdriver](Segdriver) chip to display a multiple digit number.

[on Wikipedia](http://en.wikipedia.org/wiki/Binary-coded_decimal)

[source code](https://github.com/eisental/BasicCircuits/blob/master/src/main/java/org/tal/basiccircuits/bintobcd.java)

* * *


#### I/O setup 
* At least 1 input pin.
* Any multiple of 4 output pins. 

#### Sign text
1. `   bintobcd   `

__Version history:__ Added to BasicCircuits 0.9