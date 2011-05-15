
package org.tal.redstonechips.channel;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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
    public abstract int getChannelLength();

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
    protected void initWireless(CommandSender sender, String channelString) throws IllegalArgumentException {
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

        if (this instanceof ReceivingCircuit) {
            channel = redstoneChips.registerReceiver((ReceivingCircuit)this, name);
            if (sender!=null) {
                String bits;
                if (this.getChannelLength()>1)
                    bits = "bits " + this.getStartBit() + "-" + (this.getStartBit() + this.getChannelLength()-1);
                else bits = "bit " + this.getStartBit();

                info(sender, this.getCircuitClass() + " will listen on channel " +
                        ChatColor.YELLOW + getChannel().name + redstoneChips.getPrefs().getInfoColor() + " " + bits + ".");
            }
        } else if (this instanceof TransmittingCircuit) {
            channel = redstoneChips.registerTransmitter((TransmittingCircuit)this, name);
            if (sender!=null) {
                String bits;
                if (this.getChannelLength()>1)
                    bits = "bits " + this.getStartBit() + "-" + (this.getStartBit() + this.getChannelLength()-1);
                else bits = "bit " + this.getStartBit();
                
                info(sender, this.getCircuitClass() + " will broadcast over channel " + 
                        ChatColor.YELLOW + getChannel().name + redstoneChips.getPrefs().getInfoColor() + " " + bits + ".");
            }
        }
    }

}
