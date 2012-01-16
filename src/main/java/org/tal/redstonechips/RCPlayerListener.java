package org.tal.redstonechips;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.user.UserSession;
import org.tal.redstonechips.user.UserSession.Mode;

/**
 *
 * @author Tal Eisenberg
 */
class RCPlayerListener extends PlayerListener {
    RedstoneChips rc;
    
    public RCPlayerListener(RedstoneChips rc) {
        this.rc = rc;
    }

    /**
     * Removes the player from chip debugging lists.
     * 
     * @param event 
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        UserSession s = rc.getUserSession(event.getPlayer().getPlayerListName(), false);
        if (s!=null) s.playerQuit();
        
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        UserSession s = rc.getUserSession(event.getPlayer().getPlayerListName(), false);
        if (s!=null) s.playerJoined(event.getPlayer());
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {                
        if (event.isCancelled()) return;

        if (event.getAction()==Action.RIGHT_CLICK_BLOCK) {
            UserSession session = rc.getUserSession(event.getPlayer().getPlayerListName(), false);
            if (session != null && session.useToolInHand(event.getClickedBlock())) {
                event.setCancelled(true);
            } else {
                int result = rc.getCircuitManager().checkForCircuit(event.getClickedBlock(), 
                        rc.getPrefs().getDefaultChipScanner(), event.getPlayer());
                if (result == -2 || result >= 0) event.setCancelled(true);
                
                else if (session!=null) {
                    if (session.getMode()==Mode.SELECTION) {
                        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(event.getClickedBlock().getLocation());
                        if (c!=null) { 
                            session.selectChip(c);
                            event.setCancelled(true);
                        }
                    } else if (session.getMode()==Mode.CUBOID_DEFINE) {
                        if (!event.getPlayer().getItemInHand().getType().isBlock()) {
                            session.addCuboidLocation(event.getClickedBlock().getLocation());
                            event.setCancelled(true);                            
                        }
                    }
                    
                } 
            }

            
        }
    }
}
