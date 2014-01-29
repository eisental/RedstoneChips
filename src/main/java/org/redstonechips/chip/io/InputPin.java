package org.redstonechips.chip.io;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.redstonechips.RCPrefs;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.Chip;
import org.redstonechips.util.ChunkLocation;

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
    
    private final Map<Location, Boolean> sourceBlocks;
    private final Location bottomSourceBlock;
    private long lastRedstoneChangeTick = -1;
    private int changesInTickCount = 0;

    public static int maxInputChangesPerTick;
    /**
     *
     * @param chip
     * @param inputBlock The physical location of the input pin.
     * @param index The input pin order index in the circuit.
     */
    public InputPin(Chip chip, Location inputBlock, int index) {
        super(chip, inputBlock, index);

        // assuming circuit already has its structure block set up.
        sourceBlocks = new HashMap<>();

        addSourceBlock(new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY()+1, inputBlock.getBlockZ()));
        bottomSourceBlock = new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY()-1, inputBlock.getBlockZ());
        addSourceBlock(bottomSourceBlock);
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX()+1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX()-1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()+1));
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()-1));
    }

    /**
     * @return the state of the pin according to the state of its surrounding source blocks.
     */
    public boolean getPinValue() {
        boolean ret = false;
        for (Boolean val : sourceBlocks.values()) {
            ret = ret | val;
        }
        
        return ret;
    }

    private void addSourceBlock(Location loc) {
        if (!isPartOfStructure(loc)) {
            boolean state;

            int type = chip.world.getBlockTypeIdAt(loc);
            if (type==Material.REDSTONE_WIRE.getId()) {
                byte data = chip.world.getBlockAt(loc).getData();
                state = data>0;
            } else if (type == Material.LEVER.getId()) {
                byte data = chip.world.getBlockAt(loc).getData();
                state = (data&8) == 8;
            } else {
                state = false;
            }

            sourceBlocks.put(loc, state);
        }
    }

    /**
     * Updates the known state of one of the blocks surrounding the input block.
     *
     * @param block The location of the updated block.
     * @param newVal The new redstone current of the block.
     * @param source The source type.
     * @throws IllegalArgumentException If the block in the provided location is not a source block of the input.
     */
    public void updateValue(Block block, boolean newVal, SourceType source) throws IllegalArgumentException {
        Location l = block.getLocation();

        if (!sourceBlocks.containsKey(l)) {
            RedstoneChips.inst().log(Level.WARNING, "Block @ " + block + " is not a power block of input " + index + " of chip " + chip);
        } else {
            if (source==SourceType.REDSTONE && l.equals(bottomSourceBlock)) {
                sourceBlocks.put(l, false);
                return;
            }
            
            long curTick = chip.world.getFullTime();
            if (curTick==lastRedstoneChangeTick) {
                changesInTickCount++;
                if (changesInTickCount>InputPin.maxInputChangesPerTick) {
                    abortFeedbackLoop();
                }
            } else {
                changesInTickCount = 1;
            }

            sourceBlocks.put(l, newVal);

            lastRedstoneChangeTick = curTick;
            
            try {
                chip.inputChange(getIndex(), getPinValue());
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
     * Deactivates the circuit and sends a debug message alerting about a detected feedback loop.
     */
    private void abortFeedbackLoop() {
        if (chip.isDisabled()) {
            return;
        }
        
        if (chip.hasListeners()) {
            ChatColor errorColor = RCPrefs.getErrorColor();
            ChatColor debugColor = RCPrefs.getDebugColor();
            
            chip.notifyCircuitMessage(errorColor + "Possible infinite feedback loop " + debugColor + "detected in input " + errorColor + index + debugColor + ".");
            chip.notifyCircuitMessage("Use /rcenable to reactivate the circuit after solving the problem or destroy it normally.");
        }

        chip.disable();
    }

    /**
     * refreshes the state of all source blocks according to their block state.
     */
    public void refreshSourceBlocks() {
        for (Location l : this.sourceBlocks.keySet()) {
            if (ChunkLocation.fromLocation(l).isChunkLoaded()) {
                sourceBlocks.put(l, findSourceBlockState(l));
            }
        }
    }

    /**
     *
     * @param loc The location of the block to check.
     * @return true if the block is powered and false otherwise.
     */
    public boolean findSourceBlockState(Location loc) {
        Block b = loc.getBlock();
        Material m = b.getType();
        
        switch (m) {
            case REDSTONE_WIRE:
                if (loc.equals(bottomSourceBlock)) return false;
                else return b.getData()>0;
            case LEVER:
                return (b.getData()&8) == 8;
            case REDSTONE_TORCH_OFF:
                return false;
            case REDSTONE_TORCH_ON:
                return true;
            default: // looking for direct connection to an output block.
                OutputPin out = RedstoneChips.inst().chipManager().getAllChips().getOutputPin(loc);
                if (out!=null && out.isDirect()) 
                    return out.getState();
                else return false;
        }
    }
}
