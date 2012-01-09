/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.tal.redstonechips.circuit.ChipScanner.ChipScanException;
import org.tal.redstonechips.circuit.io.IOBlock;
import org.tal.redstonechips.util.Locations;

/**
 *
 * @author Tal Eisenberg
 */
public class RecursiveChipScanner implements ChipScanner {
    CommandSender debugger = null;
    
    @SuppressWarnings("MapReplaceableByEnumMap")
    private Map<IOBlock.Type, MaterialData> ioMaterials = new HashMap<IOBlock.Type, MaterialData>();
    
    public void addIOMaterial(IOBlock.Type type, MaterialData material) {
        ioMaterials.put(type, material);
    }

    public void setDebugger(CommandSender debugger) {
        this.debugger = debugger;
    }
    
    public Map<IOBlock.Type, MaterialData> getIOMaterials() { return ioMaterials; }
    
    /**
     * Scans the chip structure starting from the sign block.
     * 
     * @param signBlock The sign block of the scanned chip.
     * @return A ScanParameters object describing the chip structure.
     * @throws ChipScanException if the structure is not a valid chip.
     */
    public ScanParameters scan(Block signBlock) throws ChipScanException {
        BlockFace direction = ((org.bukkit.material.Sign)signBlock.getState().getData()).getAttachedFace();
        Block firstChipBlock = signBlock.getRelative(direction);

        ScanParameters params = new ScanParameters();
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
                
        if (!isTypeAllowed(params, firstChipBlock.getType(), firstChipBlock.getData())) {
            throw new ChipScanException("You can't build a redstone chip using this material (" + firstChipBlock.getType().name() + "). Either it doesn't work as a chip block or it already has another function as an i/o block.");
        }
        
        if (debugger!=null) {
            debug("Sign block: " + prettyLoc(signBlock));
            debug("Chip block: " + prettyLoc(firstChipBlock));
        }
        
        try {            
            scanBranch(params);
            return params;
        } catch (StackOverflowError se) {
            throw new ChipScanException("If you're trying to build a redstone chip, your chip is way too big.");
        }

    }
    
    private void scanBranch(ScanParameters params) {
        // look in every direction for inputs, outputs or interface blocks.
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
            stepTo(params, up);
            scanBranch(params);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getRelative(BlockFace.DOWN);

        if (!params.structure.contains(down) && ((!down.getType().equals(Material.WOOL) && down.getType()==params.chipMaterial) || (down.getType().equals(Material.WOOL) && ((Wool)(down.getState().getData())).getColor().equals(params.woolColor)))) {
            params.structure.add(down);
            params.direction = direction;
            stepTo(params, down);
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
            if (debugger!=null) debug(ChatColor.LIGHT_PURPLE + "Looking for IO block @" + prettyLoc(b) + " (" + prettyMaterial(b) + ")");
            if (b.getType()==params.inputBlockType.getItemType()
                    && (b.getData()==params.inputBlockType.getData() || params.inputBlockType.getData()==-1)) {
                addInput(params, b);
            } else if (b.getType()==params.outputBlockType.getItemType()
                    && (b.getData()==params.outputBlockType.getData() || params.outputBlockType.getData()==-1)) {
                addOutput(params, b);
            } else if (b.getType()==params.interfaceBlockType.getItemType()
                    && (b.getData()==params.interfaceBlockType.getData() || params.interfaceBlockType.getData()==-1)) {
                addInterface(params, b);
            }
        }
    }

    private void checkForChipBlockOnSideFace(ScanParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if (debugger!=null) debug(ChatColor.LIGHT_PURPLE + "Looking for chip block @" + prettyLoc(b) + " (" + prettyMaterial(b) + ")");
            if ((!b.getType().equals(Material.WOOL) && b.getType()==params.chipMaterial) || 
                    (b.getType().equals(Material.WOOL) && ((Wool)(b.getState().getData())).getColor().equals(params.woolColor))) {
                params.structure.add(b);
                Block origin = params.origin;
                stepTo(params, b);

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
    
    private void debug(String msg) {
        if (debugger!=null) {
            debugger.sendMessage(ChatColor.AQUA + msg);
        }
    }
    
    private void stepTo(ScanParameters params, Block b) {
        if (debugger!=null) debug("Adding chip block @" + prettyLoc(b));
        params.origin = b;        
    }

    private void addInput(ScanParameters params, Block b) {
        if (debugger!=null) debug("Found input @" + prettyLoc(b));
        params.structure.add(b);
        params.inputs.add(b);
    }
    
    private void addOutput(ScanParameters params, Block b) {
        if (debugger!=null) debug("Found output @" + prettyLoc(b));
        params.structure.add(b);
        params.outputs.add(b);
    }

    private void addInterface(ScanParameters params, Block b) {
        if (debugger!=null) debug("Found interface @" + prettyLoc(b));
        params.structure.add(b);
        params.interfaces.add(b);
    }

    private String prettyLoc(Block b) {
        return b.getX() + ", " + b.getY() + ", " + b.getZ();
    }

    private String prettyMaterial(Block b) {
        if (b.getType()==Material.WOOL) return ((Wool)b.getState().getData()).toString();
        else return b.getType().name();
    }

    
}
