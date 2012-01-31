package org.tal.redstonechips.wireless;

import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 * A Wireless transmitter.
 * 
 * @author Tal Eisenberg
 */
public class Transmitter extends Wireless {

    /**
     * Transmit a BitSet over the transmitter channel.
     * 
     * @param bits BitSet to send.
     * @param length Transmission bit length.
     */
    public void transmit(BitSet7 bits, int length) {
        getChannel().transmit(bits, startBit, length);
    }

    /**
     * Transmit one bit over the transmitter channel.
     * 
     * @param state a bit state.
     */
    public void transmit(boolean state) {
        getChannel().transmit(state, startBit);
    }

    /**
     * Transmit a BitSet over the transmitter channel.
     * 
     * @param bits BitSet to send.
     * @param offset A bit offset that's added to the transmitter start bit.
     * @param length Transmission bit length.
     */
    public void send(BitSet7 bits, int offset, int length) {
        getChannel().transmit(bits, startBit+offset, length); 
    }
    
    /**
     * Transmit an integer over the transmitter channel.
     * 
     * @param value Integer to send.
     * @param offset A bit offset that's added to the transmitter start bit.
     * @param length Transmission bit length.
     */
    public void send(int value, int offset, int length) {
        BitSet7 bits = BitSetUtils.intToBitSet(value, length);
        getChannel().transmit(bits, startBit+offset, length);
    }
    
    /**
     * Transmit one bit over one of the channel bits.
     * 
     * @param state A bit state.
     * @param offset The channel bit to transmit on.
     */
    public void send(boolean state, int offset) {
        getChannel().transmit(state, startBit+offset);
    }
    
    /**
     * Transmit a boolean bit array over the transmitter channel.
     * 
     * @param bits A boolean bit array.
     * @param offset A bit offset that's added to the transmitter start bit.
     * @param length Transmission bit length.
     */
    public void send(boolean[] bits, int offset, int length) {
        BitSet7 bitset = BitSetUtils.boolToBitSet(bits, 0, length);
        getChannel().transmit(bitset, startBit+offset, length);
    }
}
