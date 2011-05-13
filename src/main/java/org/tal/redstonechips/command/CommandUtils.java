
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
    /**
     * Maximum number of lines that can fit on the minecraft screen.
     */
    public final static int MaxLines = 20;

    /**
     * Regards air and water as transparent materials when trying to find the block a player is looking at.
     */
    static final HashSet<Byte> transparentMaterials = new HashSet<Byte>();
    static {
        transparentMaterials.add((byte)Material.AIR.getId());
        transparentMaterials.add((byte)Material.WATER.getId());
        transparentMaterials.add((byte)Material.STATIONARY_WATER.getId());
    }


    public static Player checkIsPlayer(RedstoneChips rc, CommandSender sender) {
        if (sender instanceof Player) return (Player)sender;
        else {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Only players are allowed to use this command.");
            return null;
        }
    }

    public static Circuit findTargetCircuit(RedstoneChips rc, CommandSender sender) {
        Player player = checkIsPlayer(rc, sender);
        if (player==null) return null;

        Block target = targetBlock(player);
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);
        if (c==null) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You need to point at a block of a redstone chip.");
        }

        return c;
    }

    public static Block targetBlock(Player player) {
        return player.getTargetBlock(transparentMaterials, 100);
    }


    public static Map<CommandSender, PageInfo> playerPages = new HashMap<CommandSender, PageInfo>();

    public static void pageMaker(CommandSender s, String title, String commandName, String[] lines, ChatColor infoColor, ChatColor errorColor) {
        CommandUtils.pageMaker(s, title, commandName, lines, infoColor, errorColor, MaxLines);
    }

    public static void pageMaker(CommandSender s, String title, String commandName, String[] lines, ChatColor infoColor, ChatColor errorColor, int maxLines) {
        maxLines = maxLines - 4;
        int page;

        int pageCount = (int)(Math.ceil(lines.length/(float)maxLines));
        if (commandName!=null || !playerPages.containsKey(s)) {
            page = 1;
            playerPages.put(s, new PageInfo(title, pageCount, lines, maxLines+4, infoColor, errorColor));
        } else {
            PageInfo pageInfo = playerPages.get(s);
            page = pageInfo.page;
        } 


        if (page<1 || page>pageCount) s.sendMessage(errorColor + "Invalid page number: " + page + ". Expecting 1-" + pageCount);
        else {
            s.sendMessage(infoColor + title + ": " + (pageCount>1?"( page " + page + " / " + pageCount  + " )":""));
            s.sendMessage(infoColor + "----------------------");
            for (int i=(page-1)*maxLines; i<Math.min(lines.length, page*maxLines); i++) {
                s.sendMessage(lines[i]);
            }
            s.sendMessage(infoColor + "----------------------");
            if (pageCount>1) s.sendMessage("Use " + ChatColor.YELLOW + (s instanceof Player?"/":"") + "rcp [page#|next|prev|last]" + ChatColor.WHITE + " to see other pages.");
        }
    }

}
