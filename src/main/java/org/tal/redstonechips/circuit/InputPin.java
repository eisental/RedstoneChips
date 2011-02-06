package org.tal.redstonechips.circuit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
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
    private Location inputLoc;
    private int index;
    private Map<Location, Boolean> powerBlocks;

    /**
     *
     * @param circuit The circuit this pin belongs to.
     * @param loc The physical location of the input pin.
     * @param index The input pin's order index in the circuit.
     */
    public InputPin(Circuit circuit, Location loc, int index) {
        inputLoc = loc;
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
    public Location getInputLocation() { return inputLoc; }

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
        Block inputBlock = circuit.world.getBlockAt(inputLoc.getBlockX(), inputLoc.getBlockY(), inputLoc.getBlockZ());
        Block b = inputBlock.getFace(direction);

        if (!partOfStructure(b)) powerBlocks.put(b.getLocation(), b.getData()>0);
    }

    private boolean partOfStructure(Block b) {
        for (Location l : circuit.structure) {
            if (l.equals(b.getLocation())) {
                return true;
            }
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
    public void updateValue(Location loc, boolean newVal) throws IllegalArgumentException {
        if (!powerBlocks.containsKey(loc))
            throw new IllegalArgumentException("Block @ " + loc + " is not a power block of input " + index + " of circuit " + circuit);
        else {
            powerBlocks.put(loc, newVal);
        }

    }

    /**
     *
     * @return The power blocks surrounding this input pin.
     */
    public Iterable<Location> getPowerBlocks() {
        return powerBlocks.keySet();
    }
}
