
package org.tal.redstonechips.command;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCarg extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] cmdArgs) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;

		if (!CommandUtils.checkPermission(rc, player, command.getName())) return true;

        Block target = CommandUtils.targetBlock(player);
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);
        if (c==null) {
            player.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at a block of the circuit you wish to reset.");
            return true;
        }

        if (cmdArgs.length<2 || (cmdArgs.length%2)!=0) {
            player.sendMessage(rc.getPrefs().getErrorColor() + "Bad syntax. Expecting /rcarg <arg index/clear/add> <arg value>...<arg index> <arg value>");
            return true;
        }

        String[] args = new String[c.args.length];
        System.arraycopy(c.args, 0, args, 0, args.length);

        for (int i=0; i<cmdArgs.length; i+=2) {
            String[] editArgs = this.editArgs(sender, args, cmdArgs[i], cmdArgs[i+1]);
            if (editArgs!=null) args = editArgs;
        }

        String oldArgs[] = new String[c.args.length];
        System.arraycopy(c.args, 0, oldArgs, 0, oldArgs.length);

        editSignArgs(c, args);

        if (!rc.getCircuitManager().resetCircuit(c, sender)) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Could not reactivate circuit with new sign arguments. ");

            // revert to old args.
            editSignArgs(c, oldArgs);
            rc.getCircuitManager().resetCircuit(c, sender);
        }

        return true;
    }

    private void editSignArgs(Circuit c, String[] args) {
        final Sign sign = (Sign)c.activationBlock.getBlock().getState();
        String line = "";
        int curLine = 1;

        for (int i=0; i<args.length; i++) {
            String a = args[i];
            String added = line + " " + a;
            if (added.length()>13 && curLine!=3) {
                sign.setLine(curLine, line);
                line = a;
                curLine++;
            } else line = added;
        }

        sign.setLine(curLine, line);

        if (curLine<3)
            for (int i=curLine+1; i<4; i++) sign.setLine(i, "");

        rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, new Runnable() {
            @Override
            public void run() {
                sign.update();
            }
        });
    }

    private String[] editArgs(CommandSender sender, String[] args, String index, String value) {
        int idx;

        if (index.equalsIgnoreCase("clear")) {
            int clearIdx;

            try {
                clearIdx = Integer.decode(value) - 1;
            } catch (NumberFormatException ne) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad argument number: " + value);
                return null;
            }

            if (clearIdx>=args.length || clearIdx<0) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Argument number out of bounds: " + (clearIdx+1));
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

            sender.sendMessage(rc.getPrefs().getInfoColor() + "Removing argument #" + (clearIdx+1) + ": " + args[clearIdx]);

            args = tempArgs;
        } else {
            if (index.equalsIgnoreCase("add")) idx = args.length;
            else {
                try {
                    idx = Integer.decode(index) - 1;
                } catch (NumberFormatException ne) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad argument number: " + index);
                    return null;
                }
            }

            if (idx>args.length || idx<0) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Argument number out of bounds: " + (idx+1));
                return null;
            } else {
                if (idx==args.length) {
                    // add to last
                    sender.sendMessage(rc.getPrefs().getInfoColor() + "Adding argument #" + (idx+1) + ": " + value);
                    String[] tempArgs = new String[args.length+1];
                    System.arraycopy(args, 0, tempArgs, 0, args.length);
                    tempArgs[tempArgs.length-1] = value;
                    args = tempArgs;
                } else {
                    sender.sendMessage(rc.getPrefs().getInfoColor() + "Setting argument #" + (idx+1) + " to " + value + " (was " + args[idx] + ").");
                    args[idx] = value;
                }
            }
        }

        return args;
    }
}
