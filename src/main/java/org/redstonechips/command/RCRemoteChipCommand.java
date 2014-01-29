package org.redstonechips.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPermissions;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;

/**
 *
 * @author taleisenberg
 */
public abstract class RCRemoteChipCommand extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        Chip c;
        if (useTargetChip(args)) { // use target chip
            c = CommandUtils.findTargetChip(sender);
            if (c!=null) runWithChip(c, sender, command, label, args);
        } else {
            if (!RCPermissions.enforceRemoteCommand(sender, command.getName())) return ;            
            c = rc.chipManager().getAllChips().getById(args[0]);
            if (c!=null) runWithChip(c, sender, command, label, truncateArgs(args));
            else
                sender.sendMessage(RCPrefs.getErrorColor() + "There's no activated chip with id " + args[0]);                
        }        
    }

    protected abstract void runWithChip(Chip target, CommandSender sender, Command command, String label, String[] args);

    protected int argsCountForLocalTarget() {
        return 0;
    }
    
    private static String[] truncateArgs(String[] args) {
        String[] ret = new String[args.length-1];        
        System.arraycopy(args, 1, ret, 0, ret.length);        
        return ret;
    }

    private boolean useTargetChip(String[] args) {
        //System.out.println("len=" + args.length + " count=" + argsCountForLocalTarget());
        return args.length==argsCountForLocalTarget();
    }
        
}
