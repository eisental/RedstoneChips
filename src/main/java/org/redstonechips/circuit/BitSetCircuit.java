package org.redstonechips.circuit;

/**
 * A Redstone Circuit that treats its inputs as multiple bit sets.
 * The word length of each set is determined by the number of output bits.
 *
 * @author Tal Eisenberg
 */
public abstract class BitSetCircuit extends Circuit {
    protected boolean[][] inputBitSets;

    /**
     * The word length the circuit will deal with.
     */
    protected int wordlength;

    @Override
    public void input(boolean state, int inIdx) {
        // determine which input bitset was changed

        // index in bitset
        int idxInBitSet = inIdx % wordlength;
        int bitSetIdx = (inIdx-idxInBitSet)/wordlength;
        boolean[] changed = inputBitSets[bitSetIdx];
        changed[idxInBitSet] = state;
        bitSetChanged(bitSetIdx, changed);
    }

    /**
     * Called when the value of one input bit set has changed.
     *
     * @param bitSetIdx The index of the changed input bit set.
     * @param set The changed bit set.
     */
    protected abstract void bitSetChanged(int bitSetIdx, boolean[] set);

    @Override
    public Circuit init(String[] args) {
        // number of inputs must be an integer multiple of num of outputs
        if (outputlen==0) return error("Expecting at least 1 output pin.");

        if ((inputlen % outputlen)==0) {
            int inBitSetCount = inputlen / outputlen;
            wordlength = outputlen;
            info("Activating " + chip.getType() + " with " + inBitSetCount + " input(s) of " + wordlength + " bits each.");
            inputBitSets = new boolean[inBitSetCount][wordlength];
            return this;
        } else {
            return error("Invalid number of inputs (" + inputlen + "). Number of inputs must be a multiple of the number of outputs.");
        }
    }    
}
