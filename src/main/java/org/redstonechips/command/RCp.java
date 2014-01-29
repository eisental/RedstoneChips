
package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.paging.Pager;
import org.redstonechips.parsing.Parsing;

/**
 *
 * @author Tal Eisenberg
 */
public class RCp extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        // /rcp <page no> or /rcp <prev|next|last>

        if (Pager.hasPageInfo(sender)) {
            if (args.length>1) {
                sender.sendMessage(RCPrefs.getErrorColor() + "Bad rcp syntax. Expecting /rcp <page no.|prev|next|last>.");
                
            } else if (args.length==0) {
                Pager.nextPage(sender);
                
            } else if (args.length==1) {
                if (Parsing.isNumber(args[0])) {
                    Pager.gotoPage(sender, Integer.valueOf(args[0]));
                } else if ("previous".startsWith(args[0].toLowerCase())) {
                    Pager.previousPage(sender);
                } else if ("next".startsWith(args[0].toLowerCase())) {
                    Pager.nextPage(sender);
                } else if ("last".startsWith(args[0].toLowerCase())) {
                    Pager.lastPage(sender);
                } else {
                    sender.sendMessage(RCPrefs.getErrorColor() + "Bad rcp syntax. Expecting /rcp <page no.|prev|next|last>.");
                }
            } 
        } else {
            sender.sendMessage(RCPrefs.getErrorColor() + "You need to use a command with pages before using /rcp.");
        }
    }

}
