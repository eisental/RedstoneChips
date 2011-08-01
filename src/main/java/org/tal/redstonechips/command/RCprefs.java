
package org.tal.redstonechips.command;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Tal Eisenberg
 */
public class RCprefs extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
			if (!CommandUtils.checkPermission(rc, (Player)sender, command.getName())) return true;
		}
		
		if (args.length==0) { // list preferences
            rc.getPrefs().printYaml(sender, rc.getPrefs().getPrefs());
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Type /rcprefs <name> <value> to make changes.");
        } else if (args.length==1) { // show one key value pair
            Object o = rc.getPrefs().getPrefs().get(args[0]);
            if (o==null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown preferences key: " + args[0]);
            else {
                Map<String,Object> map = new HashMap<String,Object>();
                map.put(args[0], o);

                rc.getPrefs().printYaml(sender, map);
            }
        } else if (args.length>=2) { // set value
            if (!checkpermissions(sender, "rcprefs.set")) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to change preferences values.");
                return true;
            }

            String val = "";
            for (int i=1; i<args.length; i++)
                val += args[i] + " ";

            try {
                Map<String, Object> map = rc.getPrefs().setYaml(args[0] + ": " + val);
                rc.getPrefs().printYaml(sender, map);
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
                return true;
            }
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Saving changes...");
            rc.getPrefs().savePrefs();
        } else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad rcprefs syntax.");
        }
        
        return true;
    }
	
	private boolean checkpermissions(CommandSender sender, String command) {
		if (!rc.getPrefs().getUsePermissions()) {
			return sender.isOp();
		} else {
			if (sender instanceof Player) {
				return (((Player)sender).hasPermission("redstonechips.command." + command) && !((Player)sender).hasPermission("redstonechips.command." + command + ".deny"));
			} else {
				return true;
			}
		}
	}
}
