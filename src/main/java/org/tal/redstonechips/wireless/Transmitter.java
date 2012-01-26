package org.tal.redstonechips.wireless;

import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class Transmitter extends Wireless {

    public void transmit(BitSet7 bits, int length) {
        getChannel().transmit(bits, startBit, length);
    }

    public void transmit(boolean state) {
        getChannel().transmit(state, startBit);
    }

    public void send(BitSet7 bits, int offset, int length) {
        getChannel().transmit(bits, startBit+offset, length); 
    }
    
    public void send(int value, int offset, int length) {
        BitSet7 bits = BitSetUtils.intToBitSet(value, length);
        getChannel().transmit(bits, startBit+offset, length);
    }
    
    public void send(boolean state, int offset) {
        getChannel().transmit(state, startBit+offset);
    }
    
    public void send(boolean[] bits, int offset, int length) {
        BitSet7 bitset = BitSetUtils.boolToBitSet(bits, 0, length);
        getChannel().transmit(bitset, startBit+offset, length);
    }
}
