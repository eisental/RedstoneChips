package org.tal.redstonechips.wireless;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.Circuit;

/**
 * A Wireless device for sending or receiving over a BroadcastChannel.
 * 
 * @author Tal Eisenberg
 */
public abstract class Wireless {

    /** The 1st channel bit this Wireless will send or receive on. */
    protected int startBit;
    
    /** BroadcastChannel used by this Wireless object. */
    protected BroadcastChannel channel = null;
    
    /** The Wireless channel length in bits. */
    protected int channelLength;
    
    private RedstoneChips rc;
    private Circuit circuit;
    
    /**
     * @return The BoradcastChannel used by this Wireless object.
     */
    public BroadcastChannel getChannel() { return channel; }

    /**
     * @return The 1st channel bit this Wireless will send or receive on.
     */
    public int getStartBit() { return startBit; }

    /**
     * Sets the Wireless object 1st channel bit.
     * 
     * @param startBit 
     */
    public void setStartBit(int startBit) {
        this.startBit = startBit;
    } 

    /**
     * Sets the Wireless channel length in bits. This is the maximum number of bits the Wireless will use
     * and doesn't necessarily needs to be the same as the BroadcastChannel length.
     * @param length 
     */
    public void setChannelLength(int length) {
        channelLength = length;
    }
    
    /**
     * @return The Wireless channel length.
     */
    public int getChannelLength() { return channelLength; }

    /**
     * @return The circuit that uses this Wireless object.
     */
    public Circuit getCircuit() { return circuit; }
    
    /**
     * Parses the channel string and registers the class as a wireless device.
     * 
     * @param sender The chip creator.
     * @param channelString The channel and optionally the starting bit to communicate on. Format: channel-name[:start-bit]
     * @param channelLength
     * @param c
     * @throws IllegalArgumentException when channelString is invalid or the creator doesn't have channel permissions.
     */
    public void init(CommandSender sender, String channelString, int channelLength, Circuit c) throws IllegalArgumentException {
        String chanName;
        
        if (channelString.length()==0) throw new IllegalArgumentException("Empty channel name.");
        
        if (channelString.charAt(0)=='#') {
            channelString = channelString.substring(1);
            if (channelString.length()==0) throw new IllegalArgumentException("Empty channel name.");
        }
       
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
        return (((Player)sender).hasPermission("redstonechips.channel.admin") || rc.getChannelManager().getChannelByName(name, false).users.contains(playerName.toLowerCase()) 
                || rc.getChannelManager().getChannelByName(name, false).owners.contains(playerName.toLowerCase()));
    }
 
    /**
     * Unregister and shuts down the Wireless.
     */
    public void shutdown() {
        if (getChannel()!=null) {
            if (this instanceof Receiver) rc.getChannelManager().removeReceiver((Receiver)this);
            else if (this instanceof Transmitter) rc.getChannelManager().removeTransmitter((Transmitter)this);
        }
    }
}
