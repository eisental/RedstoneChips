package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.util.BitSet7;

/**
 * Represents a RedstoneChips circuit.
 *
 * @author Tal Eisenberg
 */
public abstract class Circuit {
    /**
     * Reference to the core plugin instance.
     */
    protected RedstoneChips redstoneChips;

    /**
     * Circuit sign arguments. Any word found on the circuit sign from line 2 onward.
     */
    public String[] args;

    /**
     * Input block locations. The circuit will listen to redstone change events around these blocks.
     */
    public Location[] inputs;

    /**
     * Output blocks. List of lever block locations.
     */
    public Location[] outputs;

    /**
     * Contains the location of any block that is part of this circuit. When any block in this array is broken the circuit is destroyed.
     * This includes the sign block, chip blocks, input blocks, output blocks and output lever blocks.
     */
    public Location[] structure;

    /**
     * Interface block locations. Used for interaction points with the "physical" world.
     */
    public Location[] interfaceBlocks;

    /**
     * The location of the sign block that was used to activate the circuit.
     */
    public Location activationBlock;

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
     * @param player The player that activated the circuit. Used for sending error or status messages after activation.
     * @param args The sign arguments of this circuit. Stored in the args field.
     * @return result of call to abstract Circuit.init() method.
     */
    public final boolean initCircuit(Player player, String[] args, RedstoneChips rc) {
        this.redstoneChips = rc;
        debuggers = new ArrayList<Player>();
        inputBits = new BitSet7(inputs.length);
        outputBits = new BitSet7(outputs.length);
        this.args = args;

        return init(player, args);
    }

    /**
     * Called by the plugin whenever a redstone change event is fired with a new redstone current on blocks surrounding or on top of 
     * a circuit input. If the new value is different from the corresponding value stored in inputBits the inputBits value is updated
     * and the inputChange(idx, newVal) method is called.
     *
     * @param block The block that changed current level.
     * @param newVal true if the current is greater than 0.
     */
    public void redstoneChange(int idx, boolean newVal) {
        if (inputBits.get(idx)==newVal) return;

        inputBits.set(idx, newVal);
        if (hasDebuggers()) debug("Input change: " + bitSetToBinaryString(inputBits, 0, inputs.length) + " (" + bitSetToUnsignedInt(inputBits, 0, inputs.length) + ")");
        inputChange(idx, newVal);
    }


    /**
     * Called when a redstone change event occurred on one of the circuit input blocks.
     *
     * @param inIdx index of changed input pin. 0 is closest to the sign block.
     * @param state The new state of the changed input.
     */
    public abstract void inputChange(int inIdx, boolean state);

    /**
     * Called when right-clicking the sign or when the plugin reads circuit data from file after restarting the server.
     * 
     * @param player The player that right-clicked the sign, or null if called on startup.
     * @param args Any words on the sign after the circuit type.
     * @return true if the init was successful, false if an error occurred.
     */
    protected abstract boolean init(Player player, String[] args);

    /**
     * Called when the plugin needs to save the circuits state to disk.
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
     * Causes the circuit to update the state of its output levers according to the current values
     * in outputBits.
     */
    public void updateOutputLevers() {
        for (int i=0; i<outputs.length; i++)
            changeLeverState(getOutputBlock(i), outputBits.get(i));
    }

    /**
     * Sets the physical state of one of the outputs.
     * Changes the data byte of the selected output block to make the lever turn on or off.
     *
     * @param outIdx Output index. 0 for first output (closest to the sign) and so forth.
     * @param state The new state of the output.
     */
    protected void sendOutput(int outIdx, boolean state) {
        if (outputBits.get(outIdx)==state) return; // nothing to update.

        outputBits.set(outIdx, state);
        changeLeverState(getOutputBlock(outIdx), state);
    }

    private void changeLeverState(Block lever, boolean level) {
        byte data = lever.getData();
        byte newData = (byte)(level? data | 0x8 : data & 0x7);

        try {
            lever.setData(newData);
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
     * @param startOutIdx First output pin that will be set.
     * @param length Number of bits to set.
     * @param bits The BitSet object to send out. Any excessive bits in the BitSet are ignored.
     */
    protected void sendBitSet(int startOutIdx, int length, BitSet7 bits) {
        for (int i=0; i<length; i++) {
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

    /**
     * Convert a BitSet to a binary representation string.
     * Converting each bit into "0" or "1".
     * Result is a binary number with its most significant bit on the left.
     * @param b BitSet to convert
     * @param startBit Start converting from this bit. Treat it as the least significant bit.
     * @param length Number of bits to read from the BitSet after the startBit.
     * @return Binary representation String of the BitSet.
     */
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


    /**
     * Stores a BitSet object as a string in a map. Used for state persistence.
     * Basically converts the BitSet into a string of "1" and "0" according to its bit values.
     *
     * @param map A Map object to store the bit set in.
     * @param key The map key in which to store the bit set.
     * @param bits The bit set that will be stored in the map.
     * @param length Number of bits to store.
     * @return The same map object as the parameter.
     */
    public static Map<String,String> storeBitSet(Map<String,String> map, String key, BitSet7 bits, int length) {
        String sbits = "";
        for (int i=0; i<length; i++)
            sbits += (bits.get(i)?"1":"0");

        map.put(key, sbits);
        return map;
    }

    /**
     * Parses a string representation of a BitSet into a BitSet7 object. Used for loading circuit state from file.
     * @param map The map to read the BitSet string from.
     * @param key The map key that points to the BitSet string.
     * @return The parsed BitSet7 object.
     */
    public static BitSet7 loadBitSet(Map<String, String> map, String key) {
        String sbits = map.get(key);
        BitSet7 bits = new BitSet7(sbits.length());
        if (sbits==null) return null;

        for (int i=0; i<sbits.length(); i++) {
            bits.set(i, (sbits.charAt(i)=='1'));
        }
        return bits;
    }

    /**
     * Useful method for posting error messages. Sends an error message to the requested player using the error chat color as
     * set in the preferences file. If player is null the message is sent to the console logger as a warning.
     * @param player The Player to send the message to.
     * @param message The error message.
     */
    protected void error(Player player, String message) {
        if (player!=null) player.sendMessage(redstoneChips.getPrefsManager().getErrorColor() + message);
        else Logger.getLogger("Minecraft").warning(redstoneChips.getDescription().getName() + ": " + this.getClass().getSimpleName() + "> " + message);
    }

    /**
     * Useful method for posting info messages. Sends an info message to the requested player using the info chat color as
     * set in the preferences file. If player is null the message is simply ignored.
     * @param player The Player to send the message to.
     * @param message The error message.
     */
    protected void info(Player player, String message) {
        if (player!=null) player.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + message);
        //else Logger.getLogger("Minecraft").info(redchips.getDescription().getName() + ": " + this.getClass().getSimpleName() + "> " + message);
    }

    /**
     * Sends a debug message to all debugging players of this circuit, using the debug chat color preferences key.
     * Please check that hasDebuggers() returns true before processing any debug messages.
     *
     * @param message The error message.
     */
    protected void debug(String message) {
        for (Player p : debuggers)
            p.sendMessage(redstoneChips.getPrefsManager().getDebugColor() + this.getClass().getSimpleName() + ": " + message);
    }

    /**
     * Adds the player as a debugger for the circuit.
     *
     * @param d The player to add.
     * @throws IllegalArgumentException If the player is already in the debuggers list.
     */
    public void addDebugger(Player d) throws IllegalArgumentException {
        if (debuggers.contains(d)) throw new IllegalArgumentException("You are already debugging this circuit.");
        debuggers.add(d);
    }

    /**
     * Removes the player from the debuggers list.
     *
     * @param d The player
     * @return
     * @throws IllegalArgumentException
     */
    public void removeDebugger(Player d) throws IllegalArgumentException {
        if (!debuggers.contains(d)) throw new IllegalArgumentException("You are not listed as a debugger of this circuit.");
        debuggers.remove(d);
    }

    /**
     * Checks if the circuit has any debuggers waiting for debug messages. This method should be used
     * before processing any debug information to avoid wasting cpu when no debuggers are listening.
     *
     * @return True if the circuit has any debuggers.
     */
    public boolean hasDebuggers() { return !debuggers.isEmpty(); }

    /**
     *
     * @return a clone of the outputBits field.
     */
    public BitSet7 getOutputBits() { return (BitSet7)outputBits.clone(); }

    /**
     *
     * @return a clone of the inputBits field.
     */
    public BitSet7 getInputBits() { return (BitSet7)inputBits.clone(); }

    /**
     *
     * @return The name of this circuit class, as it should be typed on the chip sign.
     */
    public String getCircuitClass() {
        return this.getClass().getSimpleName();
    }

    /**
     *
     * @return The circuit's debuggers list.
     */
    public List<Player> getDebuggers() {
        return debuggers;
    }

    /**
     * Used for getting a Block object for an output pin's lever.
     *
     * @param outputIdx The required output pin number.
     * @return The lever block of the specific output index.
     */
    protected Block getOutputBlock(int outputIdx) {
        Location l = outputs[outputIdx];
        return world.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    /**
     * Used for getting a Block object for an input pin's lever.
     *
     * @param inputIdx The required input pin number.
     * @return The input block (the iron block by default) of the specific input index.
     */
    protected Block getInputBlock(int inputIdx) {
        Location l = inputs[inputIdx];
        return world.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

}
