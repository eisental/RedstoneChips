package org.redstonechips.chip.scan;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.io.IOBlock;
import org.redstonechips.chip.io.InputPin;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.chip.io.OutputPin;

/**
 * Used for passing arguments when creating a new chip.
 * 
 * @author Tal Eisenberg
 */
public class ChipParameters {
    /**
     * The chip block material of the scanned chip.
     */
    public Material chipMaterial;

    /**
     * The wool color in case the chip material is wool.
     */
    public DyeColor woolColor;

    /**
     * The input block material.
     */
    public MaterialData inputBlockType;

    /**
     * The output block material.
     */
    public MaterialData outputBlockType;

    /**
     * The interface block material.
     */
    public MaterialData interfaceBlockType;

    /**
     * The activation sign block
     */
    public Block signBlock;
    
    /**
     * The 1st block right after the activation sign. The block to which the sign is attached.
     */
    public Block origin;

    /**
     * The current scanning direction.
     */
    public BlockFace direction;

    /**
     * List of discovered input blocks.
     */
    public List<Block> inputs;

    /**
     * List of discovered output blocks.
     */
    public List<Block> outputs;

    /**
     * List of discovered interface blocks.
     */
    public List<Block> interfaces;

    /**
     * List of all discovered structure blocks. Includes any block that would break the circuit when broken.
     */
    public List<Block> structure;
    
    public static ChipParameters generate(Block signBlock, Map<IOBlock.Type,MaterialData> ioMaterials) {
        if (signBlock.getType()!=Material.WALL_SIGN) return null;

        BlockFace direction = ((org.bukkit.material.Sign)signBlock.getState().getData()).getAttachedFace();
        Block firstChipBlock = signBlock.getRelative(direction);
        
        ChipParameters params = new ChipParameters();
        params.signBlock = signBlock;
        params.chipMaterial = firstChipBlock.getType();
        if(params.chipMaterial.equals(Material.WOOL))
            params.woolColor=((Wool)(firstChipBlock.getState().getData())).getColor();
        else
            params.woolColor=null;
        params.inputBlockType = ioMaterials.get(IOBlock.Type.INPUT);
        params.outputBlockType = ioMaterials.get(IOBlock.Type.OUTPUT);
        params.interfaceBlockType = ioMaterials.get(IOBlock.Type.INTERFACE);
        params.origin = firstChipBlock;
        params.inputs = new ArrayList<>();
        params.outputs = new ArrayList<>();
        params.interfaces = new ArrayList<>();
        params.structure = new ArrayList<>();
        
        params.structure.add(signBlock);
        params.structure.add(firstChipBlock);
        params.direction = direction;
        
        return params;

    }
    
    public static ChipParameters generateDefaultParams(Block signBlock) {
        return generate(signBlock, defaultIOMaterials());
    }
    
    public static Map<IOBlock.Type, MaterialData> defaultIOMaterials() {
        RedstoneChips rc = RedstoneChips.inst();
        
        Map<IOBlock.Type, MaterialData> iom = new EnumMap<>(IOBlock.Type.class);
        iom.put(IOBlock.Type.INPUT, rc.prefs().getInputBlockType());
        iom.put(IOBlock.Type.OUTPUT, rc.prefs().getOutputBlockType());
        iom.put(IOBlock.Type.INTERFACE, rc.prefs().getInterfaceBlockType());
        
        return iom;
    }
    
    public static ChipParameters generateFromChip(Chip c) {        
        ChipParameters params = new ChipParameters();
        params.signBlock = c.activationBlock.getBlock();
        Block firstChipBlock = c.structure[1].getBlock();
        params.chipMaterial = firstChipBlock.getType();
        if(params.chipMaterial.equals(Material.WOOL))
            params.woolColor=((Wool)(firstChipBlock.getState().getData())).getColor();
        else
            params.woolColor=null;

        params.origin = firstChipBlock;
        
        params.inputs = new ArrayList<>();
        for (InputPin i : c.inputPins) params.inputs.add(i.getLocation().getBlock());
        if (!params.inputs.isEmpty())
            params.inputBlockType = params.inputs.get(0).getState().getData();
        
        params.outputs = new ArrayList<>();
        for (OutputPin o : c.outputPins) params.outputs.add(o.getLocation().getBlock());
        if (!params.outputs.isEmpty())
            params.outputBlockType = params.outputs.get(0).getState().getData();
        
        params.interfaces = new ArrayList<>();
        for (InterfaceBlock i : c.interfaceBlocks) params.interfaces.add(i.getLocation().getBlock());
        if (!params.interfaces.isEmpty())
            params.interfaceBlockType = params.interfaces.get(0).getState().getData();

        params.structure = new ArrayList<>();
        for (Location l : c.structure) params.structure.add(l.getBlock());
        params.direction = ((org.bukkit.material.Sign)params.signBlock.getState().getData()).getAttachedFace();
        
        return params;
    }
}    

