package org.tal.redstonechips;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.scan.ScanParameters;
import org.tal.redstonechips.user.UserSession;
import org.tal.redstonechips.user.UserSession.Mode;

/**
 *
 * @author Tal Eisenberg
 */
class RCPlayerListener extends RCListener {
    /**
     * Removes the player from chip debugging lists.
     * 
     * @param event 
     */
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
