package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdisable extends RCRemoteChipCommand {

    @Override
    protected void runWithChip(Chip target, CommandSender sender, Command command, String label, String[] args) {
        if (!target.isDisabled()) {
            target.disable();
            sender.sendMessage(RCPrefs.getInfoColor() + "Disabled " + target + ".");
        } else sender.sendMessage(RCPrefs.getDebugColor() + "The chip is already disabled.");
    }   
}
