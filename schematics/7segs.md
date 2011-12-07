---
layout: main
title: 7-segments display
---

![7-segment digits of a digital clock](/images/rcdigit6.png)
A simple 7-segment wool display. Made of 7 [pixel](/circuitdocs/Pixel.html) chips, a [segdriver](/circuitdocs/Segdriver.html) and a [receiver](/circuitdocs/Receiver.html).

Send a 4-bit number over the receiver channel in order to display it on the digit. You should probably change the receiver channel to something more original than "digit".
To display numbers with more than 1 digit use a [bintobcd](/circuitdocs/Bintobcd.html) chip.

###[Download .schematic](7segdigit.schematic)

* * *

![](rcdigit1.png)
![](rcdigit2.png)
![](rcdigit3.png)
![](rcdigit4.png)
![](rcdigit5.png)