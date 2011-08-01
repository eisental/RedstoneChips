
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.ChunkLocation;
import org.tal.redstonechips.util.Locations;
import org.tal.redstonechips.util.Tokenizer;

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
        if (sender instanceof Player) {
			if (!CommandUtils.checkPermission(rc, (Player)sender, command.getName())) return true;
		}

        List<Filter> filters = new ArrayList<Filter>();

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
                Filter f = new WorldFilter();
                try {
                    f.parseFilter(sender, tokenizeFilter(args[0]));
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
                    return true;
                }
                filters.add(f);
            }
        }

        if (bthis || (filters.isEmpty() && (sender instanceof Player))) {
            Filter f = new WorldFilter();
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
                    Filter f = parseFilter(sender, sf);
                    filters.add(f);
                }
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
                return true;
            }
        }

        Collection<Circuit> circuits = filterCircuits(rc.getCircuitManager().getCircuits().values(), filters.toArray(new Filter[filters.size()]));
        TreeMap<Integer, Circuit> sorted = new TreeMap<Integer,Circuit>();
        for (Circuit c : circuits) sorted.put(c.id, c);

        if (circuits.isEmpty()) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no active circuits that match the criteria.");
        else {
            printCircuitList(sender, sorted.values(), null);
        }

        return true;
    }

    public void printCircuitList(CommandSender sender, Iterable<Circuit> circuits, String title) {
        List<String> lines = new ArrayList<String>();
        for (Circuit c : circuits) {
            lines.add(makeCircuitDescriptionLine(c, rc.getPrefs().getInfoColor()));
        }

        if (title==null)
            title = lines.size() + " active IC(s)";

        sender.sendMessage("");

        CommandUtils.pageMaker(sender, title, "rclist", lines.toArray(new String[lines.size()]),
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

        return c.id + ": " + ChatColor.YELLOW + c.getClass().getSimpleName() + ChatColor.WHITE + " @ "
                + c.activationBlock.getX() + "," + c.activationBlock.getY() + "," + c.activationBlock.getZ()
                + " " + sworld + argsColor + cargs;
    }

    private Collection<Circuit> filterCircuits(Collection<Circuit> values, Filter[] filters) {
        Collection<Circuit> circuits = new ArrayList<Circuit>();
        circuits.addAll(rc.getCircuitManager().getCircuits().values());

        for (Filter filter : filters) {
            circuits = filter.filter(circuits);
        }

        return circuits;
    }

    public static String[] tokenizeFilter(String string) {
        return new Tokenizer(string, ',').getTokens();
    }

    private Filter parseFilter(CommandSender sender, String sf) throws IllegalArgumentException {
        int colonIdx = sf.indexOf(":");
        if (colonIdx==-1)
            throw new IllegalArgumentException("Bad filter syntax: " + sf);
        
        String type = sf.substring(0, colonIdx).trim().toLowerCase();

        if (type.length()<=1)
            throw new IllegalArgumentException("Bad filter syntax: " + sf);

        Filter f;

        if ("location".startsWith(type)) {
            f = new LocationFilter();
        } else if ("chunk".startsWith(type)) {
            f = new ChunkFilter();
        } else if ("class".startsWith(type)) {
            f = new ClassFilter();
        } else throw new IllegalArgumentException("Unknown filter type: " + type);

        f.parseFilter(sender, tokenizeFilter(sf.substring(colonIdx+1)));

        return f;
    }

    interface Filter {
        public void parseFilter(CommandSender s, String[] string) throws IllegalArgumentException;
        public Collection<Circuit> filter(Collection<Circuit> circuits);
    }

    class LocationFilter implements Filter {
        Location location;
        int radius = 0;

        @Override
        public Collection<Circuit> filter(Collection<Circuit> circuits) {
            List<Circuit> filtered = new ArrayList<Circuit>();

            for (Circuit circuit : circuits) {
                if (Locations.isInRadius(location, circuit.activationBlock, radius))
                    filtered.add(circuit);
            }

            return filtered;
        }

        @Override
        public void parseFilter(CommandSender sender, String[] string) throws IllegalArgumentException {
            try {
                if ((string.length==1 || string.length==2) && string[0].equalsIgnoreCase("this")) {
                    if ((sender instanceof Player)) {
                        location = ((Player)sender).getLocation();
                    }

                    if (string.length==2) {
                        radius = Integer.decode(string[1]);
                    }

                    return;
                }

                if (string.length!=3 && string.length!=4) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : string) sb.append(s);

                    throw new IllegalArgumentException("Bad location filter syntax: " + sb.toString() + ". Expecting 'location: x,y,z,[radius]'");
                }

                int x = Integer.decode(string[0]);
                int y = Integer.decode(string[1]);
                int z = Integer.decode(string[2]);

                if (string.length==4) radius = Integer.decode(string[3]);
                location = new Location(null, x, y, z);
            } catch (NumberFormatException ne) {
                StringBuilder sb = new StringBuilder();
                for (String s : string) sb.append(s);

                throw new IllegalArgumentException("Bad location filter syntax: " + sb.toString() + ". Expecting 'location: x,y,z,[radius]'");
            }
        }
    }

    class ChunkFilter implements Filter {
        ChunkLocation chunk;

        @Override
        public Collection<Circuit> filter(Collection<Circuit> circuits) {
            List<Circuit> filtered = new ArrayList<Circuit>();

            for (Circuit circuit : circuits) {
                for (ChunkLocation c : circuit.circuitChunks)
                    if (c.getX()==chunk.getX() && c.getZ()==chunk.getZ()) filtered.add(circuit);
            }

            return filtered;
        }

        @Override
        public void parseFilter(CommandSender sender, String[] string) throws IllegalArgumentException {
            if (string.length==1 && string[0].equalsIgnoreCase("this") && (sender instanceof Player)) {
                chunk = ChunkLocation.fromLocation(((Player)sender).getLocation());
            } else if (string.length==2) {
                try {
                    int x = Integer.decode(string[0]);
                    int z = Integer.decode(string[1]);
                    chunk = new ChunkLocation(x,z,null);
                } catch (NumberFormatException ne) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : string) sb.append(s);

                    throw new IllegalArgumentException("Bad chunk filter: " + sb.toString() + ". Expecting 'chunk: <x>,<z>'.");
                }
            } else  {
                StringBuilder sb = new StringBuilder();
                for (String s : string) sb.append(s);

                throw new IllegalArgumentException("Bad chunk filter: " + sb.toString() + ". Expecting 'chunk: <x>,<z>'.");
            }

        }
    }

    class WorldFilter implements Filter {
        World world;

        @Override
        public void parseFilter(CommandSender sender, String[] string) throws IllegalArgumentException {
            if (string.length!=1) {
                StringBuilder sb = new StringBuilder();
                for (String s : string) sb.append(s);

                throw new IllegalArgumentException("Bad world filter: " + sb.toString() + ". Expecting 'world: <world-name>'.");
            } else {
                world = rc.getServer().getWorld(string[0]);
                if (world==null)
                    throw new IllegalArgumentException("Unknown world: " + string[0]);
            }
        }

        @Override
        public Collection<Circuit> filter(Collection<Circuit> circuits) {
            List<Circuit> filtered = new ArrayList<Circuit>();

            for (Circuit circuit : circuits) {
                if (circuit.world.getName().equals(world.getName())) filtered.add(circuit);
            }

            return filtered;
        }

    }

    class ClassFilter implements Filter {
        String cclass;

        @Override
        public void parseFilter(CommandSender sender, String[] string) throws IllegalArgumentException {
            if (string.length!=1) {
                StringBuilder sb = new StringBuilder();
                for (String s : string) sb.append(s);

                throw new IllegalArgumentException("Bad class filter: " + sb.toString() + ". Expecting 'class: <circuit class>'.");
            } else {
                for (String sclass : rc.getCircuitLoader().getCircuitClasses().keySet()) {
                    if (sclass.startsWith(string[0])) {
                        cclass = sclass;
                        break;
                    }
                }

                if (cclass==null)
                    throw new IllegalArgumentException("Unknown circuit class: " + string[0]);
            }
        }

        @Override
        public Collection<Circuit> filter(Collection<Circuit> circuits) {
            List<Circuit> filtered = new ArrayList<Circuit>();

            for (Circuit circuit : circuits) {
                if (circuit.getCircuitClass().equalsIgnoreCase(cclass)) filtered.add(circuit);
            }

            return filtered;
        }

    }
}
