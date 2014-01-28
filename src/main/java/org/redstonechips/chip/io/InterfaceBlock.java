package org.redstonechips.chip.io;

import org.bukkit.Location;
import org.redstonechips.chip.Chip;

/**
 * Represents a chip interface block.
 * 
 * @author Tal Eisenberg
 */
public class InterfaceBlock extends IOBlock {

    public InterfaceBlock(Chip c, Location l, int index) {
        super(c, l, index);
    }
}
