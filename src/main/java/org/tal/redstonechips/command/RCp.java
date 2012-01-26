
package org.tal.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.eisental.common.page.Pager;
import net.eisental.common.parsing.ParsingUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class RCp extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /rcp <page no> or /rcp <prev|next|last>

        if (Pager.hasPageInfo(sender)) {
            if (args.length>1) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad rcp syntax. Expecting /rcp <page no.|prev|next|last>.");
                return true;
                
            } else if (args.length==0) {
                Pager.nextPage(sender);
                
            } else if (args.length==1) {
                if (ParsingUtils.isNumber(args[0])) {
                    Pager.gotoPage(sender, Integer.valueOf(args[0]));
                } else if ("previous".startsWith(args[0])) {
                    Pager.previousPage(sender);
                } else if ("next".startsWith(args[0])) {
                    Pager.nextPage(sender);
                } else if ("last".startsWith(args[0])) {
                    Pager.lastPage(sender);
                } else {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad rcp syntax. Expecting /rcp <page no.|prev|next|last>.");
                }
            } 
        } else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to use a command with pages before using /rcp.");
        }
        
        return true;
    }

}
