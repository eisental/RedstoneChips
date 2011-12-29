package org.tal.redstonechips;

import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.tal.redstonechips.util.ChunkLocation;

/**
 *
 * @author Tal Eisenberg
 */
class RCWorldListener extends WorldListener {
    private RedstoneChips rc;
    private World unloadedWorld = null; 
    
    public RCWorldListener(RedstoneChips rc) {
        this.rc = rc;
    }
    
    @Override
    public void onChunkLoad(ChunkLoadEvent event) {
        rc.getCircuitManager().updateOnChunkLoad(ChunkLocation.fromChunk(event.getChunk()));
    }

    @Override
    public void onWorldUnload(WorldUnloadEvent event) {
        unloadedWorld = event.getWorld();
    }

    @Override
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

    @Override
    public void onWorldLoad(WorldLoadEvent event) {
        rc.getCircuitPersistence().loadCircuits(event.getWorld());
    }

    
}
