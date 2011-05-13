
package org.tal.redstonechips.channel;

import org.tal.redstonechips.circuit.Circuit;

/**
 * Represents a circuit tat can either transmit or receive wireless messages.
 *
 * @author Tal Eisenberg
 */
public abstract class WirelessCircuit extends Circuit {
    private int startBit;
    
    private BroadcastChannel channel = null;

    /**
     *
     * @return the broadcast channel used by this circuit.
     */
    public BroadcastChannel getChannel() { return channel; }

    /**
     *
     * @return the number of channel bits used by this circuit.
     */
    public abstract int getLength();

    /**
     *
     * @return the 1st channel bit used by this circuit.
     */
    public int getStartBit() {
        return startBit;
    }

    /**
     * Sets the 1st channel bit used by this circuit.
     * @param startBit Bit index.
     */
    public void setStartBit(int startBit) {
        this.startBit = startBit;
    }

    /**
     *
     * @param channelString
     * @throws IllegalArgumentException
     */
    protected void initWireless(String channelString) throws IllegalArgumentException {
        String name;
        int colonIdx = channelString.indexOf(":");
        if (colonIdx!=-1) {
            name = channelString.substring(0, colonIdx);
            try {
                startBit = Integer.decode(channelString.substring(colonIdx+1));
            } catch (NumberFormatException ne) {
                throw new IllegalArgumentException("Invalid channel start bit in " + channelString);
            }
        } else {
            name = channelString;
            startBit = 0;
        }

        if (this instanceof ReceivingCircuit)
            channel = redstoneChips.registerReceiver((ReceivingCircuit)this, name);
        else if (this instanceof TransmittingCircuit)
            channel = redstoneChips.registerTransmitter((TransmittingCircuit)this, name);
    }

}
