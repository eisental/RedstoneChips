package org.redstonechips.user;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.ChipCollection;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.command.RCinfo;
import org.redstonechips.command.RCpin;

/**
 * A Tool that sends chip info messages to its user.
 * 
 * @author Tal Eisenberg
 */
public class ChipProbe extends Tool {

    /**
     * Prints a chip block info. 
     * When block points to a chip pin, the player receives an /rcpin message of this pin.
     * When block points to an activation block, debug mode is toggled for this player.
     * When block points to any other structure block the chip info is sent.
     * When block is a sign block of an inactive chip the chip will be activated.
     * 
     * @param block Queried block.
     */
    @Override
    public void use(Block block) {
        Player player = session.getPlayer();
        try {
            RCpin.printPinInfo(block, player);
        } catch (IllegalArgumentException ie) {
            // not probing a pin
            ChipCollection chips = RedstoneChips.inst().chipManager().getAllChips();
            Chip c = chips.getByStructureBlock(block.getLocation());
            
            if (c!=null) {
                if (c.activationBlock.equals(block.getLocation()))
                    player.performCommand("rcdebug");
                else RCinfo.printCircuitInfo(player, c);
            } else {
                // try to activate
                MaybeChip m = RedstoneChips.inst().chipManager().maybeCreateAndActivateChip(block, player, -1);
                if (m==MaybeChip.ChipError)
                    player.sendMessage(RCPrefs.getErrorColor() + m.getError());
            }

        }
    }    
}
