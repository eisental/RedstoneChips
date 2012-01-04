package org.tal.redstonechips.circuit;

import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;

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
}    

