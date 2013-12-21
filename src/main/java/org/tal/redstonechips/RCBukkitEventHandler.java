package org.tal.redstonechips;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.material.Attachable;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.io.OutputPin;
import org.tal.redstonechips.circuit.scan.ScanParameters;
import org.tal.redstonechips.user.UserSession;
import org.tal.redstonechips.util.ChunkLocation;

/**
 *
 * @author Tal Eisenberg
 */
public class RCBukkitEventHandler implements Listener {
    RedstoneChips rc;
    
    public RCBukkitEventHandler(RedstoneChips rc) {
        this.rc = rc;
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        rc.getCircuitManager().onBlockRedstoneChange(event.getBlock(), event.getNewCurrent(), event.getOldCurrent());
    }
    
    /**
     * Deactivate a circuit if one of its structure blocks was broken. 
     * Refresh an input put if one of its power blocks was broken.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
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
    @EventHandler (priority = EventPriority.MONITOR)
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
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled()) {
            rc.getCircuitManager().checkCircuitDestroyed(event.getBlock(), null);
        }
    }

    /**
     * Breaks a chip if it exploded.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!event.isCancelled()) {
            for (Block b : event.blockList())
                rc.getCircuitManager().checkCircuitDestroyed(b, null);
        }
    }
    
    /**
     * Forces output redstone torches to stay at the right state.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.HIGHEST)
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
                    b.setTypeIdAndData(m.getId(), b.getData(), false);
                    event.setCancelled(true);                    
                } 
            }
        }  
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UserSession s = rc.getUserSession(event.getPlayer(), false);
        if (s!=null) s.playerQuit();
        
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {        
        if (UserSession.getPlayerFileFor(event.getPlayer(), rc.getDataFolder()).exists()) {
            UserSession s = rc.getUserSession(event.getPlayer(), true);
            s.playerJoined(event.getPlayer());
        }
    }
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (event.getAction()==Action.RIGHT_CLICK_BLOCK) {
            UserSession session = rc.getUserSession(event.getPlayer(), false);
            if (session != null && session.useToolInHand(event.getClickedBlock())) {
                event.setCancelled(true);
            } else {
                ScanParameters params = ScanParameters.generateDefaultParams(event.getClickedBlock(), rc);
                if (params!=null) {
                    int result = rc.getCircuitManager().checkForCircuit(params, event.getPlayer());
                    if (result == -2 || result >= 0) event.setCancelled(true);
                }

                else if (session!=null) {
                    if (session.getMode()==UserSession.Mode.SELECTION) {
                        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(event.getClickedBlock().getLocation());
                        if (c!=null) { 
                            session.selectChip(c);
                            event.setCancelled(true);
                        }
                    } else if (session.getMode()==UserSession.Mode.CUBOID_DEFINE) {
                        if (!event.getPlayer().getItemInHand().getType().isBlock()) {
                            session.addCuboidLocation(event.getClickedBlock().getLocation());
                            event.setCancelled(true);                            
                        }
                    }
                    
                } 
            }

            
        }
    }
    
    /**
     * Notifies all chips in the chunk.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        rc.getCircuitManager().updateOnChunkLoad(ChunkLocation.fromChunk(event.getChunk()));
    }

    /**
     * Stores the unloaded world to remove its chips after the WorldSaveEvent event.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        rc.getCircuitPersistence().setUnloadedWorld(event.getWorld());
    }

    /**
     * Saves the circuits file for this world and removes its chips if it was unloaded.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        if (rc.getCircuitPersistence().isWorldUnloading(event.getWorld()))
            rc.getCircuitPersistence().saveCircuits(event.getWorld());
    }

    /**
     * Loads circuits from the world's circuits file.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        rc.getCircuitPersistence().loadCircuits(event.getWorld());
    }
}
