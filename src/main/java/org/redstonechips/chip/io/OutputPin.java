
package org.redstonechips.chip.io;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.NoteBlock;
import org.bukkit.material.Attachable;
import org.bukkit.material.Command;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.io.InputPin.SourceType;
import org.redstonechips.util.ChunkLocation;

/**
 * Represents a chip output pin.
 * 
 * @author Tal Eisenberg
 */
public class OutputPin extends IOBlock {
    public static final Material[] deviceMaterials = new Material[] { Material.LEVER, Material.REDSTONE_TORCH_OFF, 
        Material.REDSTONE_TORCH_ON, Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.TRAP_DOOR, 
        Material.POWERED_RAIL, Material.NOTE_BLOCK, Material.COMMAND, Material.DISPENSER, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON,
        Material.REDSTONE_WIRE};
    
    private final List<Location> outputBlocks;
    
    private boolean state = false;
    
    /**
     * Constructs an OutputPin object.
     * 
     * @param chip
     * @param outputBlock The pin block.
     * @param index The pin index in the circuit's output list.
     */
    public OutputPin(Chip chip, Location outputBlock, int index) {
        super(chip, outputBlock, index);
        
        outputBlocks = new ArrayList<>();
        
        addOutputBlock(new Location(chip.world, outputBlock.getBlockX(), outputBlock.getBlockY()+1, outputBlock.getBlockZ()));
        addOutputBlock(new Location(chip.world, outputBlock.getBlockX(), outputBlock.getBlockY()-1, outputBlock.getBlockZ()));
        addOutputBlock(new Location(chip.world, outputBlock.getBlockX()+1, outputBlock.getBlockY(), outputBlock.getBlockZ()));
        addOutputBlock(new Location(chip.world, outputBlock.getBlockX()-1, outputBlock.getBlockY(), outputBlock.getBlockZ()));
        addOutputBlock(new Location(chip.world, outputBlock.getBlockX(), outputBlock.getBlockY(), outputBlock.getBlockZ()+1));
        addOutputBlock(new Location(chip.world, outputBlock.getBlockX(), outputBlock.getBlockY(), outputBlock.getBlockZ()-1));        
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
                InputPin in = RedstoneChips.inst().chipManager().getAllChips().getInputPin(l);
                
                if (in!=null && shouldUpdateChunk(in.getLocation())) {
                    in.updateValue(loc.getBlock(), state, SourceType.DIRECT);
                } 
            }
        }

        this.state = state;
    }

    /**
     * 
     * @return The current state of the output block (on or off).
     */
    public boolean getState() {
        return state;
    }
    
    private boolean changeBlockState(Location outputLoc, boolean state) {
        Block outputBlock = outputLoc.getBlock();
        
        switch (outputBlock.getType()) {
            case REDSTONE_WIRE:
                updateRedstoneWire(outputBlock, state);
                break;
            case LEVER:
                if (!checkAttached(outputBlock)) return false;
                updateLever(outputBlock, state);
                break;
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
                if (!checkAttached(outputBlock)) return false;
                updateRedstoneTorch(outputBlock, state);                
                break;
            case REDSTONE_LAMP_ON:
            case REDSTONE_LAMP_OFF:
                updateRedstoneLamp(outputBlock, state);
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
                break;
            case DISPENSER:
                updateDispenser(outputBlock, state);
                break;
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
    
    private void updateRedstoneLamp(Block outputBlock, boolean state) {
        Material type = (state?Material.REDSTONE_LAMP_ON:Material.REDSTONE_LAMP_OFF);
        outputBlock.setType(type);
    }
    
    private void updatePoweredRail(Block outputBlock, boolean state) {
        if (updateBlockData(outputBlock, state)) {
            outputBlock.getState().update();
        }        
    }
    
    private void updateNoteBlock(Block outputBlock, boolean state) {
        if (state) {
            boolean wasPowered = this.state || outputBlock.isBlockIndirectlyPowered();
            if (wasPowered) return;
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
                chip.world.playEffect(outputBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
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
            chip.world.playEffect(outputBlock.getLocation(), Effect.DOOR_TOGGLE, 0);
        }        
    }
    
    private void updateCommandBlock(Block outputBlock, boolean state) {
        try {
            Command command = (Command)outputBlock.getState().getData();
            command.setPowered(false);        
            if (state) 
                command.setPowered(true);
        } catch (NullPointerException e) {
            RedstoneChips.inst().log(Level.WARNING, "Null-pointer exception while getting state of command block.");
            e.printStackTrace();
        }        
    }

    private void updateDispenser(Block outputBlock, boolean state) {        
        if (state) {
            boolean wasPowered = this.state || outputBlock.isBlockIndirectlyPowered() || outputBlock.getRelative(BlockFace.UP).isBlockIndirectlyPowered();
            if (wasPowered) return;
            Dispenser dispenser = (Dispenser)outputBlock.getState();
            dispenser.dispense();
        }
    }
    
    private void updateRedstoneWire(Block outputBlock, boolean state) {
        outputBlock.setData((byte)(state?15:0), true);
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
                    return false;
                    
            }
        }
        
        return true; 
    }
    
    /**
     * 
     * @param material
     * @return true if an output pin can change the state of material.
     */
    public static boolean isOutputMaterial(Material material) {
        for (Material m : deviceMaterials)
            if (m==material) return true;
        
        return false;
    }

    /**
     * Updates the state of the pin output blocks.
     */
    public void refreshOutputs() {
        for (Location l : this.outputBlocks) {
            if (ChunkLocation.fromLocation(l).isChunkLoaded()) changeBlockState(l, state);
        }
    }

    private boolean shouldUpdateChunk(Location l) {
        ChunkLocation chunk = ChunkLocation.fromLocation(l);
        return chunk.isChunkLoaded() && !RedstoneChips.inst().chipManager().isProcessingChunk(chunk);        
    }   
}
