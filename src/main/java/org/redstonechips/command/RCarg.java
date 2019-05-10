
package org.redstonechips.command;

import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.util.Signs;

/**
 *
 * @author Tal Eisenberg
 */
public class RCarg extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] cmdArgs) {
        Chip c = CommandUtils.findTargetChip(sender);

        if (c==null) return;
        else if (cmdArgs.length<2 || (cmdArgs.length%2)!=0) {
            error(sender, "Bad syntax. Expecting /rcarg <arg index/clear/add> <arg value>...<arg index> <arg value>");
            return;
        }

        String[] args = new String[c.args.length];
        System.arraycopy(c.args, 0, args, 0, args.length);

        for (int i=0; i<cmdArgs.length; i+=2) {
            String[] newArgs = editArgs(sender, args, cmdArgs[i], cmdArgs[i+1]);
            if (newArgs!=null) args = newArgs;
        }

        String oldArgs[] = new String[c.args.length];
        System.arraycopy(c.args, 0, oldArgs, 0, oldArgs.length);

        writeChipSignArgs(c, args);

        if (!rc.chipManager().resetChip(c, sender)) {
            error(sender, "Could not reactivate circuit with new sign arguments. ");

            // revert to old args.
            writeChipSignArgs(c, oldArgs);
            rc.chipManager().resetChip(c, sender);
        }
    }

    /**
     * Replaces the sign arguments of chip c with args. 
     * @param c Affected chip.
     * @param args New sign arguments.
     */
    public static void writeChipSignArgs(Chip c, String[] args) {
        Sign sign = (Sign)c.activationBlock.getBlock().getState();
        Signs.writeSignArgs(sign, args);
        sign.update();        
    }

    private static String[] editArgs(CommandSender sender, String[] args, String index, String value) {
        int idx;

        if ("clear".startsWith(index.toLowerCase())) {
            int clearIdx;

            try {
                clearIdx = Integer.decode(value) - 1;
            } catch (NumberFormatException ne) {
                sender.sendMessage(RCPrefs.getErrorColor() + "Bad argument number: " + value);
                return null;
            }

            if (clearIdx>=args.length || clearIdx<0) {
                sender.sendMessage(RCPrefs.getErrorColor() + "Argument number out of bounds: " + (clearIdx+1));
                return null;
            }

            String[] tempArgs = new String[args.length-1];
            int tempIdx = 0;
            for (int i=0; i<args.length; i++) {
                if (i!=clearIdx) {
                    tempArgs[tempIdx] = args[i];
                    tempIdx++;
                }
            }

            sender.sendMessage(RCPrefs.getInfoColor() + "Clearing argument #" + (clearIdx+1) + ": " + args[clearIdx]);

            args = tempArgs;
        } else {
            if ("add".startsWith(index.toLowerCase())) idx = args.length;
            else {
                try {
                    idx = Integer.decode(index) - 1;
                } catch (NumberFormatException ne) {
                    sender.sendMessage(RCPrefs.getErrorColor() + "Bad argument number: " + index);
                    return null;
                }
            }

            if (idx>args.length || idx<0) {
                sender.sendMessage(RCPrefs.getErrorColor() + "Argument number out of bounds: " + (idx+1));
                return null;
            } else {
                if (idx==args.length) {
                    // add to last
                    sender.sendMessage(RCPrefs.getInfoColor() + "Adding argument #" + (idx+1) + ": " + value);
                    String[] tempArgs = new String[args.length+1];
                    System.arraycopy(args, 0, tempArgs, 0, args.length);
                    tempArgs[tempArgs.length-1] = value;
                    args = tempArgs;
                } else {
                    sender.sendMessage(RCPrefs.getInfoColor() + "Setting argument #" + (idx+1) + " to " + value + " (was " + args[idx] + ").");
                    args[idx] = value;
                }
            }
        }

        return args;
    }
}
