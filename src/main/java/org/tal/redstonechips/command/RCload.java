
package org.tal.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Tal Eisenberg
 */
public class RCload extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (checkpermissions(sender,command)) {
            rc.getCircuitPersistence().loadCircuits();
            if (sender instanceof Player)
                sender.sendMessage(rc.getPrefs().getInfoColor() + "Done loading " + rc.getCircuitManager().getCircuits().size() + " circuits. Note: Errors and warning are only printed in the server console.");
        } else
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to use command " + command.getName() + ".");

        return true;
    }

	private boolean checkpermissions(CommandSender sender, Command command) {
		if (!rc.getPrefs().getUsePermissions()) {
			return sender.isOp();
		} else {
			if (sender instanceof Player) {
				return (((Player)sender).hasPermission("redstonechips.command." + command.getName()) && !((Player)sender).hasPermission("redstonechips.command." + command.getName() + ".deny"));
			} else {
				return true;
			}
		}
	}
}
