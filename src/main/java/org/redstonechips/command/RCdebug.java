
package org.redstonechips.command;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.user.Debugger;
import org.redstonechips.user.Debugger.Flag;
import org.redstonechips.user.UserSession;

/**
 *
 * @author Tal Eisenberg
 */
public class RCdebug extends RCCommand {
    @Override
    public boolean isPlayerRequired() { return true; }
    
    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {                
        if (args.length==0) {
            // toggle debug on target chip.
            Chip c = CommandUtils.findTargetChip(sender);
            if (c!=null) {
                Debugger d = rc.getUserSession((Player)sender, true).getDebugger();                
                toggleChipDebug(sender, d, c);
            }
        } else {
            if ("list".startsWith(args[0].toLowerCase())) {
                listDebuggedChips((Player)sender);
                return;
            } 
            
            Debugger d = rc.getUserSession((Player)sender, true).getDebugger();
            if ("clear".startsWith(args[0].toLowerCase())) {
                d.clear();
                info(sender, "Cleared debug list.");

            } else if (args[0].equals(".")) {
                pauseDebugger(sender, d);
                
            } else if (args[0].equalsIgnoreCase("io")) { // toggle io messages on target chip.
                Chip c = CommandUtils.findTargetChip(sender);
                if (c!=null) toggleChipIODebug(sender, d, c);

            } else if (args.length>=2 && args[1].equalsIgnoreCase("io")) { // toggle io messages using chip id.
                Chip c = rc.chipManager().getAllChips().getById(args[0]);
                if (c!=null) toggleChipIODebug(sender, d, c); 
                else error(sender, "Unknown circuit id or bad argument: " + args[0]);

            } else { // toggle debug using chip id.
                Chip c = rc.chipManager().getAllChips().getById(args[0]);
                if (c!=null) toggleChipDebug(sender, d, c); 
                else error(sender, "Unknown circuit id or bad argument: " + args[0]);                     
            }
        }
    }

    private void listDebuggedChips(Player player) {
        UserSession s = rc.getUserSession(player, false);
        Debugger d = s.getDebugger();
        List<Chip> chips = d.getChips();
        
        if (chips.isEmpty()) {
            info(player, "Your debug list is empty.");
        } else {
            String title;
            if (d.isPaused())
                title = chips.size() + " debugged IC(s) " + ChatColor.AQUA + "(Debugging Paused)" + RCPrefs.getInfoColor();
            else title = chips.size() + " debugged IC(s)";
            
            RClist.printCircuitList(player, chips, title);
        }
    }

    private void pauseDebugger(CommandSender sender, Debugger d) {
        if (d.isPaused()) {
            d.setPaused(false);
            info(sender, "Unpaused debugging.");
        } else {
            d.setPaused(true);
            info(sender, "Paused debugging. Type '/rcdebug .' again to resume.");
        }
    }

    private void toggleChipIODebug(CommandSender sender, Debugger d, Chip c) {
        if (d.isDebugFlagSet(c, Flag.IO)) {
            d.removeFlag(c, Flag.IO);
            info(sender, "Stopped IO debugging " + c + ".");
        } else {
            if (!d.isDebugging(c)) d.addChip(c);
            d.addFlag(c, Flag.IO);
            info(sender, "IO debugging " + c + ".");
        }
    }

    private void toggleChipDebug(CommandSender sender, Debugger d, Chip c) {
        if (d.isDebugging(c)) {
            d.removeChip(c);
            info(sender, "Stopped debugging " + c + ".");
        } else {
            d.addChip(c);
            info(sender, "Debugging " + c + ".");            
        }
    }
}
