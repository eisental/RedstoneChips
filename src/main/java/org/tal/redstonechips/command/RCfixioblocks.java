
package org.tal.redstonechips.command;

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
            if (!CommandUtils.checkPermission(rc, sender, command.getName() + ".id", true, false)) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to use this command with a circuit id.");
                return true;
            }
            c = rc.getCircuitManager().getCircuitById(args[0]);
            if (c==null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown circuit id: " + args[0] + ".");
                return true;
            }
        } else { // use targeted circuit
            if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        }

        int blockCount = c.fixIOBlocks();

        sender.sendMessage(rc.getPrefs().getInfoColor() + "Finished fixing i/o blocks of " + c.getChipString() + ". " + blockCount + " blocks were replaced.");
        return true;
    }

}
