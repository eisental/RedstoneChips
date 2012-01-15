package org.tal.redstonechips.user;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.command.RCinfo;
import org.tal.redstonechips.command.RCpin;

/**
 *
 * @author Tal Eisenberg
 */
public class ChipProbe extends Tool {

    /**
     * Prints a chip block info. 
     * When block points to a chip pin, the player receives an /rcpin message of this pin.
     * When block points to an activation block, debug mode is toggled for this player.
     * When block points to any other structure block the chip info is sent.
     * 
     * @param player The player to send the info message to.
     * @param block Queried block.
     */
    @Override
    public void use(Block block) {
        Player player = session.getPlayer();
        try {
            RCpin.printPinInfo(block, player, session.getPlugin());
        } catch (IllegalArgumentException ie) {
            // not probing a pin
            Circuit c = session.getPlugin().getCircuitManager().getCircuitByStructureBlock(block.getLocation());
            
            if (c!=null) {
                if (c.activationBlock.equals(block.getLocation()))
                    player.performCommand("rcdebug");
                else RCinfo.printCircuitInfo(player, c, session.getPlugin());
            }

        }
    }    
}
