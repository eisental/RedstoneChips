package org.redstonechips.chip;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.io.IOWriter;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.BooleanSubset;


/**
 * Represents a RedstoneChips circuit.
 *
 * @author Tal Eisenberg
 */
public abstract class Circuit {
    /** 
     * The chip enclosing this circuit.
     */
    public Chip chip;
    
    /**
     * The object responsible for writing output states.
     */
    public IOWriter outWriter;
    
    /**
     * Reference to the core plugin instance.
     */
    public RedstoneChips rc;

    /**
     * Stores the current state of each input pin. Should be used only for monitoring.
     */
    public boolean[] inputs;

    /**
     * Stores the current state of each output bit. Should be used only for monitoring. 
     */
    public boolean[] outputs;

    /**
     * Number of input pins.
     */
    public int inputlen;
    
    /**
     * Number of output pins.
     */
    public int outputlen;
    
    /**
     * The recipient of info() and error() messages. This is null for most of the circuit life-cycle. Usually only set when running the init method.
     */
    public CommandSender activator = null;        
        
    /**
     * Called when an input pin state is changed.
     *
     * @param state The new state of the input pin. 
     * @param inIdx index of changed input pin.
     */
    public abstract void input(boolean state, int inIdx);

    /**
     * Called after the chip is activated by a user or after the chip is loaded from file.
     * 
     * @param args Any words on the sign after the circuit type.
     * @return The circuit if the init was successful, null if an error occurred.
     */
    public abstract Circuit init(String[] args);

    /**
     * Called when the plugin needs to save the circuits state to disk or when using /rcinfo.
     * The circuit should return a map containing any data needed to bring the circuit back to its current state
     * after a server restart.
     *
     * @return Map containing state data.
     */
    public Map<String,String> getInternalState() { return new HashMap<>(); }

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
    public Map<String,Object> getResetData() { return new HashMap<>(); }

    /**
     * Called after the plugin resets the circuit.
     *
     * @param data Map containing reset data to be restored. The map should hold the same data that was returned by setResetData()
     */
    public void setResetData(Map<String,Object> data) {}

    /**
     * Called when the circuit is physically destroyed.
     * Put here any necessary cleanups.
     */
    public void destroyed() {}

    /**
     * Called when the circuit is shutdown. Typically when the server shuts down or the plugin is disabled and before a circuit is 
     * destroyed.
     */
    public void shutdown() {}

    /**
     * Called when the chip is disabled.
     */
    public void disable() {}

    /**
     * Called when the chip is enabled.
     */
    public void enable() {}
    
    /**
     * Returns true. A stateless circuit is a circuit that will always output the same values 
     * given a set of input values.
     * A logical gate is an example of a stateless circuit while a counter is not stateless.
     * 
     * @return True if the circuit is stateless.
     */
    public boolean isStateless() {
        return true;
    }        
    
    public Circuit constructWith(Chip chip, IOWriter writer) {
        return constructWith(chip, writer, chip.inputPins.length, chip.outputPins.length);
    }
    
    public Circuit constructWith(Chip chip, IOWriter writer, int inputlen, int outputlen) {
        this.chip = chip;
        this.outWriter = writer;
        
        rc = RedstoneChips.inst();        
        
        this.inputlen = inputlen;
        this.outputlen = outputlen;

        inputs = new boolean[inputlen];
        outputs = new boolean[outputlen];
        
        return this;
    }
    
    /**
     * Sets the physical state of one of the outputs.
     *
     * @param index Output index. First output is 0.
     * @param state The new state of the output.
     */
    public void write(boolean state, int index) {
        outputs[index] = state;
        outWriter.writeOut(state, index);
    }

    /**
     * Send a long integer through a set of outputs.
     * First converts to BitSet by calling intToBitSet(), then calls sendBitSet().
     *
     * @param firstOutput output index of first output (LSB).
     * @param length number of bits/outputs to write to.
     * @param value The integer value to send out.
     */
    public void writeInt(long value, int firstOutput, int length) {
        boolean[] bits = BooleanArrays.fromInt(value, length);
        writeBits(bits, firstOutput, length);
    }

    public void writeBits(boolean[] bits, int firstOutput, int length) {
        for (int i=0; i<length; i++)
            write(bits[i], firstOutput+i);
    }
    
    public void writeBits(boolean[] bits, int firstOutput) {
        writeBits(bits, firstOutput, bits.length);
    }
    
    public void writeBits(boolean[] bits) {
        writeBits(bits, 0, outputlen);
    }
    
    public void writeBooleanSubset(BooleanSubset bits, int firstOutput, int length) {
        for (int i=0; i<length; i++)
            write(bits.get(i), firstOutput+i);
    }
    
    public void writeBooleanSubset(BooleanSubset bits, int firstOutput) {
        writeBooleanSubset(bits, firstOutput, bits.length());
    }
    
    /**
     * Sends a BitSet object to the circuit outputs.
     *
     * @param startOutIdx First output pin that will be set.
     * @param length Number of bits to set.
     * @param bits The BitSet object to send out. Any excessive bits in the BitSet are ignored.
     */
    public void writeBitSet(BitSet bits, int startOutIdx, int length) {
        for (int i=0; i<length; i++) {
            boolean b = bits.get(i);
            write(b, startOutIdx+i);
        }
    }

    /**
     * Sends a BitSet object to the circuit outputs.
     * Sends a bit to each circuit output, starting from output 0.
     *
     * @param bits BitSet object to send out.
     */
    public void writeBitSet(BitSet bits) {
        writeBitSet(bits, 0, outputlen);
    }

    /**
     * Convenience method for posting error messages. Sends an error message to the current command sender using the error chat color as
     * set in the preferences file. If sender is currently null the message is sent to the console logger as a warning.
     * 
     * @param message The error message.
     * @return Always null.
     */
    public Circuit error(String message) {
        errorForSender(activator, message);
        return null;
    }

    public void errorForSender(CommandSender sender, String message) {
        if (sender!=null) sender.sendMessage(rc.prefs().getErrorColor() + message);
        else rc.log(Level.WARNING, chip + "> " + message);
    }
    /**
     * Convenience method for posting info messages. Sends an info message to the current command sender using the info chat color as
     * set in the preferences file. If sender is null debug() is used instead.
     * 
     * @param message The error message.
     */
    public void info(String message) {
        infoForSender(activator, message);
    }

    public void infoForSender(CommandSender sender, String message) {
        if (sender!=null) sender.sendMessage(rc.prefs().getInfoColor() + message);
    }
    /**
     * Sends a debug message to all debugging players of this circuit, using the debug chat color preferences key.
     * Please check that hasDebuggers() returns true before processing any debug messages.
     *
     * @param message The error message.
     */
    public void debug(String message) {
        if (activator!=null) info(message);
        
        if (chip!=null) chip.notifyCircuitMessage(message);
    }
    
    /**
     * Turns off all outputs.
     */
    protected void clearOutputs() {
        for (int i=0; i<outputlen; i++) 
            write(false, i);
    }
    
    // -- metadata --
    
    private final Map<String, Object> metadata = new HashMap<>();
    
    public void putMeta(String key, Object val) { metadata.put(key, val); }
    
    public Object getMeta(String key) { return metadata.get(key); }
 
    // -- static --
    
    public static Circuit initalizeCircuit(Circuit circuit, CommandSender sender, String[] args) {
        if (circuit==null) return null;
        
        circuit.activator = sender;        
        Circuit initalizedCircuit = circuit.init(args);        
        circuit.activator = null;        
        
        if (circuit != initalizedCircuit) return Circuit.initalizeCircuit(initalizedCircuit, sender, args);
        else return circuit;
    }
}
