
package org.tal.redstonechips.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCreset extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Circuit c;

        if (args.length>0) { // use circuit id.
            try {
                int id = Integer.decode(args[0]);
                c = rc.getCircuitManager().getCircuits().get(id);
                if (c==null) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Invalid circuit id: " + id + ".");
                    return true;
                }
            } catch (NumberFormatException ne) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad argument: " + args[0] + ". Expecting a number.");
                return true;
            }
        } else { // use targeted circuit
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        }

        rc.getCircuitManager().resetCircuit(c, sender);

        return true;
    }

}
