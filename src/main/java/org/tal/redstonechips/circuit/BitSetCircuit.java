package org.tal.redstonechips.circuit;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.util.BitSet7;

/**
 * A Redstone Circuit that treats its inputs as multiple bit sets.
 * The word length of each set is determined by the number of output bits.
 *
 * @author Tal Eisenberg
 */
public abstract class BitSetCircuit extends Circuit {
    protected BitSet7[] inputBitSets;

    /**
     * The word length the circuit will deal with.
     */
    protected int wordlength;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        // determine which input bitset was changed

        // index in bitset
        int idxInBitSet = inIdx % wordlength;
        int bitSetIdx = (inIdx-idxInBitSet)/wordlength;
        BitSet7 changed = inputBitSets[bitSetIdx];
        changed.set(idxInBitSet, newLevel);
        bitSetChanged(bitSetIdx, changed);
    }

    /**
     * Called when the value of one input bit set has changed.
     *
     * @param bitSetIdx The index of the changed input bit set.
     * @param set The changed bit set.
     */
    protected abstract void bitSetChanged(int bitSetIdx, BitSet7 set);

    @Override
    public boolean init(CommandSender sender, String[] args) {
        // number of inputs must be an integer multiple of num of outputs
        if (outputs.length==0) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }

        if ((inputs.length % outputs.length)==0) {
            int inBitSetCount = inputs.length / outputs.length;
            wordlength = outputs.length;
            info(sender, "Creating a bit-set circuit with " + inBitSetCount + " input(s) of " + wordlength + " bits each.");
            inputBitSets = new BitSet7[inBitSetCount];
            for (int i=0; i<inBitSetCount; i++) {
                inputBitSets[i] = new BitSet7(wordlength);
                inputBitSets[i].clear();
            }

            return true;
        } else {
            error(sender, "Invalid number of inputs (" + inputs.length + "). Number of inputs must be a multiple of the number of outputs.");
            return false;
        }

    }
}
