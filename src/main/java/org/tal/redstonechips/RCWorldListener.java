package org.tal.redstonechips;

import java.util.logging.Level;
import org.tal.redstonechips.util.ChunkLocation;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 *
 * @author Tal Eisenberg
 */
class RCWorldListener extends RCListener {
    private World unloadedWorld = null; 
    
    @EventHandler (priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        rc.getCircuitManager().updateOnChunkLoad(ChunkLocation.fromChunk(event.getChunk()));
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        unloadedWorld = event.getWorld();
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldSave(WorldSaveEvent event) {
        if (!rc.isEnabled()) return;
        
        rc.log(Level.INFO, "Saving " + event.getWorld().getName() + " chip data...");                
        rc.getCircuitPersistence().saveCircuits(event.getWorld());

        // if world is unloaded remove circuits.
        if (unloadedWorld==event.getWorld()) {
            int size = rc.getCircuitManager().getCircuits().size();
            rc.getCircuitManager().unloadWorldChips(unloadedWorld);
            rc.log(Level.INFO, "Unloaded " + (size-rc.getCircuitManager().getCircuits().size()) + " chip(s).");                    
            unloadedWorld = null;
        }

    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        rc.getCircuitPersistence().loadCircuits(event.getWorld());
    }
}
