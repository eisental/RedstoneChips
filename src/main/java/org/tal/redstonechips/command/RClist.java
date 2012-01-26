
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import net.eisental.common.page.Pager;
import net.eisental.common.parsing.Tokenizer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.filter.*;

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;

        List<CircuitFilter> filters = new ArrayList<CircuitFilter>();

        boolean bthis = false;

        if (args.length>0) {
            if (args[0].equalsIgnoreCase("all"));
            else if (args[0].equalsIgnoreCase("this")) {
                if (sender instanceof Player) {
                    bthis = true;
                } else {
                    sender.sendMessage("You have to be a player to use the 'this' keyword.");
                    return true;
                } 
            } else {
                CircuitFilter f = new WorldFilter().setPlugin(rc);
                try {
                    f.parse(sender, tokenizeFilter(args[0]));
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
                    return true;
                }
                filters.add(f);
            }
        }

        if (bthis || (filters.isEmpty() && (sender instanceof Player))) {
            CircuitFilter f = new WorldFilter().setPlugin(rc);
            ((WorldFilter)f).world = ((Player)sender).getWorld();
            filters.add(f);
        }

        if (args.length>1) {
            StringBuilder concat = new StringBuilder();
            for (int i=1; i<args.length; i++) {
                concat.append(args[i]);
                concat.append(" ");
            }

            String sfilters = concat.toString().trim();
            String[] afilters = new Tokenizer(sfilters, ';').getTokens();

            try {
                for (String sf : afilters) {
                    CircuitFilter f = parseFilter(sender, sf);
                    filters.add(f);
                }
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
                return true;
            }
        }

        Collection<Circuit> circuits = filterCircuits(rc.getCircuitManager().getCircuits().values(), 
                filters.toArray(new CircuitFilter[filters.size()]));
        TreeMap<Integer, Circuit> sorted = new TreeMap<Integer,Circuit>();
        for (Circuit c : circuits) sorted.put(c.id, c);

        if (circuits.isEmpty()) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no active chips that match the criteria.");
        else {
            printCircuitList(sender, sorted.values(), null, rc);
        }

        return true;
    }

    public static void printCircuitList(CommandSender sender, Iterable<Circuit> circuits, String title, org.tal.redstonechips.RedstoneChips rc) {
        String lines = "";        
        int chipCount = 0;
        
        for (Circuit c : circuits) {
            lines += (makeCircuitDescriptionLine(c, rc.getPrefs().getInfoColor())) + "\n";
            chipCount++;
        }

        if (title==null)
            title = chipCount + " active chip(s)";

        sender.sendMessage("");

        Pager.beginPaging(sender, title, lines,
                rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
    }

    public static String makeCircuitDescriptionLine(Circuit c, ChatColor argsColor) {
        StringBuilder builder = new StringBuilder();
        for (String arg : c.args) {
            builder.append(arg);
            builder.append(" ");
        }

        String cargs = "";
        if (builder.length()>0) cargs = builder.toString().substring(0, builder.length()-1);

        if(cargs.length() > 20) cargs = cargs.substring(0, 17) + "...";
        cargs = "[ " + cargs + " ]";

        String sworld = c.world.getName() + " ";
        ChatColor nameColor = (c.isDisabled()?ChatColor.GRAY:ChatColor.YELLOW);
        
        return c.id + ": " + nameColor + c.getClass().getSimpleName() + (c.name!=null?ChatColor.AQUA + " (" + c.name + ")":"") + ChatColor.WHITE + " @ "
                + c.activationBlock.getX() + "," + c.activationBlock.getY() + "," + c.activationBlock.getZ()
                + " " + sworld + argsColor + cargs;
    }

    private Collection<Circuit> filterCircuits(Collection<Circuit> values, CircuitFilter[] filters) {
        Collection<Circuit> circuits = new ArrayList<Circuit>();
        circuits.addAll(values);

        for (CircuitFilter filter : filters) {
            circuits = filter.filter(circuits);
        }

        return circuits;
    }

    public static String[] tokenizeFilter(String string) {
        return new Tokenizer(string, ',').getTokens();
    }

    private CircuitFilter parseFilter(CommandSender sender, String sf) throws IllegalArgumentException {
        int colonIdx = sf.indexOf(":");
        if (colonIdx==-1)
            throw new IllegalArgumentException("Bad filter syntax: " + sf);
        
        String type = sf.substring(0, colonIdx).trim().toLowerCase();

        if (type.length()<=1)
            throw new IllegalArgumentException("Bad filter syntax: " + sf);

        CircuitFilter f;

        if ("location".startsWith(type)) {
            f = new LocationFilter().setPlugin(rc);
        } else if ("chunk".startsWith(type)) {
            f = new ChunkFilter().setPlugin(rc);
        } else if ("class".startsWith(type)) {
            f = new ClassFilter().setPlugin(rc);
        } else throw new IllegalArgumentException("Unknown filter type: " + type);

        f.parse(sender, tokenizeFilter(sf.substring(colonIdx+1)));

        return f;
    }
}
