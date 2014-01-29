
package org.redstonechips.command;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPermissions;
import org.redstonechips.RCPrefs;

/**
 *
 * @author Tal Eisenberg
 */
public class RCprefs extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {        
        if (args.length==0) { // list all preferences
            RCPrefs.printYaml(sender, RCPrefs.getPrefs());
            info(sender, "Type /rcprefs <name> <value> to make changes.");
        } else if (args.length==1) { // show one key value pair
            Object o = RCPrefs.getPrefs().get(args[0]);
            if (o==null) error(sender, "Unknown preferences key: " + args[0]);
            else {
                Map<String,Object> map = new HashMap<>();
                map.put(args[0], o);

                RCPrefs.printYaml(sender, map);
            }
        } else if (args.length>=2) { // set value
            if (!RCPermissions.enforceCommand(sender, command.getName() + ".set", true, false)) {
                error(sender, "You do not have permissions to change preferences values.");
                return;
            }

            String val = "";
            for (int i=1; i<args.length; i++)
                val += args[i] + " ";

            try {
                Map<String, Object> map = RCPrefs.setYaml(args[0] + ": " + val);
                RCPrefs.printYaml(sender, map);
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(RCPrefs.getErrorColor() + ie.getMessage());
                return;
            }
            
            info(sender, "Saving changes...");
            RCPrefs.savePrefs();
        } else {
            error(sender, "Bad rcprefs syntax.");
        }
    }
}
