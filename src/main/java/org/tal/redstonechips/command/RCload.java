
package org.tal.redstonechips.command;

import org.bukkit.World;
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
        for (World world : rc.getServer().getWorlds())
            rc.getCircuitPersistence().loadCircuits(world);
        
        if (sender instanceof Player)
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Done loading " + rc.getCircuitManager().getCircuits().size() + " chip(s). Note: Errors and warnings are only printed to the server console.");
        else sender.sendMessage(rc.getPrefs().getInfoColor() + "Done loading " + rc.getCircuitManager().getCircuits().size() + " chip(s).");
        return true;
    }
}
