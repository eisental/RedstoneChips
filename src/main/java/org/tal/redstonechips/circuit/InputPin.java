package org.tal.redstonechips.circuit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * Represents an input pin of a Circuit. Used for finding out if redstone current change in a block
 * should change an input value of a circuit.
 * The class keeps track of the surrounding blocks on the sides of the input location and the block on top of it.
 *
 * @author Tal Eisenberg
 */
public class InputPin {
    private Circuit circuit;
    private Location inputBlock;
    private int index;
    private Map<Location, Boolean> powerBlocks;

    private long lastRedstoneChangeTick = -1;
    private int changesInTickCount = 0;
    private final static int FeedbackDetectionCount = 100;

    /**
     *
     * @param circuit The circuit this pin belongs to.
     * @param loc The physical location of the input pin.
     * @param index The input pin's order index in the circuit.
     */
    public InputPin(Circuit circuit, Location inputBlock, int index) {
        this.inputBlock = inputBlock;
        this.circuit = circuit;
        this.index = index;

        // assuming circuit already has its structure block set up.
        powerBlocks = new HashMap<Location, Boolean>();

        addPowerBlock(BlockFace.UP);
        addPowerBlock(BlockFace.NORTH);
        addPowerBlock(BlockFace.EAST);
        addPowerBlock(BlockFace.SOUTH);
        addPowerBlock(BlockFace.WEST);
    }

    /**
     *
     * @return The circuit of this input pin.
     */
    public Circuit getCircuit() { return circuit; }

    /**
     *
     * @return The location of the input pin block (the iron block by default).
     */
    public Location getInputBlock() { return inputBlock; }

    /**
     *
     * @return The index of the input pin in its circuit.
     */
    public int getIndex() { return index; }

    /**
     * Calculates the overall state of the pin according to the state of its surrounding blocks - the power blocks.
     *
     * @return The OR value of the redstone current state in each of the power blocks.
     */
    public boolean getPinValue() {
        boolean ret = false;
        for (Boolean val : powerBlocks.values())
            ret = ret | val;

        return ret;
    }

    private void addPowerBlock(BlockFace direction) {
        Block in = circuit.world.getBlockAt(inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ());
        Block b = in.getFace(direction);

        boolean state;
        if (b.getType()==Material.REDSTONE_WIRE)
            state = b.getData()>0;
        else if (b.getType()==Material.LEVER)
            state = (b.getData()&8) == 8;
        else 
            state = false;

        if (!partOfStructure(b)) powerBlocks.put(b.getLocation(), state);
    }

    private boolean partOfStructure(Block b) {
        for (Location l : circuit.structure) {
            if (b.getLocation().equals(l))
                return true;
        }

        return false;
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

        if (!powerBlocks.containsKey(l))
            throw new IllegalArgumentException("Block @ " + block + " is not a power block of input " + index + " of circuit " + circuit);
        else {
            long curTick = circuit.world.getFullTime();
            if (curTick==lastRedstoneChangeTick) {
                changesInTickCount++;
                if (changesInTickCount>FeedbackDetectionCount) abortFeedbackLoop();
            } else changesInTickCount = 1;

            powerBlocks.put(l, newVal);

            lastRedstoneChangeTick = curTick;
        }

    }

    /**
     *
     * @return The power blocks surrounding this input pin.
     */
    public Iterable<Location> getPowerBlocks() {
        return powerBlocks.keySet();
    }

    /**
     * Deactivates the circuit and sends a debug message of a detected feedback loop.
     */
    private void abortFeedbackLoop() {
        circuit.debug("Possible infinite feedback loop detected in " + circuit.getCircuitClass() + " circuit, at");
        circuit.debug("pin " + index + ". It will no longer process any input changes. ");
        circuit.debug("Use /rc-reset to reactivate the circuit after solving the");
        circuit.debug("problem or destroy it normally.");
        circuit.disableInputs();
    }
}
