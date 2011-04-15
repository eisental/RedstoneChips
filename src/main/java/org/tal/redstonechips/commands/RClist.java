/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RClist extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        World world = null;

        TreeMap<Integer, Circuit> sorted = new TreeMap<Integer, Circuit>(rc.getCircuitManager().getCircuits());

        boolean oneWorld = true;
        if (args.length>0) {
            if (!args[0].equalsIgnoreCase("all")) {
                world = rc.getServer().getWorld(args[0]);
            } else oneWorld = false;
        }

        if (oneWorld && world==null && (sender instanceof Player)) {
            // use player world as default.
            world = ((Player)sender).getWorld();
        }

        if (sorted.isEmpty()) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no active circuits.");
        else {
            printCircuitList(sender, args, sorted, world);
        }

        return true;
    }

    public void printCircuitList(CommandSender sender, String[] args, Map<Integer, Circuit> circuits, World world) {
        String title = " active IC(s) in ";
        String commandName = "rclist";
        List<String> lines = new ArrayList<String>();
        for (Integer id : circuits.keySet()) {
            Circuit c = circuits.get(id);
            if (world==null || c.world.getName().equals(world.getName())) {
                lines.add(makeCircuitDescriptionLine(c, rc.getPrefs().getInfoColor()));
            }
        }

        if (lines.isEmpty()) {
            sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no active circuits on world " + world.getName() + ".");
            return;
        }

        String page = null;
        if (args.length>1) page = args[args.length-1];
        else if (args.length>0) page = args[0];

        if (world==null) title += "all worlds";
        else title +="world " + world.getName();
        title = lines.size() + title;

        sender.sendMessage("");

        CommandUtils.pageMaker(sender, page, title, commandName, lines.toArray(new String[lines.size()]),
                rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor(), CommandUtils.MaxLines-1);
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
}
