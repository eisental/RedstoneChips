package org.tal.redstonechips;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.tal.redstonechips.util.Locations;

/**
 *
 * @author Tal Eisenberg
 */
public class ChipScanner {

    public static class ChipScanException extends RuntimeException {
        public ChipScanException(String string) {
            super(string);
        }
    }

    /**
     * Used for passing argument when scanning recursively for chips.
     */
    public class ScanParameters {
        /**
         * The chip block material of the scanned chip.
         */
        Material chipMaterial;
        
        /**
         * The wool color in case the chip material is wool.
         */
        DyeColor woolColor;

        /**
         * The input block material.
         */
        MaterialData inputBlockType;

        /**
         * The output block material.
         */
        MaterialData outputBlockType;

        /**
         * The interface block material.
         */
        MaterialData interfaceBlockType;

        /**
         * The 1st block right after the activation sign. The block to which the sign is attached.
         */
        Block origin;

        /**
         * The current scanning direction.
         */
        BlockFace direction;

        /**
         * List of discovered input blocks.
         */
        List<Block> inputs;

        /**
         * List of discovered output blocks.
         */
        List<Block> outputs;

        /**
         * List of discovered interface blocks.
         */
        List<Block> interfaces;

        /**
         * List of all discovered structure blocks. Includes any block that would break the circuit when broken.
         */
        List<Block> structure;
    }
        
    /**
     * Scans the chip structure starting from the sign block.
     * 
     * @param signBlock The sign block of the scanned chip.
     * @return A ScanParameters object describing the chip structure.
     */
    public ScanParameters scan(Block signBlock, MaterialData inputBlockType, MaterialData outputBlockType, MaterialData interfaceBlockType) {
        BlockFace direction = ((org.bukkit.material.Sign)signBlock.getState().getData()).getAttachedFace();
        Block firstChipBlock = signBlock.getRelative(direction);

        ScanParameters params = new ScanParameters();
        params.chipMaterial = firstChipBlock.getType();
        if(params.chipMaterial.equals(Material.WOOL))
            params.woolColor=((Wool)(firstChipBlock.getState().getData())).getColor();
        else
            params.woolColor=null;
        params.inputBlockType = inputBlockType;
        params.outputBlockType = outputBlockType;
        params.interfaceBlockType = interfaceBlockType;
        params.origin = firstChipBlock;
        params.inputs = new ArrayList<Block>();
        params.outputs = new ArrayList<Block>();
        params.interfaces = new ArrayList<Block>();
        params.structure = new ArrayList<Block>();
        params.structure.add(signBlock);
        params.structure.add(firstChipBlock);
        params.direction = direction;
        
        if (!isTypeAllowed(params, firstChipBlock.getType(), firstChipBlock.getData())) {
            throw new ChipScanException("You can't build a redstone chip using this material (" + firstChipBlock.getType().name() + "). Either it doesn't work as a chip block or it already has another function as an i/o block.");
        }
        
        try {            
            scanBranch(params);
            return params;
        } catch (StackOverflowError se) {
            throw new ChipScanException("If you're trying to build a redstone chip, your chip is way too big.");
        }

    }
    
    private void scanBranch(ScanParameters params) {
        // look in every horizontal direction for inputs, outputs or interface blocks.
        checkAttachedIO(params);

        // look in every direction, inculding up and down for more chip blocks.
        checkAttachedChipBlock(params);
    }

    private void checkAttachedChipBlock(ScanParameters params) {
        BlockFace direction = params.direction;
        Block origin = params.origin;

        // look for chip blocks to the right
        params.direction = Locations.getRightFace(direction);
        checkForChipBlockOnSideFace(params);

        // look for chip blocks to the left
        params.direction = Locations.getLeftFace(direction);
        checkForChipBlockOnSideFace(params);
        
        // look for chip blocks in original direction
        params.direction = direction;
        checkForChipBlockOnSideFace(params);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        params.direction = direction.getOppositeFace();
        checkForChipBlockOnSideFace(params);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getRelative(BlockFace.UP);

        if (!params.structure.contains(up) && ((!up.getType().equals(Material.WOOL) && up.getType()==params.chipMaterial) || (up.getType().equals(Material.WOOL) && ((Wool)(up.getState().getData())).getColor().equals(params.woolColor)))) {
            params.structure.add(up);
            params.direction = direction;
            params.origin = up;
            scanBranch(params);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getRelative(BlockFace.DOWN);

        if (!params.structure.contains(down) && ((!down.getType().equals(Material.WOOL) && down.getType()==params.chipMaterial) || (down.getType().equals(Material.WOOL) && ((Wool)(down.getState().getData())).getColor().equals(params.woolColor)))) {
            params.structure.add(down);
            params.direction = direction;
            params.origin = down;
            scanBranch(params);
        }

        params.direction = direction;
        params.origin = origin;
    }

    private void checkAttachedIO(ScanParameters params) {
        BlockFace face = params.direction;

        params.direction = Locations.getRightFace(face);
        checkForIO(params);

        params.direction = Locations.getLeftFace(face);
        checkForIO(params);

        params.direction = face;
        checkForIO(params);

        params.direction = face.getOppositeFace();
        checkForIO(params);

        params.direction = BlockFace.UP;
        checkForIO(params);

        params.direction = BlockFace.DOWN;
        checkForIO(params);

        params.direction = face;
    }

    private void checkForIO(ScanParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if (b.getType()==params.inputBlockType.getItemType()
                    && (b.getData()==params.inputBlockType.getData() || params.inputBlockType.getData()==-1)) {
                params.structure.add(b);
                params.inputs.add(b);
            } else if (b.getType()==params.outputBlockType.getItemType()
                    && (b.getData()==params.outputBlockType.getData() || params.outputBlockType.getData()==-1)) {
                params.structure.add(b);
                params.outputs.add(b);
            } else if (b.getType()==params.interfaceBlockType.getItemType()
                    && (b.getData()==params.interfaceBlockType.getData() || params.interfaceBlockType.getData()==-1)) {
                params.structure.add(b);
                params.interfaces.add(b);
            }
        }
    }

    private void checkForChipBlockOnSideFace(ScanParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if ((!b.getType().equals(Material.WOOL) && b.getType()==params.chipMaterial) || (b.getType().equals(Material.WOOL) && ((Wool)(b.getState().getData())).getColor().equals(params.woolColor))) {
                params.structure.add(b);
                Block origin = params.origin;
                params.origin = b;

                scanBranch(params);

                params.origin = origin;
            }
        }
    }

    private boolean isTypeAllowed(ScanParameters params, Material material, byte data) {
        return !matchMaterial(material, params.inputBlockType, data) &&
                !matchMaterial(material, params.outputBlockType, data) &&
                !matchMaterial(material, params.interfaceBlockType, data) &&
                material.isBlock() && material!=Material.GRAVEL && material!=Material.SAND;
    }

    private boolean matchMaterial(Material m, MaterialData md, byte data) {
        if (m!=md.getItemType()) return false;
        else if (m==Material.WOOL) {
            return data==md.getData();
        } else return true;
        
    }    
}
