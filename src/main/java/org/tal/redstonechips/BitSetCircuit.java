package org.tal.redstonechips;




import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 * A Redstone Circuit that treats its inputs as multiple bit sets.
 * The word length of each set is the number of output bits.
 *
 * @author Tal Eisenberg
 */
public abstract class BitSetCircuit extends Circuit {
    protected BitSet[] inputBitSets;

    /**
     * The word length the circuit will deal with.
     */
    protected int wordlength;

    /**
     * If true, it is assumed that each output bit set has an additional carry bit at the end.
     * This means that each output set must have one more output pin than each input set.
     *
     * Not implemented yet.
     */
    protected boolean outputCarry; // TODO: implement outputCarry


    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        // determine which input bitset was changed

        // index in bitset
        int idxInBitSet = inIdx % wordlength;
        int bitSetIdx = (inIdx-idxInBitSet)/wordlength;
        BitSet changed = inputBitSets[bitSetIdx];
        changed.set(idxInBitSet, newLevel);
        bitSetChanged(bitSetIdx, changed);
    }

    /**
     * Called when the value of one input bit set has changed.
     *
     * @param bitSetIdx The index of the changed input bit set.
     * @param set The changed bit set.
     */
    protected abstract void bitSetChanged(int bitSetIdx, BitSet set);

    @Override
    public boolean init(Player player, String[] args) {
        // number of inputs must be an integer multiple of num of outputs
        if ((inputs.length % outputs.length)==0) {
            int inBitSetCount = inputs.length / outputs.length;
            wordlength = outputs.length;
            if (player!=null) player.sendMessage("BitSetCircuit with " + inBitSetCount + " inputs of " + wordlength + " bits each.");
            inputBitSets = new BitSet[inBitSetCount];
            for (int i=0; i<inBitSetCount; i++) {
                inputBitSets[i] = new BitSet(wordlength);
                inputBitSets[i].clear();
            }

            return true;
        } else {
            player.sendMessage("Invalid number of inputs (" + inputs.length + "). Number of inputs must be a multiple of the number of outputs.");
            return false;
        }

    }

    /**
     * Loads state input values to inputBitSets.
     * @param state
     */
    @Override
    public void loadState(Map<String, String> state) {
        inputBits = Circuit.loadBitSet(state, "inputBits", inputs.length);
        
        int curBit = 0;
        for (BitSet s : this.inputBitSets) {
            for (int i=0; i<wordlength; i++) {
                s.set(i, inputBits.get(curBit+i));
            }
            curBit += wordlength;
        }
    }

    /**
     * Saves state of all the input bits.
     * @return Map containing 'inputBits' key.
     */
    @Override
    public Map<String, String> saveState() {
        return Circuit.storeBitSet(new HashMap<String,String>(), "inputBits", inputBits, inputs.length);
    }
}
