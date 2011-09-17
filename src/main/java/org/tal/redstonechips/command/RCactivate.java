
package org.tal.redstonechips.command;

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
            MaterialData inputBlockType, outputBlockType, directOutputBlockType, interfaceBlockType;

            if (args.length==0) {
                inputBlockType = rc.getPrefs().getInputBlockType();
                outputBlockType = rc.getPrefs().getOutputBlockType();
                directOutputBlockType = rc.getPrefs().getDirectOutputBlockType();
                interfaceBlockType = rc.getPrefs().getInterfaceBlockType();
            } else {
                if (args.length!=4) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad syntax. Expecting /rcactivate [inputBlockType] [outputBlockType] [directOutputBlockType] [interfaceBlockType]");
                    return true;
                }
                try {
                    inputBlockType = PrefsManager.findMaterial(args[0]);
                    outputBlockType = PrefsManager.findMaterial(args[1]);
                    directOutputBlockType = PrefsManager.findMaterial(args[2]);
                    interfaceBlockType = PrefsManager.findMaterial(args[3]);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(ie.getMessage());
                    return true;
                }
            }

            if (rc.getCircuitManager().checkForCircuit(target, sender, inputBlockType, outputBlockType, directOutputBlockType, interfaceBlockType)==-2) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Could not activate integrated circuit.");
            }
        } else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at a wall sign.");
        }

        return true;
    }

}
