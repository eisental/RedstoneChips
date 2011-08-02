
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
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

        if (args.length>0) {
            if (args[0].equalsIgnoreCase("all")) {
                if (CommandUtils.checkPermission(rc, sender, command.getName() + ".all", true, false))
                    resetAllCircuits(sender);
                else sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to reset all circuits.");
                return true;
            }

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
            if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        }

        rc.getCircuitManager().resetCircuit(c, sender);

        return true;
    }

    private void resetAllCircuits(CommandSender sender) {
        List<Circuit> failed = new ArrayList<Circuit>();
        List<Circuit> allCircuits = new ArrayList<Circuit>();
        allCircuits.addAll(rc.getCircuitManager().getCircuits().values());

        for (Circuit c : allCircuits) {
            if (!rc.getCircuitManager().resetCircuit(c, sender)) {
                failed.add(c);
            }
        }

        if (sender!=null) {
            if (!failed.isEmpty()) {
                String ids = "";
                for (Circuit c : failed)
                    ids += c.id + ", ";

                ids = ids.substring(0, ids.length()-2);
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Some circuits could not reactivate: " + ids);
            } else {
                sender.sendMessage(ChatColor.AQUA + "Successfully reset all active circuits.");
            }
        }

    }
}
