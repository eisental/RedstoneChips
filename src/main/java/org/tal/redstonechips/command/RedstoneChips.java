package org.tal.redstonechips.command;

import java.io.IOException;
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
    

        String verMsg = "";
        
        try {
            String curver = rc.checkUpdate();
            if (curver==null) verMsg = " - RedstoneChips is up to date.";
            else verMsg = ChatColor.GREEN + " - A new RedstoneChips version (" + curver + ") is available.";
        } catch (IOException ex) {
            verMsg = " - Couldn't check for a new plugin version (" + ex.getClass().getSimpleName() + ".";
        }
        
        ChatColor color = (sender instanceof Player?ChatColor.GRAY:ChatColor.WHITE);
        sender.sendMessage(ChatColor.RED + "---- " + rc.getDescription().getName() + " " + rc.getDescription().getVersion() + " -----------------------------");
        sender.sendMessage(ChatColor.YELLOW + "Website: " + color + rc.getDescription().getWebsite());
        sender.sendMessage("Type /rchelp for a list of all RedstoneChips commands.");
        sender.sendMessage("");
        sender.sendMessage(verMsg);
        sender.sendMessage("");
        
        sender.sendMessage(ChatColor.YELLOW + "Installed chip libraries:");

        for (CircuitIndex i : rc.getCircuitLoader().getCircuitLibraries()) {
            sender.sendMessage(ChatColor.YELLOW + " - " + color + i.getName() + " " + i.getVersion());
        }
        sender.sendMessage(ChatColor.RED + "-----------------------------------------------------");
        return true;
    }

}
