---
layout: main
title: pirsensor
---

a PIR (Passive Infra-red) heat sensor for detecting living entities within a radius.
Once the circuit's clock pin is triggered, its output alarm pin will turn on if a living entity is within the selected radius (a 10 blocks sphere around the interface block by default) and off if no living entities were detected.

An optional sensitivity sign argument can be used to set the detection radius in blocks.

Use the `cube` argument to set the detection area shape to a cube instead of a sphere.

To specify any number of detectable entity classes add their names (these are case-sensitive) as arguments.
Some examples of entity classes may include:

- Animals
- Arrow
- Cow
- Creature
- Egg
- LivingEntity
- NPC
- Player
- Projectile

For a full list click [here](http://jd.bukkit.org/doxygen/d5/d27/namespaceorg_1_1bukkit_1_1entity.html)

on [Wikipedia](http://en.wikipedia.org/wiki/PIR_sensor)

[source code](https://github.com/eisental/SensorLibrary/blob/master/src/main/java/org/tal/sensorlibrary/pirsensor.java)
    
* * *

#### I/O setup 
* 1 clock input pin.
* 1 alarm output pin.

#### Sign text
1. `   pirsensor   `
2. `   [sensitivity]   ` (optional, defaults to 10 blocks radius)
4. `   [cube]   ` (optional, defaults to a sphere)
3. `   [entity class]...[entity class]` (optional, defaults to LivingEntity)

__Version history:__ Added to SensorLibrary 0.1