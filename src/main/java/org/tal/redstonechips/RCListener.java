package org.tal.redstonechips;

import org.bukkit.event.Listener;

/**
 * A Bukkit event listener with a reference to the plugin.
 * @author Tal Eisenberg
 */
public class RCListener implements Listener {
    RedstoneChips rc;
    
    public RCListener setPlugin(RedstoneChips rc) { this.rc = rc; return this; }
}
