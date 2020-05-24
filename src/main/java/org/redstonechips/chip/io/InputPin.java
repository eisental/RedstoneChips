package org.redstonechips.chip.io;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Switch;
import org.redstonechips.RCPrefs;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.Chip;
import org.redstonechips.util.ChunkLocation;

/**
 * Represents an input pin of a chip. 
 *
 * @author Tal Eisenberg
 */
public class InputPin extends IOBlock {
    /**
     * The type of a signal change source.
     */
    public enum SourceType { REDSTONE, DIRECT };
    
    private final Map<Location, Boolean> sourceBlocks;
    private final Location bottomSourceBlock;
    private long lastRedstoneChangeTick = -1;
    private int changesInTickCount = 0;
    private int inputsHigh;
    private boolean refreshRun;
    public static int maxInputChangesPerTick;
    /**
     *
     * @param chip
     * @param inputBlock The physical location of the input pin.
     * @param index The input pin order index in the circuit.
     */
    public InputPin(Chip chip, Location inputBlock, int index) {
        super(chip, inputBlock, index);

        // assuming circuit already has its structure block set up.
        sourceBlocks = new HashMap<>();

        addSourceBlock(new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY()+1, inputBlock.getBlockZ()));
        bottomSourceBlock = new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY()-1, inputBlock.getBlockZ());
        addSourceBlock(bottomSourceBlock);
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX()+1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX()-1, inputBlock.getBlockY(), inputBlock.getBlockZ()));
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()+1));
        addSourceBlock(new Location(chip.world, inputBlock.getBlockX(), inputBlock.getBlockY(), inputBlock.getBlockZ()-1));
    }

    /**
     * @return the state of the pin according to the state of its surrounding source blocks.
     */
    public boolean getPinValue() {
        return inputsHigh > 0;
    }

    private void addSourceBlock(Location loc) {
        if (!isPartOfStructure(loc)) {
            boolean state;

            Block b = chip.world.getBlockAt(loc);
            BlockData d = b.getBlockData();
            
            if (d instanceof RedstoneWire) {
            	state = ((RedstoneWire)d).getPower()>0;
            } else if (d instanceof Switch) {
            	state = ((Powerable)d).isPowered();
            } else {
            	state = false;
            }

            sourceBlocks.put(loc, state);
        }
    }

    /**
     * Updates the known state of one of the blocks surrounding the input block.
     *
     * @param block The location of the updated block.
     * @param newVal The new redstone current of the block.
     * @param source The source type.
     * @throws IllegalArgumentException If the block in the provided location is not a source block of the input.
     */
    public void updateValue(Block block, boolean newVal, SourceType source) throws IllegalArgumentException {
        Location l = block.getLocation();
        
        Boolean oldValue = sourceBlocks.get(l);
        if (oldValue == null) {
            RedstoneChips.inst().log(Level.WARNING, "Block @ " + block + " is not a power block of input " + index + " of chip " + chip);
        } else {
            if (source==SourceType.REDSTONE && l.equals(bottomSourceBlock)) {
                newVal = false;
            }
            
            if (oldValue == newVal) {
                return;
            }
            
            sourceBlocks.put(l, newVal);
            
            boolean oldPinValue = getPinValue();
            if (newVal) {
                inputsHigh++;
        //        System.out.println("Chip " + chip.toString() + " InputPin.updateValue inputsHigh ++ =" + inputsHigh);
                //System.out.println("updateValue's inputsHigh++ =" + inputsHigh++); 
            } else {
                inputsHigh--;                
                if (inputsHigh<0) {
                                  inputsHigh=0;
            //                      System.out.println("inputsHigh went negative");
                }
      //          System.out.println("Chip " + chip.toString() + " InputPin.updateValue inputsHigh -- =" + inputsHigh);
            }
            
            boolean newPinValue = getPinValue();
            if (oldPinValue == newPinValue) {
                return;
            }

            long curTick = chip.world.getFullTime();
            if (curTick==lastRedstoneChangeTick) {
                changesInTickCount++;
                if (changesInTickCount>InputPin.maxInputChangesPerTick) {
                    abortFeedbackLoop();
                }
            } else {
                changesInTickCount = 1;
            }

            lastRedstoneChangeTick = curTick;
            
            try {
                chip.inputChange(getIndex(), newPinValue);
            } catch (StackOverflowError e) {
                abortFeedbackLoop();
            }
        }

    }

    /**
     *
     * @return All source blocks surrounding this input pin.
     */
    public Iterable<Location> getSourceBlocks() {
        return sourceBlocks.keySet();
    }

    /**
     * Deactivates the circuit and sends a debug message alerting about a detected feedback loop.
     */
    private void abortFeedbackLoop() {
        if (chip.isDisabled()) {
            return;
        }
        
        if (chip.hasListeners()) {
            ChatColor errorColor = RCPrefs.getErrorColor();
            ChatColor debugColor = RCPrefs.getDebugColor();
            
            chip.notifyCircuitMessage(errorColor + "Possible infinite feedback loop " + debugColor + "detected in input " + errorColor + index + debugColor + ".");
            chip.notifyCircuitMessage("Use /rcenable to reactivate the circuit after solving the problem or destroy it normally.");
        }

        chip.disable();
    }

    /**
     * refreshes the state of all source blocks according to their block state.
     */
    public void refreshSourceBlocks() {
        //System.out.println("Previous inputsHigh = " + inputsHigh);            	
        inputsHigh = 0;
        refreshRun = false;
    	//System.out.println("After inputsHigh = 0 = " + inputsHigh);
        Location ioblock = getLocation();
        //System.out.println("Chip " + chip.toString() + " Function - refreshSourceBlocks");        
        for (Location l : this.sourceBlocks.keySet()) {
        	
        	if (ChunkLocation.fromLocation(l).isChunkLoaded()) {        	
               	int blockEW = l.getBlockX() - ioblock.getBlockX();
            	int blockNS = l.getBlockZ() - ioblock.getBlockZ();
            	int blockAB = l.getBlockY() - ioblock.getBlockY();
        //		System.out.println("Chip " + chip.toString() + " Chunk IS loaded");
                boolean high = findSourceBlockState(l);
        		sourceBlocks.put(l, high);
        		if (refreshRun==true) {
        //			System.out.println("Refresh Problem");
        			inputsHigh = 0;
        			refreshRun=false;
        		}
                if (high==true) {                	
                	//if (blockEW<0) System.out.println("Chip " + chip.toString() + " Source FOUND to the West for input " + getIndex() + " is West @ ");                	
                	//if (blockEW>0) System.out.println("Chip " + chip.toString() + " Source FOUND to the Eastfor input " + getIndex() + " is East");
                	//if (blockNS<0) System.out.println("Chip " + chip.toString() + " Source FOUND to the North for input " + getIndex() + " is North");
                	//if (blockNS>0) System.out.println("Chip " + chip.toString() + " Source FOUND to the Southfor input " + getIndex() + " is South");
                	//if (blockAB>0) System.out.println("Chip " + chip.toString() + " Source FOUND above for input " + getIndex() + " is Above");
                	//if (blockEW==0&&blockAB==0&&blockNS==0) System.out.println("Chip " + chip.toString() + " Source FOUND for input " + getIndex() + " is the input itself");
                	inputsHigh++;
                }
                else {
                	//if (blockEW<0) System.out.println("Chip " + chip.toString() + " Source NOT found to the West for input " + getIndex());
                	//if (blockEW>0) System.out.println("Chip " + chip.toString() + " Source NOT found to the East for input " + getIndex());
                	//if (blockNS<0) System.out.println("Chip " + chip.toString() + " Source NOT found to the North for input " + getIndex());
                	//if (blockNS>0) System.out.println("Chip " + chip.toString() + " Source NOT found to the South for input " + getIndex());
                	//if (blockAB>0) System.out.println("Chip " + chip.toString() + " Source NOT found Above for input " + getIndex());
                	//if (blockAB<0) System.out.println("Chip " + chip.toString() + " Source NOT found Below for input " + getIndex());
                }
                
            }
        	//else System.out.println("Chip " + chip.toString() + " Chunk is NOT Loaded");
        }
        refreshRun = true;
        //System.out.println("Chip " + chip.toString() + " inputsHigh AFTER refresh = " + inputsHigh);
    }

    /**
     *
     * @param loc The location of the block to check.
     * @return true if the block is powered and false otherwise.
     */
    public boolean findSourceBlockState(Location loc) {
        Block b = loc.getBlock();
        Material m = b.getType();
        
        switch (m) {
            case REDSTONE_WIRE: {
                if (loc.equals(bottomSourceBlock)) return false;
                else return ((RedstoneWire)b.getBlockData()).getPower() > 0;
            }
            case LEVER: {
            	//System.out.println("Chip " + chip.toString() + " Lever connection found");
                return ((Switch)b.getBlockData()).isPowered();
                }
            case REDSTONE_TORCH: {
            	return ((Lightable)b.getBlockData()).isLit();
            	}
            default: // looking for direct connection to an output block.
                OutputPin out = RedstoneChips.inst().chipManager().getAllChips().getOutputPin(loc);                
                if (out!=null && out.isDirect()) {
                	//System.out.println("Chip " + chip.toString() + " Direct connection found from " + out.chip.toString());
                	
                	if (out.chip!=chip) {
                		return out.getState();
                	}
                	else return false;
                }
                else return false;
        }     
    }
}
