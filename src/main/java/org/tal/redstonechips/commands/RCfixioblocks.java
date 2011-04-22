
package org.tal.redstonechips.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCfixioblocks extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Circuit c;

        if (args.length>0) { // use circuit id.
            if (!sender.isOp()) {
                sender.sendMessage("Only ops (admins) are allowed to use this command with a circuit id.");
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
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        }

        int blockCount = c.fixIOBlocks();

        sender.sendMessage(rc.getPrefs().getInfoColor() + "Finished fixing i/o blocks of circuit " + c.id + ". " + blockCount + " blocks were replaced.");
        return true;
    }

}
