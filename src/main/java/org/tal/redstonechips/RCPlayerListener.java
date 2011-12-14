package org.tal.redstonechips;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Tal Eisenberg
 */
class RCPlayerListener extends PlayerListener {
    RedstoneChips rc;
    
    public RCPlayerListener(RedstoneChips rc) {
        this.rc = rc;
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        rc.getCircuitManager().checkDebuggerQuit(event.getPlayer());
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {                
        if (event.isCancelled()) return;

        if (event.getAction()==Action.LEFT_CLICK_BLOCK) {
            
            if (!rc.getPrefs().getRightClickToActivate() && event.getPlayer().getGameMode()==GameMode.SURVIVAL) {
                int result = rc.getCircuitManager().checkForCircuit(event.getClickedBlock(), event.getPlayer());
                if (result == -2 || result >= 0) event.setCancelled(true);
            }

        } else if (event.getAction()==Action.RIGHT_CLICK_BLOCK) {
            if (isUsingChipProbe(event.getPlayer())) {
                rc.probeChipBlock(event.getPlayer(), event.getClickedBlock());
                event.setCancelled(true);
                
            } else if (rc.getPrefs().getRightClickToActivate() || event.getPlayer().getGameMode()==GameMode.CREATIVE) {
                int result = rc.getCircuitManager().checkForCircuit(event.getClickedBlock(), event.getPlayer());
                if (result == -2 || result >= 0) event.setCancelled(true);
            }

            if (!event.getPlayer().getItemInHand().getType().isBlock()) {
                rc.getRCsel().cuboidLocation(event.getPlayer(), event.getClickedBlock().getLocation());
            }
        }
    }
    
    private boolean isUsingChipProbe(Player p) {
        if (!rc.getPlayerChipProbe().containsKey(p.getName())) return false;
        else return p.getItemInHand().getType() == rc.getPlayerChipProbe().get(p.getName());
    }
    
}
