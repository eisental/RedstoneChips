
package org.redstonechips.chip;

import org.redstonechips.circuit.Circuit;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.io.InputPin;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.chip.io.OutputPin;
import org.redstonechips.util.ChunkLocation;
import org.redstonechips.Serializer;
import org.redstonechips.chip.io.IOWriter;

/**
 *
 * @author taleisenberg
 */
public class Chip implements IOWriter {
    /**
     * The circuit inside this chip.
     */
    public Circuit circuit;
    
    private String type;
    
    /**
     * Circuit sign arguments. Any word found on the circuit sign from line 2 onward.
     */
    public String[] args;    
    
    /**
     * Ordered list of input pins. 
     */
    public InputPin[] inputPins;

    /**
     * Ordered list of output pins.
     */
    public OutputPin[] outputPins;

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
    public ChunkLocation[] chunks;    
    
    /** 
     * 
     * Called by the plugin whenever an input pin changes state.
     * If the new state is different from the previous state stored in inputBits the inputBits value is updated
     * and the inputChange(idx, newVal) method is called.
     *
     * @param idx The changed input pin index.
     * @param newVal true if the current is greater than 0.
     */
    public void inputChange(int idx, boolean newVal) {
        if (disabled) return;
        
        circuit.inputs[idx] = newVal;

        notifyInputChanged(idx, newVal);
        
        // call circuit event.
        try {
            circuit.input(newVal, idx);
        } catch (Exception e) {
            circuit.debug(RCPrefs.getErrorColor() + "On input: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Resets outputs, calls circuitShutdown() and circuitDestroyed().
     * @param destroyer
     */
    public void chipDestroyed(CommandSender destroyer) {
        shutdown();

        for (OutputPin o : outputPins) o.setState(false);
        
        // call circuit event.
        try {
            circuit.destroyed();
        } catch (Exception e) {
            circuit.error("Error while calling destroyed(): " + e.getMessage());
            e.printStackTrace();
        }
        notifyChipDestroyed(destroyer);
    }
    
    /**
     * Shuts down the chip and informs all ChipListeners.
     * invokes Circuit.shutdown().
     */
    public void shutdown() {
        try {
            circuit.shutdown();
        } catch (Exception e) {
            circuit.error("Error while calling shutdown(): " + e.getMessage());
            e.printStackTrace();
        }
        RedstoneChips.inst().channelManager().removeCircuitWireless(circuit);
        notifyShutdown();
    }
        
    @Override
    public void writeOut(boolean state, int index) {
        outputPins[index].setState(state);
        for (ChipListener l : listeners) l.outputChanged(Chip.this, index, state);        
    }    
    
   /**
     * Called when any of the circuit's chunks has loaded. Causes the circuit to update the state of its output levers 
     * according to the current values in outputBits.
     */
    public void chipChunkLoaded() {
        for (InputPin i : inputPins)
            i.refreshSourceBlocks();

        for (int i=0; i<outputPins.length; i++)
            outputPins[i].setState(circuit.outputs[i]);
    }
        
    // -- Enable / Disable --
    
    /**
     * When set to true any input changes will be ignored.
     */
    public boolean disabled = false;
    
    /**
     *
     * @return true if the circuit's inputs are disabled.
     */
    public boolean isDisabled() { return disabled; }
 
    /**
     * Forces the circuit to stop processing input changes.
     */
    public void disable() {
        disabled = true;
        circuit.disable();
        updateSign(true);        
        notifyChipDisabled();
    }

    /**
     * Enables the chip, allowing it to process input changes.
     */
    public void enable() {
        disabled = false;
        updateSign(true);
        circuit.enable();
        notifyChipEnabled();
    }
        
    /**
     * Updates the text and color of the 1st line of the circuit activation sign.
     * @param activated When true the class name is colored in the selected signColor preference key. When false the color is removed.
     */
    public void updateSign(boolean activated) {
        if (!ChunkLocation.fromLocation(activationBlock).isChunkLoaded()) return;
        
        try {
            BlockState state = activationBlock.getBlock().getState();
            if (state==null || !(state instanceof Sign)) return;

            final Sign sign = (Sign)state;

            String line;
            if (activated) {
                String signColor;
                if (isDisabled()) signColor = "8";
                else signColor = RCPrefs.getSignColor();
                line = (char)167 + signColor + type;
            } else {
                line = type;
            }
        
            if (!line.equals(sign.getLine(0))) {
                sign.setLine(0, line);

                RedstoneChips.inst().getServer().getScheduler().scheduleSyncDelayedTask(RedstoneChips.inst(), new Runnable() {
                    @Override
                    public void run() {
                        sign.update();
                    }
                });
            }
        } catch (NullPointerException ne) { }
    }
        
    public Serializer getSerializer() { return new ChipSerializer(); }
    
    public void setType(String type) {
        if (this.type == null)
            this.type = type;
        else throw new IllegalStateException("Chip already has a type.");
    }

    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return type + " (" + (name!=null?name + "/":"") + id + ")";
    }
    
    // -- Listener mechanics --
    
    /**
     * List of circuit listeners that receive events from this circuit.
     */
    private final List<ChipListener> listeners = new ArrayList<>();;
    
    /**
     * Adds a circuit listener.
     *
     * @param l The listener.
     */
    public void addListener(ChipListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    /**
     * Removes a circuit listener.
     *
     * @param l The listener.
     * @return true if the listener was found.
     */
    public boolean removeListener(ChipListener l) {
        return listeners.remove(l);
    }

    /**
     *
     * @return The circuit listeners list.
     */
    public List<ChipListener> getListeners() {
        return listeners;
    }
    
    /**
     * Checks if the circuit has any listeners. This method should be used
     * before processing any debug messages to avoid wasting cpu when no one is listening.
     *
     * @return True if the circuit has any listeners.
     */
    public boolean hasListeners() { return !listeners.isEmpty(); }

    public void notifyCircuitMessage(String message) {
        for (ChipListener l : listeners) {
            l.circuitMessage(this, message);
        }
    }

    private void notifyChipEnabled() {
        for (ChipListener l : listeners)
            l.chipEnabled(this);
    }

    private void notifyChipDisabled() {
        for (ChipListener l : listeners)
            l.chipDisabled(this);
    }
    
    private void notifyShutdown() {
        for (ChipListener l : listeners)
            l.chipShutdown(this);
    }

    private void notifyChipDestroyed(CommandSender destroyer) {
        for (ChipListener l : listeners)
            l.chipDestroyed(this, destroyer);        
    }

    private void notifyInputChanged(int idx, boolean newVal) {
        for (ChipListener l : listeners)
            l.inputChanged(this, idx, newVal);
    }      
}
