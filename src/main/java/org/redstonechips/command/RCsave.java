
package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPersistence;
import org.redstonechips.RCPrefs;

/**
 *
 * @author Tal Eisenberg
 */
public class RCsave extends RCCommand {
    @Override
    public boolean isOpRequired() { return true; }
    
    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        RCPersistence.saveAll();
        if (sender instanceof Player)
            sender.sendMessage(RCPrefs.getInfoColor() + "Done saving " + rc.chipManager().getAllChips().size() + " chips.");
    }
}
