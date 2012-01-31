package org.tal.redstonechips.wireless;

import org.tal.redstonechips.bitset.BitSet7;

/**
 * A Wireless Receiver.
 * 
 * @author Tal Eisenberg
 */
public abstract class Receiver extends Wireless {
    
    /**
     * Called when the receives receives a message.
     * @param bits Transmitted bits.
     */
    public abstract void receive(BitSet7 bits);
    
}
