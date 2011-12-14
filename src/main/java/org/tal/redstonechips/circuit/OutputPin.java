
package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Lever;
import org.tal.redstonechips.circuit.InputPin.InputSource;

/**
 *
 * @author Tal Eisenberg
 */
public class OutputPin extends IOBlock {
    private List<Location> outputBlocks;
    
    public OutputPin(Circuit circuit, Location outputBlock, int index) {
        super(circuit, outputBlock, index);
        
        outputBlocks = new ArrayList<Location>();
        
        addOutputBlock(new Location(circuit.world, outputBlock.getBlockX(), outputBlock.getBlockY()+1, outputBlock.getBlockZ()));
        addOutputBlock(new Location(circuit.world, outputBlock.getBlockX(), outputBlock.getBlockY()-1, outputBlock.getBlockZ()));
        addOutputBlock(new Location(circuit.world, outputBlock.getBlockX()+1, outputBlock.getBlockY(), outputBlock.getBlockZ()));
        addOutputBlock(new Location(circuit.world, outputBlock.getBlockX()-1, outputBlock.getBlockY(), outputBlock.getBlockZ()));
        addOutputBlock(new Location(circuit.world, outputBlock.getBlockX(), outputBlock.getBlockY(), outputBlock.getBlockZ()+1));
        addOutputBlock(new Location(circuit.world, outputBlock.getBlockX(), outputBlock.getBlockY(), outputBlock.getBlockZ()-1));
        
    }

    private void addOutputBlock(Location loc) {
        if (!isPartOfStructure(loc)) {
            outputBlocks.add(loc);
        }        
    }
    
    public List<Location> getOutputBlocks() {
        return outputBlocks;
    }

    private static final BlockFace[] adjacentFaces = new BlockFace[] { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    public void changeOutputState(boolean state) {  
        boolean hasActuator = false;
        for (Location l : outputBlocks) {            
            if (changeBlockState(l.getBlock(), state)) hasActuator = true;
        }
        
        if (!hasActuator) {
            for (Location l : outputBlocks) {
                InputPin i = circuit.getPlugin().getCircuitManager().lookupInputBlock(l.getBlock());

                if (i!=null) {
                    i.updateValue(loc.getBlock(), state, InputSource.DIRECT);
                } 
            }
            
        }
    }

    private boolean changeBlockState(Block outputBlock, boolean state) {
        Block ioBlock = loc.getBlock();        
        if (!circuit.world.isChunkLoaded(outputBlock.getChunk())) return true;        

        if (outputBlock.getType()==Material.LEVER) {
            Lever lever = (Lever)outputBlock.getState().getData();
            if (outputBlock.getRelative(lever.getAttachedFace()).equals(ioBlock)) {
                updateOutputBlock(outputBlock, state);
                ioBlock.getState().update();
            } else return false;
        } else if (outputBlock.getType()==Material.POWERED_RAIL) {
            updateOutputBlock(outputBlock, state);
            ioBlock.getState().update();
        } else return false;

        updateAdjacentFaces(ioBlock);

        return true;
        
    }
    
    private void updateOutputBlock(Block b, boolean state) {
        byte data = b.getData();
        boolean oldLevel = ((data&0x08) > 0);
        if (oldLevel==state) return;

        byte newData = (byte)(state? data | 0x8 : data & 0x7);

        b.setData(newData);
    }

    private void updateAdjacentFaces(Block ioBlock) {
        for (int i=0; i<adjacentFaces.length; i++) {
            BlockState adj = ioBlock.getRelative(adjacentFaces[i]).getState();
            if (adj.getType().equals(Material.REDSTONE_WIRE) ||
                    adj.getType().equals(Material.REDSTONE_TORCH_ON) ||
                    adj.getType().equals(Material.REDSTONE_TORCH_OFF) ||
                    adj.getType().equals(Material.RAILS) ||
                    adj.getType().equals(Material.POWERED_RAIL) ||
                    adj.getType().equals(Material.PISTON_BASE) ||
                    adj.getType().equals(Material.PISTON_STICKY_BASE) ||
                    adj.getType().equals(Material.WOODEN_DOOR) ||
                    adj.getType().equals(Material.IRON_DOOR_BLOCK) ||
                    adj.getType().equals(Material.DIODE))
                adj.update();
        }        
    }

    public boolean isDirect() {
        for (Location b : outputBlocks) {
            Material m = b.getBlock().getType();
            if (m==Material.LEVER || m==Material.POWERED_RAIL) return false;
        }
        
        return true;
    }

    public boolean getState() {
        return circuit.getOutputBits().get(index);
    }
}
