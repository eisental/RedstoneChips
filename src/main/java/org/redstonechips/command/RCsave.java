
package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Tal Eisenberg
 */
public class RCsave extends RCCommand {
    @Override
    public boolean isOpRequired() { return true; }
    
    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        rc.circuitPersistence().saveAll();
        if (sender instanceof Player)
            sender.sendMessage(rc.prefs().getInfoColor() + "Done saving " + rc.chipManager().getAllChips().size() + " chips.");
    }
}
