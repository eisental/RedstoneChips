
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
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), true, true)) return true;
        rc.getCircuitPersistence().loadCircuits();
        if (sender instanceof Player)
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Done loading " + rc.getCircuitManager().getCircuits().size() + " circuits. Note: Errors and warning are only printed in the server console.");

        return true;
    }
}
