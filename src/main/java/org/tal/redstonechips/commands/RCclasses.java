/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Tal Eisenberg
 */
public class RCclasses extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<String,Class> circuitClasses = rc.getCircuitLoader().getCircuitClasses();
        
        if (circuitClasses.isEmpty()) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no circuit classes installed.");
        else {
            printClassesList(sender, circuitClasses);
        }

        return true;
    }

    private void printClassesList(CommandSender sender, Map<String, Class> circuitClasses) {
        List<String> names = Arrays.asList(circuitClasses.keySet().toArray(new String[circuitClasses.size()]));
        Collections.sort(names);
        sender.sendMessage("");
        sender.sendMessage(rc.getPrefs().getInfoColor() + "Installed circuit classes:");
        sender.sendMessage(rc.getPrefs().getInfoColor() + "-----------------------");
        String list = "";
        ChatColor color = ChatColor.WHITE;
        for (String name : names) {
            list += color + name + ", ";
            if (list.length()>50) {
                sender.sendMessage(list.substring(0, list.length()-2));
                list = "";
            }
            if (color==ChatColor.WHITE)
                color = ChatColor.YELLOW;
            else color = ChatColor.WHITE;
        }
        if (!list.isEmpty()) sender.sendMessage(list.substring(0, list.length()-2));
        sender.sendMessage(rc.getPrefs().getInfoColor() + "----------------------");
        sender.sendMessage("");
    }

}
