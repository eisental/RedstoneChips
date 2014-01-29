
package org.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPermissions;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class RCreset extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (args.length>0) {
            if (args[0].equalsIgnoreCase("all")) {
                if (RCPermissions.enforceCommand(sender, command.getName() + ".all", true, false))
                    resetAllCircuits(sender);
                else error(sender, "You do not have permissions to reset all chips.");
            } else {
                if (RCPermissions.enforceRemoteCommand(sender, command.getName())) {
                    Chip c = rc.chipManager().getAllChips().getById(args[0]);
                    if (c!=null) rc.chipManager().resetChip(c, sender); 
                    else error(sender, "Unknown chip id: " + args[0] + ".");
                }
            }
        } else { // use target chip
            Chip c = CommandUtils.findTargetChip(sender);
            if (c!=null) rc.chipManager().resetChip(c, sender);
        } 
    }

    private void resetAllCircuits(CommandSender sender) {
        List<Chip> failed = new ArrayList<>();
        List<Chip> allChips = new ArrayList<>();
        allChips.addAll(rc.chipManager().getAllChips().values());

        for (Chip c : allChips) {
            if (!rc.chipManager().resetChip(c, sender)) {
                failed.add(c);
            }
        }

        if (sender!=null) {
            if (!failed.isEmpty()) {
                String ids = "";
                for (Chip c : failed) {
                    ids += (c.name==null?c.id:c.name) + ", ";
                }

                ids = ids.substring(0, ids.length()-2);
                error(sender, "Some chip could not reactivate: " + ids);
            } else {
                info(sender, ChatColor.AQUA + "Successfully reset all active chips.");
            }
        }

    }
}
