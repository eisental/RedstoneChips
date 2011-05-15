
package org.tal.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.util.ParsingUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class RCp extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /rcp <page no> or /rcp <prev|next|last>

        if (CommandUtils.playerPages.containsKey(sender)) {
            PageInfo pageInfo = CommandUtils.playerPages.get(sender);

            if (args.length>1) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad rcp syntax. Expecting /rcp <page no.|prev|next|last>.");
                return true;
            } else if (args.length==0) {
                if (pageInfo.page<pageInfo.pageCount)
                    pageInfo.page = pageInfo.page + 1;
                else {
                    pageInfo.page = 1;
                }
            } else if (args.length==1) {
                if (ParsingUtils.isNumber(args[0])) {
                    pageInfo.page = Integer.valueOf(args[0]);
                } else if ("previous".startsWith(args[0])) {
                    if (pageInfo.page>1)
                        pageInfo.page = pageInfo.page - 1;
                    else {
                        sender.sendMessage(rc.getPrefs().getErrorColor() + "There's no previous page.");
                        return true;
                    }
                } else if ("next".startsWith(args[0])) {
                    if (pageInfo.page<pageInfo.pageCount)
                        pageInfo.page = pageInfo.page + 1;
                    else {
                        sender.sendMessage(rc.getPrefs().getErrorColor() + "There are no more pages.");
                        return true;
                    }
                } else if ("last".startsWith(args[0])) {
                    pageInfo.page = pageInfo.pageCount;
                } else {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad rcp syntax. Expecting /rcp <page no.|prev|next|last>.");
                    return true;
                }
            } 

            CommandUtils.pageMaker(sender, pageInfo.title, null, pageInfo.lines, pageInfo.infoColor, pageInfo.errorColor, pageInfo.linesPerPage);
            return true;

        } else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to use a command with pages before using /rcp.");
            return true;
        }
    }

}
