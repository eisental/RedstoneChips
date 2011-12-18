
package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Attachable;
import org.bukkit.material.Door;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.circuit.InputPin.SourceType;

/**
 * Represents a chip output pin.
 * 
 * @author Tal Eisenberg
 */
public class OutputPin extends IOBlock {
    private static Material[] outputMaterials = new Material[] { Material.LEVER, Material.REDSTONE_TORCH_OFF, 
        Material.REDSTONE_TORCH_ON, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.TRAP_DOOR, Material.POWERED_RAIL };
    
    private List<Location> outputBlocks;
    
    /**
     * Constructs an OutputPin object.
     * 
     * @param circuit Owner of the output pin.
     * @param outputBlock The pin block.
     * @param index The pin index in the circuit's output list.
     */
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
    
    /**
     * 
     * @return a list of blocks surrounding the output block that can receive a signal from this output pin.
     */
    public List<Location> getOutputBlocks() {
        return outputBlocks;
    }

    private static final BlockFace[] adjacentFaces = new BlockFace[] { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    /**
     * Updates the state of blocks that are touching the output block.
     * 
     * @param state The new output state.
     */
    public void changeOutputState(boolean state) {  
        boolean hasActuator = false;
        for (Location l : outputBlocks) {            
            if (changeBlockState(l.getBlock(), state)) hasActuator = true;
        }
        
        if (!hasActuator) {
            for (Location l : outputBlocks) {
                InputPin i = circuit.getPlugin().getCircuitManager().getInputPin(l);

                if (i!=null) {
                    i.updateValue(loc.getBlock(), state, SourceType.DIRECT);
                } 
            }
            
        }
    }

    private boolean changeBlockState(Block outputBlock, boolean state) {
        Block ioBlock = loc.getBlock();        
        if (!circuit.world.isChunkLoaded(outputBlock.getChunk())) return true;        

        if (outputBlock.getType()==Material.LEVER) {
            Lever lever = (Lever)outputBlock.getState().getData();
            BlockFace f = lever.getAttachedFace();
            if (f!=null && outputBlock.getRelative(f).equals(ioBlock)) {
                if (updateBlockData(outputBlock, state)) {
                    outputBlock.getState().update();
                    updateAdjacentFaces(outputBlock);
                }
            } else return false;    
        } else if (outputBlock.getType()==Material.POWERED_RAIL) {
            if (updateBlockData(outputBlock, state)) {
                outputBlock.getState().update();
                updateAdjacentFaces(outputBlock);
            }
        } else if (outputBlock.getType()==Material.WOODEN_DOOR || outputBlock.getType()==Material.IRON_DOOR_BLOCK) {
            Block otherBlock = outputBlock.getRelative(BlockFace.UP);
            if (otherBlock.getType()!=outputBlock.getType()) {
                otherBlock = outputBlock.getRelative(BlockFace.DOWN);
                if (otherBlock.getType()!=outputBlock.getType())
                    otherBlock = null;
            }
                            
            if (otherBlock!=null) {
                BlockState s1 = outputBlock.getState();
                Door door = (Door)s1.getData();
                if (door.isOpen()!=state) {
                    door.setOpen(state);            
                    s1.setData(door);
                    s1.update();

                    BlockState s2 = otherBlock.getState();
                    Door door2 = (Door)s2.getData();
                    door2.setOpen(state);            
                    s2.setData(door2);
                    s2.update();
                    circuit.world.playEffect(outputBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
                }
            }
        } else if (outputBlock.getType()==Material.TRAP_DOOR) {
            BlockState s = outputBlock.getState();
            MaterialData md = s.getData();
            byte oldData = md.getData();
            if (state) md.setData((byte)(md.getData() | 0x4));
            else md.setData((byte)(md.getData() & 0x3));
            if (oldData!=md.getData()) {
                s.setData(md);
                s.update();
                circuit.world.playEffect(outputBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
            }
        } else if (outputBlock.getType()==Material.REDSTONE_TORCH_OFF || outputBlock.getType()==Material.REDSTONE_TORCH_ON) {            
            Attachable a = (Attachable)outputBlock.getState().getData();
            BlockFace f = a.getAttachedFace();
            Block attached;
            if (f!=null) attached = outputBlock.getRelative(f);
            else return false;

            if (attached.equals(ioBlock)) {
                outputBlock.setType(state?Material.REDSTONE_TORCH_ON:Material.REDSTONE_TORCH_OFF);
            } else return false;    
            
            
        } else return false;

        return true;
        
    }
    
    private boolean updateBlockData(Block b, boolean state) {
        byte data = b.getData();
        boolean oldLevel = ((data&0x08) > 0);
        if (oldLevel==state) return false;

        byte newData = (byte)(state? data | 0x8 : data & 0x7);

        b.setData(newData, true);
        
        return true;
    }

    private void updateAdjacentFaces(Block ioBlock) {
        for (BlockFace f : adjacentFaces) {
            BlockState adj = ioBlock.getRelative(f).getState();

            if (adj.getType().equals(Material.REDSTONE_WIRE) ||
                    adj.getType().equals(Material.REDSTONE_TORCH_ON) ||
                    adj.getType().equals(Material.REDSTONE_TORCH_OFF) ||
                    adj.getType().equals(Material.RAILS) ||
                    adj.getType().equals(Material.POWERED_RAIL) ||
                    adj.getType().equals(Material.PISTON_BASE) ||
                    adj.getType().equals(Material.PISTON_STICKY_BASE) ||
                    adj.getType().equals(Material.WOODEN_DOOR) ||
                    adj.getType().equals(Material.IRON_DOOR_BLOCK) ||
                    adj.getType().equals(Material.DIODE)) {
                //adj.getBlock().setData(adj.getData().getData(), true);
                //adj.update(true);
            }
        }        
    }

    /**
     * Returns whether this output pin uses direct connections. 
     * This is true only when there are no other output devices connected to the output block.
     */
    public boolean isDirect() {
        for (Location l : outputBlocks) {
            Block b = l.getBlock();
            Material m = b.getType();
            if (m==Material.LEVER ||  m==Material.REDSTONE_TORCH_OFF || m==Material.REDSTONE_TORCH_ON) {
                Attachable a = (Attachable)b.getState().getData();
                if (b.getRelative(a.getAttachedFace()).equals(loc.getBlock())) return false;
            } else if (m==Material.WOODEN_DOOR || m==Material.IRON_DOOR_BLOCK || m==Material.TRAP_DOOR || m==Material.POWERED_RAIL)
                return false;
        }
        
        return true; 
    }

    /**
     * 
     * @return The current state of the output block (on or off).
     */
    public boolean getState() {
        return circuit.getOutputBits().get(index);
    }
    
    /**
     * 
     * @param material
     * @return true if an output pin can change the state of material.
     */
    public static boolean isOutputMaterial(Material material) {
        for (Material m : outputMaterials)
            if (m==material) return true;
        
        return false;
    }

    /**
     * Updates the state of the pin output blocks.
     */
    public void refreshOutputs() {
        boolean state = this.getState();
        for (Location l : this.outputBlocks)
            changeBlockState(l.getBlock(), state);
    }
    
}
