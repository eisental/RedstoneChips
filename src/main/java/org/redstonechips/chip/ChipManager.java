package org.redstonechips.chip;

import java.util.*;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Redstone;
import org.redstonechips.PrefsManager;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.chip.io.IOBlock;
import org.redstonechips.chip.io.InputPin;
import org.redstonechips.chip.io.InputPin.SourceType;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.chip.io.OutputPin;
import org.redstonechips.chip.scan.ChipParameters;
import org.redstonechips.parsing.Parsing;
import org.redstonechips.user.Debugger;
import org.redstonechips.util.ChunkLocation;
import org.redstonechips.wireless.Wireless;

/**
 *
 * @author Tal Eisenberg
 */
public class ChipManager {

    private final RedstoneChips rc;
    
    private final ChipCollection chips = new ChipCollection();
    
    public ChipManager(RedstoneChips plugin) { rc = plugin; }
    
    public MaybeChip maybeScanChip(Block activationBlock, CommandSender activator, int verboseLevel) {
        return maybeScanChip(activationBlock, activator, ChipParameters.defaultIOMaterials(), verboseLevel);
    }
    
    public MaybeChip maybeScanChip(Block activationBlock, CommandSender activator, Map<IOBlock.Type, MaterialData> iom, int verboseLevel) {
        ChipParameters params = ChipParameters.generate(activationBlock, iom);
        if (params!=null) {
            MaybeChip mChip = ChipFactory.maybeCreateChip(params, activator);
            if (mChip==MaybeChip.AChip) {
                if (initializeChip(mChip.getChip(), activator, verboseLevel))
                    return mChip;
                else return MaybeChip.ChipError.withError("Could not initialize chip.");
            }
        } return MaybeChip.NotAChip;                
    }
    
    /**
     * Activates an already scanned chip.
     *
     * @param chip Chip to initialize.
     * @param activator The activator.
     * @param id The desired circuit id. When less than 0, a new id is generated.
     * @return true when the chip is successfully activated, false otherwise.
     */
    public boolean initializeChip(Chip chip, CommandSender activator, int id) {
        boolean ret;
        
        List<ChunkLocation> chunksToUnload = new ArrayList<>();
        
        for (ChunkLocation chunk : chip.chunks) {
            if (!chunk.isChunkLoaded()) {
                chunksToUnload.add(chunk);
                workOnChunk(chunk);
            }
        }
        
        if (chip.init(activator)) {
            if (id<0)
                chip.id = generateId();
            else
                chip.id = id;
            chips.put(chip.id, chip);

            if (activator != null) {
                ChatColor ic = rc.prefs().getInfoColor();
                ChatColor dc = rc.prefs().getDebugColor();
                activator.sendMessage(ic + "Activated " + ChatColor.YELLOW + chip + ic + ": "
                        + dc + ChatColor.GRAY + chip.inputPins.length + dc + " input"
                        + (chip.inputPins.length!=1?"s":"") + ", " + ChatColor.YELLOW + chip.outputPins.length + dc + " output"
                        + (chip.outputPins.length!=1?"s":"") + " and " + ChatColor.BLUE + chip.interfaceBlocks.length + dc
                        + " interface block" + (chip.interfaceBlocks.length!=1?"s":"") + ".");
            }

            chip.updateCircuitSign(true);

            ret = true;
        } else {
            //if (activator!=null)
            //    activator.sendMessage(rc.prefs().getErrorColor() + chip.getClass().getSimpleName() + " was not activated.");
            
            chip.updateCircuitSign(false);
            ret = false;
        }
        
        for (ChunkLocation chunk : chunksToUnload) {
            releaseChunk(chunk);
        }

        return ret;
    }

    /**
     * Redstone change event handler. Checks if this event reports an input change in any circuit's input pins.
     * When the new redstone state is different than the current one, the input pin is updated and the circuit is notified.
     *
     * @param b
     * @param newCurrent
     * @param oldCurrent
     */
    public void redstoneStateChanged(Block b, int newCurrent, int oldCurrent) { 
        boolean newVal = (newCurrent>0);
        boolean oldVal = (oldCurrent>0);
        if (newVal==oldVal) return; // not a change

        List<InputPin> inputList = chips.getInputPinBySource(b.getLocation());
        if (inputList==null) return;
        for (InputPin inputPin : inputList)
            inputPin.updateValue(b, newVal, SourceType.REDSTONE);
        
    }
    
    /**
     * Checks whether the block b is part of a circuit and if so deactivates the circuit.
     *
     * @param b The block that was broken.
     * @param s The breaker. Can be null.
     * @return True if a circuit was deactivated.
     */
    public boolean maybeChipDestroyed(Block b, CommandSender s) {
        Chip destroyed = chips.getByStructureBlock(b.getLocation());

        if (destroyed!=null && chips.containsValue(destroyed)) {
            if (destroyChip(destroyed, s, false)) {
                if (s!=null) s.sendMessage(rc.prefs().getErrorColor() + "You destroyed " + destroyed.toString() + ".");
            } else {
                return false;
            }
        }
        return true;
    }

    private final static Class redstoneClass = Redstone.class;

    /**
     * Called on block place and block break to see if any chip input pin state is affected by the change.
     *
     * @param block The block that was placed or broken.
     * @param player The player who placed or broke the block.
     * @param isBroken True if the block was broken and false if it was placed.
     * @return true if an input source block was placed or broken.
     */
    public boolean maybeChipInputBlockChanged(Block block, Player player, boolean isBroken) {
        Class<? extends MaterialData> dataClass = block.getType().getData();
        if (dataClass!=null && redstoneClass.isAssignableFrom(dataClass)) {
            List<InputPin> inputs = chips.getInputPinBySource(block.getLocation());
            if (inputs!=null) {
                for (InputPin pin : inputs) {
                    if (isBroken) pin.updateValue(block, false, SourceType.REDSTONE);
                    else pin.updateValue(block, pin.findSourceBlockState(block.getLocation()), SourceType.REDSTONE);
                }
                return true;
            }

        }
        
        return false;
    }

    /**
     * Called on block place to see if any circuit output pin needs to refresh.
     * 
     * @param block The block that was placed.
     * @param player The player who placed the block.
     * @return true if an output block was placed.
     */
    public boolean maybeChipOutputBlockPlaced(Block block, Player player) {
        if (OutputPin.isOutputMaterial(block.getType())) {
            final List<OutputPin> outputs = chips.getOutputPinByOutputBlock(block.getLocation());
            if (outputs!=null && !outputs.isEmpty()) {
                for (OutputPin pin : outputs) {
                    pin.refreshOutputs();
                }
                return true;
            }

        }
        
        return false;
    }
    
    public boolean destroyChip(Chip destroyed, CommandSender destroyer, boolean destroyBlocks) {
        return destroyChip(destroyed, destroyer, destroyBlocks, true);
    }
    
    /**
     * Deactivates the specified circuit, possibly changing all of its structure blocks into air.
     *
     * @param destroyed The circuit that was destroyed.
     * @param destroyer The circuit's destroyer.
     * @param destroyBlocks Whether the circuit's blocks should turn into air.
     * @param updateSign
     * @return true if successful.
     */
    public boolean destroyChip(Chip destroyed, CommandSender destroyer, boolean destroyBlocks, boolean updateSign) {
        if (destroyBlocks) {
            boolean enableDestroyCommand = (Boolean)rc.prefs().getPref(PrefsManager.Prefs.enableDestroyCommand.name());
            if (!enableDestroyCommand) {
                if (destroyer!=null) destroyer.sendMessage(rc.prefs().getErrorColor()+"/rcdestroy is disabled. You can enable it using /rcprefs enableDestroyCommand true");
                return false;
            }
        }

        if (!rc.permissionManager().checkChipPermission(destroyer, destroyed.getType(), false)) {
            if (destroyer!=null) destroyer.sendMessage(rc.prefs().getErrorColor() + "You do not have permission to destroy circuits of type " + destroyed.getType() + ".");
            return false;
        }
        
        if (destroyer!=null) {
            List<Wireless> list = rc.channelManager().getCircuitWireless(destroyed.circuit);
            for (Wireless w : list) {
                if (w.getChannel()!=null && !rc.permissionManager().enforceChannel(destroyer, w.getChannel(), true)) {                    
                    return false;
                }
            }
        }

        destroyed.chipDestroyed(destroyer);

        chips.remove(destroyed.id);
        
        if (destroyBlocks) {
            for (Location l : destroyed.structure)
                destroyed.world.getBlockAt(l).setType(Material.AIR);
        } else if (updateSign) {
            destroyed.updateCircuitSign(false);
        }        

        return true;
    }

    /**
     * Resets a circuit. First the circuit is deactivated and then reactivated. Any listeners of the circuit are copied over
     * to the new circuit.
     *
     * @param c The circuit to reset.
     * @param reseter The reseter.
     * @return true if the circuit was reactivated.
     */
    public boolean resetChip(Chip c, CommandSender reseter) {
        Block activationBlock = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
        List<ChipListener> listeners = c.getListeners();
        Map<String,Object> data = c.circuit.getResetData();

        int id = c.id;
        String name = c.name;
        
        if (!destroyChip(c, reseter, false, false)) return false; // do not try to update sign. we're not finished.
        MaybeChip mChip = ChipFactory.maybeCreateChip(ChipParameters.generateDefaultParams(activationBlock), reseter);

        
        if (mChip==MaybeChip.AChip) {            
            Chip newChip = mChip.getChip();
            for (ChipListener l : listeners) {
                if (l instanceof Debugger) ((Debugger)l).addChip(newChip);
                else newChip.addListener(l);
            }             
            
            if (initializeChip(newChip, reseter, id)) {
                newChip.name = name;
                newChip.circuit.setResetData(data);

                if (reseter!=null) reseter.sendMessage(rc.prefs().getInfoColor() + "Successfully reactivated " + ChatColor.YELLOW + newChip + rc.prefs().getInfoColor() + ".");
                return true;
            } else return false;
        } else {
            return false;
        }
    }

    /**
     * Calls Chip.shutdown on every activated circuit.
     */
    public void shutdownAllChips() {
        for (Chip c : chips.values()) c.shutdown();
    }

    /**
     * @return a map of all active circuits. The map keys are circuit ids.
     */
    public ChipCollection getAllChips() {
        return chips;
    }
    
    /**
     * Unloads all chip in the specified world.
     * 
     * @param unloadedWorld 
     */
    public void unloadWorldChips(World unloadedWorld) {
        Map<Integer, Chip> unloadedCircuits = chips.getInWorld(unloadedWorld);
        for (Chip c : unloadedCircuits.values()) {
            c.shutdown();
            
            chips.remove(c.id);
        }
    }
    
    /**
     * Called on every chunk load event. Finds any circuits in the loaded chunk and calls their .circuitChunkLoaded() method.
     * 
     * @param chunk The loaded chunk.
     */
    public void maybeChipChunkLoaded(ChunkLocation chunk) {
        if (processedChunks.contains(chunk)) return;
        List<Chip> chipsInChunk = chips.getInChunk(chunk);

        if (chipsInChunk!=null) {
            for (Chip c : chipsInChunk) {
                c.chipChunkLoaded();
            }
        }
    }
    
    /**
     * Generates a circuit id.
     * 
     * @return a unique (unused) circuit id number.
     */
    public int generateId() {
        int i = 0;

        if (chips!=null)
            while(chips.containsKey(i)) i++;

        return i;
    }    

    /**
     * Names a chip for id and debug purposes. 
     * 
     * @param target chip
     * @param name A unique (unused), non-number name.
     * @throws IllegalArgumentException when name is invalid.
     */
    public void nameChip(Chip target, String name) throws IllegalArgumentException {
        if (Parsing.isInt(name)) 
            throw new IllegalArgumentException("Chip name can't be a number.");
        
        Chip existing = chips.getById(name);
        if (existing!=null) 
            throw new IllegalArgumentException("Chip name is already used.");
        
        target.name = name;
    }
    
    // -- Maintain chip structure --
    
    /**
     * Check each active chip to see if all of its blocks are in place. See checkIntegrity().
     * Damaged chips are deactivated and destroyed.
     * Any unloaded chunks are first loaded and then unloaded after the check is over.
     * 
     * @param world Check circuits in this world.
     */
    public void checkChipsIntegrityIn(World world) {
        if (chips==null) return;

        List<Integer> invalidIds = new ArrayList<>();

        List<ChunkLocation> unloadedChunks = new ArrayList<>();
        
        for (Chip c : chips.values()) {
            if(c.world.equals(world)) {
                for (ChunkLocation chunk : c.chunks) {
                    if (!chunk.isChunkLoaded() && !unloadedChunks.contains(chunk))
                        unloadedChunks.add(chunk);
                }

                // we also might need to load/unload some chunks that don't have i/o blocks in them
                for (Location s : c.structure) {
                    ChunkLocation chunk = ChunkLocation.fromLocation(s);
                    if (!chunk.isChunkLoaded() && !unloadedChunks.contains(chunk))
                        unloadedChunks.add(chunk);
                }
            }
        }

        for (ChunkLocation c : unloadedChunks)
            workOnChunk(c);
        
        for (Chip c : chips.values()) {
            if(c.world.equals(world)) {
                if (!checkChipIntegrity(c)) {
                    invalidIds.add(c.id);
                }
            }
        }

        String msg = "";
        if (!invalidIds.isEmpty()) {
            String ids = "";

            for (int i : invalidIds) {
                Chip c = chips.get(i);
                String details = "("  + c.getType() + " @ " + c.activationBlock.getBlockX() + ", " + c.activationBlock.getBlockY() + ", " + c.activationBlock.getBlockZ() + " on " + c.world.getName() + ")";
                this.destroyChip(chips.get(i), null, false);
                ids += i + " " + details + ", ";
            }

            ids = ids.substring(0, ids.length()-2);

            msg = "Deactivated " + invalidIds.size() + " damaged circuits: " + ids;
        }

        for (ChunkLocation chunk : unloadedChunks) {
            releaseChunk(chunk);
        }

        if (!invalidIds.isEmpty()) rc.log(Level.INFO, "Done checking circuits. " + msg);
    }
    
    /**
     * Checks whether all of the chip blocks are in place.
     * Makes sure that each output lever block of lever material, checks that the activation sign is in place
     * and that none of the circuit's structure blocks are air.
     * 
     * @param c
     * @return True if the test passed.
     */
    public boolean checkChipIntegrity(Chip c) {
        if (c.world.getBlockTypeIdAt(c.activationBlock)!=Material.WALL_SIGN.getId()) {
            rc.log(Level.WARNING, "Circuit " + c.id + ": Sign is missing at " + c.activationBlock.getBlockX() + "," + c.activationBlock.getBlockY() + ", " + c.activationBlock.getBlockZ() + ".");
            return false;
        } 
        
        for (Location s : c.structure) {
            if (!s.equals(c.activationBlock)) {
                if (c.world.getBlockTypeIdAt(s)==Material.AIR.getId()) {
                    rc.log(Level.WARNING, "Circuit " + c.id + ": Chip block is missing at " + s.getBlockX() + "," + s.getBlockY() + ", " + s.getBlockZ() + ".");
                    return false;
                }
            }
        }

        return true;
    }    
    
    /**
     * Replaces the chip input, output and interface block materials to the currently set materials in the plugin preferences.
     * @param c
     * @return The number of blocks that were replaced.
     */
    public int fixIOBlocks(Chip c) {
        int blockCount = 0;

        int inputType = rc.prefs().getInputBlockType().getItemTypeId();
        byte inputData = rc.prefs().getInputBlockType().getData();

        int outputType = rc.prefs().getOutputBlockType().getItemTypeId();
        byte outputData = rc.prefs().getOutputBlockType().getData();

        int interfaceType = rc.prefs().getInterfaceBlockType().getItemTypeId();
        byte interfaceData = rc.prefs().getInterfaceBlockType().getData();

        List<ChunkLocation> chunksToUnload = new ArrayList<>();
        for (ChunkLocation chunk : c.chunks) {
            if (!chunk.isChunkLoaded()) {
                chunksToUnload.add(chunk);
                workOnChunk(chunk);
            }
            
        }

        for (InputPin i : c.inputPins) {
            Block input = i.getLocation().getBlock();

            if (input.getTypeId()!=inputType || input.getData()!=inputData) {
                input.setTypeIdAndData(inputType, inputData, false);
                blockCount++;
            }
        }

        for (OutputPin o : c.outputPins) {
            Block output = o.getLocation().getBlock();

            if (output.getTypeId()!=outputType || output.getData()!=outputData) {
                output.setTypeIdAndData(outputType, outputData, false);
                blockCount++;
            }
        }

        for (InterfaceBlock t : c.interfaceBlocks) {
            Block tb = t.getLocation().getBlock();

            if (tb.getTypeId()!=interfaceType || tb.getData()!=interfaceData) {
                tb.setTypeIdAndData(interfaceType, interfaceData, false);
                blockCount++;
            }
        }

        for (ChunkLocation chunk : chunksToUnload) {
            releaseChunk(chunk);
        }

        return blockCount;
    }    
    // -- Chunk processing methods --
    
    private static final List<ChunkLocation> processedChunks = new ArrayList<>();
    
    /**
     * Mark a chunk as a processed chunk. While marked the chunk would not be allowed to unload.
     * Call releaseChunk(chunk) to release it.
     * 
     * @param chunk The chunk to keep alive.
     */
    public void workOnChunk(ChunkLocation chunk) {
        if (!processedChunks.contains(chunk)) {
            processedChunks.add(chunk);
            chunk.loadChunk();
        }
    }
    
    /**
     * Release a used chunk and allow it to unload.
     * 
     * @param chunk The chunk to release
     */
    public void releaseChunk(ChunkLocation chunk) {
        if (processedChunks.remove(chunk))
            chunk.unloadChunk();
    }

    /**
     * 
     * @param chunk The chunk to test.
     * @return true if the chunk was loaded, or is kept loaded, for processing.
     */
    public boolean isProcessingChunk(ChunkLocation chunk) {
        return processedChunks.contains(chunk);
    }
}
