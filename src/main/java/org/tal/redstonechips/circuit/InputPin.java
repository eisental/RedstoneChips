/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *
 * @author Tal Eisenberg
 */
public class InputPin {
    private Circuit circuit;
    private Location inputLoc;
    private int index;
    private Map<Location, Boolean> powerBlocks;

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

    public Circuit getCircuit() { return circuit; }

    public Location getInputLocation() { return inputLoc; }

    public int getIndex() { return index; }

    public boolean getORedValue() {
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

    public void updateValue(Location loc, boolean newVal) {
        if (!powerBlocks.containsKey(loc))
            throw new IllegalArgumentException("Block @ " + loc + " is not a power block of input " + index + " of circuit " + circuit);
        else {
            powerBlocks.put(loc, newVal);
        }

    }

    public Iterable<Location> getPowerBlocks() {
        return powerBlocks.keySet();
    }
}
