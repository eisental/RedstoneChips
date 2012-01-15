package org.tal.redstonechips.user;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class Tool {
    protected Material item; 
    protected UserSession session;
    
    public void setSession(UserSession session) {
        this.session = session;
    }
    
    public void setItem(Material item) throws IllegalArgumentException {
        if (item.isBlock()) throw new IllegalArgumentException("Blocks can't be used as tools.");
        else this.item = item;
    }
    
    public Material getItem() { return item; }
        
    public abstract void use(Block b);
}
