package org.tal.redstonechips.circuits;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public abstract class BitSetCircuit extends Circuit {
    protected BitSet[] inputBitSets;
    protected int bitCount;

    /**
     * If true, it is assumed that each output bit set has an additional carry bit at the end.
     * This means that each output set must have one more output pin than each input set.
     */
    protected boolean outputCarry; // TODO: implement outputCarry

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        // determine which input bitset was changed

        // index in bitset
        int idxInBitSet = inIdx % bitCount;
        int bitSetIdx = (inIdx-idxInBitSet)/bitCount;
        BitSet changed = inputBitSets[bitSetIdx];
        changed.set(idxInBitSet, newLevel);
        bitSetChanged(bitSetIdx, changed);
    }

    protected abstract void bitSetChanged(int bitSetIdx, BitSet set);

    @Override
    public boolean init(Player player, String[] args) {
        // number of inputs must be an integer multiple of num of outputs
        if ((inputs.length % outputs.length)==0) {
            int inBitSetCount = inputs.length / outputs.length;
            bitCount = outputs.length;
            if (player!=null) player.sendMessage("BitSetCircuit with " + inBitSetCount + " inputs of " + bitCount + " bits each.");
            inputBitSets = new BitSet[inBitSetCount];
            for (int i=0; i<inBitSetCount; i++) {
                inputBitSets[i] = new BitSet(bitCount);
                inputBitSets[i].clear();
            }

            return true;
        } else {
            player.sendMessage("Invalid number of inputs (" + inputs.length + "). Number of inputs must be a multiple of the number of outputs.");
            return false;
        }

    }

    @Override
    protected void loadState(Map<String, String> state) {
        inputBits = Circuit.loadBitSet(state, "inputBits", inputs.length);
        
        int curBit = 0;
        for (BitSet s : this.inputBitSets) {
            for (int i=0; i<bitCount; i++) {
                s.set(i, inputBits.get(curBit+i));
            }
            curBit += bitCount;
        }
    }

    @Override
    protected Map<String, String> saveState() {
        return Circuit.storeBitSet(new HashMap<String,String>(), "inputBits", inputBits, inputs.length);
    }
}
