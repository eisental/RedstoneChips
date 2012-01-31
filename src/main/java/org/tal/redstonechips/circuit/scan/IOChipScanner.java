package org.tal.redstonechips.circuit.scan;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

/**
 * An abstract ChipScanner that supports debugging and adding IO blocks.
 * @author Tal Eisenberg
 */
public abstract class IOChipScanner implements ChipScanner {
    protected CommandSender debugger = null;
    protected int debugLevel;
    
    /** 
     * Sets the debugging player of this chip scanner and the debug level.
     * 
     * @param debugger
     * @param level 
     */
    public void setDebugger(CommandSender debugger, int level) {
        this.debugger = debugger;
        this.debugLevel = level;
    }
    
    protected void debug(int level, String msg) {
        if (debugger!=null && level<=debugLevel) {
            debugger.sendMessage(ChatColor.AQUA + msg);
        }
    }

    protected void addInput(ScanParameters params, Block b) {
        if (debugger!=null) debug(1, "Found input #" + (params.inputs.size()) + " @" + prettyLoc(b));
        params.structure.add(b);
        params.inputs.add(b);
    }    
    
    protected void addOutput(ScanParameters params, Block b) {
        if (debugger!=null) debug(1, "Found output #" + (params.outputs.size()) + " @" + prettyLoc(b));
        params.structure.add(b);
        params.outputs.add(b);
    }

    protected void addInterface(ScanParameters params, Block b) {
        if (debugger!=null) debug(1, "Found interface #" + (params.interfaces.size()) + " @" + prettyLoc(b));
        params.structure.add(b);
        params.interfaces.add(b);
    }

    protected String prettyLoc(Block b) {
        return b.getX() + ", " + b.getY() + ", " + b.getZ();
    }

    protected String prettyMaterial(Block b) {
        if (b.getType()==Material.WOOL) return ((Wool)b.getState().getData()).toString();
        else return b.getType().name();
    }
 
    public boolean isTypeAllowed(ScanParameters params, Material material, byte data) {
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
