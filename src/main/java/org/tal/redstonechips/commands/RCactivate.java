
package org.tal.redstonechips.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.PrefsManager;

/**
 *
 * @author Tal Eisenberg
 */
public class RCactivate extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;

        Block target = CommandUtils.targetBlock(player);
        if (target.getType()==Material.WALL_SIGN) {
            MaterialData inputBlockType, outputBlockType, interfaceBlockType;

            if (args.length==0) {
                inputBlockType = rc.getPrefs().getInputBlockType();
                outputBlockType = rc.getPrefs().getOutputBlockType();
                interfaceBlockType = rc.getPrefs().getInterfaceBlockType();
            } else {
                if (args.length!=3) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad syntax. Expecting /rccuboid activate [inputBlockType] [outputBlockType] [interfaceBlockType]");
                    return true;
                }
                try {
                    inputBlockType = PrefsManager.findMaterial(args[0]);
                    outputBlockType = PrefsManager.findMaterial(args[1]);
                    interfaceBlockType = PrefsManager.findMaterial(args[2]);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(ie.getMessage());
                    return true;
                }
            }

            if (rc.getCircuitManager().checkForCircuit(target, sender, inputBlockType, outputBlockType, interfaceBlockType)==-2) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Could not activate integrated circuit.");
            }
        } else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at a wall sign.");
        }

        return true;
    }

}
