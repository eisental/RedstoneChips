/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author Tal Eisenberg
 */
class RCEntityListener extends EntityListener {
    RedstoneChips rc;
    
    public RCEntityListener(RedstoneChips rc) {
        this.rc = rc;
    }
    
    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        for (Block b : event.blockList())
            rc.getCircuitManager().checkCircuitDestroyed(b, null);
    }
    
}
