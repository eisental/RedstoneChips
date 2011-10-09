/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.CircuitIndex;

/**
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
    
        ChatColor color = (sender instanceof Player?ChatColor.GRAY:ChatColor.WHITE);
        sender.sendMessage(ChatColor.RED + "---- " + rc.getDescription().getName() + " " + rc.getDescription().getVersion() + " ------------------------------");
        sender.sendMessage(ChatColor.YELLOW + "For more info see: " + color + rc.getDescription().getWebsite());
        sender.sendMessage("Type /rchelp for a list of all RedstoneChips commands.");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Installed circuit libraries:");

        for (CircuitIndex i : org.tal.redstonechips.RedstoneChips.circuitLibraries) {
            sender.sendMessage(ChatColor.YELLOW + " - " + color + i.getName() + " " + i.getVersion());
        }
        sender.sendMessage(ChatColor.RED + "-----------------------------------------------------");
        return true;
    }

}
