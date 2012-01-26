package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import net.eisental.common.parsing.ParsingUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class RCname extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
    
        Circuit c = null;
        if (args.length==1) { // use target block.
            c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
        } else if (args.length==2) { // use circuit id.
            if (!CommandUtils.checkPermission(rc, sender, command.getName() + ".id", true, false)) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to remotely name a circuit.");
                return true;
            }

            c = rc.getCircuitManager().getCircuitById(args[0]);
            if (c==null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "There's no activated circuit with id " + args[0]);
                return true;
            }
        } else {
            sender.sendMessage("Bad /rcname command.");
            return true;
        }
        
        String name = args[args.length-1];
        
        if (ParsingUtils.isInt(name)) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Chip name can't be a number.");
            return true;
        }

        if (name.equalsIgnoreCase("remove"))
            name = null;

        Circuit b = rc.getCircuitManager().getCircuitById(name);
        if (b!=null) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Chip name is already used.");
            return true;
        }
        
        c.name = name;
        
        if (name!=null) {
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Renamed chip: " + ChatColor.YELLOW + c.getChipString());
        } else {
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Removed name: " + ChatColor.YELLOW + c.getChipString());
                
        }

        return true;
    }
    
}
