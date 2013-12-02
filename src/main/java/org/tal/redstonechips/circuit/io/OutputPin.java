
package org.tal.redstonechips.circuit.io;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;
import org.bukkit.material.Attachable;
import org.bukkit.material.Command;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.io.InputPin.SourceType;
import org.tal.redstonechips.util.ChunkLocation;

/**
 * Represents a chip output pin.
 * 
 * @author Tal Eisenberg
 */
public class OutputPin extends IOBlock {
    private static final Material[] outputMaterials = new Material[] { Material.LEVER, Material.REDSTONE_TORCH_OFF, 
        Material.REDSTONE_TORCH_ON, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.TRAP_DOOR, 
        Material.POWERED_RAIL, Material.NOTE_BLOCK, Material.COMMAND };
    
    private final List<Location> outputBlocks;
    
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

    /**
     * Updates the state of blocks that are touching the output block.
     * 
     * @param state The new output state.
     */
    public void setState(boolean state) {  
        boolean hasActuator = false;
        
        for (Location l : outputBlocks) { 
            if (shouldUpdateChunk(l)) {
                if (changeBlockState(l, state)) hasActuator = true;
            }
        }
        
        if (!hasActuator) {
            for (Location l : outputBlocks) {
                InputPin i = circuit.getPlugin().getCircuitManager().getInputPin(l);
                
                if (i!=null && shouldUpdateChunk(i.getLocation())) {
                    i.updateValue(loc.getBlock(), state, SourceType.DIRECT);
                } 
            }
        }
    }

    private boolean changeBlockState(Location outputLoc, boolean state) {
        Block outputBlock = outputLoc.getBlock();
        
        switch (outputBlock.getType()) {
            case LEVER:
                if (!checkAttached(outputBlock)) return false;
                updateLever(outputBlock, state);
                break;
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
                if (!checkAttached(outputBlock)) return false;
                updateRedstoneTorch(outputBlock, state);                
                break;                
            case POWERED_RAIL: updatePoweredRail(outputBlock, state);
                break;
            case IRON_DOOR_BLOCK:
            case WOODEN_DOOR: updateDoor(outputBlock, state);
                break;
            case TRAP_DOOR: updateTrapDoor(outputBlock, state);
                break;
            case NOTE_BLOCK: updateNoteBlock(outputBlock, state);
                break;
            case COMMAND: updateCommandBlock(outputBlock, state);
            default:
                return false;
        }
        
        return true;
    }

    private boolean checkAttached(Block outputDevice) {
        Attachable a = (Attachable)outputDevice.getState().getData();
        BlockFace f = a.getAttachedFace();
        return f!=null && outputDevice.getRelative(f).equals(loc.getBlock());
    }
    
    private void updateLever(Block outputBlock, boolean state) {
        if (updateBlockData(outputBlock, state)) {
            outputBlock.getState().update();
            Block b = loc.getBlock();
            byte oldData = b.getData();
            byte notData;
            if (oldData>1) notData = (byte)(oldData-1);
            else if (oldData<15) notData = (byte)(oldData+1);
            else notData = 0;
            b.setData(notData, true);
            b.setData(oldData, true);
        }
    }
    
    private void updateRedstoneTorch(Block outputBlock, boolean state) {
        byte oldData = outputBlock.getData();
        int type = (state?Material.REDSTONE_TORCH_ON:Material.REDSTONE_TORCH_OFF).getId();
        outputBlock.setTypeIdAndData(type, oldData, true);
    }
    
    private void updatePoweredRail(Block outputBlock, boolean state) {
        if (updateBlockData(outputBlock, state)) {
            outputBlock.getState().update();
        }        
    }
    
    private void updateNoteBlock(Block outputBlock, boolean state) {
        if (state) {
            NoteBlock note = (NoteBlock)outputBlock.getState();
            note.play();
        }        
    }
    
    private void updateDoor(Block outputBlock, boolean state) {
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
    }
    
    private void updateTrapDoor(Block outputBlock, boolean state) {
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
    }
    
    private boolean updateBlockData(Block b, boolean state) {
        byte data = b.getData();
        boolean oldLevel = ((data&0x08) > 0);
        if (oldLevel==state) return false;

        byte newData = (byte)(state? data | 0x8 : data & 0x7);

        b.setData(newData, true);
        
        return true;
    }

    /**
     * Return true only when there are no other output devices connected to the output block.
     * @return whether this output pin uses direct connections. 
     */
    public boolean isDirect() {
        for (Location l : outputBlocks) {
            Block b = l.getBlock();
            Material m = b.getType();
            switch (m) {
                case LEVER:
                case REDSTONE_TORCH_OFF:
                case REDSTONE_TORCH_ON:
                    Attachable a = (Attachable)b.getState().getData();
                    BlockFace f = a.getAttachedFace();
                    if (f!=null && b.getRelative(f).equals(loc.getBlock())) return false;
                    break;
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                case TRAP_DOOR:
                case POWERED_RAIL:
                case NOTE_BLOCK:
                case COMMAND:
                default:
                    return false;
                    
            }
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
        for (Location l : this.outputBlocks) {
            if (ChunkLocation.fromLocation(l).isChunkLoaded()) changeBlockState(l, state);
        }
    }

    private boolean shouldUpdateChunk(Location l) {
        ChunkLocation chunk = ChunkLocation.fromLocation(l);
        return chunk.isChunkLoaded() && !circuit.getPlugin().getCircuitManager().isProcessingChunk(chunk);        
    }   

    private void updateCommandBlock(Block outputBlock, boolean state) {
        Command command = (Command)outputBlock.getState().getData();
        command.setPowered(false);
        command.setPowered(true);
    }
}