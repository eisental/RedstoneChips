
package org.tal.redstonechips.command;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.PrefsManager;
import org.tal.redstonechips.circuit.RecursiveChipScanner;
import org.tal.redstonechips.circuit.ScanParameters;
import org.tal.redstonechips.circuit.io.IOBlock.Type;

/**
 *
 * @author Tal Eisenberg
 */
public class RCactivate extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;
        
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;

        Block target = CommandUtils.targetBlock(player);
        if (target.getType()==Material.WALL_SIGN) {
            MaterialData inputBlockType = null, outputBlockType = null, interfaceBlockType = null;
            
            boolean verbose = false;
            if (args.length>0) {
                if (args[args.length-1].equalsIgnoreCase("verbose")) {
                    verbose = true;
                }
                
                try {
                    if (args.length>=(verbose?2:1))
                        inputBlockType = PrefsManager.findMaterial(args[0]);
                    if (args.length>=(verbose?3:2))
                        outputBlockType = PrefsManager.findMaterial(args[1]);
                    if (args.length>=(verbose?4:3))
                        interfaceBlockType = PrefsManager.findMaterial(args[2]);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(ie.getMessage());
                    return true;
                }                
                    
            }
            

            if (inputBlockType==null) inputBlockType = rc.getPrefs().getInputBlockType();
            if (outputBlockType==null) outputBlockType = rc.getPrefs().getOutputBlockType();
            if (interfaceBlockType==null) interfaceBlockType = rc.getPrefs().getInterfaceBlockType();
            
            activate(target, inputBlockType, outputBlockType, interfaceBlockType, sender, verbose, rc);
            
        } else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at a wall sign.");
        }

        return true;
    }

    public static boolean activate(Block target, MaterialData inputBlockType, MaterialData outputBlockType, 
            MaterialData interfaceBlockType, CommandSender sender, boolean verbose, org.tal.redstonechips.RedstoneChips rc) {

        Map<Type, MaterialData> iom = new HashMap<Type, MaterialData>();        
        iom.put(Type.INPUT, inputBlockType);
        iom.put(Type.OUTPUT, outputBlockType);
        iom.put(Type.INTERFACE, interfaceBlockType);
        if (rc.getCircuitManager().checkForCircuit(ScanParameters.generate(target, iom), sender, verbose)==-2) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Could not activate chip.");
            return false;
        } else return true;        
    }

}
