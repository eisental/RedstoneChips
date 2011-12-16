
package org.tal.redstonechips.channel;

/**
 * Represents a transmitting wireless circuit.
 * 
 * @author Tal Eisenberg
 */
public abstract class TransmittingCircuit extends WirelessCircuit {
    @Override
    public void circuitShutdown() {
        if (this.getChannel()!=null) redstoneChips.getChannelManager().removeTransmitter(this);
    }
    
}
