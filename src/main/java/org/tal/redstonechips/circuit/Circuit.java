package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

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
    public InputPin[] inputs;

    /**
     * Output blocks. List of lever block locations.
     */
    public BlockVector[] outputs;

    /**
     * Contains the location of any block that is part of this circuit. When any block in this array is broken the circuit is destroyed.
     * This includes the sign block, chip blocks, input blocks, output blocks and output lever blocks.
     */
    public BlockVector[] structure;

    /**
     * Interface block locations. Used for interaction points with the "physical" world.
     */
    public BlockVector[] interfaceBlocks;

    /**
     * The location of the sign block that was used to activate the circuit.
     */
    public BlockVector activationBlock;

    /**
     * Reference to the minecraft World this circuit was built in.
     */
    public World world;

    /**
     * List of command senders that will receive debug messages from this circuit.
     */
    private List<CommandSender> debuggers;

    /**
     * The current state of each input bit. Should be used only for monitoring. Do not change its value.
     */
    protected BitSet7 inputBits;

    /**
     * The current state of each output bit. Should be used only for monitoring. Do not change its value.
     */
    protected BitSet7 outputBits;

    /**
     * When set to true any input changes will be ignored.
     */
    protected boolean inputsDisabled = false;

    /**
     *
     * @param sender The sender that activated the circuit. Used for sending error or status messages after activation.
     * @param args The sign arguments of this circuit. Stored in the args field.
     * @return result of call to abstract Circuit.init() method.
     */
    public final boolean initCircuit(CommandSender sender, String[] args, RedstoneChips rc) {
        this.redstoneChips = rc;
        debuggers = new ArrayList<CommandSender>();
        inputBits = new BitSet7(inputs.length);
        outputBits = new BitSet7(outputs.length);
        this.args = args;

        updateInputBits();
        boolean result = init(sender, args);
        if (result!=false) {
            if (isStateless()) {
                for (int i=0; i<inputs.length; i++)
                    this.inputChange(i, inputBits.get(i));
            }
        }

        return result;
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
        if (inputsDisabled) {
            debug(redstoneChips.getPrefsManager().getErrorColor() + "Inputs are disabled.");
            return;
        }

        if (inputBits.get(idx)==newVal) return;

        inputBits.set(idx, newVal);
        if (hasDebuggers()) debug("Input change: " + BitSetUtils.bitSetToBinaryString(inputBits, 0, inputs.length) + " (" +
                BitSetUtils.bitSetToUnsignedInt(inputBits, 0, inputs.length) + ")");
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
     * @param sender The command sender that activated the chip, or null if called on startup.
     * @param args Any words on the sign after the circuit type.
     * @return true if the init was successful, false if an error occurred.
     */
    protected abstract boolean init(CommandSender sender, String[] args);

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
     * Sets the physical state of one of the outputs.
     * Changes the data byte of the selected output block to make the lever turn on or off.
     *
     * @param outIdx Output index. 0 for first output (closest to the sign) and so forth.
     * @param state The new state of the output.
     */
    protected void sendOutput(int outIdx, boolean state) {
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
        BitSet7 bits = BitSetUtils.intToBitSet(value, length);
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
     * Useful method for posting error messages. Sends an error message to the requested command sender using the error chat color as
     * set in the preferences file. If sender is null the message is sent to the console logger as a warning.
     * @param sender The command sender to send the message to.
     * @param message The error message.
     */
    protected void error(CommandSender sender, String message) {
        if (sender!=null) sender.sendMessage(redstoneChips.getPrefsManager().getErrorColor() + message);
        else Logger.getLogger("Minecraft").warning(redstoneChips.getDescription().getName() + ": " + this.getClass().getSimpleName() + "> " + message);
    }

    /**
     * Useful method for posting info messages. Sends an info message to the requested command sender using the info chat color as
     * set in the preferences file. If sender is null the message is simply ignored.
     * @param sender The CommandSender to send the message to.
     * @param message The error message.
     */
    protected void info(CommandSender sender, String message) {
        if (sender!=null) sender.sendMessage(redstoneChips.getPrefsManager().getInfoColor() + message);
    }

    /**
     * Sends a debug message to all debugging players of this circuit, using the debug chat color preferences key.
     * Please check that hasDebuggers() returns true before processing any debug messages.
     *
     * @param message The error message.
     */
    protected void debug(String message) {
        int id = redstoneChips.getCircuitManager().getCircuits().indexOf(this);
        
        for (CommandSender s : debuggers)
            s.sendMessage(redstoneChips.getPrefsManager().getDebugColor() + this.getClass().getSimpleName() + " (" + id + "): " + message);
    }

    /**
     * Adds the command sender as a debugger for the circuit.
     *
     * @param d The command sender to add.
     * @throws IllegalArgumentException If the player is already in the debuggers list.
     */
    public void addDebugger(CommandSender d) throws IllegalArgumentException {
        if (debuggers.contains(d)) throw new IllegalArgumentException("You are already debugging this circuit.");
        debuggers.add(d);
    }

    /**
     * Removes the command sender from the debuggers list.
     *
     * @param d The command sender.
     * @return
     * @throws IllegalArgumentException
     */
    public void removeDebugger(CommandSender d) throws IllegalArgumentException {
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
    public List<CommandSender> getDebuggers() {
        return debuggers;
    }

    /**
     * Used for getting a Block object for an output pin's lever.
     *
     * @param outputIdx The required output pin number.
     * @return The lever block of the specific output index.
     */
    protected Block getOutputBlock(int outputIdx) {
        BlockVector v = outputs[outputIdx];
        return world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    /**
     * Used for getting a Block object for an input pin's lever.
     *
     * @param inputIdx The required input pin number.
     * @return The input block (the iron block by default) of the specific input index.
     */
    protected Block getInputBlock(int inputIdx) {
        BlockVector v = inputs[inputIdx].getInputBlock();
        return world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }

    /**
     * Causes the circuit to update the state of its output levers according to the current values
     * in outputBits.
     */
    public void updateOutputLevers() {
        for (int i=0; i<outputs.length; i++)
            changeLeverState(getOutputBlock(i), outputBits.get(i));
    }


    /**
     * Update the inputBits BitSet according to the current input pin values.
     */
    public void updateInputBits() {
        for (int i=0; i<inputs.length; i++)
            inputBits.set(i, inputs[i].getPinValue());
    }

    /**
     * Returns true. A stateless circuit is a circuit that will always have the same output values for a set of input values.
     * A logical gate is an example of a stateless circuit while a counter is not stateless
     * @return True if the circuit is stateless.
     */
    protected boolean isStateless() {
        return true;
    }

    /**
     * Forces the circuit to stop processing input changes.
     */
    public void disableInputs() {
        inputsDisabled = true;
    }

    /**
     *
     * @return true if the circuit's inputs are disabled.
     */
    public boolean isDisabled() { return inputsDisabled; }
}
