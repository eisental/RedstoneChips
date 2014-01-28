
package org.redstonechips.command;

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
    public boolean isOpRequired() { return true; }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        for (World world : rc.getServer().getWorlds())
            rc.circuitPersistence().loadChipsOf(world);
        
        if (sender instanceof Player)
            info(sender, "Done loading " + rc.chipManager().getAllChips().size() + " chip(s). Note: Errors and warnings are only printed to the server console.");
        else info(sender, "Done loading " + rc.chipManager().getAllChips().size() + " chip(s).");
    }
}
