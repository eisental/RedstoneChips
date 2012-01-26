package org.tal.redstonechips.memory;

import org.tal.redstonechips.bitset.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public interface RamListener {
    public void dataChanged(Ram ram, BitSet7 address, BitSet7 data);
}
