---
layout: main
title: beacon
---

The beacon is a circuit for controlling and monitoring the load/unload behavior of a chunk or a range of chunks. 
When at least 1 chunk in the beacon's range is loaded the beacon transmits 1 over the specified broadcast channel.
When all chunks in the beacon's range are unloaded the beacon transmits 0 over the broadcast channel.

When the beacon has an input pin, setting it on will cause it to try to keep each chunk in its radius constantly loaded. When the pin is set to off all chunks in range will go back to normal load/unload behavior.

By default the beacon will monitor the chunk where the circuit's interface block is in. When the optional `radius` argument is used the beacon will monitor
all chunks in the specified radius. 

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/beacon.java)
    
* * *


#### I/O setup 
* 1 interface block.
* 1 optional keepalive input pin.

#### Sign text
1. `   beacon   `
2. `   <broadcast channel>   ` 
3. `   [radius]   ` (optional, defaults to 0)

__Version history:__ Added to SensorLibrary 0.25