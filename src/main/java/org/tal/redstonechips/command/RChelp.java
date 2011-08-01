
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Tal Eisenberg
 */
public class RChelp extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    if (sender instanceof Player) {
			if (!CommandUtils.checkPermission(rc, (Player)sender, cmd.getName())) return true;
		}

        Map commands = (Map)rc.getDescription().getCommands();
        ChatColor infoColor = rc.getPrefs().getInfoColor();
        ChatColor errorColor = rc.getPrefs().getErrorColor();

        if (args.length==0) {
            printCommandList(sender, commands, null, infoColor, errorColor);
        } else {
            Map commandMap = (Map)commands.get(args[0]);
            if (commandMap==null) {
                printCommandList(sender, commands, args, infoColor, errorColor);
            } else printCommandHelp(sender, args, commandMap, infoColor, errorColor);
        }

        return true;
    }

    private void printCommandList(CommandSender sender, Map commands, String[] args, ChatColor infoColor, ChatColor errorColor) {
        String[] lines = new String[commands.size()];

        int i = 0;

        for (Object command : commands.keySet()) {
            lines[i] = ChatColor.YELLOW + command.toString() + ChatColor.WHITE + " - " + ((Map)commands.get(command)).get("description");
            i++;
        }

        CommandUtils.pageMaker(sender, "RedstoneChips commands", "rchelp", lines, infoColor, errorColor, CommandUtils.MaxLines-1);
        sender.sendMessage("Use " + ChatColor.YELLOW + (sender instanceof Player?"/":"") + "rchelp <command name>" + ChatColor.WHITE + " for help on a specific command.");
    }

    private void printCommandHelp(CommandSender sender, String[] args, Map commandMap, ChatColor infoColor, ChatColor errorColor) {
        List<String> lines = new ArrayList<String>();

        lines.add(ChatColor.YELLOW+commandMap.get("description").toString());
        if (commandMap.containsKey("usage") && commandMap.get("usage")!=null) {
            String[] usage = commandMap.get("usage").toString().split("\\n");
            sender.sendMessage("");

            for (String line : usage)
                lines.add(line.toString());
        }

        CommandUtils.pageMaker(sender, "/" + args[0], "rchelp", lines.toArray(new String[lines.size()]), infoColor, errorColor);
    }

}
