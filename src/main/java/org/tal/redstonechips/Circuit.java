package org.tal.redstonechips;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Represents a redstone integrated circuit.
 *
 * @author Tal Eisenberg
 */
public abstract class Circuit {

    /**
     * Circuit sign arguments. Any word found on the circuit sign from line 2 onward.
     */
    public String[] args;

    /**
     * Input blocks. Starting count from sign block.
     */
    public Block[] inputs;

    /**
     * Output blocks. Starting count from sign block.
     */
    public Block[] outputs;

    /**
     * Contains any block that is part of this circuit. When any block is broken the circuit is destroyed.
     * This includes the sign block, body blocks, input blocks, output blocks and output lever blocks.
     */
    public Block[] structure;

    /**
     * The sign block. Used to activate the circuit.
     */
    public Block activationBlock;

    /**
     * The last block in the line. Can be used to find where the chip ends.
     */
    public Block lastLineBlock;

    /**
     * One block further in the line after the lastLineBlock. This block is not part of the circuit
     * and can be used as physical output such as changing sign text, wool color, etc.
     */
    public Block outputBlock;

    /**
     * The World this circuit was built in.
     */
    public World world;

    protected BitSet inputBits, outputBits;

    /**
     *
     * @param player The player that activated the circuit. Used for sending error or status messages when activating.
     * @param args The sign arguments of this circuit. Stored in this.args.
     * @return result of call to Circuit.init()
     */
    public final boolean initCircuit(Player player, String[] args) {
        inputBits = new BitSet(inputs.length);
        outputBits = new BitSet(outputs.length);
        this.args = args;

        return init(player, args);
    }

    /**
     * Called by the plugin whenever a redstone change event is fired with a new redstone current.
     * If block is an input of this circuit, the new value is stored in inputBits bit set
     * and Circuit.inputChange() method is called.
     * @param block The block that changed current level.
     * @param newVal true if the current is greater than 0.
     * @return true if block is an input of this circuit.
     */
    public boolean redstoneChange(Block block, boolean newVal) {
        for (int i=0; i<inputs.length; i++) {
            if (block.equals(inputs[i])) {
                inputBits.set(i, newVal);
                inputChange(i, newVal);
                return true;
            }
        }

        return false;
    }


    /**
     * Called when a redstone change event occurred on one of the circuit input blocks.
     *
     * @param inIdx index of changed input pin. 0 is closest to the sign block.
     * @param newLevel new level of the changed input.
     */
    public abstract void inputChange(int inIdx, boolean newLevel);

    /**
     * Called when right-clicking the sign or when the plugin read circuit data from file.
     * @param player The player that right-clicked the sign
     * @param args Any words on the sign after the circuit type.
     * @return true if the init was successful, false if an error occurred.
     */
    protected abstract boolean init(Player player, String[] args);

    /**
     * Called when the plugin needs to save circuits to disk.
     * The circuit should return a map containing any data needed to bring the circuit back to its current state
     * after a server restart.
     *
     * @return Map containing state data.
     */
    public Map<String,String> saveState() { return new HashMap<String,String>(); }

    /**
     * Called when the plugin loads a circuit from disk after a server restart.
     *
     * @param state Map containing state data that was read from file. should hold the same data that was returned by saveState()
     */
    public void loadState(Map<String,String> state) {}

    /**
     * Called when the circuit is physically destroyed.
     * Put here any necessary cleanups.
     */
    public void circuitDestroyed() {}

    /**
     * Sets the physical state of one of the outputs.
     * Changes the data byte of the selected output block to make a lever on or off.
     *
     * @param outIdx Output index. 0 for first output (closest to the sign) and so forth.
     * @param on The new state of the output.
     */
    protected void sendOutput(int outIdx, boolean on) {
        outputBits.set(outIdx, on);
        byte data = outputs[outIdx].getData();
        outputs[outIdx].setData((byte)(on?8|data:data&7));
    }

    /**
     * Send an integer through a set of outputs.
     * First converts to BitSet by calling intToBitSet(), then calls sendBitSet().
     *
     * @param startOutIdx output index of first output (LSB).
     * @param length number of bits/outputs to write to.
     * @param value The integer value to send out.
     */
    protected void sendInt(int startOutIdx, int length, int value) {
        BitSet bits = intToBitSet(value, length);
        sendBitSet(startOutIdx, length, bits);
    }

    /**
     * Sends a BitSet object to the circuit outputs.
     *
     * @param startOutIdx First output index
     * @param length Number of bits to send out.
     * @param bits The BitSet object to send out. Any excessive bits in the BitSet is ignored.
     */
    protected void sendBitSet(int startOutIdx, int length, BitSet bits) {        
        for (int i=length-1; i>=0; i--) {
            sendOutput(startOutIdx+i, bits.get(i));
        }
    }

    /**
     * Sends a BitSet object to the circuit outputs.
     * Sends a bit to each circuit output, starting from output 0.
     *
     * @param bits BitSet object to send out.
     */
    protected void sendBitSet(BitSet bits) {
        sendBitSet(0, outputs.length, bits);
    }

    /**
     * Convert a BitSet to an unsigned integer.
     *
     * @param b BitSet to convert.
     * @param startBit LSB bit of the integer.
     * @param length Number of bits to read.
     * @return an unsigned integer number.
     */
    protected static int bitSetToUnsignedInt(BitSet b, int startBit, int length) {
        int val = 0;
        for (int i=0; i<length; i++) {
            if (b.get(i+startBit)) val += Math.pow(2,i);
        }
        
        return val;
    }

    /**
     * Convert a BitSet to a signed integer using two's complement encoding.
     *
     * @param b BitSet to convert.
     * @param startBit LSB bit of the integer.
     * @param length Number of bits to read.
     * @return a signed integer number.
     */
    protected static int bitSetToSignedInt(BitSet b, int startBit, int length) {
        // treats the bit set as a two's complement encoding binary number.
        int signed = -(b.get(startBit+length-1)?1:0) * (int)Math.pow(2, length-1);
        for (int i=0; i<length-1; i++) {
            if (b.get(startBit+i)) signed += Math.pow(2, i);
        }

        return signed;
    }

    /**
     * Shifts bits of a BitSet object one place to the left.
     * Stores new value in the same BitSet.
     *
     * @param s Shifted BitSet object.
     * @param length Number of bits to shift.
     * @return BitSet s after shifting.
     */
    protected static BitSet shiftLeft(BitSet s, int length) {
        for (int i=length; i>0; i--) {
            s.set(i, s.get(i-1));
        }
        s.set(0, false);

        return s;
    }

    /**
     * Shifts bits of a BitSet object one place to the right.
     * Stores new value in the same BitSet.
     *
     * @param s Shifted BitSet object.
     * @param length Number of bits to shift.
     * @param logical true for logical right shift; false for arithmetic right shift.
     * @return BitSet s after shifting.
     */
    protected static BitSet shiftRight(BitSet s, int length, boolean logical) {
        for (int i=0; i<length-1; i++) {
            s.set(i, s.get(i+1));
        }

        if (logical) s.set(length-1, 0);

        return s;
    }

    /**
     * Stores an integer number in a BitSet object.
     *
     * @param value integer number to store
     * @param length number of bits to use.
     * @return
     */
    protected static BitSet intToBitSet(int value, int length) {
        BitSet bits = new BitSet(length);
        int index = 0;
        while (value != 0) {
            if (value % 2 != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        
        return bits;
    }


    public static Map<String,String> storeBitSet(Map<String,String> map, String key, BitSet bits, int length) {
        String sbits = "";
        for (int i=0; i<length; i++)
            sbits += (bits.get(i)?"1":"0");

        map.put(key, sbits);
        return map;
    }

    public static BitSet loadBitSet(Map<String, String> map, String key, int length) {
        BitSet bits = new BitSet(length);
        String sbits = map.get(key);
        if (sbits==null) return null;

        for (int i=0; i<length; i++) {
            bits.set(i, (sbits.charAt(i)=='1'));
        }
        return bits;
    }
}
