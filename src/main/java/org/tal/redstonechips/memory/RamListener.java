package org.tal.redstonechips.memory;

import org.tal.redstonechips.bitset.BitSet7;

/**
 * An interface for listening to Ram data changes.
 * 
 * @author Tal Eisenberg
 */
public interface RamListener {
    /**
     * Called whenever Ram data changes/
     * @param ram The affected Ram object.
     * @param address The address that was written to.
     * @param data The new data.
     */
    public void dataChanged(Ram ram, BitSet7 address, BitSet7 data);
}
