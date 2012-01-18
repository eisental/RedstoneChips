package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tal.redstonechips.user.ChipProbe;
import org.tal.redstonechips.user.Tool;
import org.tal.redstonechips.user.UserSession;

/**
 *
 * @author Tal Eisenberg
 */
public class RCtool extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;
        
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;

        ItemStack item = player.getItemInHand();
        Material type = item.getType();
        
        try {
            UserSession session = rc.getUserSession(player, true);
            Tool t = new ChipProbe();
            t.setItem(type);
            session.addTool(t);
        } catch (IllegalArgumentException ie) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
            return true;            
        }
        
        sender.sendMessage(rc.getPrefs().getInfoColor() + "Chip probe set to " + ChatColor.YELLOW + type.name().toLowerCase() + ". " 
                + rc.getPrefs().getInfoColor() + "Right-click a chip block to get info.");
        return true;
    }    
}
