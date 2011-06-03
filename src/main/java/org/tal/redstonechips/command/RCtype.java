
package org.tal.redstonechips.command;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import java.util.List;

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
		List<rcTypeReceiver> receiverList = rc.rcTypeReceivers.get(block.getLocation());
	
        if (receiverList==null) {
            player.sendMessage(rc.getPrefs().getErrorColor() + "You must point towards a typing block (a terminal circuit's interface block for example) to type.");
        } else {
			for (rcTypeReceiver receiver : receiverList)
				receiver.type(args, player);
            player.sendMessage(rc.getPrefs().getInfoColor() + "Input sent.");
        }

        return true;
    }

}
