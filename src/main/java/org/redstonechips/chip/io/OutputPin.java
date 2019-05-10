
package org.redstonechips.chip.io;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.RedstoneRail;
import org.bukkit.block.data.type.Switch;
import org.bukkit.material.Attachable;
import org.bukkit.material.Command;
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
	public final static Class[] allowedDataTypes = new Class[] { 
		Lightable.class, 
		Switch.class, 
		Powerable.class,
		AnaloguePowerable.class,
		NoteBlock.class, 
		CommandBlock.class,
		Dispenser.class,
		Openable.class
	};
	
    public static boolean isAllowedDataType(BlockData d) {
    	for (Class c: allowedDataTypes) {
    		if (c.isAssignableFrom(d.getClass())) return true; 
    	}
    	
    	return false;
    }
	
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
     * Updates the output pin state. Only updates when the new state is different 
     * than the pin state in memory.
     * 
     * @param state The new output state.
     */
    public void setState(boolean state) { 
        if (this.state != state) forceState(state);
    }
    
    /**
     * Forces updating the output pin state. 
     * 
     * @param state The new output state.
     */
    public void forceState(boolean state) {
        this.state = state;
        
        for (Location l : outputBlocks) {       
            if (shouldUpdateChunk(l)) {
                InputPin in = RedstoneChips.inst().chipManager().getAllChips().getInputPin(l);
                if (in==null) changeBlockState(l, state);
                else in.updateValue(loc.getBlock(), state, SourceType.DIRECT);
            }
        }                   
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
        BlockData data = outputBlock.getBlockData();

//        System.out.println("block=" + outputBlock + " interfaces: " + Arrays.toString(outputBlock.getClass().getInterfaces()));
//        System.out.println("data=" + data + ": " + data.getClass() + " , " + Arrays.toString(data.getClass().getInterfaces()));        
//        System.out.println("state=" + outputBlock.getState() + " interfaces: " + Arrays.toString(outputBlock.getState().getClass().getInterfaces()));        
        if (data instanceof Dispenser) {
        	if (state) {
        		((org.bukkit.block.Dispenser)outputBlock.getState()).dispense();
        	}
        } else if (data instanceof NoteBlock) {
        	// DOESN'T WORK
        	
        } else if (data instanceof CommandBlock) {
        	updateCommandBlock(outputBlock, state); // DOESN'T WORK
        	
        } else if (data instanceof RedstoneRail) {
        	((RedstoneRail)data).setPowered(state); // DOESN'T WORK
        	
        } else if (data instanceof Openable) {
        	((Openable)data).setOpen(state);
        	
        } else if (data instanceof Switch) {
            if ((data instanceof Directional) && (!checkAttached(outputBlock))) 
        		return false;

            ((Switch)data).setPowered(state);
            
        } else if (data instanceof Lightable) {
            if ((data instanceof Directional) && (!checkAttached(outputBlock))) 
        		return false;
        	
        	((Lightable)data).setLit(state);
        	
        } else if (data instanceof Powerable) {
            if ((data instanceof Directional) && (!checkAttached(outputBlock))) 
        		return false;        
            
        	((Powerable)data).setPowered(state);
        	
        } else if (data instanceof AnaloguePowerable) {
        	((AnaloguePowerable)data).setPower(state?15:0);
        	
        } else {
        	return false;
        }

        outputBlock.setBlockData(data);
        outputBlock.getState().update();
        return true;
    }

    private boolean checkAttached(Block outputDevice) {
        Directional a = (Directional)outputDevice.getBlockData();
        BlockFace f = a.getFacing().getOppositeFace();
        return f!=null && outputDevice.getRelative(f).equals(loc.getBlock());
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
        
    /**
     * Return true only when there are no other output devices connected to the output block.
     * @return whether this output pin uses direct connections. 
     */
    public boolean isDirect() {
        for (Location l : outputBlocks) {
            Block b = l.getBlock();
            BlockData d = b.getBlockData();

            if (d instanceof Attachable) {
                BlockFace f = ((Attachable)d).getAttachedFace();
                if (f!=null && b.getRelative(f).equals(loc.getBlock())) return false;            	
            } else if (isAllowedDataType(d)) {
            	return false;
            }
        }
        
        return true; 
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
