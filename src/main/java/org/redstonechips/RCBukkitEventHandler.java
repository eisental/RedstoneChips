package org.redstonechips;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Lightable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.material.Attachable;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.chip.ChipManager;
import org.redstonechips.chip.io.OutputPin;
import org.redstonechips.user.UserSession;
import org.redstonechips.util.ChunkLocation;

/**
 * Main plugin event handler. Routes events to other objects, adding some logic
 * on the way.
 * 
 * @author Tal Eisenberg
 */
public class RCBukkitEventHandler implements Listener {
    RedstoneChips rc;
    ChipManager cm;
    
    public RCBukkitEventHandler(RedstoneChips rc) {
        this.rc = rc;
        this.cm = rc.chipManager();
    }
    
    /**
     * Pass redstone change events over to the chip manager. Also keeps Redstone lamps ON.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block b = event.getBlock();
        
        if (b.getType()==Material.REDSTONE_LAMP) {
            List<OutputPin> pins = cm.getAllChips().getOutputPinByOutputBlock(b.getLocation());
            if (pins!=null) {
                for (OutputPin p : pins) {
                    Lightable data = (Lightable)b.getBlockData();
                    data.setLit(p.getState());
                    b.setBlockData(data);
                    if (p.getState()) {
                        event.setNewCurrent(100);
                        break;
                    }
                }
            }
        } 
        else if (b.getType()==Material.REDSTONE_WIRE) {
            List<OutputPin> pins = cm.getAllChips().getOutputPinByOutputBlock(b.getLocation());
            if (pins != null) {
                for (OutputPin p : pins) {
                    event.setNewCurrent(p.getState()?15:0);
                }
            }
        } 
        
        cm.redstoneStateChanged(event.getBlock(), event.getNewCurrent(), event.getOldCurrent());
    }
    
    /**
     * Deactivate a circuit if one of its structure blocks was broken. 
     * Refresh an input put if one of its power blocks was broken.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled()) {
            if (!cm.maybeChipDestroyed(event.getBlock(), event.getPlayer())) 
                event.setCancelled(true);
            
            cm.maybeChipInputBlockChanged(event.getBlock(), event.getPlayer(), true);
        }
    }

    /**
     * Refresh an input pin in case a block was placed in one of its power blocks.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled()) {
            if (!cm.maybeChipInputBlockChanged(event.getBlock(), event.getPlayer(), false))
                cm.maybeChipOutputBlockPlaced(event.getBlock(), event.getPlayer());
        }
    }

    /**
     * Breaks a chip if it's burning.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!event.isCancelled()) {
            cm.maybeChipDestroyed(event.getBlock(), null);
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
                cm.maybeChipDestroyed(b, null);
        }
    }
    
    /**
     * Forces output redstone torches and lamps to stay at the right state.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block b = event.getBlock();
        if (b.getType()==Material.REDSTONE_TORCH || (b.getType()==Material.REDSTONE_WALL_TORCH)) {
            // check if its an output device of a chip:
            List<OutputPin> pins = cm.getAllChips().getOutputPinByOutputBlock(b.getLocation());            
            if (pins==null) return;
            
            Attachable a = (Attachable)b.getState().getData();
            BlockFace f = a.getAttachedFace();
            if (f==null) return;
            
            Block attached = b.getRelative(f);

            for (OutputPin o : pins) {
                if (attached.getLocation().equals(o.getLocation())) {
                    Lightable data = (Lightable)b.getBlockData();
                    data.setLit(o.getState());
                    b.setBlockData(data);
                    event.setCancelled(true);                    
                } 
            }
        } 
    }
    
    /**
     * Notify the players UserSession that he quit the server.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UserSession s = rc.getUserSession(event.getPlayer(), false);
        if (s!=null) s.playerQuit();
        
    }

    /**
     * Load UserSession file for the new player if one exists.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {        
        if (UserSession.getPlayerFileFor(event.getPlayer(), rc.getDataFolder()).exists()) {
            UserSession s = rc.getUserSession(event.getPlayer(), true);
            s.playerJoined(event.getPlayer());
        }
    }
    
    /**
     * Handle right-click actions: chip activation, selection or defining cuboids.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        
        if (event.getAction()==Action.RIGHT_CLICK_BLOCK) {
            UserSession session = rc.getUserSession(event.getPlayer(), false);
            
            if (session!=null && session.useToolInHand(event.getClickedBlock())) event.setCancelled(true); // use session tool
            else if (session!=null && session.getMode()==UserSession.Mode.SELECTION) { // in chip selection mode
                Chip c = cm.getAllChips().getByStructureBlock(event.getClickedBlock().getLocation());
                if (c!=null) { 
                    session.selectChip(c);
                    event.setCancelled(true);
                }
            } else if (session!=null && session.getMode()==UserSession.Mode.CUBOID_DEFINE) { // in cuboid define mode
                if (!event.getPlayer().getItemInHand().getType().isBlock()) {
                    session.addCuboidLocation(event.getClickedBlock().getLocation());
                    event.setCancelled(true);                            
                }
            } else {
                MaybeChip mChip = cm.maybeCreateAndActivateChip(event.getClickedBlock(), event.getPlayer(), -1);
                if (mChip!=MaybeChip.NotAChip) event.setCancelled(true);
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
        rc.chipManager().maybeChipChunkLoaded(ChunkLocation.fromChunk(event.getChunk()));
    }

    /**
     * Stores the unloaded world to remove its chips after the WorldSaveEvent event.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        WorldsObserver.setUnloadingWorld(event.getWorld());
    }

    /**
     * Saves the circuits file for this world and removes its chips if it was unloaded.
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        if (WorldsObserver.isWorldUnloading(event.getWorld())) {
            RCPersistence.saveChipsOf(event.getWorld());
            WorldsObserver.removeUnloadingWorld();
        }
    }

    /**
     * Loads circuits from the world circuits file.
     * 
     * @param event 
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        RCPersistence.loadChipsOf(event.getWorld());
    }
}
