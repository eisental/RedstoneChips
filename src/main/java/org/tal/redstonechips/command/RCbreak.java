
package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCbreak extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
    
        Circuit c = null;        
        if (args.length>0) { // use chip id
            if (!CommandUtils.checkPermission(rc, sender, command.getName() + ".id", true, false)) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to remotely deactivate a circuit.");
                return true;
            }
            
            c = rc.getCircuitManager().getCircuitById(args[0]);

            if (c==null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "There's no activated circuit with id " + args[0]);
                return true;
            }            
        } else { // use target block.
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        }

        if (rc.getCircuitManager().destroyCircuit(c, sender, false))
            sender.sendMessage(ChatColor.YELLOW + c.getChipString() + rc.getPrefs().getInfoColor() + " was deactivated.");

        return true;
    }

}
