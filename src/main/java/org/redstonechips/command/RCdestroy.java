
package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdestroy extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        Chip c = CommandUtils.findTargetChip(sender);
        if (c!=null) {
            if (rc.chipManager().destroyChip(c, sender, true))
                sender.sendMessage(RCPrefs.getInfoColor() + "The " + c.getType() + " chip is destroyed.");
        }
    }
}
