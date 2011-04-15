/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.tal.redstonechips.PrefsManager;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCsel extends RCCommand {
    private List<Player> definingCuboids = new ArrayList<Player>();
    private Map<Player,Location[]> playerCuboids = new HashMap<Player,Location[]>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = CommandUtils.checkIsPlayer(rc, sender);
        if (p==null) return true;

        if (args.length==0) {
            if (playerCuboids.containsKey(p))
                sender.sendMessage(rc.getPrefs().getInfoColor() + "Clearing previous selection coordinates.");

            sender.sendMessage(rc.getPrefs().getInfoColor() + "Right-click 2 blocks at opposite corners of your cuboid. Right-clicking while holding a block in hand is ignored.");
            definingCuboids.add(p);
            playerCuboids.put(p, new Location[2]);
        } else {
            ChatColor infoColor = ChatColor.AQUA;

            long start = System.nanoTime();
            Location[] cuboid = playerCuboids.get(p);
            if (cuboid==null || cuboid[0]==null || cuboid[1]==null) {
                // try to use worldedit selection instead
                if (isWorldEditInstalled()) {
                    cuboid = getWorldEditSelection(p);
                    sender.sendMessage(infoColor + "No /rcsel selection defined. Using WorldEdit selection instead.");
                } else {
                    sender.sendMessage(infoColor + "No /rcsel selection defined and WorldEdit is not installed or doesn't have a selection. Use /rcsel with no arguments and right-click two opposite corners to define.");
                    return true;
                }
            } else sender.sendMessage(infoColor + "Using /rcsel selection. Type /rcsel clear to use WorldEdit's selection instead.");

            if (args[0].equalsIgnoreCase("activate")) {
                massActivate(sender, args, cuboid, infoColor);

            } else if (args[0].equalsIgnoreCase("reset")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    rc.getCircuitManager().resetCircuit(c, sender);
                sender.sendMessage(infoColor + "Reset " + circuits.size() + " circuit(s).");

            } else if (args[0].equalsIgnoreCase("break")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    rc.getCircuitManager().destroyCircuit(c, sender, false);
                sender.sendMessage(infoColor + "Deactivated " + circuits.size() + " circuit(s).");

            } else if (args[0].equalsIgnoreCase("list")) {
                printList(sender, cuboid, (args.length>1 ? args[1]: null));

            } else if (args[0].equalsIgnoreCase("destroy")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                for (Circuit c : circuits)
                    rc.getCircuitManager().destroyCircuit(c, sender, true);
                sender.sendMessage(infoColor + "Destroyed " + circuits.size() + " circuit(s).");

            } else if (args[0].equalsIgnoreCase("fixioblocks")) {
                List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);
                int blockCount = 0;
                for (Circuit c : circuits)
                    blockCount += c.fixIOBlocks();
                sender.sendMessage(infoColor + "Fixed i/o blocks of " + circuits.size() + " circuit(s). " + blockCount +" blocks were replaced.");

            } else if (args[0].equalsIgnoreCase("clear")) {
                if (definingCuboids.contains(p) || playerCuboids.containsKey(p)) {
                    playerCuboids.remove(p);
                    definingCuboids.remove(p);
                    sender.sendMessage(rc.getPrefs().getInfoColor() + "The selection is cleared.");
                } else sender.sendMessage(rc.getPrefs().getErrorColor() + "There's no selection to clear.");
            } else {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown selection command: " + args[0]);
            }

            if (!args[0].equalsIgnoreCase("list")) {
                long delta = System.nanoTime()-start;
                String timing = String.format( "%.3fms", (float)delta / 1000000d );
                sender.sendMessage(infoColor + "Mass edit finished in " + timing + ".");
            }
        }

        return true;
    }

    public void cuboidLocation(Player p, Location point) {
        if (definingCuboids.contains(p)) {
            Location[] coords = playerCuboids.get(p);

            if (coords[0]==null && coords[1]==null) {
                coords[0] = point;
                p.sendMessage(rc.getPrefs().getInfoColor() + "1st selection corner selected.");
            } else if (coords[0] != null && coords[1] == null) {
                coords[1] = point;
                p.sendMessage(rc.getPrefs().getInfoColor() + "Cuboid defined: " + coords[0].getBlockX() + "," + coords[0].getBlockY() + "," + coords[0].getBlockZ()
                        + " to " + coords[1].getBlockX() + "," + coords[1].getBlockY() + "," + coords[1].getBlockZ());
                p.sendMessage(rc.getPrefs().getInfoColor() + "You can now use any of the /rcsel commands. Type /rcsel clear to clear your selection.");
                definingCuboids.remove(p);
            }
        }
    }

    private boolean isWorldEditInstalled() {
        return rc.getServer().getPluginManager().getPlugin("WorldEdit")!=null;
    }

    private Location[] getWorldEditSelection(Player player) {
        Plugin worldEdit = rc.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            return null;
        }

        // get access to WorldEditPlugin.getSelection(Player player);
        Method getSelectionMethod = null;
        for (Method m : worldEdit.getClass().getMethods()) {
            if (m.getName().equals("getSelection") && m.getParameterTypes().length==1 && m.getParameterTypes()[0]==Player.class) {
                getSelectionMethod = m;
            }
        }

        if (getSelectionMethod!=null) {
            try {
                // try to get the current selection.
                Object selection = getSelectionMethod.invoke(worldEdit, player);
                if (selection==null) return null;

                // getting two opposite corners of selection.
                Location[] ret = new Location[2];
                ret[0] = (Location)selection.getClass().getMethod("getMinimumPoint").invoke(selection);
                ret[1] = (Location)selection.getClass().getMethod("getMaximumPoint").invoke(selection);

                return ret;
            } catch (IllegalAccessException ex) {
                rc.log(Level.SEVERE, "While communicating with WorldEdit: " + ex.toString());
            } catch (IllegalArgumentException ex) {
                rc.log(Level.SEVERE, "While communicating with WorldEdit: " + ex.toString());
            } catch (InvocationTargetException ex) {
                rc.log(Level.SEVERE, "While communicating with WorldEdit: " + ex.toString());
            } catch (NoSuchMethodException ex) {
                rc.log(Level.SEVERE, "While communicating with WorldEdit: " + ex.toString());
            }
        }

        return null;
    }

    public List<Circuit> findActiveCircuitsInCuboid(CommandSender s, Location[] cuboid) {
        int lowx = Math.min(cuboid[0].getBlockX(), cuboid[1].getBlockX());
        int highx = Math.max(cuboid[0].getBlockX(), cuboid[1].getBlockX());

        int lowy = Math.min(cuboid[0].getBlockY(), cuboid[1].getBlockY());
        int highy = Math.max(cuboid[0].getBlockY(), cuboid[1].getBlockY());

        int lowz = Math.min(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());
        int highz = Math.max(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());

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
            s.sendMessage(rc.getPrefs().getErrorColor() + "Your cuboid doesn't contain any active circuits.");
        }

        return result;
    }

    private void massActivate(CommandSender sender, String[] args, Location[] cuboid, ChatColor infoColor) {
        MaterialData inputBlockType, outputBlockType, interfaceBlockType;

        if (args.length==1) {
            inputBlockType = rc.getPrefs().getInputBlockType();
            outputBlockType = rc.getPrefs().getOutputBlockType();
            interfaceBlockType = rc.getPrefs().getInterfaceBlockType();
        } else {
            if (args.length!=4) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad syntax. Expecting /rcsel activate [<inputBlockType> <outputBlockType> <interfaceBlockType>]");
                return;
            }
            try {
                inputBlockType = PrefsManager.findMaterial(args[1]);
                outputBlockType = PrefsManager.findMaterial(args[2]);
                interfaceBlockType = PrefsManager.findMaterial(args[3]);
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(ie.getMessage());
                return;
            }
        }

        int lowx = Math.min(cuboid[0].getBlockX(), cuboid[1].getBlockX());
        int highx = Math.max(cuboid[0].getBlockX(), cuboid[1].getBlockX());

        int lowy = Math.min(cuboid[0].getBlockY(), cuboid[1].getBlockY());
        int highy = Math.max(cuboid[0].getBlockY(), cuboid[1].getBlockY());

        int lowz = Math.min(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());
        int highz = Math.max(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());

        int wallSignId = Material.WALL_SIGN.getId();

        int count = 0;

        for (int x=lowx; x<=highx; x++) {
            for (int y=lowy; y<=highy; y++) {
                for (int z=lowz; z<=highz; z++) {
                    Block b = cuboid[0].getWorld().getBlockAt(x, y, z);
                    if (b.getTypeId()==wallSignId) {
                        if (rc.getCircuitManager().checkForCircuit(b, sender, inputBlockType, outputBlockType, interfaceBlockType)>=0)
                            count++;
                    }
                }
            }
        }

        sender.sendMessage(infoColor + "Activated " + count + " circuit(s).");
    }

    private void printList(CommandSender sender, Location[] cuboid, String page) {
        List<Circuit> circuits = findActiveCircuitsInCuboid(sender, cuboid);

        String selection = cuboid[0].getBlockX() + ", " + cuboid[0].getBlockY() + ", " + cuboid[0].getBlockZ() + " - "
                + cuboid[1].getBlockX() + "," + cuboid[1].getBlockY() + "," + cuboid[1].getBlockZ();

        String lines[] = new String[circuits.size()];
        for (int i=0; i<lines.length; i++) {
            lines[i] = RClist.makeCircuitDescriptionLine(circuits.get(i), rc.getPrefs().getInfoColor());
        }

        CommandUtils.pageMaker(sender, page, "Active circuits in selection (" + selection + ")", "rcsel list", lines, rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
    }
}
