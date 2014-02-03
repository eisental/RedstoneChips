package org.redstonechips.wireless;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.RedstoneChips;
import org.redstonechips.circuit.Circuit;

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
    
    /** The number of bits this Wireless will send/receive with. */
    protected int length;
    
    private ChannelManager channelManager;
    
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
    public void setLength(int length) {
        this.length = length;
    }
    
    /**
     * @return The Wireless channel length.
     */
    public int getLength() { return length; }

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

        this.channelManager = RedstoneChips.inst().channelManager();
        this.circuit = c;
        
        if (!checkChanUserPermissions(sender, chanName)) {
            throw new IllegalArgumentException("You do not have permissions to use channel " + chanName + ".");
        }

        this.length = channelLength;
        
        if (this instanceof Receiver) {
            channel = channelManager.registerReceiver((Receiver)this, chanName);
        } else if (this instanceof Transmitter) {
            channel = channelManager.registerTransmitter((Transmitter)this, chanName);
        }

        if (sender!=null) {
            String bits;
            if (this.getLength()>1)
                bits = "bits " + ChatColor.YELLOW + this.getStartBit() + "-" + (this.getStartBit() + this.getLength()-1);
            else bits = "bit " + ChatColor.YELLOW + this.getStartBit();

            ChatColor infoColor = RCPrefs.getInfoColor();
            sender.sendMessage(ChatColor.YELLOW + circuit.getClass().getSimpleName() + infoColor + " will " + 
                    (this instanceof Receiver?"listen on channel ":"broadcast over channel ") +
                    ChatColor.YELLOW + getChannel().name + infoColor + " " + bits + " (" + this.getLength() + ") " + infoColor + ".");
        }        
    }
    
    private boolean checkChanUserPermissions(CommandSender sender, String name) {
        if (!(sender instanceof Player)) return true;
        
        if (!(channelManager.getBroadcastChannels().containsKey(name))) return true;
        
        if (!(channelManager.getChannelByName(name, false).isProtected())) return true;
        
        String playerName = ((Player)sender).getName();
        return (((Player)sender).hasPermission("redstonechips.channel.admin") || channelManager.getChannelByName(name, false).users.contains(playerName.toLowerCase()) 
                || channelManager.getChannelByName(name, false).owners.contains(playerName.toLowerCase()));
    }
 
    /**
     * Unregister and shuts down the Wireless.
     */
    public void shutdown() {
        if (getChannel()!=null) {
            channelManager.removeWireless(this);
        }
    }
}
