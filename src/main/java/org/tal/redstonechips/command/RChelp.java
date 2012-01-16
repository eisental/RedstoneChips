
package org.tal.redstonechips.command;

import org.tal.redstonechips.page.ArrayLineSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.tal.redstonechips.page.Pager;

/**
 *
 * @author Tal Eisenberg
 */
public class RChelp extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, cmd.getName(), false, true)) return true;

        Map commands = (Map)rc.getDescription().getCommands();
        ChatColor infoColor = rc.getPrefs().getInfoColor();
        ChatColor errorColor = rc.getPrefs().getErrorColor();

        if (args.length==0) {
            printCommandList(sender, commands, null, infoColor, errorColor);
        } else if (args[0].equalsIgnoreCase("all")) {
            String help = "";
            for (Object command : commands.keySet()) {
                help += infoColor + "/" + command.toString() + ": ";
                help += getCommandHelp((String)command, rc) + "\n";
            }
            sender.sendMessage(help);
        } else {
            if (!commands.containsKey(args[0])) {
                printCommandList(sender, commands, args, infoColor, errorColor);
            } else printCommandHelp(sender, args[0], rc);
        }

        return true;
    }

    public static void printCommandList(CommandSender sender, Map commands, String[] args, ChatColor infoColor, ChatColor errorColor) {
        String[] lines = new String[commands.size()];

        int i = 0;

        for (Object command : commands.keySet()) {
            lines[i] = ChatColor.YELLOW + command.toString() + ChatColor.WHITE + " - " + ((Map)commands.get(command)).get("description");
            i++;
        }

        Pager.beginPaging(sender, "RedstoneChips commands", new ArrayLineSource(lines), infoColor, errorColor, Pager.MaxLines-1);
        sender.sendMessage("Use " + ChatColor.YELLOW + (sender instanceof Player?"/":"") + "rchelp <command name>" + ChatColor.WHITE + " for help on a specific command.");
    }

    public static String getCommandHelp(String  command, org.tal.redstonechips.RedstoneChips rc) {
        Map commands = (Map)rc.getDescription().getCommands();        
        Map commandMap = (Map)commands.get(command);
        String help = "";
        help += ChatColor.YELLOW+commandMap.get("description").toString();
        if (commandMap.containsKey("usage") && commandMap.get("usage")!=null) {
            help += "\n" + ChatColor.WHITE + commandMap.get("usage").toString();
        }
        
        List<Permission> perms = new ArrayList<Permission>();
        for (Permission p : rc.getDescription().getPermissions()) {
            if (p.getName().startsWith("redstonechips.command." + command.toString())) {
                perms.add(p);
            }
        }
        
        if (commandMap.containsKey("aliases") && commandMap.get("aliases")!=null) {
            String alist = "";
            for (String a : (List<String>)commandMap.get("aliases"))
                alist += "/" + a + ", ";
            
            help += "\n" + ChatColor.AQUA + "aliases: " + alist.substring(0, alist.length()-2) + "\n";
        }
        
        if (!perms.isEmpty()) {
            help += "\n" + ChatColor.RED + "permissions:\n";
            for (Permission p : perms) 
                help += ChatColor.YELLOW + " - " + p.getName() + "\n   " + ChatColor.WHITE + p.getDescription() + "\n";
        }

        return help;
    }

    public static void printCommandHelp(CommandSender sender, String command, org.tal.redstonechips.RedstoneChips rc) {
        Pager.beginPaging(sender, "/" + command, getCommandHelp(command, rc), 
                rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
    }
    
}
