
package org.redstonechips.command;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class CommandUtils {
    private final static int DefaultBlockTargetDistance = 50;
    
    /**
     * Regards air and water as transparent materials when trying to find the block a player is looking at.
     */
    static final HashSet<Byte> transparentMaterials = new HashSet<>();
    static {
        transparentMaterials.add((byte)Material.AIR.getId());
        transparentMaterials.add((byte)Material.WATER.getId());
        transparentMaterials.add((byte)Material.STATIONARY_WATER.getId());
    }

    public static Chip findTargetChip(CommandSender sender, boolean report) {
        return findTargetChip(sender, DefaultBlockTargetDistance, report);
    }
    
    public static Chip findTargetChip(CommandSender sender) {
        return findTargetChip(sender, true);
    }
    
    /**
     * Checks whether the player is pointing towards a chip block.
     * Sends an error message to the player in case no chip was found.
     * 
     * @param sender 
     * @param distance 
     * @param report 
     * @return The chip pointed by the player or null if one was not found.
     */
    public static Chip findTargetChip(CommandSender sender, int distance, boolean report) {
        Player player = enforceIsPlayer(sender, report);
        if (player==null) return null;

        Block target = targetBlock(player, distance);
        Chip c = org.redstonechips.RedstoneChips.inst().chipManager().getAllChips().getByStructureBlock(target.getLocation());
        if (c==null && report) {
            RCCommand.error(sender, "You need to point at a block of a redstone chip.");
        }

        return c;        
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
     * Calls enforceIsPlayer with report set to true.
     *
     * @param sender
     * @return
     */
    public static Player enforceIsPlayer(CommandSender sender) {
        return enforceIsPlayer(sender, true);
    }

    /**
     * Checks whether this command sender is a player and sends the command sender an error otherwise.
     *
     * @param sender The command sender to check.
     * @param report
     * @return the sender object cast to the Player class.
     */
    public static Player enforceIsPlayer(CommandSender sender, boolean report) {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            if (report) {
                RCCommand.error(sender, "Only players are allowed to use this command.");
            }
            return null;
        }
    }
}
