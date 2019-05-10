
package org.redstonechips.command;

import org.redstonechips.paging.ArrayLineSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.redstonechips.RCPrefs;
import org.redstonechips.paging.Pager;

/**
 *
 * @author Tal Eisenberg
 */
public class RChelp extends RCCommand {

    @Override
    public void run(CommandSender sender, Command cmd, String label, String[] args) {
        Map<String, Map<String, Object>> commands = rc.getDescription().getCommands();
        ChatColor infoColor = RCPrefs.getInfoColor();
        ChatColor errorColor = RCPrefs.getErrorColor();

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
    }

    public static void printCommandList(CommandSender sender, Map commands, String[] args, ChatColor infoColor, ChatColor errorColor) {
        List<String> lines = new ArrayList<>();

        for (Object command : commands.keySet()) 
            lines.add(ChatColor.YELLOW + command.toString() + ChatColor.WHITE + " - " + ((Map)commands.get(command)).get("description"));
        
        Collections.sort(lines);
        Pager.beginPaging(sender, "RedstoneChips commands", new ArrayLineSource(lines.toArray(new String[0])), infoColor, errorColor, Pager.MaxLines-1);
        sender.sendMessage("Use " + ChatColor.YELLOW + (sender instanceof Player?"/":"") + "rchelp <command name>" + ChatColor.WHITE + " for help on a specific command.");
    }

    public static String getCommandHelp(String  command, org.redstonechips.RedstoneChips rc) {
    	Map<String, Map<String, Object>> commands = rc.getDescription().getCommands();        
        Map<String, Object> commandMap = (Map)commands.get(command);
        String help = "";
        help += ChatColor.YELLOW+commandMap.get("description").toString();
        if (commandMap.containsKey("usage") && commandMap.get("usage")!=null) {
            help += "\n" + ChatColor.WHITE + commandMap.get("usage").toString();
        }
        
        List<Permission> perms = new ArrayList<>();
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

    public static void printCommandHelp(CommandSender sender, String command, org.redstonechips.RedstoneChips rc) {
        Pager.beginPaging(sender, "/" + command, getCommandHelp(command, rc), 
                RCPrefs.getInfoColor(), RCPrefs.getErrorColor());
    }
}
