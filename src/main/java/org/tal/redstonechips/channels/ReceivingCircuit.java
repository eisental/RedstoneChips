
package org.tal.redstonechips.channels;

import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a circuit that can receive bit sets over a broadcast channel.
 *
 * @author Tal Eisenberg
 */
public abstract class ReceivingCircuit extends WirelessCircuit {
    public abstract void receive(BitSet7 bits);
}
