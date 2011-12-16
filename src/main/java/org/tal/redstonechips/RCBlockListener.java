package org.tal.redstonechips;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.RedstoneTorch;
import org.tal.redstonechips.circuit.OutputPin;

/**
 *
 * @author Tal Eisenberg
 */
class RCBlockListener extends BlockListener {
    RedstoneChips rc;
    
    public RCBlockListener(RedstoneChips rc) {
        this.rc = rc;
    }
    
    /**
     * Update circuit inputs in case of a relevant redstone signal change.
     * @param event 
     */
    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        rc.getCircuitManager().redstoneChange(event);
    }

    /**
     * Deactivate a circuit if one of its structure blocks was broken. 
     * Refresh an input put if one of its power blocks was broken.
     * @param event 
     */
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            if (!rc.getCircuitManager().checkCircuitDestroyed(event.getBlock(), event.getPlayer())) event.setCancelled(true);
            rc.getCircuitManager().checkCircuitInputBlockChanged(event.getBlock(), event.getPlayer(), true);
        }
    }

    /**
     * Refresh an input pin in case a block was placed in one of its power blocks.
     * @param event 
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            CircuitManager cm = rc.getCircuitManager();
            if (!cm.checkCircuitInputBlockChanged(event.getBlock(), event.getPlayer(), false))
                cm.checkCircuitOutputBlockPlaced(event.getBlock(), event.getPlayer());
        }
    }

    /**
     * Break circuit if it's burning.
     * 
     * @param event 
     */
    @Override
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled()) {
            rc.getCircuitManager().checkCircuitDestroyed(event.getBlock(), null);
        }
    }

    /**
     * Forces output redstone torches to stay at the right state.
     * 
     * @param event 
     */
    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) return;
        
        Block b = event.getBlock();
        if (b.getType()==Material.REDSTONE_TORCH_ON || b.getType()==Material.REDSTONE_TORCH_OFF) {
            BlockState s = b.getState();
            if (s.getData().getData()==(byte)5) return;
            RedstoneTorch t = (RedstoneTorch)s.getData();
            BlockFace f = t.getAttachedFace();            
            if (f==null) return;
            
            Block attached = b.getRelative(f);
            byte data = b.getData();
            OutputPin o = rc.getCircuitManager().getOutputPin(attached.getLocation());
            if (o!=null) {
                if (o.getState()) {
                    s.setType(Material.REDSTONE_TORCH_ON);
                } else s.setType(Material.REDSTONE_TORCH_OFF);
                s.getData().setData(data);
                event.setCancelled(true);
                s.update();
            }
        }  
    }    
}
