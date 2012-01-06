package org.tal.redstonechips.wireless;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class Wireless {

    protected int startBit;    
    protected BroadcastChannel channel = null;
    protected int channelLength;
    
    private RedstoneChips rc;
    private Circuit circuit;
    
    public BroadcastChannel getChannel() { return channel; }

    public int getStartBit() { return startBit; }

    public void setStartBit(int startBit) {
        this.startBit = startBit;
    } 

    public void setChannelLength(int length) {
        channelLength = length;
    }
    
    public int getChannelLength() { return channelLength; }

    public Circuit getCircuit() { return circuit; }
    
    /**
     * Parses the channel string and registers the class as a wireless device.
     * 
     * @param sender The chip creator.
     * @param channelString The channel and optionally the starting bit to communicate on. Format: channel-name[:start-bit]
     * @throws IllegalArgumentException when channelString is invalid or the creator doesn't have channel permissions.
     */
    public void init(CommandSender sender, String channelString, int channelLength, Circuit c) throws IllegalArgumentException {
        String chanName;
        int colonIdx = channelString.indexOf(":");
        if (colonIdx!=-1) {
            chanName = channelString.substring(0, colonIdx);
            try {
                startBit = Integer.decode(channelString.substring(colonIdx+1));
            } catch (NumberFormatException ne) {
                throw new IllegalArgumentException("Invalid channel start bit in " + channelString);
            }
        } else {
            chanName = channelString;
            startBit = 0;
        }

        this.rc = c.getPlugin();
        this.circuit = c;
        
        if (!checkChanUserPermissions(sender, chanName)) {
            throw new IllegalArgumentException("You do not have permissions to use channel " + chanName + ".");
        }

        this.channelLength = channelLength;
        
        if (this instanceof Receiver) {
            channel = rc.getChannelManager().registerReceiver((Receiver)this, chanName);
        } else if (this instanceof Transmitter) {
            channel = rc.getChannelManager().registerTransmitter((Transmitter)this, chanName);
        }

        if (sender!=null) {
            String bits;
            if (this.getChannelLength()>1)
                bits = "bits " + ChatColor.YELLOW + this.getStartBit() + "-" + (this.getStartBit() + this.getChannelLength()-1);
            else bits = "bit " + ChatColor.YELLOW + this.getStartBit();

            sender.sendMessage(ChatColor.YELLOW + circuit.getClass().getSimpleName() + rc.getPrefs().getInfoColor() + " will " + 
                    (this instanceof Receiver?"listen on channel ":"broadcast over channel ") +
                    ChatColor.YELLOW + getChannel().name + rc.getPrefs().getInfoColor() + " " + bits + rc.getPrefs().getInfoColor() + ".");
        }        
    }
    
    private boolean checkChanUserPermissions(CommandSender sender, String name) {
        if (!(sender instanceof Player)) return true;
        
        if (!(rc.getChannelManager().getBroadcastChannels().containsKey(name))) return true;
        
        if (!(rc.getChannelManager().getChannelByName(name, false).isProtected())) return true;
        
        String playerName = ((Player)sender).getName();
        if (((Player)sender).hasPermission("redstonechips.channel.admin") || rc.getChannelManager().getChannelByName(name, false).users.contains(playerName.toLowerCase()) 
                || rc.getChannelManager().getChannelByName(name, false).owners.contains(playerName.toLowerCase())) {
            return true;
        }
        
        return false;
    }
 
    public void shutdown() {
        if (getChannel()!=null) {
            if (this instanceof Receiver) rc.getChannelManager().removeReceiver((Receiver)this);
            else if (this instanceof Transmitter) rc.getChannelManager().removeTransmitter((Transmitter)this);
        }
    }
}
