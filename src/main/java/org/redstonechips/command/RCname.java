package org.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class RCname extends RCRemoteChipCommand {
    @Override
    protected int argsCountForLocalTarget() { return 1; }

    @Override
    protected void runWithChip(Chip target, CommandSender sender, Command command, String label, String[] args) {
        String name;
        
        if (args.length>1) { 
            error(sender, "Bad /rcname command.");
            return;
        }
                        
        if (args[0].equalsIgnoreCase("remove"))
            name = null;
        else name = args[0];

        try {
            rc.chipManager().nameChip(target, name);
        
            if (name!=null) 
                 info(sender, "Renamed chip: " + ChatColor.YELLOW + target);
            else info(sender, "Removed name: " + ChatColor.YELLOW + target);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(RCPrefs.getErrorColor() + e.getMessage());
        }
    }
    
}
