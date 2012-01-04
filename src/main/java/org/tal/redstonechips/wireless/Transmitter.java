/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips.wireless;

import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class Transmitter extends Wireless {

    public void transmit(BitSet7 bits, int length) {
        getChannel().transmit(bits, startBit, length);        
    }

    public void transmit(boolean b) {
        getChannel().transmit(b, startBit);
    }
    
}
