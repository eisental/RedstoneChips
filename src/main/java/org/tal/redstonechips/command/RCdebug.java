
package org.tal.redstonechips.command;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.user.Debugger;
import org.tal.redstonechips.user.Debugger.Flag;
import org.tal.redstonechips.user.UserSession;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdebug extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        Player p = CommandUtils.checkIsPlayer(rc, sender);
        if (p==null) return true;
                
        if (args.length==0) {
            // toggle debug on target chip.
            Circuit c = CommandUtils.findTargetCircuit(rc, sender);
            if (c==null) return true;
            else {
                Debugger d = rc.getUserSession(p, true).getDebugger();                
                toggleCircuitDebug(sender, d, c);
            }

        } else {
            if ("list".startsWith(args[0].toLowerCase())) {
                listDebuggedCircuits(p);
                return true;
            } 
            
            Debugger d = rc.getUserSession(p, true).getDebugger();
            if ("clear".startsWith(args[0].toLowerCase())) {
                d.clear();
                sender.sendMessage(rc.getPrefs().getInfoColor() + "Cleared debug list.");
            } else if (args[0].equals(".")) {
                pauseDebugger(sender, d);
                
            } else if (args[0].equalsIgnoreCase("io")) { // toggle io messages on target chip.
                Circuit c = CommandUtils.findTargetCircuit(rc, sender);
                if (c==null) return true;                
                else toggleCircuitIODebug(sender, d, c);
                
            } else if (args.length>=2 && args[1].equalsIgnoreCase("io")) { // toggle io messages using chip id.
                Circuit c = rc.getCircuitManager().getCircuitById(args[0]);
                if (c==null) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown circuit id or bad argument: " + args[0]);
                    return true;
                } else
                    toggleCircuitIODebug(sender, d, c);                
                
            } else { // toggle debug using chip id.
                Circuit c = rc.getCircuitManager().getCircuitById(args[0]);
                if (c==null) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown circuit id or bad argument: " + args[0]);
                    return true;
                } else
                    toggleCircuitDebug(sender, d, c);
            }
        }

        return true;
    }

    private void listDebuggedCircuits(Player player) {
        UserSession s = rc.getUserSession(player, false);
        Debugger d = s.getDebugger();
        List<Circuit> circuits = d.getCircuits();
        
        if (circuits.isEmpty()) {
            player.sendMessage(rc.getPrefs().getInfoColor() + "Your debug list is empty.");
        } else {
            String title;
            if (d.isPaused())
                title = circuits.size() + " debugged IC(s) " + ChatColor.AQUA + "(Debugging Paused)" + rc.getPrefs().getInfoColor();
            else title = circuits.size() + " debugged IC(s)";
            
            RClist.printCircuitList(player, circuits, title, rc);
        }
    }

    private void pauseDebugger(CommandSender s, Debugger d) {
        if (d.isPaused()) {
            d.setPaused(false);
            s.sendMessage(rc.getPrefs().getInfoColor() + "Unpaused debugging.");
        } else {
            d.setPaused(true);
            s.sendMessage(rc.getPrefs().getInfoColor() + "Paused debugging. Type '/rcdebug .' again to resume.");
        }
    }

    private void toggleCircuitIODebug(CommandSender sender, Debugger d, Circuit c) {
        if (d.isDebugFlagSet(c, Flag.IO)) {
            d.removeFlag(c, Flag.IO);
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Stopped IO debugging " + c.getChipString() + ".");
        } else {
            if (!d.isDebugging(c)) d.addCircuit(c);
            d.addFlag(c, Flag.IO);
            sender.sendMessage(rc.getPrefs().getInfoColor() + "IO debugging " + c.getChipString() + ".");
        }
    }

    private void toggleCircuitDebug(CommandSender sender, Debugger d, Circuit c) {
        if (d.isDebugging(c)) {
            d.removeCircuit(c);
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Stopped debugging " + c.getChipString() + ".");
        } else {
            d.addCircuit(c);
            sender.sendMessage(rc.getPrefs().getInfoColor() + "Debugging " + c.getChipString() + ".");            
        }
    }
}
