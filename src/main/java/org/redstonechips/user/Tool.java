package org.redstonechips.user;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * An abstract RedstoneChip tool.
 * @author Tal Eisenberg
 */
public abstract class Tool {
    /** Tool material. Can't be a block. */
    protected Material item;
    
    /** The user session using this tool. */
    protected UserSession session;
    
    /**
     * 
     * @param session The user session using this tool.
     */
    public void setSession(UserSession session) {
        this.session = session;
    }
    
    /**
     * Sets the tool material.
     * 
     * @param item a non-block Material.
     * @throws IllegalArgumentException if the item is invalid.
     */
    public void setItem(Material item) throws IllegalArgumentException {
        if (item.isBlock()) throw new IllegalArgumentException("Blocks can't be used as tools.");
        else this.item = item;
    }
    
    /**
     * @return The tool material.
     */
    public Material getItem() { return item; }
        
    /**
     * Called when the tool is used on a block.
     * 
     * @param b a Block.
     */
    public abstract void use(Block b);
}
