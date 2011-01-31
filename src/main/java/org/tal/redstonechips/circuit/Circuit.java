package org.tal.redstonechips.circuit;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a redstone integrated circuit.
 *
 * @author Tal Eisenberg
 */
public abstract class Circuit {
    /**
     * Reference to the core plugin instance.
     */
    private RedstoneChips redchips;

    /**
     * Circuit sign arguments. Any word found on the circuit sign from line 2 onward.
     */
    public String[] args;

    /**
     * Input blocks. Will listen to redstone change events on these blocks.
     */
    public Block[] inputs;

    /**
     * Output blocks. List of lever blocks.
     */
    public Block[] outputs;

    /**
     * Contains any block that is part of this circuit. When any block is broken the circuit is destroyed.
     * This includes the sign block, body blocks, input blocks, output blocks and output lever blocks.
     */
    public Block[] structure;

    /**
     * Interface blocks. Used for interaction points with the physical world.
     */
    public Block[] interfaceBlocks;

    /**
     * The sign block. Used to activate the circuit.
     */
    public Block activationBlock;

    /**
     * Reference to the minecraft World this circuit was built in.
     */
    public World world;

    /**
     * List of players that will receive debug messages from this circuit.
     */
    private List<Player> debuggers;

    /**
     * The current state of each input bit. Should be used only for monitoring. Do not change its value.
     */
    protected BitSet7 inputBits;

    /**
     * The current state of each output bit. Should be used only for monitoring. Do not change its value.
     */
    protected BitSet7 outputBits;

    /**
     *
     * @param player The player that activated the circuit. Used for sending error or status messages when activating.
     * @param args The sign arguments of this circuit. Stored in this.args.
     * @return result of call to Circuit.init()
     */
    public final boolean initCircuit(Player player, String[] args, RedstoneChips rc) {
        this.redchips = rc;
        debuggers = new ArrayList<Player>();
        inputBits = new BitSet7(inputs.length);
        outputBits = new BitSet7(outputs.length);
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
    public void redstoneChange(int idx, boolean newVal) {
        inputBits.set(idx, newVal);
        if (hasDebuggers()) debug("Input change: " + bitSetToBinaryString(inputBits, 0, inputs.length) + " (" + bitSetToUnsignedInt(inputBits, 0, inputs.length) + ")");
        inputChange(idx, newVal);
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
     * 
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
     * @param level The new state of the output.
     */
    protected void sendOutput(int outIdx, boolean level) {
        if (outputBits.get(outIdx)==level) return; // nothing to update.

        outputBits.set(outIdx, level);

        byte data = outputs[outIdx].getData();
        try {
            outputs[outIdx].setData((byte)(level ? 0x8 : data&0x7));
        } catch (ConcurrentModificationException me) {
            //Logger.getLogger("Minecraft").warning("We had another concurrent modification at sendoutput");
        }
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
        BitSet7 bits = intToBitSet(value, length);
        sendBitSet(startOutIdx, length, bits);
    }

    /**
     * Sends a BitSet object to the circuit outputs.
     *
     * @param startOutIdx First output index
     * @param length Number of bits to send out.
     * @param bits The BitSet object to send out. Any excessive bits in the BitSet is ignored.
     */
    protected void sendBitSet(int startOutIdx, int length, BitSet7 bits) {
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
    protected void sendBitSet(BitSet7 bits) {
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
    protected static int bitSetToUnsignedInt(BitSet7 b, int startBit, int length) {
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
    protected static int bitSetToSignedInt(BitSet7 b, int startBit, int length) {
        // treats the bit set as a two's complement encoding binary number.
        int signed = -(b.get(startBit+length-1)?1:0) * (int)Math.pow(2, length-1);
        for (int i=0; i<length-1; i++) {
            if (b.get(startBit+i)) signed += Math.pow(2, i);
        }

        return signed;
    }

    protected static String bitSetToBinaryString(BitSet7 b, int startBit, int length) {
        String ret = "";
        for (int i=length+startBit-1; i>=startBit; i--) ret += (b.get(i)?"1":"0");
        return ret;
    }

    /**
     * Shifts bits of a BitSet object one place to the left.
     * Stores new value in the same BitSet.
     *
     * @param s Shifted BitSet object.
     * @param length Number of bits to shift.
     * @return BitSet s after shifting.
     */
    protected static BitSet7 shiftLeft(BitSet7 s, int length) {
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
    protected static BitSet7 shiftRight(BitSet7 s, int length, boolean logical) {
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
    protected static BitSet7 intToBitSet(int value, int length) {
        BitSet7 bits = new BitSet7(length);
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


    public static Map<String,String> storeBitSet(Map<String,String> map, String key, BitSet7 bits, int length) {
        String sbits = "";
        for (int i=0; i<length; i++)
            sbits += (bits.get(i)?"1":"0");

        map.put(key, sbits);
        return map;
    }

    public static BitSet7 loadBitSet(Map<String, String> map, String key, int length) {
        BitSet7 bits = new BitSet7(length);
        String sbits = map.get(key);
        if (sbits==null) return null;

        for (int i=0; i<length; i++) {
            bits.set(i, (sbits.charAt(i)=='1'));
        }
        return bits;
    }

    protected void error(Player player, String message) {
        if (player!=null) player.sendMessage(redchips.getPrefsManager().getErrorColor() + message);
        else Logger.getLogger("Minecraft").warning(this.getClass().getSimpleName() + ": " + message);
    }

    protected void info(Player player, String message) {
        if (player!=null) player.sendMessage(redchips.getPrefsManager().getInfoColor() + message);
        else Logger.getLogger("Minecraft").info(this.getClass().getSimpleName() + ": " + message);
    }

    protected void debug(String message) {
        for (Player p : debuggers)
            p.sendMessage(redchips.getPrefsManager().getDebugColor() + this.getClass().getSimpleName() + ": " + message);
    }

    public void addDebugger(Player d) {
        if (debuggers.contains(d)) throw new IllegalArgumentException("You are already debugging this circuit.");
        debuggers.add(d);
    }

    public boolean removeDebugger(Player d) {
        if (!debuggers.contains(d)) throw new IllegalArgumentException("You are not listed as a debugger of this circuit.");
        return debuggers.remove(d);
    }

    public boolean hasDebuggers() { return !debuggers.isEmpty(); }

    public BitSet7 getOutputBits() { return (BitSet7)outputBits.clone(); }
    public BitSet7 getInputBits() { return (BitSet7)inputBits.clone(); }

    public String getCircuitClass() {
        return this.getClass().getSimpleName();
    }

    public Iterable<Player> getDebuggers() {
        return debuggers;
    }
}
