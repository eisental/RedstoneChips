
package org.redstonechips.command;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
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
    
    /**
     * The default target distance. Any blocks further away are ignored.
     */
    private final static int DefaultBlockTargetDistance = 50;
    
    /**
     * Regards air and water as transparent materials when trying to find the block a player is looking at.
     */
    static final HashSet<Material> transparentMaterials = new HashSet<>();
    static {
    	transparentMaterials.add(Material.WATER);
    	transparentMaterials.add(Material.AIR);
    }

    /**
     * {@link #findTargetChip(org.bukkit.command.CommandSender, int, boolean) findTargetChip} with default target distance.
     */
    public static Chip findTargetChip(CommandSender sender, boolean report) {
        return findTargetChip(sender, DefaultBlockTargetDistance, report);
    }
    
    /**
     * {@link #findTargetChip(org.bukkit.command.CommandSender, int, boolean) findTargetChip} with default target distance and report set to true.
     */
    public static Chip findTargetChip(CommandSender sender) {
        return findTargetChip(sender, true);
    }
    
    /**
     * Checks whether the player is pointing towards a chip block.
     * 
     * @param sender The player.
     * @param distance The maximum distance a target block can be away from the player to be identified.
     * @param report When true, an error message is sent to sender if sender is not a player or a target chip was not foudn.
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
     * {@link #enforceIsPlayer(org.bukkit.command.CommandSender, boolean) enforceIsPlayer} with report set to true.
     *
     * @param sender
     * @return
     */
    public static Player enforceIsPlayer(CommandSender sender) {
        return enforceIsPlayer(sender, true);
    }

    /**
     * Checks whether sender is a Player.
     *
     * @param sender The command sender to check.
     * @param report When true, an error message is sent to sender when he is not a Player.
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
    
    /**
     * Add ChatColor to text according to some rules.
     * Currently it only colors ` tags (as in `sometext`).
     * @param str String to colorize.
     * @param baseColor Color of untagged text.
     * @return Colorized String.
     */
    public static String colorize(String str, ChatColor baseColor) {
        Pattern p = Pattern.compile("(`)([^`]*)(`)");
        Matcher m = p.matcher(str);
        return baseColor + m.replaceAll(ChatColor.GRAY + "$2" + baseColor);
    }
}
