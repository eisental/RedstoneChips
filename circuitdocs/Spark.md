---
layout: main
title: spark
---

Fires a lightning bolt over an interface block. The chip can have any number of input and interface block pairs. When one of the inputs is triggered, a lightning will fire over the corresponding interface block. 

When adding 'bin' as a sign argument, it is possible to set an interface block to fire by setting the chips data pins (inputs 2 and above) to the desired binary number and then triggering the 1st input pin.

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/spark.java)
    
* * *


#### I/O setup 
* The same number of input pins and interface blocks.
* In bin (binary) mode 1st input triggers the chip while the rest determine the interface block to fire.

#### Sign text
1. `   spark   `
2. `   bin     ` (optional)

__Version history:__ Added to SensorLibrary 0.3