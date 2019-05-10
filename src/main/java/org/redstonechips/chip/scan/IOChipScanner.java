package org.redstonechips.chip.scan;

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

    protected void addInput(ChipParameters params, Block b) {
        if (debugger!=null) debug(1, "Found input #" + (params.inputs.size()) + " @" + prettyLoc(b));
        if (!params.structure.contains(b)) params.structure.add(b);
        params.inputs.add(b);
    }    
    
    protected void addOutput(ChipParameters params, Block b) {
        if (debugger!=null) debug(1, "Found output #" + (params.outputs.size()) + " @" + prettyLoc(b));
        if (!params.structure.contains(b)) params.structure.add(b);
        params.outputs.add(b);
    }

    protected void addInterface(ChipParameters params, Block b) {
        if (debugger!=null) debug(1, "Found interface #" + (params.interfaces.size()) + " @" + prettyLoc(b));
        if (!params.structure.contains(b)) params.structure.add(b);
        params.interfaces.add(b);
    }

    protected String prettyLoc(Block b) {
        return b.getX() + ", " + b.getY() + ", " + b.getZ();
    }

    public boolean isTypeAllowed(ChipParameters params, Material material) {
    	return material!=params.inputBlockType && 
    			material!=params.outputBlockType &&
    			material!=params.interfaceBlockType &&
    			material.isBlock() && material!=Material.GRAVEL && material!=Material.SAND;
    }    
}
