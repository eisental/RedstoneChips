package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.io.IOBlock;

/**
 * Used for passing arguments when scanning recursively for chips.
 * 
 * @author Tal Eisenberg
 */
public class ScanParameters {
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
    
    public static ScanParameters generate(Block signBlock, Map<IOBlock.Type,MaterialData> ioMaterials) {
        if (signBlock.getType()!=Material.WALL_SIGN) return null;
        
        BlockFace direction = ((org.bukkit.material.Sign)signBlock.getState().getData()).getAttachedFace();
        Block firstChipBlock = signBlock.getRelative(direction);
        
        ScanParameters params = new ScanParameters();
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
        params.inputs = new ArrayList<Block>();
        params.outputs = new ArrayList<Block>();
        params.interfaces = new ArrayList<Block>();
        params.structure = new ArrayList<Block>();
        
        params.structure.add(signBlock);
        params.structure.add(firstChipBlock);
        params.direction = direction;
        
        return params;

    }
    
    public static ScanParameters generateDefaultParams(Block signBlock, RedstoneChips rc) {
        Map<IOBlock.Type, MaterialData> iom = new HashMap<IOBlock.Type, MaterialData>();
        iom.put(IOBlock.Type.INPUT, rc.getPrefs().getInputBlockType());
        iom.put(IOBlock.Type.OUTPUT, rc.getPrefs().getOutputBlockType());
        iom.put(IOBlock.Type.INTERFACE, rc.getPrefs().getInterfaceBlockType());
        
        return generate(signBlock, iom);
    }
}    

