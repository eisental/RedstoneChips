
package org.tal.redstonechips.command;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.RCTypeReceiver;

/**
 *
 * @author Tal Eisenberg
 */
public class RCtype extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;
        
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        
        Block block = CommandUtils.targetBlock(player);
        RCTypeReceiver t = rc.rcTypeReceivers.get(block.getLocation());
    
        if (t==null) {
            player.sendMessage(rc.getPrefs().getErrorColor() + "You must point towards a typing block (check the docs of your chip) to type.");
        } else {
            player.sendMessage(rc.getPrefs().getInfoColor() + "Input sent.");
            t.type(args, player);
        }

        return true;
    }

}
