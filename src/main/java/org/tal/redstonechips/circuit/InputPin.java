package org.tal.redstonechips.circuit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Represents an input pin of a circuit. Used for finding out if redstone current change in a block
 * should change an input value of a circuit.
 * The class keeps track of the surrounding blocks on the sides of the input location and the block on top of it.
 *
 * @author Tal Eisenberg
 */
public class InputPin extends IOBlock {
    private Map<Location, Boolean> sourceBlocks;

    private long lastRedstoneChangeTick = -1;
    private int changesInTickCount = 0;

    /**
     *
     * @param circuit The circuit this pin belongs to.
     * @param loc The physical location of the input pin.
     * @param index The input pin's order index in the circuit.
     */
    public InputPin(Circuit circuit, Location inputBlock, int index) {
        super(circuit, inputBlock, index);

        // assuming circuit already has its structure block set up.
        sourceBlocks = new HashMap<Location, Boolean>();

        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY()+1, inputBlock.getBlockZ()));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY()-1, inputBlock.getBlockZ()));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX()+1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX()-1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()+1));
        addSourceBlock(new Location(circuit.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()-1));
    }

    /**
     * Calculates the overall state of the pin according to the state of its surrounding blocks - the power blocks.
     *
     * @return The OR value of the redstone current state in each of the power blocks.
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
     * @throws IllegalArgumentException If the block in the provided location is not a power block of the input.
     */
    public void updateValue(Block block, boolean newVal) throws IllegalArgumentException {
        Location l = block.getLocation();

        if (!sourceBlocks.containsKey(l))
            throw new IllegalArgumentException("Block @ " + block + " is not a power block of input " + index + " of circuit " + circuit);
        else {
            long curTick = circuit.world.getFullTime();
            if (curTick==lastRedstoneChangeTick) {
                changesInTickCount++;
                if (changesInTickCount>circuit.redstoneChips.getPrefs().getMaxInputChangesPerTick()) abortFeedbackLoop();
            } else changesInTickCount = 1;

            sourceBlocks.put(l, newVal);

            lastRedstoneChangeTick = curTick;

            circuit.redstoneChange(getIndex(), getPinValue());
        }

    }

    /**
     *
     * @return The power blocks surrounding this input pin.
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
            circuit.debug("Possible infinite feedback loop detected in " + circuit.getChipString() + ", at"
                    + "pin " + index + ". It will no longer process any input changes. ");
            circuit.debug("Use /rcreset to reactivate the circuit after solving the problem or destroy it normally.");
        }

        circuit.disable();
    }

    /**
     * refreshes the state of all source blocks according to their block state.
     */
    public void refreshSourceBlocks() {
        for (Location l : this.sourceBlocks.keySet())
            sourceBlocks.put(l, InputPin.findSourceBlockState(l));
    }

    /**
     *
     * @param loc The location of the block to check.
     * @return true if the block is powered and false otherwise.
     */
    public static boolean findSourceBlockState(Location loc) {
        boolean state = false;
        Block b = loc.getBlock();

        if (b.getType()==Material.REDSTONE_WIRE) {
            state = b.getData()>0;
        } else if (b.getType() == Material.LEVER) {
            byte data = b.getData();
            state = (data&8) == 8;
        } else if (b.getType() == Material.REDSTONE_TORCH_OFF) {
            state = false;
        } else if (b.getType() == Material.REDSTONE_TORCH_ON) {
            state = true;
        }

        return state;
    }
}
