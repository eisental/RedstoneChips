package org.tal.redstonechips.channels;

import java.util.ArrayList;
import java.util.List;
import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a wireless broadcast channel which receivers and transmitters can communicate over.
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
        receivers.add(r);
        if (r.getStartBit()+r.getLength()>length)
            length = r.getStartBit()+r.getLength();        
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
        transmitters.add(t);
        if (t.getStartBit()+t.getLength()>length)
            length = t.getStartBit()+t.getLength();

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
            if (t.getStartBit()+t.getLength()>length) length = t.getStartBit()+t.getLength();

        for (ReceivingCircuit r : receivers)
            if (r.getStartBit()+r.getLength()>length) length = r.getStartBit()+r.getLength();

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
        for (int i=startBit; i<startBit+length; i++)
            bits.set(i, tbits.get(i-startBit));

        for (ReceivingCircuit r : receivers) {
            transmitToReceiver(r);
        }
    }

    /**
     *
     * @return true if the channel has no registered circuits.
     */
    public boolean isDeserted() {
        return receivers.isEmpty() && transmitters.isEmpty();
    }

    private void transmitToReceiver(ReceivingCircuit r) {
        if (r.getLength()!=0)
            r.receive(bits.get(r.getStartBit(), r.getStartBit()+r.getLength()));
    }
}
