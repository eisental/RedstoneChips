
package org.tal.redstonechips.command;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.InputPin;

/**
 *
 * @author Tal Eisenberg
 */
public class RCpin extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;
		
		if (!CommandUtils.checkPermission(rc, player, command.getName())) return true;
        
        Block target = CommandUtils.targetBlock(player);
        printPinInfo(target, player);

        return true;
    }

    private void printPinInfo(Block pinBlock, CommandSender sender) {
        List<InputPin> inputList = rc.getCircuitManager().lookupInputBlock(pinBlock);
        if (inputList==null) {
            Object[] oo = rc.getCircuitManager().lookupOutputBlock(pinBlock);
            if (oo==null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at an output lever or input redstone source.");
            } else { // output pin
                Circuit c = (Circuit)oo[0];
                int i = (Integer)oo[1];
                sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "output pin "
                        + i + " - " + (c.getOutputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
            }
        } else { // input pin
            for (InputPin io : inputList) {
                Circuit c = io.getCircuit();
                int i = io.getIndex();
                sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.WHITE + "input pin "
                        + i + " - " + (c.getInputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
            }
        }
    }
}
