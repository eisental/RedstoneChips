package org.redstonechips.chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.redstonechips.chip.io.InputPin;
import org.redstonechips.chip.io.OutputPin;
import org.redstonechips.parsing.Parsing;
import org.redstonechips.util.ChunkLocation;

/**
 * A collection of chips mapped to their id numbers. Used for referencing all the
 * chips running on the server. Contains many methods and lookup maps for quickly
 * finding chips according to various criteria.
 * 
 * @author taleisenberg
 */
public class ChipCollection extends HashMap<Integer, Chip> {
    private final Map<ChunkLocation, List<Chip>> chunkLookupMap = new HashMap<>();
    private final Map<Location, List<InputPin>> sourceLookupMap = new HashMap<>();
    private final Map<Location, InputPin> inputPinLookupMap = new HashMap<>();
    private final Map<Location, OutputPin> outputPinLookupMap = new HashMap<>();
    private final Map<Location, List<OutputPin>> outputLookupMap = new HashMap<>();
    private final Map<Location, Chip> structureLookupMap = new HashMap<>();
    private final Map<Location, Chip> activationLookupMap = new HashMap<>();
    
    /**
     *
     * @param structureBlock
     * @return The chip that the block belongs to or null if a circuit was not found.
     */
    public Chip getByStructureBlock(Location structureBlock) {
        return structureLookupMap.get(structureBlock);
    }

    /**
     *
     * @param activationBlock
     * @return The chip that uses this activation sign or null if a circuit was not found.
     */
    public Chip getByActivationBlock(Location activationBlock) {
        return activationLookupMap.get(activationBlock);
    }

    /**
     * 
     * @param outputBlock An output block of a chip.
     * @return The output pin represented by this block or null if none was found.
     */
    public OutputPin getOutputPin(Location outputBlock) {
        return outputPinLookupMap.get(outputBlock);
    }

    /**
     * 
     * @param inputBlock An input block of a chip.
     * @return The input pin represented by this block or null if none was found.
     */    
    public InputPin getInputPin(Location inputBlock) {
        return this.inputPinLookupMap.get(inputBlock);
    }
    
    /**
     * 
     * @param sourceBlock A signal source block surrounding an input block.
     * @return A list of input pins that can receive a signal from this block or null if none was found.
     */
    public List<InputPin> getInputPinBySource(Location sourceBlock) {
        return sourceLookupMap.get(sourceBlock);
    }

    /**
     * 
     * @param outputBlock A block that receives signal from an output pin.
     * @return A list of output pins that can change the state of the output block or null if none was found.
     */
    public List<OutputPin> getOutputPinByOutputBlock(Location outputBlock) {
        return outputLookupMap.get(outputBlock);
    }
    
    /**
     * 
     * @param id A chip id number or name.
     * @return The chip that has this id or null if none was found.
     */
    public Chip getById(String id) {  
        if (id==null) return null;

        if (Parsing.isInt(id)) { // as id number
            return get(Integer.decode(id));
        } else { // as name
            for (Chip c : values()) {
                if (c.name!=null && c.name.equals(id)) return c;
            }
        }
        
        return null;
    }    

    /**
     * @param world
     * @return a map of all active chips in the specified world. The map keys are chip ids.
     */
    public Map<Integer, Chip> getInWorld(World world) {
        Map<Integer, Chip> worldCircuits = new HashMap<>();
        for (Integer id : keySet()) {
          Chip c = get(id);
          if (c.world.equals(world)) {
            worldCircuits.put(id,c);
          }
        }
        
        return worldCircuits;
    }
    
    /**
     * 
     * @param chunk
     * @return All chips in chunk or null if none was found.
     */
    public List<Chip> getInChunk(ChunkLocation chunk) {
        return chunkLookupMap.get(chunk);
    }
    
    /**
     * Adds a chip to the collection. Updating lookup maps for fast retrieval.
     * @param id Chip id number.
     * @param chip The chip.
     * @return The result of HashMap.put(id, chip)
     */
    @Override
    public Chip put(Integer id, Chip chip) {
        addToLookupTables(chip); 
        return super.put(id, chip);
    }
    
    /**
     * Adds a chip to all lookup tables for fast lookups.
     * 
     * @param c A chip.
     */
    private void addToLookupTables(Chip c) {
        for (Location structure : c.structure)
            structureLookupMap.put(structure, c);

        activationLookupMap.put(c.activationBlock, c);
        
        for (InputPin input : c.inputPins) {
            for (Location l : input.getSourceBlocks()) {
                if (!sourceLookupMap.containsKey(l))
                    sourceLookupMap.put(l, new ArrayList<InputPin>());
                sourceLookupMap.get(l).add(input);
            }
            inputPinLookupMap.put(input.getLocation(), input);
        }
        
        for (OutputPin output : c.outputPins) {
            for (Location l : output.getOutputBlocks()) {
                if (!outputLookupMap.containsKey(l))
                    outputLookupMap.put(l, new ArrayList<OutputPin>());
                outputLookupMap.get(l).add(output);
            }
            outputPinLookupMap.put(output.getLocation(), output);
        }
        
        for (ChunkLocation chunk : c.chunks) {
            if (!chunkLookupMap.containsKey(chunk))
                chunkLookupMap.put(chunk, new ArrayList<Chip>());
            chunkLookupMap.get(chunk).add(c);
        }
    }

    /**
     * Removes a chip from the collection and all lookup maps contained within.
     * @param id An Integer object boxing the chip id number.
     * @return The removed chip or null if the chip was not found.
     */
    @Override
    public Chip remove(Object id) {
        Chip c = super.remove(id);
        if (c!=null) removeCircuitLookups(c);
        return c;
    }    

    /**
     * {@inheritDoc}
     * 
     * Clears all lookup maps as well.
     */
    @Override
    public void clear() {
        structureLookupMap.clear();
        outputPinLookupMap.clear();
        inputPinLookupMap.clear();
        sourceLookupMap.clear();
        outputLookupMap.clear();
        activationLookupMap.clear();
        chunkLookupMap.clear();
        
        super.clear(); 
    }
    /**
     * Removes a chip from all lookup tables.
     * 
     * @param c A chip.
     */
    private void removeCircuitLookups(Chip c) {
        for (Location l : c.structure)
            structureLookupMap.remove(l);

        for (OutputPin o : c.outputPins) {
            outputPinLookupMap.remove(o.getLocation());
        }

        for (InputPin i : c.inputPins) {
            inputPinLookupMap.remove(i.getLocation());
        }
        
        List<Location> inputBlocksToRemove = new ArrayList<>();
        for (Location l : sourceLookupMap.keySet()) {
            List<InputPin> pins = sourceLookupMap.get(l);
            List<InputPin> toRemove = new ArrayList<>();
            for (InputPin pin : pins) {
                if (pin.getChip()==c)
                    toRemove.add(pin);
            }

            pins.removeAll(toRemove);
            if (pins.isEmpty())
                inputBlocksToRemove.add(l);
        }
        for (Location l : inputBlocksToRemove)
            sourceLookupMap.remove(l);
        
        List<Location> outputBlocksToRemove = new ArrayList<>();
        for (Location l : outputLookupMap.keySet()) {
            List<OutputPin> pins = outputLookupMap.get(l);
            List<OutputPin> toRemove = new ArrayList<>();
            for (OutputPin pin : pins) {
                if (pin.getChip()==c)
                    toRemove.add(pin);
            }
            
            pins.removeAll(toRemove);
            if (pins.isEmpty())
                outputBlocksToRemove.add(l);
        }
        for (Location l : outputBlocksToRemove)
            outputLookupMap.remove(l);
        
        activationLookupMap.remove(c.activationBlock);

        List<ChunkLocation> emptyChunks = new ArrayList<>();

        for (ChunkLocation loc : c.chunks) {
            if (chunkLookupMap.containsKey(loc)) {
                List<Chip> ccircuits = chunkLookupMap.get(loc);

                if (ccircuits!=null) {
                    ccircuits.remove(c);
                    if (ccircuits.isEmpty())
                        emptyChunks.add(loc);
                }
            }
        }
        
        for (ChunkLocation loc : emptyChunks)
           chunkLookupMap.remove(loc);
    }    
}
