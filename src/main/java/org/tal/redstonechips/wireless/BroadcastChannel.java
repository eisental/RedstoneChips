package org.tal.redstonechips.wireless;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.bitset.BitSet7;

/**
 * Represents a wireless broadcast channel that receivers and transmitters can communicate over.
 *
 * @author Tal Eisenberg
 */
public class BroadcastChannel {
    private final List<Receiver> receivers = new ArrayList<Receiver>();
    private final List<Transmitter> transmitters = new ArrayList<Transmitter>();
    
    /**
     * The channel identifying name.
     */
    public String name;

    /**
     * Contains the last broadcasted message.
     */
    public BitSet7 bits = new BitSet7();

    /**
     * Channel owners when it's protected. 
     */
    public List<String> owners = new ArrayList<String>();
    
    /**
     * Channel users when it's protected.
     */
    public List<String> users = new ArrayList<String>();

    private int length = 0;

    /**
     * Creates a new BroadcastChannel
     * @param channelName A unique channel name.
     */
    public BroadcastChannel(String channelName) {
        this.name = channelName;
    }

    /**
     * Adds a receiver to receive broadcasts on this channel.
     * @param r The receiving circuit.
     */
    public void addReceiver(final Receiver r) {
        if (receivers.contains(r)) return;

        receivers.add(r);
        if (r.getStartBit()+r.getChannelLength()>length)
            length = r.getStartBit()+r.getChannelLength();
    }
    
    /**
     * Stops the receiving circuit from receiving broadcasts on this channel.
     * @param r The receiving circuit.
     * @return true if the receiver was actually removed.
     */
    public boolean removeReceiver(Receiver r) {
        boolean res = receivers.remove(r);
        calcChannelLength();
        return res;

    }

    /**
     * Adds a transmitter to transmit broadcasts on this channel.
     * @param t The transmitting circuit.
     */
    public void addTransmitter(Transmitter t) {
        if (transmitters.contains(t)) return;
        
        transmitters.add(t);
        if (t.getStartBit()+t.getChannelLength()>length)
            length = t.getStartBit()+t.getChannelLength();

    }

    /**
     * Stops the transmitter from transmitting broadcasts on this channel.
     * @param t The transmitting circuit.
     * @return true if the transmitter was actually removed.
     */
    public boolean removeTransmitter(Transmitter t) {
        boolean res = transmitters.remove(t);
        calcChannelLength();
        return res;
    }

    /**
     *
     * @return All transmitters that are broadcasting on this channel.
     */
    public List<Transmitter> getTransmitters() {
        return transmitters;
    }

    /**
     *
     * @return All receivers that are receiving broadcasts on this channel.
     */
    public List<Receiver> getReceivers() {
        return receivers;
    }

    private void calcChannelLength() {
        length = 0;
        for (Transmitter t : transmitters)
            if (t.getStartBit()+t.getChannelLength()>length) length = t.getStartBit()+t.getChannelLength();

        for (Receiver r : receivers)
            if (r.getStartBit()+r.getChannelLength()>length) length = r.getStartBit()+r.getChannelLength();

    }

    /**
     *
     * @return The number of bits used by the channel. This value automatically changes according to attached
     * receivers and transmitters.
     */
    public int getLength() {
        return length;
    }

    /**
     * Transmits a message over this channel to all its receivers.
     * @param tbits The bits to transmit
     * @param startBit The channel bit the message will start at.
     * @param length The length of the message.
     */
    public void transmit(BitSet7 tbits, int startBit, int length) {
        for (int i=0; i<length; i++)
            bits.set(i+startBit, tbits.get(i));

        for (Receiver r : receivers) {
            transmitToReceiver(r, startBit, length);
        }
    }

    /**
     * Transmits 1 bit over this channel to all receivers.
     * The transmission can set any bit of the channel bit set. To set the 1st bit position should be 0.
     * 
     * @param bit The bit to transmit.
     * @param position Position of the bit in the channel's bitset.
     */
    public void transmit(boolean bit, int position) {
        bits.set(position, bit);

        for (Receiver r : receivers) {
            transmitToReceiver(r, position, 1);
        }
    }

    /**
     *
     * @return true if the channel has no registered circuits.
     */
    public boolean isDeserted() {
        return receivers.isEmpty() && transmitters.isEmpty();
    }

    private void transmitToReceiver(Receiver r, int startBit, int length) {
        if (r.getChannelLength()!=0) {
            // only send to receiver if the change is in its bit range.
            if ((startBit>=r.getStartBit() && startBit<r.getStartBit()+r.getChannelLength()) ||
                    (startBit<r.getStartBit() && startBit+length>r.getStartBit()))
                        r.receive(bits.get(r.getStartBit(), r.getStartBit()+r.getChannelLength()));
        }
    }

    /**
     * Sends the receiving circuit all current channel bit values.
     * @param r A receiving circuit.
     */
    public void updateReceiver(Receiver r) {
        transmitToReceiver(r, 0, length);
    }
    
    public boolean isProtected() {
        return !owners.isEmpty() || !users.isEmpty();
    }
    
    /**
     * 
     * @param sender
     * @param admin whether sender should be an admin/owner of this channel.
     * @return true if the sender have permissions.
     */
    public boolean checkChanPermissions(CommandSender sender, boolean admin) {
        if (!(sender instanceof Player)) return true;
        
        if (owners.isEmpty() && users.isEmpty()) return true;
        
        String playerName = ((Player)sender).getName();
        if (((Player)sender).hasPermission("redstonechips.channel.admin") || owners.contains(playerName.toLowerCase()) || (admin?false:users.contains(playerName.toLowerCase()))) {
            return true;
        }
        
        return false;
    }
}
