/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import org.tal.redstonechips.circuit.Circuit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.circuit.ReceivingCircuit;
import org.tal.redstonechips.circuit.TransmittingCircuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.util.BitSetUtils;
import org.tal.redstonechips.util.ChatFixUtil;
import org.tal.redstonechips.util.TargetBlock;

/**
 *
 * @author Tal Eisenberg
 */
public class CommandHandler {
    RedstoneChips rc;
    private final static int MaxLines = 15;

    public CommandHandler(RedstoneChips plugin) {
        rc = plugin; 
    }


    public void listActiveCircuits(CommandSender p, String[] args) {
        List<Circuit> circuits = rc.getCircuitManager().getCircuits();
        if (circuits.isEmpty()) ChatFixUtil.sendCSMessage(p, rc.getPrefsManager().getInfoColor() + "There are no active circuits.");
        else {
            String title = "Active redstone circuits";
            String commandName = "rc-list";
            String[] lines = new String[circuits.size()];

            for (int i=0; i<lines.length; i++) {
                Circuit c = circuits.get(i);
                lines[i] = i + ": " + ChatColor.YELLOW + c.getClass().getSimpleName() + ChatColor.WHITE + " @ " + 
                        c.activationBlock.getX() + ", " + c.activationBlock.getY() + ", " + c.activationBlock.getZ();
            }
            pageMaker(p, args, title, commandName, lines, rc.getPrefsManager().getInfoColor(), rc.getPrefsManager().getErrorColor());
        }

    }

    public void listCircuitClasses(CommandSender p) {
        Map<String,Class> circuitClasses = rc.getCircuitLoader().getCircuitClasses();
        if (circuitClasses.isEmpty()) ChatFixUtil.sendCSMessage(p, rc.getPrefsManager().getInfoColor() + "There are no circuit classes installed.");
        else {
            List<String> names = Arrays.asList(circuitClasses.keySet().toArray(new String[circuitClasses.size()]));
            Collections.sort(names);
            p.sendMessage("");
            p.sendMessage(rc.getPrefsManager().getInfoColor() + "Installed circuit classes:");
            p.sendMessage(rc.getPrefsManager().getInfoColor() + "-----------------------");
            String list = "";
            ChatColor color = ChatColor.WHITE;
            for (String name : names) {
                list += color + name + ", ";
                if (list.length()>50) {
                    p.sendMessage(list.substring(0, list.length()-2));
                    list = "";
                }
                if (color==ChatColor.WHITE)
                    color = ChatColor.YELLOW;
                else color = ChatColor.WHITE;
            }
            if (!list.isEmpty()) ChatFixUtil.sendCSMessage(p, list.substring(0, list.length()-2));
            p.sendMessage(rc.getPrefsManager().getInfoColor() + "----------------------");
            p.sendMessage("");
        }
    }

    public void prefsCommand(String[] args, CommandSender sender) {
            if (args.length==0) { // list preferences
                rc.getPrefsManager().printYaml(sender, rc.getPrefsManager().getPrefs());
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Type /redchips-prefs <name> <value> to make changes.");
            } else if (args.length==1) { // show one key value pair
                Object o = rc.getPrefsManager().getPrefs().get(args[0]);
                if (o==null) sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Unknown preferences key: " + args[0]);
                else {
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(args[0], o);

                    rc.getPrefsManager().printYaml(sender, map);
                }
            } else if (args.length>=2) { // set value
                if (!sender.isOp()) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Only admins are authorized to change preferences values.");
                    return;
                }

                String val = "";
                for (int i=1; i<args.length; i++)
                    val += args[i] + " ";

                try {
                    Map<String, Object> map = rc.getPrefsManager().setYaml(args[0] + ": " + val);
                    rc.getPrefsManager().printYaml(sender, map);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + ie.getMessage());
                }
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Saving changes...");
                rc.getPrefsManager().savePrefs();
            } else {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad redchips-prefs syntax.");
            }

    }

    public void debugCommand(CommandSender sender, String[] args) {
        Player player = checkIsPlayer(sender);
        if (player==null) return;

        boolean add = true;
        if (args.length>0) {
            if (args[0].equalsIgnoreCase("off"))
                add = false;
            else if (args[0].equalsIgnoreCase("on"))
                add = true;
            else {
                sender.sendMessage("Bad argument " + args[0] + ". Syntax should be: /redchips-debug on | off");
            }
        }

        Block target = new TargetBlock(player).getTargetBlock();
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);

        if (c==null) {
            sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to debug.");
        } else {
            if (add) {
                try {
                    c.addDebugger(player);
                } catch (IllegalArgumentException ie) {
                    ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getInfoColor() + ie.getMessage());
                    return;
                }
                sender.sendMessage(rc.getPrefsManager().getDebugColor() + "You are now a debugger of the " + c.getClass().getSimpleName() + " circuit.");
            } else {
                try {
                    c.removeDebugger(player);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefsManager().getInfoColor() + ie.getMessage());
                    return;
                }
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "You will not receive any more debug messages ");
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "from the " + c.getClass().getSimpleName() + " circuit.");
            }
        }
    }

    public void destroyCommand(CommandSender sender) {
        Player player = checkIsPlayer(sender);
        if (player==null) return;

        Block target = new TargetBlock(player).getTargetBlock();
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);
        if (c==null) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to destroy.");
        } else {
            for (BlockVector v : c.structure)
                c.world.getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).setType(Material.AIR);

            rc.getCircuitManager().destroyCircuit(c, sender);
            player.sendMessage(rc.getPrefsManager().getInfoColor() + "The " + c.getCircuitClass() + " chip is destroyed.");
        }
    }

    public void deactivateCommand(CommandSender sender, String[] args) {
        int idx = -1;
        if (args.length>0) {
            try {
                idx = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                sender.sendMessage("Bad circuit id number: " + args[0]);
            }
        }
        

        Circuit c = null;
        if (idx==-1) {
            Player player = checkIsPlayer(sender);
            if (player==null) return;

            if (!sender.isOp()) {
                ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getErrorColor() + "You must be an admin to remotely deactivate a circuit.");
            }
            Block target = new TargetBlock(player).getTargetBlock();
            c = rc.getCircuitManager().getCircuitByStructureBlock(target);
            if (c==null) {
                ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to deactivate.");
                return;
            }
        } else {
            if (idx<rc.getCircuitManager().getCircuits().size()) {
                c = rc.getCircuitManager().getCircuits().get(idx);
            } else sender.sendMessage(rc.getPrefsManager().getErrorColor() + "There's no activated circuit with id " + idx);
        }

        rc.getCircuitManager().destroyCircuit(c, sender);
        sender.sendMessage(rc.getPrefsManager().getInfoColor() + "The " + c.getCircuitClass() + " circuit is now deactivated.");
    }

    public void handleRcType(CommandSender sender, String[] args) {
        Player player = checkIsPlayer(sender);

        TargetBlock b = new TargetBlock(player);
        Block block = b.getTargetBlock();
        BlockVector v = new BlockVector(block.getX(), block.getY(), block.getZ());
        rcTypeReceiver t = rc.rcTypeReceivers.get(v);
        if (t==null) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "You must point towards a typing block (a terminal circuit's interface block for example) to type.");
        } else {
            player.sendMessage(rc.getPrefsManager().getInfoColor() + "Input sent.");
            t.type(args, player);
        }
    }

    public void pinCommand(CommandSender sender) {
        Player player = checkIsPlayer(sender);

        Block target = new TargetBlock(player).getTargetBlock();
        sendPinInfo(target, player);

    }

    private void sendPinInfo(Block target, CommandSender sender) {

        List<InputPin> inputList = rc.getCircuitManager().lookupInputBlock(target);
        if (inputList==null) {
            Object[] oo = rc.getCircuitManager().lookupOutputBlock(target);
            if (oo==null) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at an output lever or input block.");
            } else { // output pin
                Circuit c = (Circuit)oo[0];
                int i = (Integer)oo[1];
                ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getInfoColor() + c.getClass().getSimpleName() + ": output pin " + ChatColor.YELLOW + i + rc.getPrefsManager().getInfoColor() + " (" + (c.getOutputBits().get(i)?"on":"off") + ")");
            }
        } else { // input pin
            for (InputPin io : inputList) {
                Circuit c = io.getCircuit();
                int i = io.getIndex();
                ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getInfoColor() + c.getClass().getSimpleName() + ": input pin " + ChatColor.YELLOW + i + " (" + (c.getInputBits().get(i)?"on":"off") + ")");
            }
        }
    }


    public void printCircuitInfo(CommandSender sender, String[] args) {
        List<Circuit> circuits = rc.getCircuitManager().getCircuits();

        Circuit c;
        if (args.length==0) {
            Player p = checkIsPlayer(sender);
            if (p==null) return;
            Block target = new TargetBlock(p).getTargetBlock();
            c = rc.getCircuitManager().getCircuitByStructureBlock(target);
            if (c==null) {
                ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to get info about.");
                return;
            }
        } else {
            try {
                int i = Integer.decode(args[0]);
                if (i>=circuits.size()) {
                    ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getErrorColor() + "Bad id. There are only " + circuits.size() + " active circuits.");
                    return;
                }

                c = rc.getCircuitManager().getCircuits().get(i);
            } catch (NumberFormatException ie) {
                ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getErrorColor() + "Bad circuit id argument: " + args[0]);
                return;
            }
        }

        /*
         * print circuit (inputs disabled)
         * --------------
         * 4 input pins, 2 output pins and 6 interface blocks.
         * id: 50, location: x,y,z, world: world
         * input states: 010010
         * output states: 11011
         * sign args: binary scroll
         * internal state:
         *    text: 'some text blah blah'
         */

        ChatColor infoColor = rc.getPrefsManager().getInfoColor();
        ChatColor errorColor = rc.getPrefsManager().getErrorColor();
        ChatColor extraColor = ChatColor.YELLOW;

        String disabled;
        if (c.isDisabled()) disabled = errorColor + " (inputs disabled)";
        else disabled = "";

        String loc = c.activationBlock.getBlockX() + ", " + c.activationBlock.getBlockY() + ", " + c.activationBlock.getBlockZ();
        ChatFixUtil.sendCSMessage(sender, "");
        ChatFixUtil.sendCSMessage(sender, infoColor + c.getCircuitClass() + " circuit" + disabled);
        ChatFixUtil.sendCSMessage(sender, extraColor + "----------------------");

        ChatFixUtil.sendCSMessage(sender, infoColor + "" + c.inputs.length + " input(s), " + c.outputs.length + " output(s) and " + c.interfaceBlocks.length + " interface blocks.");
        ChatFixUtil.sendCSMessage(sender, infoColor + "id: " + extraColor + circuits.indexOf(c) + infoColor +
                " location: " + extraColor + loc + infoColor + " world: " + extraColor + c.world.getName());

        ChatFixUtil.sendCSMessage(sender, infoColor + "input states: " + extraColor + BitSetUtils.bitSetToBinaryString(c.getInputBits(), 0, c.inputs.length));
        ChatFixUtil.sendCSMessage(sender, infoColor + "output states: " + extraColor + BitSetUtils.bitSetToBinaryString(c.getOutputBits(), 0, c.outputs.length));

        String signargs = "";
        for (String arg : c.args)
            signargs += arg + " ";

        ChatFixUtil.sendCSMessage(sender, infoColor + "sign args: " + extraColor + signargs);
        
        Map<String,String> internalState = c.saveState();
        if (!internalState.isEmpty()) {
            sender.sendMessage(infoColor + "internal state:");
            for (String key : internalState.keySet())
                sender.sendMessage(infoColor + "   " + key + ": " + extraColor + internalState.get(key));
        }
    }

    private Player checkIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return (Player)sender;
        else {
            sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Only players are allowed to use this command.");
            return null;
        }
    }

    public void resetCircuit(CommandSender sender) {
        Player player = checkIsPlayer(sender);
        if (player==null) return;

        Block target = new TargetBlock(player).getTargetBlock();
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);
        if (c==null) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to reset.");
        } else {
            Block activationBlock = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
            List<Player> debuggers = c.getDebuggers();
            rc.getCircuitManager().destroyCircuit(c, sender);
            Block a = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
            rc.getCircuitManager().checkForCircuit(a, player);

            Circuit newCircuit = rc.getCircuitManager().getCircuitByActivationBlock(activationBlock);
            if (newCircuit!=null) {
                for (Player d : debuggers) newCircuit.addDebugger(d);
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "The " + c.getCircuitClass() + " circuit is reactivated.");
            }


        }
    }

    public static void pageMaker(CommandSender s, String[] args, String title, String commandName, String[] lines, ChatColor infoColor, ChatColor errorColor) {
            int page = 1;
            if (args.length>0) {
                try {
                    page = Integer.decode(args[0]);
                } catch (NumberFormatException ne) {
                    ChatFixUtil.sendCSMessage(s, errorColor + "Invalid page number: " + args[0]);
                }
            }

            if (page<1) ChatFixUtil.sendCSMessage(s, errorColor + "Invalid page number: " + page);
            else if (page>(Math.ceil(lines.length/MaxLines)+1)) ChatFixUtil.sendCSMessage(s, errorColor + "Invalid page number: " + page);
            else {
                s.sendMessage("");
                s.sendMessage(infoColor + title + ": ( page " + page + " / " + (int)(Math.ceil(lines.length/MaxLines)+1)  + " )");
                s.sendMessage(infoColor + "----------------------");
                for (int i=(page-1)*MaxLines; i<Math.min(lines.length, page*MaxLines); i++) {
                    ChatFixUtil.sendCSMessage(s, lines[i]);
                }
                s.sendMessage(infoColor + "----------------------");
                s.sendMessage("Use /" + commandName + " <page number> to see other pages.");
                s.sendMessage("");
            }

    }

    public void listBroadcastChannels(CommandSender sender) {
        SortedSet<String> channels = new TreeSet<String>();
        for (TransmittingCircuit t : rc.transmitters) channels.add(t.getChannel());
        for (ReceivingCircuit r : rc.receivers) channels.add(r.getChannel());
        if (channels.isEmpty()) {
            ChatFixUtil.sendCSMessage(sender, rc.getPrefsManager().getInfoColor() + "There are no registered channels.");
        } else {
            sender.sendMessage("");
            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Currently used broadcast channels:");
            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "------------------------------");
            String list = "";
            ChatColor color = ChatColor.WHITE;
            for (String channel : channels) {
                list += color + channel + ", ";
                if (list.length()>50) {
                    ChatFixUtil.sendCSMessage(sender, list.substring(0, list.length()-2));
                    list = "";
                }
                if (color==ChatColor.WHITE)
                    color = ChatColor.YELLOW;
                else color = ChatColor.WHITE;
            }
            if (!list.isEmpty()) sender.sendMessage(list.substring(0, list.length()-2));
            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "------------------------------");
            sender.sendMessage("");
        }
    }
}
