
package org.tal.redstonechips.commands;

import java.util.ArrayList;
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
            printClassesList(sender, args, circuitClasses);
        }

        return true;
    }

    private void printClassesList(CommandSender sender, String[] args, Map<String, Class> circuitClasses) {
        List<String> names = Arrays.asList(circuitClasses.keySet().toArray(new String[circuitClasses.size()]));
        Collections.sort(names);

        List<String> lines = new ArrayList<String>();

        String list = "";
        ChatColor color = ChatColor.WHITE;
        for (String name : names) {
            list += color + name + ", ";
            if (list.length()>50) {
                lines.add(list.substring(0, list.length()-2));
                list = "";
            }
            
            if (color==ChatColor.WHITE) color = ChatColor.YELLOW;
            else color = ChatColor.WHITE;
        }

        if (!list.isEmpty()) 
            CommandUtils.pageMaker(sender, "Installed circuit classes", "rcclasses", args, lines.toArray(new String[lines.size()]), rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
    }

}
