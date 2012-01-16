
package org.tal.redstonechips.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class CommandUtils {
    private final static int DefaultBlockTargetDistance = 50;
    
    /**
     * Regards air and water as transparent materials when trying to find the block a player is looking at.
     */
    static final HashSet<Byte> transparentMaterials = new HashSet<Byte>();
    static {
        transparentMaterials.add((byte)Material.AIR.getId());
        transparentMaterials.add((byte)Material.WATER.getId());
        transparentMaterials.add((byte)Material.STATIONARY_WATER.getId());
    }


    /**
     * Checks whether this command sender is a player and sends the command sender an error otherwise.
     * 
     * @param rc
     * @param sender The command sender to check.
     * @return the sender object cast to the Player class.
     */
    public static Player checkIsPlayer(RedstoneChips rc, CommandSender sender, boolean report) {
        if (sender instanceof Player) return (Player)sender;
        else {
            if (report) sender.sendMessage(rc.getPrefs().getErrorColor() + "Only players are allowed to use this command.");
            return null;
        }
    }
    
    public static Player checkIsPlayer(RedstoneChips rc, CommandSender sender) {
        return checkIsPlayer(rc, sender, true);
    }

    /**
     * Checks whether the player is pointing towards a chip block.
     * Sends an error message to the player in case no chip was found.
     * 
     * @param rc
     * @param sender 
     * @return The chip pointed by the player or null if one was not found.
     */
    public static Circuit findTargetCircuit(RedstoneChips rc, CommandSender sender, int distance, boolean report) {
        Player player = checkIsPlayer(rc, sender, report);
        if (player==null) return null;

        Block target = targetBlock(player, distance);
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target.getLocation());
        if (c==null && report) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at a block of a redstone chip.");
        }

        return c;        
    }
    
    public static Circuit findTargetCircuit(RedstoneChips rc, CommandSender sender, boolean report) {
        return findTargetCircuit(rc, sender, DefaultBlockTargetDistance, report);
    }
    
    public static Circuit findTargetCircuit(RedstoneChips rc, CommandSender sender) {
        return findTargetCircuit(rc, sender, true);
    }

    /**
     * See transparentMaterials for a list of materials that are considered transparent.
     * 
     * @param player
     * @param distance The max distance between block and player.
     * @return The block pointed by the player.
     */
    public static Block targetBlock(Player player, int distance) {
        return player.getTargetBlock(transparentMaterials, distance);
    }

    public static Block targetBlock(Player player) {
        return targetBlock(player, DefaultBlockTargetDistance);
    }
    
    /**
     * Checks whether the command sender has a permission to use a command.
     * 
     * @param rc
     * @param sender
     * @param commandName command name without the slash (/).
     * @param opRequired used in case a permission plugin was not found. If set to true, permission is only granted if the sender is op.
     * @param report Determines whether an error message is sent to the sender in case it doesn't have permission.
     * @return true if the sender has permission to use the command.
     */
    public static boolean checkPermission(RedstoneChips rc, CommandSender sender, String commandName, boolean opRequired, boolean report) {
        if (!rc.getPrefs().getUsePermissions()) return (opRequired?sender.isOp():true);
        if (!(sender instanceof Player)) return true;
        if(((Player)sender).hasPermission("redstonechips.command." + commandName) && !((Player)sender).hasPermission("redstonechips.command." + commandName + ".deny")) {
            return true;
        } else {
            if (report) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to use command " + commandName + ".");
            return false;
        }
    }
}
