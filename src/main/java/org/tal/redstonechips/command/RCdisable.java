package org.tal.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdisable extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;

        Circuit c;
        if (args.length==0) { // use target circuit
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        } else {
            if (!CommandUtils.checkPermission(rc, sender, command.getName() + ".id", true, false)) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to remotely disable a circuit.");
                return true;                
            }
            
            c = rc.getCircuitManager().getCircuitById(args[0]);
            if (c==null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "There's no activated chip with id " + args[0]);
                return true;
            }

        }

        if (!c.isDisabled()) {
            c.disable();
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Disabled " + c.getChipString() + ".");
        } else sender.sendMessage(rc.getPrefs().getDebugColor() + "The chip is already disabled.");
        
        return true;
    }
    
}
