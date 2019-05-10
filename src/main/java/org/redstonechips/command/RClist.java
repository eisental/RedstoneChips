
package org.redstonechips.command;

import org.redstonechips.command.filters.ChipFilter;
import org.redstonechips.command.filters.ChunkFilter;
import org.redstonechips.command.filters.WorldFilter;
import org.redstonechips.command.filters.LocationFilter;
import org.redstonechips.command.filters.TypeFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import org.redstonechips.paging.Pager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.parsing.Tokenizer;

/**
 *
 * @author Tal Eisenberg
 */
public class RClist extends RCCommand {
    /* /rclist <world name>
     * /rclist <world name> [<filter>: <filter arg>, <filter arg>;...]
     * /rclist this chunk: -11 -7; loc
     * /rclist class:
     */

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        List<ChipFilter> filters = new ArrayList<>();
        if (args.length==0) { // when player, list all chips in world. else, list all chips on server.
            if (sender instanceof Player) {
                printWorldList(sender, "this");
            } else printCircuitList(sender, rc.chipManager().getAllChips().values(), null);
            
        } else if (args.length==1 && args[0].equalsIgnoreCase("all")) {
            printCircuitList(sender, rc.chipManager().getAllChips().values(), null);
        } else if (args.length==1 && !(args[0].contains(":") || args[0].contains(";"))) {
            ChipFilter f = new WorldFilter();
            try {
                f.parse(sender, new String[] {args[0]});
                printCircuitList(sender, f.filter(rc.chipManager().getAllChips().values()), null);
            } catch (IllegalArgumentException ie) {
                error(sender, ie.getMessage());
            }
        } else {
            StringBuilder concat = new StringBuilder();
            for (String arg : args) concat.append(arg).append(" ");

            String sfilters = concat.toString().trim();
            String[] afilters = new Tokenizer(sfilters, ';').getTokens();

            try {
                for (String sf : afilters) {
                    ChipFilter f = parseFilter(sender, sf);
                    filters.add(f);
                }

                Collection<Chip> chips = filterCircuits(rc.chipManager().getAllChips().values(), filters);                        
                TreeMap<Integer, Chip> sorted = new TreeMap<>();
                for (Chip c : chips) sorted.put(c.id, c);
                if (chips.isEmpty()) info(sender, "There are no active chips that match the criteria.");
                else {
                    printCircuitList(sender, sorted.values(), null);
                }
            } catch (IllegalArgumentException ie) {
                error(sender, ie.getMessage());
            }
        }
    }

    public static void printCircuitList(CommandSender sender, Iterable<Chip> chips, String title) {
        String lines = "";        
        int chipCount = 0;
        
        for (Chip c : chips) {
            lines += (makeCircuitDescriptionLine(c, RCPrefs.getInfoColor())) + "\n";
            chipCount++;
        }

        if (title==null)
            title = chipCount + " active chip(s)";

        sender.sendMessage("");

        Pager.beginPaging(sender, title, lines, RCPrefs.getInfoColor(), RCPrefs.getErrorColor());
    }

    public static final int lineLength = 20;
    public static String makeCircuitDescriptionLine(Chip c, ChatColor argsColor) {
        StringBuilder concat = new StringBuilder();
        for (String arg : c.args) concat.append(arg).append(" ");

        String cargs = "";
        if (concat.length() > 0) cargs = concat.toString().substring(0, concat.length()-1);

        if (cargs.length() > lineLength) cargs = cargs.substring(0, lineLength-3) + "...";
        cargs = "[ " + cargs + " ]";

        String sworld = c.world.getName() + " ";
        ChatColor nameColor = (c.isDisabled()?ChatColor.GRAY:ChatColor.YELLOW);
        
        return c.id + ": " + nameColor + c.getType() + (c.name!=null?ChatColor.AQUA + " (" + c.name + ")":"") + ChatColor.WHITE + " @ "
                + c.activationBlock.getBlockX() + "," + c.activationBlock.getBlockY() + "," + c.activationBlock.getBlockZ()
                + " " + sworld + argsColor + cargs;
    }

    private Collection<Chip> filterCircuits(Collection<Chip> values, List<ChipFilter> filters) {
        Collection<Chip> chips = new ArrayList<>();
        chips.addAll(values);

        for (ChipFilter filter : filters) {
            chips = filter.filter(chips);
        }

        return chips;
    }

    public static String[] tokenizeFilter(String string) {
        return new Tokenizer(string, ',').getTokens();
    }

    private ChipFilter parseFilter(CommandSender sender, String sf) throws IllegalArgumentException {
        int colonIdx = sf.indexOf(":");
        if (colonIdx==-1)
            throw new IllegalArgumentException("Bad filter syntax: " + sf);
        
        String type = sf.substring(0, colonIdx).trim().toLowerCase();

        if (type.length()<=1)
            throw new IllegalArgumentException("Bad filter syntax: " + sf);

        ChipFilter f;

        if ("location".startsWith(type)) {
            f = new LocationFilter();
        } else if ("chunk".startsWith(type)) {
            f = new ChunkFilter();
        } else if ("class".startsWith(type)) {
            f = new TypeFilter();
        } else throw new IllegalArgumentException("Unknown filter type: " + type);

        f.parse(sender, tokenizeFilter(sf.substring(colonIdx+1)));

        return f;
    }

    private void printWorldList(CommandSender sender, String world) {
        ChipFilter f = new WorldFilter();
        try {
            f.parse(sender, new String[] {world});
            printCircuitList(sender, f.filter(rc.chipManager().getAllChips().values()), null);
        } catch (IllegalArgumentException ie) {
            error(sender, ie.getMessage());
        }

    }
}
