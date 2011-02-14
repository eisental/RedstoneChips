/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a circuit that can receive bit sets over a broadcast channel.
 *
 * @author Tal Eisenberg
 */
public interface ReceivingCircuit {

    public void receive(BitSet7 bits);

    public String getChannel();
}
