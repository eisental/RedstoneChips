
package org.tal.redstonechips.channel;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        if (!checkChanUserPermissions(sender, name)) {
            throw new IllegalArgumentException("You do not have permissions to use channel " + name + ".");
        }

        if (this instanceof ReceivingCircuit) {
            channel = redstoneChips.getChannelManager().registerReceiver((ReceivingCircuit)this, name);
            if (sender!=null) {
                String bits;
                if (this.getChannelLength()>1)
                    bits = "bits " + this.getStartBit() + "-" + (this.getStartBit() + this.getChannelLength()-1);
                else bits = "bit " + this.getStartBit();

                info(sender, this.getCircuitClass() + " will listen on channel " +
                        ChatColor.YELLOW + getChannel().name + redstoneChips.getPrefs().getInfoColor() + " " + bits + ".");
            }
        } else if (this instanceof TransmittingCircuit) {
            channel = redstoneChips.getChannelManager().registerTransmitter((TransmittingCircuit)this, name);
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
    
    private boolean checkChanUserPermissions(CommandSender sender, String name) {
        if (!(sender instanceof Player)) return true;
        
        if (!(redstoneChips.getChannelManager().getBroadcastChannels().containsKey(name))) return true;
        
        if (!(redstoneChips.getChannelManager().getChannelByName(name, false).isProtected())) return true;
        
        String playerName = ((Player)sender).getName();
        if (((Player)sender).hasPermission("redstonechips.channel.admin") || redstoneChips.getChannelManager().getChannelByName(name, false).users.contains(playerName.toLowerCase()) 
                || redstoneChips.getChannelManager().getChannelByName(name, false).owners.contains(playerName.toLowerCase())) {
            return true;
        }
        
        return false;
    }
}
