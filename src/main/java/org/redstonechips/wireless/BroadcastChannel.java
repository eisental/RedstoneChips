package org.redstonechips.wireless;

import java.util.ArrayList;
import java.util.List;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.BooleanSubset;

/**
 * Represents a wireless broadcast channel that receivers and transmitters can communicate over.
 *
 * @author Tal Eisenberg
 */
public class BroadcastChannel {
    private final List<Wireless> devices = new ArrayList<>();
    private final List<Receiver> receivers = new ArrayList<>();
    /**
     * The channel identifying name.
     */
    public String name;

    /**
     * Contains the last broadcasted message.
     */
    public boolean[] bits = BooleanArrays.zero;

    /**
     * Channel owners when it's protected. 
     */
    public List<String> owners = new ArrayList<>();
    
    /**
     * Channel users when it's protected.
     */
    public List<String> users = new ArrayList<>();

    /**
     * Creates a new BroadcastChannel
     * @param channelName A unique channel name.
     */
    public BroadcastChannel(String channelName) {
        this.name = channelName;
    }

    /**
     * Adds a receiver to receive broadcasts on this channel.
     * @param w The receiving circuit.
     */
    public void addWirelessDevice(Wireless w) {
        if (devices.contains(w)) return;

        devices.add(w);
        if (w instanceof Receiver) receivers.add((Receiver)w);
        
        calcChannelLength();
    }
    
    /**
     * Stops the receiving circuit from receiving broadcasts on this channel.
     * @param w The receiving circuit.
     * @return true if the receiver was actually removed.
     */
    public boolean removeWirelessDevice(Wireless w) {
        boolean res = devices.remove(w);
        if (res && (w instanceof Receiver)) receivers.remove((Receiver)w);
        calcChannelLength();
        return res;

    }

    /**
     *
     * @return All receivers that are receiving broadcasts on this channel.
     */
    public List<Wireless> getWirelessDevices() {
        return devices;
    }

    private void calcChannelLength() {
        int clength = 0;
        for (Wireless w : devices)
            if (w.getStartBit()+w.getLength()>clength) clength = w.getStartBit()+w.getLength();
        updateChannelLength(clength);
    }

    private void updateChannelLength(int newlength) {
        if (newlength==bits.length) return;
        boolean[] newbits = new boolean[newlength];

        System.arraycopy(bits, 0, newbits, 0, Math.min(newlength, bits.length));
        bits = newbits;
    }
    
    /**
     *
     * @return The number of bits used by the channel. This value automatically changes according to attached
     * receivers and transmitters.
     */
    public int getLength() {
        return bits.length;
    }

    /**
     * Transmits a message over this channel to all its receivers.
     * @param tbits The bits to transmit
     * @param start The channel bit the message will start at.
     * @param length The length of the message.
     */
    public void transmit(boolean[] tbits, int start, int length) {
        System.arraycopy(tbits, 0, bits, start, length);

        for (Receiver r : receivers) {
            transmitToReceiver(r, start, length);
        }
    }

    /**
     * Transmits 1 bit over this channel to all receivers.
     * The transmission can set any bit of the channel bit set. To set the 1st bit, index should be 0.
     * 
     * @param bit The bit to transmit.
     * @param index Index of the bit in the channel bit set.
     */
    public void transmit(boolean bit, int index) {
        if (index>=bits.length) return;
        
        bits[index] = bit;

        for (Receiver r : receivers) {
            transmitToReceiver(r, index, 1);
        }
    }

    /**
     * Transmit a {@link org.redstonechips.util.BooleanSubset BooleanSubset} over this channel to all receivers.
     * 
     * @param subset The subset to transmit.
     * @param start The index of the first channel bit that will be modified.
     */
    public void transmitSubset(BooleanSubset subset, int start) {
        int len = subset.copyInto(bits, start);

        for (Receiver r :receivers) {
            transmitToReceiver(r, start, len);
        }
    }
    
    /**
     *
     * @return true if the channel has no registered circuits.
     */
    public boolean isDeserted() {
        return devices.isEmpty();
    }

    private void transmitToReceiver(Receiver r, int startBit, int length) {
        if (r.getLength()!=0) {
            // only send to receiver if the change is in its bit range.
            if ((startBit>=r.getStartBit() && startBit<r.getStartBit()+r.getLength()) ||
                    (startBit<r.getStartBit() && startBit+length>r.getStartBit())) {
                r.receive(new BooleanSubset(bits, r.getStartBit(), r.getLength()));
            }
                        
        }
    }

    /**
     * Sends the receiving circuit all current channel bit values.
     * @param r A receiving circuit.
     */
    public void updateReceiver(Receiver r) {
        transmitToReceiver(r, 0, bits.length);
    }
    
    public boolean isProtected() {
        return !owners.isEmpty() || !users.isEmpty();
    }    

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public List<Transmitter> getTransmitters() {
        List<Transmitter> ts = new ArrayList<>();
        
        for (Wireless w : devices) if (w instanceof Transmitter) ts.add((Transmitter)w);
        return ts;
    }
}
