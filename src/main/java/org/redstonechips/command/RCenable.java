package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class RCenable extends RCRemoteChipCommand {

    @Override
    protected void runWithChip(Chip target, CommandSender sender, Command command, String label, String[] args) {
        if (target.isDisabled()) {
            target.enable();
            sender.sendMessage(rc.prefs().getInfoColor() + "Enabled " + target + ".");
        } else sender.sendMessage(rc.prefs().getDebugColor() + "The chip is already enabled.");
    }    
}
