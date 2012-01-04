
package org.tal.redstonechips.channel;

import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a circuit that can receive bit sets over a broadcast channel.
 *
 * @author Tal Eisenberg
 */
public abstract class ReceivingCircuit extends WirelessCircuit {

    /**
     * Called when the receiving circuit receives a transmission.
     * @param bits Transmitted bits.
     */
    public abstract void receive(BitSet7 bits);
    
    /**
     * unregisters the receiver.
     */
    @Override
    public void circuitShutdown() {
        if (getChannel()!=null) redstoneChips.getChannelManager().removeReceiver(this);
    }
    
}
