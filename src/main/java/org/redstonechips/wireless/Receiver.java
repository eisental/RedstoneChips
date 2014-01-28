package org.redstonechips.wireless;

import org.redstonechips.util.BooleanSubset;

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
    public abstract void receive(BooleanSubset bits);
    
}
