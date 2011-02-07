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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.InputPin;
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
        int page = 1;
        if (args.length>0) {
            try {
                page = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                p.sendMessage(rc.getPrefsManager().getErrorColor() + "Invalid page number: " + args[0]);
            }
        }

        List<Circuit> circuits = rc.getCircuitManager().getCircuits();

        if (page<1) p.sendMessage(rc.getPrefsManager().getErrorColor() + "Invalid page number: " + page);
        else if (page>(Math.ceil(circuits.size()/MaxLines)+1)) p.sendMessage(rc.getPrefsManager().getErrorColor() + "Invalid page number: " + page);
        if (circuits.isEmpty()) p.sendMessage(rc.getPrefsManager().getInfoColor() + "There are no active circuits.");
        else {
            p.sendMessage("");
            p.sendMessage(rc.getPrefsManager().getInfoColor() + "Active redstone circuits: ( page " + page + " / " + (int)(Math.ceil(circuits.size()/MaxLines)+1)  + " )");
            p.sendMessage(rc.getPrefsManager().getInfoColor() + "----------------------");
            for (int i=(page-1)*MaxLines; i<Math.min(circuits.size(), page*MaxLines); i++) {
                Circuit c = circuits.get(i);
                p.sendMessage(circuits.indexOf(c) + ": " + ChatColor.YELLOW + c.getClass().getSimpleName() + ChatColor.WHITE + " @ " + c.activationBlock.getX() + ", " + c.activationBlock.getY() + ", " + c.activationBlock.getZ());
            }
            p.sendMessage(rc.getPrefsManager().getInfoColor() + "----------------------");
            p.sendMessage("Use /redchips-active <page number> to see other pages.");
            p.sendMessage("");
        }

    }

    public void listCircuitClasses(CommandSender p) {
        Map<String,Class> circuitClasses = rc.getCircuitLoader().getCircuitClasses();
        if (circuitClasses.isEmpty()) p.sendMessage(rc.getPrefsManager().getInfoColor() + "There are no circuit classes installed.");
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
            if (!list.isEmpty()) p.sendMessage(list.substring(0, list.length()-2));
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
                player.sendMessage("Bad argument " + args[0] + ". Syntax should be: /redchips-debug on | off");
            }
        }

        Block target = new TargetBlock(player).getTargetBlock();
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);

        if (c==null) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to debug.");
        } else {
            if (add) {
                try {
                    c.addDebugger(player);
                } catch (IllegalArgumentException ie) {
                    player.sendMessage(rc.getPrefsManager().getInfoColor() + ie.getMessage());
                    return;
                }
                player.sendMessage(rc.getPrefsManager().getDebugColor() + "You are now a debugger of the " + c.getClass().getSimpleName() + " circuit.");
            } else {
                try {
                    c.removeDebugger(player);
                } catch (IllegalArgumentException ie) {
                    player.sendMessage(rc.getPrefsManager().getInfoColor() + ie.getMessage());
                    return;
                }
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "You will not receive any more debug messages ");
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "from the " + c.getClass().getSimpleName() + " circuit.");
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
            for (Location l : c.structure)
                c.world.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()).setType(Material.AIR);

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
        
        Player player = checkIsPlayer(sender);
        if (player==null) return;

        Circuit c = null;
        if (idx==-1) {
            if (!sender.isOp()) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You must be an admin to remotely deactivate a circuit.");
            }
            Block target = new TargetBlock(player).getTargetBlock();
            c = rc.getCircuitManager().getCircuitByStructureBlock(target);
            if (c==null) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to deactivate.");
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
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + c.getClass().getSimpleName() + ": output pin " + ChatColor.YELLOW + i + rc.getPrefsManager().getInfoColor() + " (" + (c.getOutputBits().get(i)?"on":"off") + ")");
            }
        } else { // input pin
            for (InputPin io : inputList) {
                Circuit c = io.getCircuit();
                int i = io.getIndex();
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + c.getClass().getSimpleName() + ": input pin " + ChatColor.YELLOW + i + " (" + (c.getInputBits().get(i)?"on":"off") + ")");
            }
        }
    }

    private Player checkIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return (Player)sender;
        else {
            sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Only players are allowed to use this command.");
            return null;
        }
    }

}
