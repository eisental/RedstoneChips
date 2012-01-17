package org.tal.redstonechips;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Attachable;
import org.tal.redstonechips.circuit.io.OutputPin;

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
     * Deactivate a circuit if one of its structure blocks was broken. 
     * Refresh an input put if one of its power blocks was broken.
     * @param event 
     */
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            if (!rc.getCircuitManager().checkCircuitDestroyed(event.getBlock(), event.getPlayer())) 
                event.setCancelled(true);
            
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
        Block b = event.getBlock();
        if (b.getType()==Material.REDSTONE_TORCH_ON || b.getType()==Material.REDSTONE_TORCH_OFF) {
            // check if its an output device of a chip:
            List<OutputPin> pins = rc.getCircuitManager().getOutputPinByOutputBlock(b.getLocation());            
            if (pins==null) return;
            
            Attachable a = (Attachable)b.getState().getData();
            BlockFace f = a.getAttachedFace();
            if (f==null) return;
            
            Block attached = b.getRelative(f);

            for (OutputPin o : pins) {
                if (attached.getLocation().equals(o.getLocation())) {
                    Material m = o.getState()?Material.REDSTONE_TORCH_ON:Material.REDSTONE_TORCH_OFF;
                    b.setType(m);
                    event.setCancelled(true);                    
                } 
            }
        }  
    }    
}
