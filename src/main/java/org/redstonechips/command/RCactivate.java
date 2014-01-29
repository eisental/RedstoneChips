
package org.redstonechips.command;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.chip.io.IOBlock;
import org.redstonechips.chip.io.IOBlock.Type;
import org.redstonechips.parsing.Parsing;

/**
 *
 * @author Tal Eisenberg
 */
public class RCactivate extends RCCommand {
    @Override
    public boolean isPlayerRequired() { return true; }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        Block target = CommandUtils.targetBlock((Player)sender);
        if (target.getType()==Material.WALL_SIGN) {
            MaterialData inputBlockType = null, outputBlockType = null, interfaceBlockType = null;
            
            int verboseLevel = -1;
            if (args.length>0) {
                String lastArg = args[args.length-1];
                if (lastArg.equalsIgnoreCase("-v")) {
                    verboseLevel = 1;
                } else if (lastArg.startsWith("-v")) {
                    String sl = lastArg.substring(2);
                    if (Parsing.isInt(sl))
                        verboseLevel = Integer.parseInt(sl);
                    else sender.sendMessage(RCPrefs.getErrorColor() + "Unknown option: " + lastArg);
                }
                
                try {
                    if (args.length>=(verboseLevel!=-1?2:1))
                        inputBlockType = RCPrefs.findMaterial(args[0]);
                    if (args.length>=(verboseLevel!=-1?3:2))
                        outputBlockType = RCPrefs.findMaterial(args[1]);
                    if (args.length>=(verboseLevel!=-1?4:3))
                        interfaceBlockType = RCPrefs.findMaterial(args[2]);
                    
                    if (verboseLevel==-1) verboseLevel = 0;
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(RCPrefs.getErrorColor() + ie.getMessage());
                    return;
                }                
                    
            }
                        
            MaybeChip mChip = activate(sender, target, verboseLevel, inputBlockType, outputBlockType, interfaceBlockType);
            switch (mChip) {
                case ChipError:
                    sender.sendMessage(mChip.getError());
                    sender.sendMessage(RCPrefs.getErrorColor() + "Could not activate chip.");
                    break;
                case NotAChip:
                    sender.sendMessage(RCPrefs.getErrorColor() + "Can't find a chip at target block.");
                    break;
            }
        } else {
            sender.sendMessage(RCPrefs.getErrorColor() + "You need to point at a wall sign.");
        }
    }

    public static MaybeChip activate(CommandSender sender, Block target, int debugLevel, MaterialData inputBlockType, MaterialData outputBlockType, MaterialData interfaceBlockType) {

        org.redstonechips.RedstoneChips rc = org.redstonechips.RedstoneChips.inst();
        
        if (inputBlockType==null) inputBlockType = RCPrefs.getInputBlockType();
        if (outputBlockType==null) outputBlockType = RCPrefs.getOutputBlockType();
        if (interfaceBlockType==null) interfaceBlockType = RCPrefs.getInterfaceBlockType();
        
        Map<Type, MaterialData> iom = new EnumMap<>(IOBlock.Type.class);
        iom.put(IOBlock.Type.INPUT, inputBlockType);
        iom.put(IOBlock.Type.OUTPUT, outputBlockType);
        iom.put(IOBlock.Type.INTERFACE, interfaceBlockType);
        
        MaybeChip mChip = rc.chipManager().maybeCreateAndActivateChip(target, sender, iom, debugLevel);        
        return mChip;
    }

}
