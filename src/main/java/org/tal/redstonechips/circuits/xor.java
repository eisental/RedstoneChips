package org.tal.redstonechips.circuits;


import java.util.BitSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class xor extends BitSetCircuit {

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet set) {
        BitSet out = (BitSet)inputBitSets[0].clone();
        for (int i=1; i<this.inputBitSets.length; i++) {
            out.xor(inputBitSets[i]);
        }
        this.sendBitSet(out);
    }

}
