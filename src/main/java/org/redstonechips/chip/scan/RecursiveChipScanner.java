package org.redstonechips.chip.scan;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.redstonechips.util.Locations;

/**
 *
 * @author Tal Eisenberg
 */
public class RecursiveChipScanner extends IOChipScanner {    
    /**
     * Scans the chip structure starting from the sign block.
     * 
     * @param params
     * @throws ChipScanException if the structure is not a valid chip.
     */
    @Override
    public void scan(ChipParameters params) throws ChipScanException {        
        if (debugger!=null) {
            debug(1, "Origin block: " + prettyLoc(params.origin));
        }
        
        try {            
            scanBranch(params);
        } catch (StackOverflowError se) {
            throw new ChipScanException("If you're trying to build a redstone chip, your chip is way too big.");
        }

    }
    
    private void scanBranch(ChipParameters params) {
        // look in every direction for inputs, outputs or interface blocks.
        checkAttachedIO(params);

        // look in every direction, including up and down for more chip blocks.
        checkAttachedChipBlock(params);
    }

    private void checkAttachedChipBlock(ChipParameters params) {
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

        if (!params.structure.contains(up) && up.getType()==params.chipMaterial) { 
            params.structure.add(up);
            params.direction = direction;
            stepTo(params, up);
            scanBranch(params);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getRelative(BlockFace.DOWN);

        if (!params.structure.contains(down) && down.getType()==params.chipMaterial) { 
            params.structure.add(down);
            params.direction = direction;
            stepTo(params, down);
            scanBranch(params);
        }

        params.direction = direction;
        params.origin = origin;
    }

    private void checkAttachedIO(ChipParameters params) {
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

    private void checkForIO(ChipParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if (debugger!=null) debug(2, ChatColor.LIGHT_PURPLE + "Looking for IO block @" + prettyLoc(b) + " (" + b.getType().name() + ")");
            if (b.getType()==params.inputBlockType) {
                addInput(params, b);
            } else if (b.getType()==params.outputBlockType) {
                addOutput(params, b);
            } else if (b.getType()==params.interfaceBlockType) {
                addInterface(params, b);
            }
        }
    }

    private void checkForChipBlockOnSideFace(ChipParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if (debugger!=null) debug(2, ChatColor.LIGHT_PURPLE + "Looking for chip block @" + prettyLoc(b) + " (" + b.getType().name() + ")");
            if (b.getType()==params.chipMaterial) {
                params.structure.add(b);
                Block origin = params.origin;
                stepTo(params, b);

                scanBranch(params);

                params.origin = origin;
            }
        }
    }
        
    private void stepTo(ChipParameters params, Block b) {
        if (debugger!=null) debug(1, "Found chip block @" + prettyLoc(b));
        params.origin = b;        
    }
}
