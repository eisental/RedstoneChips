
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.circuit.InterfaceBlock;
import org.tal.redstonechips.circuit.OutputPin;

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
        List<InputPin> inputList = rc.getCircuitManager().lookupInputSource(pinBlock);
        
        if (inputList!=null) printInputInfo(sender, inputList, rc);

        InputPin input = rc.getCircuitManager().lookupInputBlock(pinBlock);
        if (input!=null) {
            List<InputPin> i = new ArrayList<InputPin>();
            i.add(input);
            printInputInfo(sender, i, rc);
        }
        
        OutputPin o = rc.getCircuitManager().lookupOutputBlock(pinBlock);

        if (o!=null) printOutputInfo(sender, o, rc);
                
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(pinBlock);
        if (c==null || c.interfaceBlocks == null) return;
        
        InterfaceBlock i = null;
        Location pinLoc = pinBlock.getLocation();
        
        for (InterfaceBlock bl : c.interfaceBlocks) {
            if (bl.getLocation().equals(pinLoc)) {
                i = bl;
                break;
            }
        }

        if (i!=null) 
            printInterfaceInfo(sender, i, rc);
    }

    private static void printInputInfo(CommandSender sender, List<InputPin> inputList, org.tal.redstonechips.RedstoneChips rc) {
        for (InputPin io : inputList) {
            Circuit c = io.getCircuit();
            int i = io.getIndex();
            sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.WHITE + "input pin "
                    + i + " - " + (c.getInputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
        }            
    }

    private static void printOutputInfo(CommandSender sender, OutputPin o, org.tal.redstonechips.RedstoneChips rc) {
        Circuit c = o.getCircuit();
        int i = o.getIndex();
        sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "output pin "
                + i + " - " + (c.getOutputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));                        
    }
    
    private static void printInterfaceInfo(CommandSender sender, InterfaceBlock in, org.tal.redstonechips.RedstoneChips rc) {
        Circuit c = in.getCircuit();
        int i = in.getIndex();
        sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "interface block "
                + i + ".");                                
    }
}
