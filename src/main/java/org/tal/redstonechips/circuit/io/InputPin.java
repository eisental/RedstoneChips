package org.tal.redstonechips.circuit.io;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.ChunkLocation;

/**
 * Represents an input pin of a chip. 
 *
 * @author Tal Eisenberg
 */
public class InputPin extends IOBlock {
    /**
     * The type of a signal change source.
     */
    public enum SourceType { REDSTONE, DIRECT };
    
    private Map<Location, Boolean> sourceBlocks;
    private Location bottomSourceBlock;
    private long lastRedstoneChangeTick = -1;
    private int changesInTickCount = 0;

    /**
     *
     * @param circuit The circuit this pin belongs to.
     * @param loc The physical location of the input pin.
     * @param index The input pin order index in the circuit.
     */
    public InputPin(Circuit circuit, Location inputBlock, int index) {
        super(circuit, inputBlock, index);

        // assuming circuit already has its structure block set up.
        sourceBlocks = new HashMap<Location, Boolean>();

        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY()+1, inputBlock.getBlockZ()));
        bottomSourceBlock = new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY()-1, inputBlock.getBlockZ());
        addSourceBlock(bottomSourceBlock);
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX()+1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX()-1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()+1));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()-1));
    }

    /**
     * Calculates the overall state of the pin according to the state of its surrounding source blocks.
     */
    public boolean getPinValue() {
        boolean ret = false;
        for (Boolean val : sourceBlocks.values())
            ret = ret | val;

        return ret;
    }

    private void addSourceBlock(Location loc) {

        if (!isPartOfStructure(loc)) {
            boolean state = false;

            int type = circuit.world.getBlockTypeIdAt(loc);
            if (type==Material.REDSTONE_WIRE.getId()) {
                byte data = circuit.world.getBlockAt(loc).getData();
                state = data>0;
            } else if (type == Material.LEVER.getId()) {
                byte data = circuit.world.getBlockAt(loc).getData();
                state = (data&8) == 8;
            } else {
                state = false;
            }

            sourceBlocks.put(loc, state);
        }
    }

    /**
     * Update the state of one of the blocks surrounding the input block.
     *
     * @param loc The location of the updated block.
     * @param newVal The new redstone current of the block.
     * @param source The source type.
     * @throws IllegalArgumentException If the block in the provided location is not a source block of the input.
     */
    public void updateValue(Block block, boolean newVal, SourceType source) throws IllegalArgumentException {
        Location l = block.getLocation();

        if (!sourceBlocks.containsKey(l))
            circuit.getPlugin().log(Level.WARNING, "Block @ " + block + " is not a power block of input " + index + " of circuit " + circuit);
        else {
            if (source==SourceType.REDSTONE && l.equals(bottomSourceBlock)) {
                sourceBlocks.put(l, false);
                return;
            }
            
            long curTick = circuit.world.getFullTime();
            if (curTick==lastRedstoneChangeTick) {
                changesInTickCount++;
                if (changesInTickCount>circuit.getPlugin().getPrefs().getMaxInputChangesPerTick()) abortFeedbackLoop();
            } else changesInTickCount = 1;

            sourceBlocks.put(l, newVal);

            lastRedstoneChangeTick = curTick;
            try {
                circuit.stateChange(getIndex(), getPinValue());
            } catch (StackOverflowError e) {
                abortFeedbackLoop();
            }
        }

    }

    /**
     *
     * @return All source blocks surrounding this input pin.
     */
    public Iterable<Location> getSourceBlocks() {
        return sourceBlocks.keySet();
    }

    /**
     * Deactivates the circuit and sends a debug message of a detected feedback loop.
     */
    private void abortFeedbackLoop() {
        if (circuit.isDisabled()) return;
        
        if (circuit.hasDebuggers()) {
            ChatColor errorColor = circuit.getPlugin().getPrefs().getErrorColor();
            ChatColor debugColor = circuit.getPlugin().getPrefs().getDebugColor();
            circuit.debug(errorColor + "Possible infinite feedback loop " + debugColor + "detected in input " + errorColor + index + debugColor + ".");
            circuit.debug("Use /rcenable to reactivate the circuit after solving the problem or destroy it normally.");
        }

        circuit.disable();
    }

    /**
     * refreshes the state of all source blocks according to their block state.
     */
    public void refreshSourceBlocks() {
        for (Location l : this.sourceBlocks.keySet())
            if (ChunkLocation.fromLocation(l).isChunkLoaded()) sourceBlocks.put(l, findSourceBlockState(l));
    }

    /**
     *
     * @param loc The location of the block to check.
     * @return true if the block is powered and false otherwise.
     */
    public boolean findSourceBlockState(Location loc) {
        boolean state = false;
        Block b = loc.getBlock();

        if (b.getType()==Material.REDSTONE_WIRE) {
            if (loc.equals(bottomSourceBlock)) state = false;
            else state = b.getData()>0;
        } else if (b.getType() == Material.LEVER) {
            byte data = b.getData();
            state = (data&8) == 8;
        } else if (b.getType() == Material.REDSTONE_TORCH_OFF) {
            state = false;
        } else if (b.getType() == Material.REDSTONE_TORCH_ON) {
            state = true;
        } else {
            // looking for direct connection to an output block.
            OutputPin out = circuit.getPlugin().getCircuitManager().getOutputPin(loc);
            if (out!=null && out.isDirect())  
                state = out.getState();            
        }

        return state;
    }
}
