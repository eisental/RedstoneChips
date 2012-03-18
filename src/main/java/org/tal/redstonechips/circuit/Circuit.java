package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;
import org.tal.redstonechips.circuit.io.InputPin;
import org.tal.redstonechips.circuit.io.InterfaceBlock;
import org.tal.redstonechips.circuit.io.OutputPin;
import org.tal.redstonechips.util.ChunkLocation;
import org.tal.redstonechips.wireless.Wireless;


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
     * Ordered list of input pins. 
     */
    public InputPin[] inputs;

    /**
     * Ordered list of output pins.
     */
    public OutputPin[] outputs;

    /**
     * Contains the location of any block that is part of this circuit. When any block in this array is broken the circuit is destroyed.
     * This includes the sign block, chip blocks, input blocks, output blocks and output lever blocks.
     */
    public Location[] structure;

    /**
     * Ordered list of interface blocks. Used for interaction points with the "physical" world.
     */
    public InterfaceBlock[] interfaceBlocks;

    /**
     * The location of the sign block that was used to activate the circuit.
     */
    public Location activationBlock;

    /**
     * Reference to the minecraft World this circuit was built in.
     */
    public World world;

    /**
     * List of circuit listeners that receive events from this circuit.
     */
    private List<CircuitListener> listeners;

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
    public boolean disabled = false;

    /**
     * The circuit id. Set by CircuitManager.
     */
    public int id = -1;

    /**
     * An optional circuit instance name.
     */
    public String name = null;
    
    /**
     * Set to the chunk coordinates of the circuit's activation block
     */
    public ChunkLocation[] circuitChunks;

    /**
     * Initializes the circuit. 
     * Updates input pin values according to source blocks.
     * Refreshes the output pins according to input states if the circuit is stateless.
     * 
     * @param sender The sender that activated the circuit. Used for sending error or status messages after activation.
     * @param args The sign arguments of this circuit. Stored in the args field.
     * @return result of call to abstract Circuit.init() method.
     */
    public final boolean initCircuit(CommandSender sender, RedstoneChips rc) {
        this.redstoneChips = rc;

        listeners = new ArrayList<CircuitListener>();
        inputBits = new BitSet7(inputs.length);
        if (outputBits==null) outputBits = new BitSet7(outputs.length);
        
        updateInputBits();
        
        // circuit class speicifc initialization.
        boolean result = init(sender, args);

        if (disabled) disable();
        else if (result!=false && isStateless()) {
            runInputLogic();
        }

        return result;
    }

    /**
     * Called by the plugin whenever an input pin changes state.
     * If the new state is different from the previous state stored in inputBits the inputBits value is updated
     * and the inputChange(idx, newVal) method is called.
     *
     * @param idx The changed input pin index.
     * @param newVal true if the current is greater than 0.
     */
    public void stateChange(int idx, boolean newVal) {
        if (disabled) return;

        if (inputBits.get(idx)==newVal) return;

        inputBits.set(idx, newVal);

        for (CircuitListener l : listeners) {
            l.inputChanged(this, idx, newVal);
        }
        
        inputChange(idx, newVal);
    }

    /**
     * Resets outputs, calls circuitShutdown() and circuitDestroyed().
     */
    public void destroyCircuit(CommandSender destroyer) {
        shutdownCircuit();

        for (OutputPin o : outputs) o.setState(false);
        
        circuitDestroyed();
        
        for (CircuitListener l : listeners)
            l.circuitDestroyed(this, destroyer);

    }
    
    /**
     * Shuts down the circuit and any Wireless classes associated with it and informs all CircuitListeners.
     * Additional shut down code can be inserted by overriding Circuit.circuitShutdown().
     */
    public void shutdownCircuit() {
        circuitShutdown();

        for (CircuitListener l : listeners)
            l.circuitShutdown(this);
        
        List<Wireless> wireless = redstoneChips.getChannelManager().getCircuitWireless(this);
        for (Wireless w : wireless) w.shutdown();
    }
    
    /**
     * Called when an input pin state is changed.
     *
     * @param inIdx index of changed input pin. 
     * @param state The new state of the input pin.
     */
    public abstract void inputChange(int inIdx, boolean state);

    /**
     * Called after the chip is activated by a user or after the chip is loaded from file.
     * 
     * @param sender The command sender that activated the chip, or null if called on startup.
     * @param args Any words on the sign after the circuit type.
     * @return true if the init was successful, false if an error occurred.
     */
    protected abstract boolean init(CommandSender sender, String[] args);

    /**
     * Called when the plugin needs to save the circuits state to disk or when using /rcinfo.
     * The circuit should return a map containing any data needed to bring the circuit back to its current state
     * after a server restart.
     *
     * @return Map containing state data.
     */
    public Map<String,String> getInternalState() { return new HashMap<String,String>(); }

    /**
     * Called whenever the plugin is requested to save its data. 
     */
    public void save() { }

    /**
     * Called when the plugin loads a circuit from file.
     *
     * @param state Map containing state data that was read from the file. The map should hold the same data that was returned by getInternalState()
     */
    public void setInternalState(Map<String,String> state) {}

    /**
     * Called before the plugin resets the circuit.
     * The circuit should return a map containing any data that may not be available on a reset condition.
     *
     * @return Map containing reset data.
     */
    public Map<String,Object> getResetData() { return new HashMap<String,Object>(); }

    /**
     * Called after the plugin resets the circuit.
     *
     * @param reset Map containing reset data to be restored. The map should hold the same data that was returned by setResetData()
     */
    public void setResetData(Map<String,Object> data) {}

    /**
     * Called when the circuit is physically destroyed.
     * Put here any necessary cleanups.
     */
    public void circuitDestroyed() {}

    /**
     * Called when the circuit is shutdown. Typically when the server shutsdown or the plugin is disabled and before a circuit is 
     * destroyed.
     */
    protected void circuitShutdown() {}

    /**
     * Sets the physical state of one of the outputs.
     *
     * @param outIdx Output index. First output is 0.
     * @param state The new state of the output.
     */
    protected void sendOutput(int outIdx, boolean state) {
        outputBits.set(outIdx, state);
        
        for (CircuitListener l : listeners)
            l.outputChanged(this, outIdx, state);
        outputs[outIdx].setState(state);
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
            boolean b = bits.get(i);
            sendOutput(startOutIdx+i, b);
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
    public void error(CommandSender sender, String message) {
        if (sender!=null) sender.sendMessage(redstoneChips.getPrefs().getErrorColor() + message);
        else redstoneChips.log(Level.WARNING, this.getClass().getSimpleName() + "> " + message);
    }

    /**
     * Useful method for posting info messages. Sends an info message to the requested command sender using the info chat color as
     * set in the preferences file. If sender is null the message is simply ignored.
     * @param sender The CommandSender to send the message to.
     * @param message The error message.
     */
    public void info(CommandSender sender, String message) {
        if (sender!=null) sender.sendMessage(redstoneChips.getPrefs().getInfoColor() + message);
    }

    /**
     * Sends a debug message to all debugging players of this circuit, using the debug chat color preferences key.
     * Please check that hasDebuggers() returns true before processing any debug messages.
     *
     * @param message The error message.
     */
    public void debug(String message) {        
        for (CircuitListener l : listeners) {
            l.circuitMessage(this, message);
        }
    }

    /**
     * Adds a circuit listener.
     *
     * @param d The listener.
     */
    public void addListener(CircuitListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    /**
     * Removes a circuit listener.
     *
     * @param l The listener.
     * @return true if the listener was found.
     */
    public boolean removeListener(CircuitListener l) {
        return listeners.remove(l);
    }

    /**
     *
     * @return The circuit listeners list.
     */
    public List<CircuitListener> getListeners() {
        return listeners;
    }
    
    /**
     * Checks if the circuit has any listeners. This method should be used
     * before processing any debug messages to avoid wasting cpu when no one is listening.
     *
     * @return True if the circuit has any listeners.
     */
    public boolean hasListeners() { return !listeners.isEmpty(); }

    @Deprecated
    public boolean hasDebuggers() { return hasListeners(); }
    
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
     * @return The name of the circuit class and chip id and name.
     */
    public String getChipString() {
        return getCircuitClass() + " (" + (name!=null?name + "/":"") + id + ")";
    }
    
    /**
     *
     * @param outputIdx The required output pin number.
     * @return The output block (gold block by default) of the specific output index.
     */
    protected Block getOutputBlock(int outputIdx) {
        Location l = outputs[outputIdx].getLocation();
        return world.getBlockAt(l);
    }

    /**
     * @param inputIdx The required input pin number.
     * @return The input block (iron block by default) of the specific input index.
     */
    protected Block getInputBlock(int inputIdx) {
        Location l = inputs[inputIdx].getLocation();
        return world.getBlockAt(l);
    }

    /**
     * Called when any of the circuit's chunks has loaded. Causes the circuit to update the state of its output levers 
     * according to the current values in outputBits.
     */
    public void circuitChunkLoaded() {
        for (InputPin i : inputs)
            i.refreshSourceBlocks();

        for (int i=0; i<outputs.length; i++)
            outputs[i].setState(outputBits.get(i));
    }

    /**
     * Update the inputBits BitSet according to the current input pin values.
     */
    public void updateInputBits() {
        for (int i=0; i<inputs.length; i++) {
            inputs[i].refreshSourceBlocks();
            inputBits.set(i, inputs[i].getPinValue());
        }
    }

    /**
     * Returns true. A stateless circuit is a circuit that will always output the same values 
     * for a set of input values.
     * A logical gate is an example of a stateless circuit while a counter is not stateless.
     * 
     * @return True if the circuit is stateless.
     */
    protected boolean isStateless() {
        return true;
    }

    /**
     * Disables or enables the chip according to the parameter.
     * 
     * @param d true to disable or false to enable.
     */
    public void setDisabled(Boolean d) {
        if (d) disable();
        else enable();
    }
    
    /**
     * Forces the circuit to stop processing input changes.
     */
    public void disable() {
        disabled = true;
        updateCircuitSign(true);
        for (CircuitListener l : listeners)
            l.circuitDisabled(this);
    }

    /**
     * Enables the chip, allowing it to process input changes.
     */
    public void enable() {
        disabled = false;
        updateCircuitSign(true);
        for (CircuitListener l : listeners)
            l.circuitDisabled(this);

    }
    
    /**
     *
     * @return true if the circuit's inputs are disabled.
     */
    public boolean isDisabled() { return disabled; }

    /**
     * Replaces the chip input, output and interface block materials to the currently set materials in the preferences.
     * @return The number of blocks that were replaced.
     */
    public int fixIOBlocks() {
        int blockCount = 0;

        int inputType = redstoneChips.getPrefs().getInputBlockType().getItemTypeId();
        byte inputData = redstoneChips.getPrefs().getInputBlockType().getData();

        int outputType = redstoneChips.getPrefs().getOutputBlockType().getItemTypeId();
        byte outputData = redstoneChips.getPrefs().getOutputBlockType().getData();

        int interfaceType = redstoneChips.getPrefs().getInterfaceBlockType().getItemTypeId();
        byte interfaceData = redstoneChips.getPrefs().getInterfaceBlockType().getData();

        List<ChunkLocation> chunksToUnload = new ArrayList<ChunkLocation>();
        for (ChunkLocation chunk : circuitChunks) {
            if (!chunk.isChunkLoaded()) {
                chunksToUnload.add(chunk);
                redstoneChips.getCircuitManager().workOnChunk(chunk);
            }
            
        }

        for (InputPin i : inputs) {
            Block input = i.getLocation().getBlock();

            if (input.getTypeId()!=inputType || input.getData()!=inputData) {
                input.setTypeIdAndData(inputType, inputData, false);
                blockCount++;
            }
        }

        for (OutputPin o : outputs) {
            Block output = o.getLocation().getBlock();

            if (output.getTypeId()!=outputType || output.getData()!=outputData) {
                output.setTypeIdAndData(outputType, outputData, false);
                blockCount++;
            }
        }

        for (InterfaceBlock t : interfaceBlocks) {
            Block tb = t.getLocation().getBlock();

            if (tb.getTypeId()!=interfaceType || tb.getData()!=interfaceData) {
                tb.setTypeIdAndData(interfaceType, interfaceData, false);
                blockCount++;
            }
        }

        for (ChunkLocation chunk : chunksToUnload) {
            redstoneChips.getCircuitManager().releaseChunk(chunk);
        }

        return blockCount;
    }

    /**
     * Updates the text and color of the 1st line of the circuit activation sign.
     * @param activated When true the class name is colored in the selected signColor preference key. When false the color is removed.
     */
    public void updateCircuitSign(boolean activated) {
        if (!ChunkLocation.fromLocation(activationBlock).isChunkLoaded()) return;
        
        BlockState state = activationBlock.getBlock().getState();
        if (!(state instanceof Sign)) return;

        final Sign sign = (Sign)state;
        if (sign==null) return;
        String line;
        if (activated) {
            String signColor;
            if (isDisabled()) signColor = "8";
            else signColor = redstoneChips.getPrefs().getSignColor();
            line = (char)167 + signColor + this.getCircuitClass();
        } else {
            line = this.getCircuitClass();
        }

        try {
            if (!line.equals(sign.getLine(0))) {
                sign.setLine(0, line);

                redstoneChips.getServer().getScheduler().scheduleSyncDelayedTask(redstoneChips, new Runnable() {
                    @Override
                    public void run() {
                        sign.update();
                    }
                });
            }
        } catch (NullPointerException ne) { }
    }

    private void runInputLogic() {
        for (int i=0; i<inputs.length; i++)
            this.inputChange(i, inputBits.get(i));
    }

    /**
     * Checks whether all of the circuit's blocks are in place.
     * Makes sure that each output lever block of lever material, checks that the activation sign is in place
     * and that none of the circuit's structure blocks are air.
     * 
     * @return True if the test passed.
     */
    public boolean checkIntegrity() {
        if (world.getBlockTypeIdAt(activationBlock)!=Material.WALL_SIGN.getId()) {
            redstoneChips.log(Level.WARNING, "Circuit " + id + ": Sign is missing at " + activationBlock.getBlockX() + "," + activationBlock.getBlockY() + ", " + activationBlock.getBlockZ() + ".");
            return false;
        } 
        
        for (Location s : structure) {
            if (!s.equals(activationBlock)) {
                if (world.getBlockTypeIdAt(s)==Material.AIR.getId()) {
                    redstoneChips.log(Level.WARNING, "Circuit " + id + ": Chip block is missing at " + s.getBlockX() + "," + s.getBlockY() + ", " + s.getBlockZ() + ".");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Turns off all outputs.
     */
    public void resetOutputs() {
        for (OutputPin o : outputs)            
            o.setState(false);
    }

    /**
     * 
     * @return an instance of the RedstoneChips plugin.
     */
    public RedstoneChips getPlugin() {
        return redstoneChips;
    }

    /**
     * Initializes the output buffer. Can only be used before calling initCircuit().
     * @param bits 
     */
    public void setOutputBits(BitSet7 bits) {
        if (outputBits==null) outputBits = bits;
        else throw new RuntimeException("Trying to set outputBits while it's already set.");
    }
}
