
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
        boolean success = false;
        
        List<InputPin> inputList = rc.getCircuitManager().getInputPinBySource(pinBlock.getLocation());
        
        if (inputList!=null) { printInputInfo(sender, inputList, rc); success = true; }

        InputPin input = rc.getCircuitManager().getInputPin(pinBlock.getLocation());
        if (input!=null) {
            List<InputPin> i = new ArrayList<InputPin>();
            i.add(input);
            printInputInfo(sender, i, rc);
            success = true;
        }

        List<OutputPin> outputList = rc.getCircuitManager().getOutputPinByOutputBlock(pinBlock.getLocation());
        
        if (outputList!=null) { printOutputInfo(sender, outputList, rc); success = true; }
        
        OutputPin o = rc.getCircuitManager().getOutputPin(pinBlock.getLocation());
        if (o!=null) { 
            List<OutputPin> list = new ArrayList<OutputPin>();
            list.add(o);
            printOutputInfo(sender, list, rc); 
            success = true;            
        }
                
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(pinBlock.getLocation());
        if (c!=null && c.interfaceBlocks != null) {

            InterfaceBlock i = null;
            Location pinLoc = pinBlock.getLocation();

            for (InterfaceBlock bl : c.interfaceBlocks) {
                if (bl.getLocation().equals(pinLoc)) {
                    i = bl;
                    break;
                }
            }

            if (i!=null) {
                printInterfaceInfo(sender, i, rc);
                success = true;
            } 
        }
        
        if (!success) throw new IllegalArgumentException("You must point at a chip io block.");
    }

    private static void printInputInfo(CommandSender sender, List<InputPin> inputList, org.tal.redstonechips.RedstoneChips rc) {
        for (InputPin io : inputList) {
            Circuit c = io.getCircuit();
            int i = io.getIndex();
            sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.WHITE + "input pin "
                    + i + " - " + (c.getInputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
        }            
    }

    private static void printOutputInfo(CommandSender sender, List<OutputPin> outputList, org.tal.redstonechips.RedstoneChips rc) {
        for (OutputPin o : outputList) {
            Circuit c = o.getCircuit();
            int i = o.getIndex();
            sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "output pin "
                    + i + " - " + (c.getOutputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));                        
        }
    }
    
    private static void printInterfaceInfo(CommandSender sender, InterfaceBlock in, org.tal.redstonechips.RedstoneChips rc) {
        Circuit c = in.getCircuit();
        int i = in.getIndex();
        sender.sendMessage(rc.getPrefs().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "interface block "
                + i + ".");                                
    }
}
