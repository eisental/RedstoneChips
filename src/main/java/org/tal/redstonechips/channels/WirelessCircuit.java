/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.channels;

import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class WirelessCircuit extends Circuit {
    private int startBit;
    
    private BroadcastChannel channel = null;

    public BroadcastChannel getChannel() { return channel; }

    public abstract int getLength();

    public int getStartBit() {
        return startBit;
    }

    public void setStartBit(int startBit) {
        this.startBit = startBit;
    }

    protected void parseChannelString(String channelString) throws IllegalArgumentException {
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
