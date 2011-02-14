/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

/**
 *
 * @author Tal Eisenberg
 */
public interface TransmittingCircuit {
    public String getChannel();

    public void addReceiver(ReceivingCircuit receiver);

    public void removeReceiver(ReceivingCircuit receiver);
}
