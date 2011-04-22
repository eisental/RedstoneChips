
package org.tal.redstonechips.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdebug extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int id = -1;
        boolean add = true;
        boolean alloff = false;

        if (args.length==1) {
            // on, off or id (then on)
            if (args[0].equalsIgnoreCase("on"))
                add = true;
            else if (args[0].equalsIgnoreCase("off"))
                add = false;
            else if (args[0].equals("alloff"))
                alloff = true;
            else {
                try {
                    id = Integer.decode(args[0]);
                    add = true;
                } catch (NumberFormatException ne) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad argument: " + args[0] + ". Expecting on, off or a chip id.");
                }
            }
        } else if (args.length==2) {
            try {
                id = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad argument: " + args[0] + ". Expecting a chip id number.");
                return true;
            }

            if (args[1].equalsIgnoreCase("on"))
                add = true;
            else if (args[1].equalsIgnoreCase("off"))
                add = false;
            else {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad argument: " + args[1] + ". Expecting on or off.");
                return true;
            }
        }

        if (alloff) {
            for (Circuit c : rc.getCircuitManager().getCircuits().values())
                if (c.getDebuggers().contains(sender)) c.removeDebugger(sender);
            sender.sendMessage(rc.getPrefs().getInfoColor() + "You will not receive debug messages from any chip.");
        } else {
            Circuit c;
            if (id!=-1) {
                if (!rc.getCircuitManager().getCircuits().containsKey(id)) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad chip id " + id + ". Could only find " + rc.getCircuitManager().getCircuits().size() + " active chips.");
                    return true;
                }
                c = rc.getCircuitManager().getCircuits().get(id);
            } else {
                c = CommandUtils.findTargetCircuit(rc, sender);
                if (c==null) return true;
            }

            if (add) {
                try {
                    if (id!=-1 && !sender.isOp()) {
                        sender.sendMessage(rc.getPrefs().getErrorColor() + "You must have admin priviliges to debug a chip by id.");
                        return true;
                    } else
                        c.addDebugger(sender);
                } catch (IllegalArgumentException ie) {
                    try {
                        c.removeDebugger(sender);
                    } catch (IllegalArgumentException me) {
                        sender.sendMessage(rc.getPrefs().getInfoColor() + me.getMessage());
                        return true;
                    }
                    sender.sendMessage(rc.getPrefs().getInfoColor() + "You will not receive any more debug messages from the " + c.getClass().getSimpleName() + " circuit.");

                    return true;
                }
                sender.sendMessage(rc.getPrefs().getDebugColor() + "You are now a debugger of the " + c.getClass().getSimpleName() + " circuit.");
            } else {
                try {
                    c.removeDebugger(sender);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefs().getInfoColor() + ie.getMessage());
                    return true;
                }
                sender.sendMessage(rc.getPrefs().getInfoColor() + "You will not receive any more debug messages from the " + c.getClass().getSimpleName() + " circuit.");
            }
        }

        return true;
    }
}
