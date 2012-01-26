package org.tal.redstonechips;

import org.bukkit.event.Listener;

/**
 *
 * @author Tal Eisenberg
 */
public class RCListener implements Listener {
    RedstoneChips rc;
    
    public RCListener setPlugin(RedstoneChips rc) { this.rc = rc; return this; }
}
