
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
        
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        
        Block target = CommandUtils.targetBlock(player);
        try {
            printPinInfo(target, player, rc);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + e.getMessage());
        }

        return true;
    }

    public static void printPinInfo(Block pinBlock, CommandSender sender, org.tal.redstonechips.RedstoneChips rc) {
        List<InputPin> inputList = rc.getCircuitManager().lookupInputBlock(pinBlock);
        
        if (inputList!=null) printInputInfo(sender, inputList, rc);
        else {
            Object[] o = rc.getCircuitManager().lookupOutputBlock(pinBlock);
            
            if (o!=null) printOutputInfo(sender, o, rc);
            else throw new IllegalArgumentException("You need to point at an output lever or an input redstone source.");
        }
        
    }

    private static void printInputInfo(CommandSender sender, List<InputPin> inputList, org.tal.redstonechips.RedstoneChips rc) {
        for (InputPin io : inputList) {
            Circuit c = io.getCircuit();
            int i = io.getIndex();
            sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.WHITE + "input pin "
                    + i + " - " + (c.getInputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
        }            
    }

    private static void printOutputInfo(CommandSender sender, Object[] o, org.tal.redstonechips.RedstoneChips rc) {
        Circuit c = (Circuit)o[0];
        int i = (Integer)o[1];
        sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "output pin "
                + i + " - " + (c.getOutputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));                        
    }
}
