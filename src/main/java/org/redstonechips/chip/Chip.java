
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
import org.redstonechips.chip.io.IOWriter;

/**
 * Represents a chip in a world. Every chip has a Circuit object tied to it that
 * represents the logic of the chip. The chip is responsible for connecting its 
 * circuit to the outside world, sending it any input changes and updating outputs
 * when the circuit writes values out.
 * 
 * @author taleisenberg
 */
public class Chip implements IOWriter {
    /**
     * The circuit running inside this chip.
     */
    public Circuit circuit;
    
    /**
     * The chip type (usually the circuit simple class name).
     */
    private String type;
    
    /**
     * Chip sign arguments. Any words found on the circuit sign from line 2 onward.
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
     * Contains the location of any block that is part of this chip. When any block in this array is broken the circuit is destroyed.
     * This includes the sign block, chip blocks, input blocks, output blocks and output lever blocks, as well as any blocks the circuit 
     * added to the structure.
     */
    public Location[] structure;

    /**
     * Ordered list of interface blocks. Used for interaction points with the "physical" world.
     */
    public InterfaceBlock[] interfaceBlocks;

    /**
     * The location of the sign block that was used to activate the chip.
     */
    public Location activationBlock;

    /**
     * Reference to the minecraft World this chip was built in.
     */
    public World world;
    
    /**
     * The chip id. Set by CircuitManager.
     */
    public int id = -1;

    /**
     * An optional chip instance name. Used for identification when a simple 
     * number is not enough.
     */
    public String name = null;
    
    /**
     * Set to the chunk coordinates of the chip activation block.
     */
    public ChunkLocation[] chunks;    
    
    /** 
     * 
     * Called by the plugin whenever an input pin changes state.
     * Updates the circuit inputs[] array, notifies any chip listeners and invokes
     * the circuit input() method.
     *
     * @param idx The changed input pin index.
     * @param newState New input pin state.
     */
    public void inputChange(int idx, boolean newState) {
        if (disabled) return;
        
        circuit.inputs[idx] = newState;

        notifyInputChanged(idx, newState);
        
        try {
            circuit.input(newState, idx);
        } catch (Exception e) {
            circuit.debug(RCPrefs.getErrorColor() + "On input: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Called when the chip is destroyed. 
     * Resets outputs, invoke circuit destroyed() method and notifies chip listeners.
     * 
     * @param destroyer
     */
    public void chipDestroyed(CommandSender destroyer) {
        shutdown();

        for (OutputPin o : outputPins) o.setState(false);
        
        try {
            circuit.destroyed();
        } catch (Exception e) {
            circuit.error("Error while calling destroyed(): " + e.getMessage());
            e.printStackTrace();
        }
        notifyChipDestroyed(destroyer);
    }
    
    /**
     * Called when the chip shuts down.
     * Invokes circuit shutdown() method, removes any wireless objects associated
     * with the chip circuit and notifies chip listeners.
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

    /**
     * Updates an output pin state.
     * 
     * @param state new state.
     * @param index output pin index.
     */
    @Override
    public void writeOut(Circuit circuit, boolean state, int index) {
        outputPins[index].setState(state);
        for (ChipListener l : listeners) l.outputChanged(Chip.this, index, state);        
    }    
    
    /**
      * Called when any of the chip chunks has loaded. Causes the chip to update 
      * the state of its output levers according to the current values in circuit.outputs[].
      */
    public void chipChunkLoaded() {
        for (InputPin i : inputPins)
            i.refreshSourceBlocks();

        for (int i=0; i<outputPins.length; i++)
            outputPins[i].forceState(circuit.outputs[i]);
    }
        
    // -- Enable / Disable --
    
    /**
     * When set to true any input changes will be ignored by the circuit.
     */
    public boolean disabled = false;
    
    /**
     *
     * @return true if the chip is disabled.
     */
    public boolean isDisabled() { return disabled; }
 
    /**
     * Forces the chip to stop processing input changes.
     * Invokes the circuit disable() method, updates the chip sign color and
     * notifies chip listeners.
     */
    public void disable() {
        disabled = true;
        circuit.disable();
        updateSign(true);        
        notifyChipDisabled();
    }

    /**
     * Enables the chip, allowing it to process input changes.
     * Invokes the circuit enable() method, updates the chip sign color and
     * notifies chip listeners.
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

//                RedstoneChips.inst().getServer().getScheduler().runTask(RedstoneChips.inst(), new Runnable() {
//                    @Override
//                    public void run() {
                        sign.update();
//                    }
//                });
            }
        } catch (NullPointerException ne) { }
    }
        

    /**
     * Sets the type of the chip. Can only be called once.
     * @param type Chip type.
     */
    public void setType(String type) {
        if (this.type == null)
            this.type = type;
        else throw new IllegalStateException("Chip already has a type.");
    }

    /**
     * 
     * @return The type of this chip. Usually the chip circuit simple class name.
     */
    public String getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return type + " (" + (name!=null?name + "/":"") + id + ")";
    }
    
    // -- Listener mechanics --
    
    /**
     * List of chip listeners that receive events from this chip.
     */
    private final List<ChipListener> listeners = new ArrayList<>();;
    
    /**
     * Add a chip listener.
     *
     * @param l The listener.
     */
    public void addListener(ChipListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    /**
     * Remove a chip listener.
     *
     * @param l The listener.
     * @return true if the listener was found and removed.
     */
    public boolean removeListener(ChipListener l) {
        return listeners.remove(l);
    }

    /**
     *
     * @return The chip listeners list.
     */
    public List<ChipListener> getListeners() {
        return listeners;
    }
    
    /**
     * Checks if the chip has any listeners. This method should be used
     * before processing any debug messages to avoid wasting cpu when no one is listening.
     *
     * @return True if the chip has any listeners.
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
