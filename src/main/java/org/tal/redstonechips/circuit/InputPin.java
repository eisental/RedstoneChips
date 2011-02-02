/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 *
 * @author Tal Eisenberg
 */
public class InputPin {
    private Circuit circuit;
    private Block inputBlock;
    private int index;
    private Map<Block, Boolean> powerBlocks;

    public InputPin(Circuit circuit, Block block, int index) {
        inputBlock = block;
        this.circuit = circuit;
        this.index = index;

        // assuming circuit already has its structure block set up.
        powerBlocks = new HashMap<Block, Boolean>();

        addPowerBlock(BlockFace.UP);
        addPowerBlock(BlockFace.NORTH);
        addPowerBlock(BlockFace.EAST);
        addPowerBlock(BlockFace.SOUTH);
        addPowerBlock(BlockFace.WEST);
    }

    public Circuit getCircuit() { return circuit; }

    public Block getInputBlock() { return inputBlock; }

    public int getIndex() { return index; }

    public boolean getORedValue() {
        boolean ret = false;
        for (Boolean val : powerBlocks.values())
            ret = ret | val;

        return ret;
    }

    private void addPowerBlock(BlockFace direction) {
        Block b = inputBlock.getFace(direction);
        if (!partOfStructure(b)) powerBlocks.put(b, b.isBlockPowered());
    }

    private boolean partOfStructure(Block b) {
        for (Block s : circuit.structure) {
            if (s==b) {
                System.out.println("Not adding block " + b + " as input src since already part of structure.");
                return true;
            }
        }

        return false;
    }

    public void updateValue(Block block, boolean newVal) {
        if (!powerBlocks.containsKey(block))
            throw new IllegalArgumentException("Block " + block + " is not a power block of input " + index + " of circuit " + circuit);
        else {
            powerBlocks.put(block, newVal);
        }

    }

    public Iterable<Block> getPowerBlocks() {
        return powerBlocks.keySet();
    }
}
