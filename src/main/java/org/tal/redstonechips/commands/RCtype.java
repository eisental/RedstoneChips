
package org.tal.redstonechips.commands;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.rcTypeReceiver;

/**
 *
 * @author Tal Eisenberg
 */
public class RCtype extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;
        
        Block block = CommandUtils.targetBlock(player);
        rcTypeReceiver t = rc.rcTypeReceivers.get(block.getLocation());

        if (t==null) {
            player.sendMessage(rc.getPrefs().getErrorColor() + "You must point towards a typing block (a terminal circuit's interface block for example) to type.");
        } else {
            player.sendMessage(rc.getPrefs().getInfoColor() + "Input sent.");
            t.type(args, player);
        }

        return true;
    }

}
