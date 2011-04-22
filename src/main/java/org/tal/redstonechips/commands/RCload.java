
package org.tal.redstonechips.commands;

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
        if (sender.isOp()) {
            rc.getCircuitPersistence().loadCircuits();
            if (sender instanceof Player)
                sender.sendMessage(rc.getPrefs().getInfoColor() + "Done loading " + rc.getCircuitManager().getCircuits().size() + " circuits. Note: Errors and warning are only printed in the server console.");
        } else
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Only ops (admins) are allowed to use this command.");

        return true;
    }

}
