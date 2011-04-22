
package org.tal.redstonechips.commands;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Tal Eisenberg
 */
public class RCprefs extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
            if (!sender.isOp()) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Only admins are authorized to change preferences values.");
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
}
