/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 *
 * @author Tal Eisenberg
 */
class RCBlockListener extends BlockListener {
    RedstoneChips rc;
    
    public RCBlockListener(RedstoneChips rc) {
        this.rc = rc;
    }
    
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        rc.getCircuitManager().redstoneChange(event);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            if (!rc.getCircuitManager().checkCircuitDestroyed(event.getBlock(), event.getPlayer())) event.setCancelled(true);
            rc.getCircuitManager().checkCircuitInputChanged(event.getBlock(), event.getPlayer(), true);
        }
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            rc.getCircuitManager().checkCircuitInputChanged(event.getBlock(), event.getPlayer(), false);
        }
    }

    @Override
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled()) {
            rc.getCircuitManager().checkCircuitDestroyed(event.getBlock(), null);
        }
    }
}
