
package org.tal.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdestroy extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        
        Circuit c = CommandUtils.findTargetCircuit(rc, sender);
        if (c!=null) {
            if (rc.getCircuitManager().destroyCircuit(c, sender, true))
                sender.sendMessage(rc.getPrefs().getInfoColor() + "The " + c.getCircuitClass() + " chip is destroyed.");
        }

        return true;
    }

}
