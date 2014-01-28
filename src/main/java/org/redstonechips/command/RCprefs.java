
package org.redstonechips.command;

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
    public void run(CommandSender sender, Command command, String label, String[] args) {        
        if (args.length==0) { // list all preferences
            rc.prefs().printYaml(sender, rc.prefs().getPrefs());
            info(sender, "Type /rcprefs <name> <value> to make changes.");
        } else if (args.length==1) { // show one key value pair
            Object o = rc.prefs().getPrefs().get(args[0]);
            if (o==null) error(sender, "Unknown preferences key: " + args[0]);
            else {
                Map<String,Object> map = new HashMap<>();
                map.put(args[0], o);

                rc.prefs().printYaml(sender, map);
            }
        } else if (args.length>=2) { // set value
            if (!rc.permissionManager().enforceCommand(sender, command.getName() + ".set", true, false)) {
                error(sender, "You do not have permissions to change preferences values.");
                return;
            }

            String val = "";
            for (int i=1; i<args.length; i++)
                val += args[i] + " ";

            try {
                Map<String, Object> map = rc.prefs().setYaml(args[0] + ": " + val);
                rc.prefs().printYaml(sender, map);
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(rc.prefs().getErrorColor() + ie.getMessage());
                return;
            }
            
            info(sender, "Saving changes...");
            rc.prefs().savePrefs();
        } else {
            error(sender, "Bad rcprefs syntax.");
        }
    }
}
