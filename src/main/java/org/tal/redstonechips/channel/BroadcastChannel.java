package org.tal.redstonechips.channel;

import java.util.ArrayList;
import java.util.List;
import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a wireless broadcast channel that receivers and transmitters can communicate over.
 *
 * @author Tal Eisenberg
 */
public class BroadcastChannel {
    private List<ReceivingCircuit> receivers = new ArrayList<ReceivingCircuit>();
    private List<TransmittingCircuit> transmitters = new ArrayList<TransmittingCircuit>();

    /**
     * The channel identifying name.
     */
    public String name;

    /**
     * Contains the last broadcasted message.
     */
    public BitSet7 bits = new BitSet7();

	/**
     * Security Information.
     */
	public List<String> owners = new ArrayList<String>();
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
    public void addReceiver(ReceivingCircuit r) {
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
    public boolean removeReceiver(ReceivingCircuit r) {
        boolean res = receivers.remove(r);
        calcChannelLength();
        return res;

    }

    /**
     * Adds a transmitter to transmit broadcasts on this channel.
     * @param r The transmitting circuit.
     */
    public void addTransmitter(TransmittingCircuit t) {
        if (transmitters.contains(t)) return;
        
        transmitters.add(t);
        if (t.getStartBit()+t.getChannelLength()>length)
            length = t.getStartBit()+t.getChannelLength();

    }

    /**
     * Stops the transmitting circuit from transmitting broadcasts on this channel.
     * @param r The transmitting circuit.
     * @return true if the transmitter was actually removed.
     */
    public boolean removeTransmitter(TransmittingCircuit t) {
        boolean res = transmitters.remove(t);
        calcChannelLength();
        return res;
    }

    /**
     *
     * @return All transmitters that are broadcasting on this channel.
     */
    public List<TransmittingCircuit> getTransmitters() {
        return transmitters;
    }

    /**
     *
     * @return All receivers that are receiving broadcasts on this channel.
     */
    public List<ReceivingCircuit> getReceivers() {
        return receivers;
    }

    private void calcChannelLength() {
        length = 0;
        for (TransmittingCircuit t : transmitters)
            if (t.getStartBit()+t.getChannelLength()>length) length = t.getStartBit()+t.getChannelLength();

        for (ReceivingCircuit r : receivers)
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

        for (ReceivingCircuit r : receivers) {
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

        for (ReceivingCircuit r : receivers) {
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

    private void transmitToReceiver(ReceivingCircuit r, int startBit, int length) {
        if (r.getChannelLength()!=0) {
            // only send to receiver if the change is in its bit range.
            if ((startBit>=r.getStartBit() && startBit<r.getStartBit()+r.getChannelLength()) ||
                    (startBit<r.getStartBit() && startBit+length>r.getStartBit()))
                        r.receive(bits.get(r.getStartBit(), r.getStartBit()+r.getChannelLength()));
        }
    }
	
	public boolean isProtected() {
		return !owners.isEmpty() || !users.isEmpty();
	}
}
