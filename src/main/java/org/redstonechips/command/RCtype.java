
package org.redstonechips.command;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCTypeReceiver;

/**
 *
 * @author Tal Eisenberg
 */
public class RCtype extends RCCommand {

    @Override
    public boolean isPlayerRequired() { return true; }
    
    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player)sender;
        Block block = CommandUtils.targetBlock(player);
        RCTypeReceiver t = rc.rcTypeReceivers.get(block.getLocation());
    
        if (t==null) {
            error(sender, "You must point towards a typing block (check the docs of your chip) to type.");
        } else {
            info(sender, "Input sent.");
            t.type(args, player);
        }
    }

}
