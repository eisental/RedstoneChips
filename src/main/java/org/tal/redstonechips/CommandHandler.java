/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import java.util.ArrayList;
import org.tal.redstonechips.circuit.Circuit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.channels.BroadcastChannel;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.channels.ReceivingCircuit;
import org.tal.redstonechips.channels.TransmittingCircuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class CommandHandler {
    RedstoneChips rc;
    private final static int MaxLines = 15;

    private List<Player> definingCuboids = new ArrayList<Player>();
    private Map<Player,List<Location>> playerCuboids = new HashMap<Player,List<Location>>();

    static final HashSet<Byte> transparentMaterials = new HashSet<Byte>();
    static {
        transparentMaterials.add((byte)Material.AIR.getId());
        transparentMaterials.add((byte)Material.WATER.getId());
        transparentMaterials.add((byte)Material.STATIONARY_WATER.getId());
    }



    public CommandHandler(RedstoneChips plugin) {
        rc = plugin; 
    }

    public void commandHelp(CommandSender sender, String[] args) {
        Map commands = (Map)rc.getDescription().getCommands();
        ChatColor infoColor = rc.getPrefsManager().getInfoColor();
        ChatColor errorColor = rc.getPrefsManager().getErrorColor();

        if (args.length==0) {
            sender.sendMessage("");
            sender.sendMessage(infoColor + "RedstoneChips commands" + ":");
            sender.sendMessage(infoColor + "----------------------");

            for (Object command : commands.keySet()) {
                sender.sendMessage(ChatColor.YELLOW + command.toString());
            }

            sender.sendMessage(infoColor + "----------------------");
            sender.sendMessage("Use /rc-help <command name> for help on a specific command (omit the / sign).");
            sender.sendMessage("");
        } else {
            Map commandMap = (Map)commands.get(args[0]);
            if (commandMap==null) {
                sender.sendMessage(errorColor + "Unknown rc command: " + args[0]);
                return;
            }

            sender.sendMessage("");
            sender.sendMessage(infoColor + "/" + args[0] + ":");
            sender.sendMessage(infoColor + "----------------------");

            sender.sendMessage(ChatColor.YELLOW+commandMap.get("description").toString());

            sender.sendMessage(infoColor + "----------------------");
            sender.sendMessage("");

        }
    }

    public void listActiveCircuits(CommandSender p, String[] args) {
        World world = null;

        TreeMap<Integer, Circuit> sorted = new TreeMap<Integer, Circuit>(rc.getCircuitManager().getCircuits());

        boolean oneWorld = true;
        if (args.length>0) {
            if (!args[0].equalsIgnoreCase("all")) {
                world = rc.getServer().getWorld(args[0]);
            } else oneWorld = false;
        }

        if (oneWorld && world==null && (p instanceof Player)) {
            // use player world as default.
            world = ((Player)p).getWorld();
        }

        if (sorted.isEmpty()) p.sendMessage(rc.getPrefsManager().getInfoColor() + "There are no active circuits.");
        else {
            String title = " active IC(s) in ";
            String commandName = "rc-list";
            List<String> lines = new ArrayList<String>();
            for (Integer id : sorted.keySet()) {
                Circuit c = sorted.get(id);
                if (world==null || c.world.getName().equals(world.getName())) {
                    StringBuilder builder = new StringBuilder();
                    for (String arg : c.args) {
                        builder.append(arg);
                        builder.append(" ");
                    }

                    String cargs = "";
                    if (builder.length()>0) cargs = builder.toString().substring(0, builder.length()-1);
                    
                    if(cargs.length() > 20) cargs = cargs.substring(0, 17) + "...";
                    cargs = "[ " + cargs + " ]";


                    String sworld = "";
                    if (world==null) sworld = c.world.getName() + " ";
                    lines.add(c.id + ": " + ChatColor.YELLOW + c.getClass().getSimpleName() + ChatColor.WHITE + " @ "
                            + c.activationBlock.getX() + "," + c.activationBlock.getY() + "," + c.activationBlock.getZ()
                            + " " + sworld + rc.getPrefsManager().getInfoColor() + cargs);
                } 
            }

            if (lines.isEmpty()) {
                p.sendMessage(rc.getPrefsManager().getInfoColor() + "There are no active circuits on world " + world.getName() + ".");
                return;
            }

            String page = null;
            if (args.length>1) page = args[args.length-1];
            else if (args.length>0) page = args[0];

            if (world==null) title += "all worlds";
            else title +="world " + world.getName();
            title = lines.size() + title;

            p.sendMessage("");
            pageMaker(p, page, title, commandName, lines.toArray(new String[lines.size()]),
                    rc.getPrefsManager().getInfoColor(), rc.getPrefsManager().getErrorColor());
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

    public void listBroadcastChannels(CommandSender sender, String[] args) {
        if (rc.broadcastChannels.isEmpty()) {
            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "There are no active broadcast channels.");
        } else {
            if (args.length>0 && rc.broadcastChannels.containsKey(args[0])) {
                printChannelInfo(sender, args[0]);
            } else {
                String[] lines = new String[rc.broadcastChannels.size()];
                int idx = 0;
                for (BroadcastChannel channel : rc.broadcastChannels.values()) {
                    lines[idx] = ChatColor.YELLOW + channel.name + ChatColor.WHITE + " - " + channel.getLength() + " bits, " + channel.getTransmitters().size() + " transmitters, " + channel.getReceivers().size() + " receivers.";
                    idx++;
                }

                sender.sendMessage("");
                pageMaker(sender, (args.length>0?args[0]:null), "Active wireless broadcast channels", "rc-channels", lines, rc.getPrefsManager().getInfoColor(), rc.getPrefsManager().getErrorColor());
                sender.sendMessage("Use /rc-channels <channel name> for more info about that channel.");
                sender.sendMessage("");
            }
        }
    }

    private void printChannelInfo(CommandSender sender, String channelName) {
        ChatColor infoColor = rc.getPrefsManager().getInfoColor();
        ChatColor errorColor = rc.getPrefsManager().getErrorColor();
        ChatColor extraColor = ChatColor.YELLOW;

        BroadcastChannel channel = rc.broadcastChannels.get(channelName);
        if (channel==null) {
            sender.sendMessage(errorColor + "Channel " + channelName + " doesn't exist.");
        } else {
            String sTransmitters = "";
            for (TransmittingCircuit t : channel.getTransmitters()) {
                String range = "[";
                if (t.getLength()>1)
                    range += "bits " + t.getStartBit() + "-" + (t.getLength()+t.getStartBit()-1) + "]";
                else range += "bit " + t.getStartBit() + "]";

                sTransmitters += t.getCircuitClass() + " (" + t.id + ") " + range + ", ";
            }

            String sReceivers = "";
            for (ReceivingCircuit r : channel.getReceivers()) {
                String range = "[";
                if (r.getLength()>1)
                    range += "bits " + r.getStartBit() + "-" + (r.getLength()+r.getStartBit()-1) + "]";
                else range += "bit " + r.getStartBit() + "]";
                sReceivers += r.getCircuitClass() + " (" + r.id + ") " + range + ", ";
            }

            sender.sendMessage("");
            sender.sendMessage(extraColor + channel.name + ":");
            sender.sendMessage(extraColor + "----------------------");

            sender.sendMessage(infoColor + "last broadcast: " + extraColor + BitSetUtils.bitSetToBinaryString(channel.bits, 0, channel.getLength()) + infoColor + " length: " + extraColor + channel.getLength());

            if (!sTransmitters.isEmpty())
                sender.sendMessage(infoColor + "transmitters: " + extraColor + sTransmitters.substring(0, sTransmitters.length()-2));
            if (!sReceivers.isEmpty())
                sender.sendMessage(infoColor + "receivers: " + extraColor + sReceivers.substring(0, sReceivers.length()-2));

        }
    }

    public void infoCommand(CommandSender sender, String[] args) {
        HashMap<Integer, Circuit> circuits = rc.getCircuitManager().getCircuits();

        Circuit c;
        if (args.length==0) { // use target circuit
            c = findTargetCircuit(sender);
            if (c==null) return;
        } else {
            try {
                int id = Integer.decode(args[0]);
                if (!circuits.containsKey(id)) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "There's no activated circuit with id " + id);
                    return;
                }

                c = rc.getCircuitManager().getCircuits().get(id);
            } catch (NumberFormatException ie) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad circuit id argument: " + args[0]);
                return;
            }
        }

        printCircuitInfo(sender, c);
    }

    public void printCircuitInfo(CommandSender sender, Circuit c) {
        ChatColor infoColor = rc.getPrefsManager().getInfoColor();
        ChatColor errorColor = rc.getPrefsManager().getErrorColor();
        ChatColor extraColor = ChatColor.YELLOW;

        String disabled;
        if (c.isDisabled()) disabled = errorColor + " (inputs disabled)";
        else disabled = "";

        String loc = c.activationBlock.getBlockX() + ", " + c.activationBlock.getBlockY() + ", " + c.activationBlock.getBlockZ();
        sender.sendMessage("");
        sender.sendMessage(extraColor + Integer.toString(c.id) + ": " + infoColor + c.getCircuitClass() + " circuit" + disabled);
        sender.sendMessage(extraColor + "----------------------");

        sender.sendMessage(infoColor + "" + c.inputs.length + " input(s), " + c.outputs.length + " output(s) and " + c.interfaceBlocks.length + " interface blocks.");
        sender.sendMessage(infoColor +
                "location: " + extraColor + loc + infoColor + " world: " + extraColor + c.world.getName());

        int inputInt = BitSetUtils.bitSetToUnsignedInt(c.getInputBits(), 0, c.inputs.length);
        int outputInt = BitSetUtils.bitSetToUnsignedInt(c.getOutputBits(), 0, c.outputs.length);

        if (c.inputs.length>0)
            sender.sendMessage(infoColor + "input states: " + extraColor + BitSetUtils.bitSetToBinaryString(c.getInputBits(), 0, c.inputs.length)
                    + infoColor + " (0x" + Integer.toHexString(inputInt) + ")");

        if (c.outputs.length>0)
            sender.sendMessage(infoColor + "output states: " + extraColor + BitSetUtils.bitSetToBinaryString(c.getOutputBits(), 0, c.outputs.length)
                    + infoColor + " (0x" + Integer.toHexString(outputInt) + ")");

        String signargs = "";
        for (String arg : c.args)
            signargs += arg + " ";

        sender.sendMessage(infoColor + "sign args: " + extraColor + signargs);

        Map<String,String> internalState = c.saveState();
        if (!internalState.isEmpty()) {
            sender.sendMessage(infoColor + "internal state:");
            for (String key : internalState.keySet())
                sender.sendMessage(infoColor + "   " + key + ": " + extraColor + internalState.get(key));
        }
    }

    public void prefsCommand(String[] args, CommandSender sender) {
            if (args.length==0) { // list preferences
                rc.getPrefsManager().printYaml(sender, rc.getPrefsManager().getPrefs());
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Type /rc-prefs <name> <value> to make changes.");
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
                    return;
                }
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Saving changes...");
                rc.getPrefsManager().savePrefs();
            } else {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad rc-prefs syntax.");
            }

    }

    public void debugCommand(CommandSender sender, String[] args) {
        int id = -1;
        boolean add = true;
        boolean alloff = false;

        if (args.length==1) {
            // on, off or id (then on)
            if (args[0].equalsIgnoreCase("on"))
                add = true;
            else if (args[0].equalsIgnoreCase("off"))
                add = false;
            else if (args[0].equals("alloff"))
                alloff = true;
            else {
                try {
                    id = Integer.decode(args[0]);
                    add = true;
                } catch (NumberFormatException ne) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad argument: " + args[0] + ". Expecting on, off or a chip id.");
                }
            }
        } else if (args.length==2) {
            try {
                id = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad argument: " + args[0] + ". Expecting a chip id number.");
                return;
            }

            if (args[1].equalsIgnoreCase("on"))
                add = true;
            else if (args[1].equalsIgnoreCase("off"))
                add = false;
            else {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad argument: " + args[1] + ". Expecting on or off.");
                return;
            }
        }

        if (alloff) {
            for (Circuit c : rc.getCircuitManager().getCircuits().values())
                if (c.getDebuggers().contains(sender)) c.removeDebugger(sender);
            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "You will not receive debug messages from any chip.");
        } else {
            Circuit c;
            if (id!=-1) {
                if (rc.getCircuitManager().getCircuits().size()<=id || id<0) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad chip id " + id + ". Could only find " + rc.getCircuitManager().getCircuits().size() + " active chips.");
                    return;
                }
                c = rc.getCircuitManager().getCircuits().get(id);
            } else {
                c = findTargetCircuit(sender);
                if (c==null) return;
            }

            if (add) {
                try {
                    if (id!=-1 && !sender.isOp()) {
                        sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You must have admin priviliges to debug a chip by id.");
                        return;
                    } else
                        c.addDebugger(sender);
                } catch (IllegalArgumentException ie) {
                    try {
                        c.removeDebugger(sender);
                    } catch (IllegalArgumentException me) {
                        sender.sendMessage(rc.getPrefsManager().getInfoColor() + me.getMessage());
                        return;
                    }
                    sender.sendMessage(rc.getPrefsManager().getInfoColor() + "You will not receive any more debug messages from the " + c.getClass().getSimpleName() + " circuit.");

                    return;
                }
                sender.sendMessage(rc.getPrefsManager().getDebugColor() + "You are now a debugger of the " + c.getClass().getSimpleName() + " circuit.");
            } else {
                try {
                    c.removeDebugger(sender);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefsManager().getInfoColor() + ie.getMessage());
                    return;
                }
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "You will not receive any more debug messages from the " + c.getClass().getSimpleName() + " circuit.");
            }
        }
    }

    public void destroyCommand(CommandSender sender) {
        Circuit c = findTargetCircuit(sender);
        if (c!=null) {
            if (rc.getCircuitManager().destroyCircuit(c, sender, true));
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "The " + c.getCircuitClass() + " chip is destroyed.");
        }
    }

    public void deactivateCommand(CommandSender sender, String[] args) {
        int id = -1;
        if (args.length>0) {
            try {
                id = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                sender.sendMessage("Bad circuit id number: " + args[0]);
            }
        }
        

        Circuit c = null;
        if (id==-1) { // use target block.
            c = findTargetCircuit(sender);
            if (c==null) return;
        } else { // use circuit id.
            if (!sender.isOp()) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You must be an admin to remotely deactivate a circuit.");
                return;
            }

            if (rc.getCircuitManager().getCircuits().containsKey(id)) {
                c = rc.getCircuitManager().getCircuits().get(id);
            } else {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "There's no activated circuit with id " + id);
                return;
            }
        }

        rc.getCircuitManager().destroyCircuit(c, sender, false);
        sender.sendMessage(rc.getPrefsManager().getInfoColor() + "The " + ChatColor.YELLOW + c.getCircuitClass() + " (" + c.id + ")" + rc.getPrefsManager().getInfoColor() + " circuit is now deactivated.");
    }

    public void handleRcType(CommandSender sender, String[] args) {
        Player player = checkIsPlayer(sender);

        
        Block block = targetBlock(player);
        rcTypeReceiver t = rc.rcTypeReceivers.get(block.getLocation());

        if (t==null) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "You must point towards a typing block (a terminal circuit's interface block for example) to type.");
        } else {
            player.sendMessage(rc.getPrefsManager().getInfoColor() + "Input sent.");
            t.type(args, player);
        }
    }

    public void pinCommand(CommandSender sender) {
        Player player = checkIsPlayer(sender);

        Block target = targetBlock(player);
        printPinInfo(target, player);

    }

    private void printPinInfo(Block pinBlock, CommandSender sender) {

        List<InputPin> inputList = rc.getCircuitManager().lookupInputBlock(pinBlock);
        if (inputList==null) {
            Object[] oo = rc.getCircuitManager().lookupOutputBlock(pinBlock);
            if (oo==null) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at an output lever or input redstone source.");
            } else { // output pin
                Circuit c = (Circuit)oo[0];
                int i = (Integer)oo[1];
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.YELLOW + "output pin "
                        + i + " - " + (c.getOutputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
            }
        } else { // input pin
            for (InputPin io : inputList) {
                Circuit c = io.getCircuit();
                int i = io.getIndex();
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + c.getClass().getSimpleName() + ": " + ChatColor.WHITE + "input pin "
                        + i + " - " + (c.getInputBits().get(i)?ChatColor.RED+"on":ChatColor.WHITE+"off"));
            }
        }
    }

    public void activateCommand(CommandSender sender, String[] args) {
        Player player = checkIsPlayer(sender);
        if (player==null) return;

        Block target = targetBlock(player);
        if (target.getType()==Material.WALL_SIGN) {
            MaterialData inputBlockType, outputBlockType, interfaceBlockType;

            if (args.length==0) {
                inputBlockType = rc.getPrefsManager().getInputBlockType();
                outputBlockType = rc.getPrefsManager().getOutputBlockType();
                interfaceBlockType = rc.getPrefsManager().getInterfaceBlockType();
            } else {
                if (args.length!=3) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad syntax. Expecting /rccuboid activate [inputBlockType] [outputBlockType] [interfaceBlockType]");
                    return;
                }
                try {
                    inputBlockType = PrefsManager.findMaterial(args[0]);
                    outputBlockType = PrefsManager.findMaterial(args[1]);
                    interfaceBlockType = PrefsManager.findMaterial(args[2]);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(ie.getMessage());
                    return;
                }
            }

            if (rc.getCircuitManager().checkForCircuit(target, sender, inputBlockType, outputBlockType, interfaceBlockType)==-1) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Could not activate chip.");
            }
        } else {
            sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a wall sign.");
        }

    }

    public void resetCircuit(CommandSender sender, String[] args) {
        Circuit c;

        if (args.length>0) { // use circuit id.
            try {
                int id = Integer.decode(args[0]);
                c = rc.getCircuitManager().getCircuits().get(id);
                if (c==null) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Invalid circuit id: " + id + ".");
                    return;
                }
            } catch (NumberFormatException ne) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad argument: " + args[0] + ". Expecting a number.");
                return;
            }
        } else { // use targeted circuit
            c = findTargetCircuit(sender);
            if (c==null) return;
        }

        rc.getCircuitManager().resetCircuit(c, sender);
    }

    public void argumentCommand(CommandSender sender, String[] args) {
        Player player = checkIsPlayer(sender);
        if (player==null) return;

        Block target = targetBlock(player);
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);
        if (c==null) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of the circuit you wish to reset.");
            return;
        }

        if (args.length<2) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad syntax. Expecting /rcarg <arg number> <arg value>.");
            return;
        }

        try {
            int idx = Integer.decode(args[0]);

            String arg = "";
            for (int i=1; i<args.length; i++) arg += args[i] + " ";
            arg = arg.substring(0, arg.length()-1);

            if (idx>c.args.length || idx<0) {
                player.sendMessage(rc.getPrefsManager().getErrorColor() + "Argument number out of bounds: " + idx);
            } else {
                if (idx==c.args.length) {
                    // add to last
                    String[] tempArgs = new String[c.args.length+1];
                    System.arraycopy(c.args, 0, tempArgs, 0, c.args.length);
                    tempArgs[tempArgs.length-1] = arg;
                } else {
                    c.args[idx] = arg;
                }

                Sign sign = (Sign)c.activationBlock.getBlock().getState();

                for (int i=0; i<c.args.length; i++) {
                    String a = c.args[i];

                    //if (c.args.length>3) // try to word wrap the args putting more than 1 per line.
                        //TODO: do something
                    //else // put each arg on a separate line
                      //  sign.setLine(i,a);

                }


            }
        } catch (NumberFormatException ne) {
            player.sendMessage(rc.getPrefsManager().getErrorColor() + "Bad argument number: " + args[0]);
        }
    }

    public void cuboidCommand(CommandSender sender, String[] args) {
        Player p = checkIsPlayer(sender);
        if (p==null) return;

        if (args.length==0) {
            if (playerCuboids.containsKey(p))
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Clearing previous cuboid coordinates.");

            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Right-click 2 blocks at opposite corners of your cuboid. Right-clicking while holding a block in hand is ignored.");
            definingCuboids.add(p);
            playerCuboids.put(p, new ArrayList<Location>());
        } else {
            long start = System.nanoTime();
            List<Location> cuboid = playerCuboids.get(p);
            if (cuboid==null) {
                sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to first define a cuboid. Use /rccuboid with no arguments and left-click two opposite corners to define.");
            }
            if (args[0].equalsIgnoreCase("activate")) {
                MaterialData inputBlockType, outputBlockType, interfaceBlockType;

                if (args.length==1) {
                    inputBlockType = rc.getPrefsManager().getInputBlockType();
                    outputBlockType = rc.getPrefsManager().getOutputBlockType();
                    interfaceBlockType = rc.getPrefsManager().getInterfaceBlockType();
                } else {
                    if (args.length!=4) {
                        p.sendMessage("Bad syntax. Expecting /rccuboid activate [inputBlockType] [outputBlockType] [interfaceBlockType]");
                        return;
                    }
                    try {
                        inputBlockType = PrefsManager.findMaterial(args[1]);
                        outputBlockType = PrefsManager.findMaterial(args[2]);
                        interfaceBlockType = PrefsManager.findMaterial(args[3]);
                    } catch (IllegalArgumentException ie) {
                        p.sendMessage(ie.getMessage());
                        return;
                    }
                }

                int lowx = Math.min(cuboid.get(0).getBlockX(), cuboid.get(1).getBlockX());
                int highx = Math.max(cuboid.get(0).getBlockX(), cuboid.get(1).getBlockX());

                int lowy = Math.min(cuboid.get(0).getBlockY(), cuboid.get(1).getBlockY());
                int highy = Math.max(cuboid.get(0).getBlockY(), cuboid.get(1).getBlockY());

                int lowz = Math.min(cuboid.get(0).getBlockZ(), cuboid.get(1).getBlockZ());
                int highz = Math.max(cuboid.get(0).getBlockZ(), cuboid.get(1).getBlockZ());

                int wallSignId = Material.WALL_SIGN.getId();

                for (int x=lowx; x<=highx; x++) {
                    for (int y=lowy; y<=highy; y++) {
                        for (int z=lowz; z<=highz; z++) {
                            Block b = cuboid.get(0).getWorld().getBlockAt(x, y, z);
                            if (b.getTypeId()==wallSignId) {
                                rc.getCircuitManager().checkForCircuit(b, sender, inputBlockType, outputBlockType, interfaceBlockType);
                            }
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    rc.getCircuitManager().resetCircuit(c, sender);
            } else if (args[0].equalsIgnoreCase("break")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    rc.getCircuitManager().destroyCircuit(c, sender, false);
            } else if (args[0].equalsIgnoreCase("destroy")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    rc.getCircuitManager().destroyCircuit(c, sender, true);
            } else if (args[0].equalsIgnoreCase("forceblocks")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    c.forceIOBlockMaterials();
            } else if (args[0].equalsIgnoreCase("clear")) {
                playerCuboids.remove(p);
                definingCuboids.remove(p);
                sender.sendMessage("The cuboid is cleared.");
            }

            long delta = System.nanoTime()/start;
            String timing = String.format( "%.3fms", (float)delta / 1000000d );
            sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Cuboid command finished in " + timing + ".");

        }
    }

    public void cuboidLocation(Player p, Location point) {
        if (definingCuboids.contains(p)) {
            List<Location> coords = playerCuboids.get(p);
            if (coords.size()>=2) return;

            coords.add(point);
            if (coords.size()==1) {
                p.sendMessage(rc.getPrefsManager().getInfoColor() + "1st cuboid corner selected.");
            } else if (coords.size()==2) {
                p.sendMessage(rc.getPrefsManager().getInfoColor() + "Cuboid defined: " + coords.get(0).getBlockX() + "," + coords.get(0).getBlockY() + "," + coords.get(0).getBlockZ()
                        + " to " + coords.get(1).getBlockX() + "," + coords.get(1).getBlockY() + "," + coords.get(1).getBlockZ());
                p.sendMessage(rc.getPrefsManager().getInfoColor() + "You can now use any of the /rccuboid commands. Type /rccuboid clear to clear your cuboid.");
                definingCuboids.remove(p);
            }

        }
    }

    public static void pageMaker(CommandSender s, String spage, String title, String commandName, String[] lines, ChatColor infoColor, ChatColor errorColor) {
            int page = 1;
            if (spage!=null) {
                try {
                    page = Integer.decode(spage);
                } catch (NumberFormatException ne) {
                    //s.sendMessage(errorColor + "Invalid page number: " + args[args.length-1]);
                }
            }

            int pageCount = (int)(Math.ceil(lines.length/(float)MaxLines));
            if (page<1 || page>pageCount) s.sendMessage(errorColor + "Invalid page number: " + page + ". Expecting 1-" + pageCount);
            else {
                s.sendMessage(infoColor + title + ": " + (pageCount>1?"( page " + page + " / " + pageCount  + " )":""));
                s.sendMessage(infoColor + "----------------------");
                for (int i=(page-1)*MaxLines; i<Math.min(lines.length, page*MaxLines); i++) {
                    s.sendMessage(lines[i]);
                }
                s.sendMessage(infoColor + "----------------------");
                if (pageCount>1) s.sendMessage("Use /" + commandName + " <page number> to see other pages.");
            }

    }

    private Player checkIsPlayer(CommandSender sender) {
        if (sender instanceof Player) return (Player)sender;
        else {
            sender.sendMessage(rc.getPrefsManager().getErrorColor() + "Only players are allowed to use this command.");
            return null;
        }
    }


    public Circuit findTargetCircuit(CommandSender sender) {
        Player player = checkIsPlayer(sender);
        if (player==null) return null;

        Block target = targetBlock(player);
        Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(target);
        if (c==null) {
            sender.sendMessage(rc.getPrefsManager().getErrorColor() + "You need to point at a block of a redstone chip.");
        }

        return c;
    }

    private Block targetBlock(Player player) {
        return player.getTargetBlock(transparentMaterials, 100);
    }

    private List<Circuit> findActiveCircuitsInCuboid(CommandSender s, List<Location> cuboid) {
        int lowx = Math.min(cuboid.get(0).getBlockX(), cuboid.get(1).getBlockX());
        int highx = Math.max(cuboid.get(0).getBlockX(), cuboid.get(1).getBlockX());

        int lowy = Math.min(cuboid.get(0).getBlockY(), cuboid.get(1).getBlockY());
        int highy = Math.max(cuboid.get(0).getBlockY(), cuboid.get(1).getBlockY());

        int lowz = Math.min(cuboid.get(0).getBlockZ(), cuboid.get(1).getBlockZ());
        int highz = Math.max(cuboid.get(0).getBlockZ(), cuboid.get(1).getBlockZ());

        List<Circuit> result = new ArrayList<Circuit>();

        for (Circuit c : rc.getCircuitManager().getCircuits().values()) {
            Location l = c.activationBlock;
            if (l.getBlockX()>=lowx && l.getBlockX()<=highx
                    && l.getBlockY()>=lowy && l.getBlockY()<=highy
                    && l.getBlockZ()>=lowz && l.getBlockZ()<=highz) {
                result.add(c);
            }
        }

        if (result.isEmpty()) {
            s.sendMessage(rc.getPrefsManager().getErrorColor() + "Your cuboid doesn't contain any active circuits.");
        }

        return result;
    }
}
