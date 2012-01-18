/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.PrefsManager;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.user.UserSession;
import org.tal.redstonechips.user.UserSession.Mode;
import org.tal.redstonechips.user.WorldEditHook;

/**
 *
 * @author Tal Eisenberg
 */
public class RCsel extends RCCommand {
    private static final ChatColor color = ChatColor.AQUA;

    private enum SelCommand {
        WORLD(null), CUBOID(null), ID(null), ACTIVATE("Activated"), RESET("Reset"), BREAK("Deactivated"), LIST(null), 
        DESTROY("Destroyed"), FIXIOBLOCKS("Fixed "), CLEAR(null), ENABLE("Enabled"), DISABLE("Disabled");
        
        String verb;
        
        SelCommand(String verb) { this.verb = verb; }
        
        public static SelCommand startsWith(String s) {
            s = s.toLowerCase();
            for (SelCommand c : SelCommand.values())
                if (c.name().toLowerCase().startsWith(s))
                    return c;
            
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = CommandUtils.checkIsPlayer(rc, sender);
        if (p==null) return true;

        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        
        SelCommand selCommand = null;
        
        if (args.length==0) {
            UserSession session = rc.getUserSession(p, true);
            
            if (session.getMode()==Mode.SELECTION) {
                session.setMode(Mode.NORMAL);
                p.sendMessage(color + "Stopped selecting chips.");
            } else {
                session.setMode(Mode.SELECTION);
                p.sendMessage(rc.getPrefs().getInfoColor() + "rcselection");
                p.sendMessage(rc.getPrefs().getInfoColor() + "-----------------");
                p.sendMessage(color + "Right-click a chip block to select it. Run " + ChatColor.YELLOW + "/rcsel" + color + " again to stop selecting.");
                p.sendMessage(color + "Run " + ChatColor.YELLOW + "/rchelp rcsel" + color + " for more selection commands.");
            }
            
            printSelMessage(p, session);
            return true;
        } else {
            selCommand = SelCommand.startsWith(args[0]);
            if (selCommand==null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown selection command: " + args[0]);            
                return true;
            }
        }

        if (selCommand == SelCommand.CUBOID) {
            UserSession session = rc.getUserSession(p, true);
            Location[] sel = null;
            if (WorldEditHook.isWorldEditInstalled(rc)) {
                sel = WorldEditHook.getWorldEditSelection(p, rc);
            } 
            
            if (sel!=null) {
                p.sendMessage(rc.getPrefs().getInfoColor() + "Using WorldEdit selection.");
                session.selectChipsInCuboid(sel, false);
                session.setCuboid(sel);
                printSelMessage(p, session);
            } else {
                if (session.getMode()==Mode.CUBOID_DEFINE) {
                    session.clearCuboid();
                }
                
                session.defineCuboid();
                
                p.sendMessage(rc.getPrefs().getInfoColor() + "Right-click 2 blocks at opposite corners of your cuboid.");                
            }
            
        } else if (selCommand == SelCommand.ID) {
            selectById(p, args);
        } else if (selCommand == SelCommand.ACTIVATE) {
            UserSession session = rc.getUserSession(p, true);
            massActivate(p, args, session);            
        } else {
            UserSession session = rc.getUserSession(p, false);
            if (session == null || session.getSelection().isEmpty()) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "Selection is empty.");
                return true;
            }

            if (selCommand==SelCommand.LIST) {
                RClist.printCircuitList(p, session.getSelection(), "Selection (" + session.getSelection().size() + ")", rc);
                
            } else if (selCommand==SelCommand.CLEAR) {
                session.getSelection().clear();
                p.sendMessage(rc.getPrefs().getInfoColor() + "Cleared chip selection.");
            } else {
                massCommand(selCommand, p, session);
            }    
        }

        return true;
    }      
    
    private void massCommand(SelCommand selCommand, CommandSender sender, UserSession session) {
        long start = System.nanoTime();
        int count = 0;
        
        for (Circuit c : session.getSelection()) {
            switch (selCommand) {
                case RESET:
                    if (rc.getCircuitManager().resetCircuit(c, sender)) count++;
                    break;
                case BREAK:
                    if (rc.getCircuitManager().destroyCircuit(c, sender, false)) count++;        
                    break;
                case DESTROY:
                    if (rc.getCircuitManager().destroyCircuit(c, sender, true)) count++;
                    break;
                case FIXIOBLOCKS:
                    if (c.fixIOBlocks()>0) count++;
                    break;
                case ENABLE:
                    if (c.isDisabled()) {
                        c.enable();
                        count++;
                    }
                    break;
                case DISABLE:
                    if (!c.isDisabled()) {
                        c.disable();
                        count++;
                    }
            }
        }
     
        long delta = System.nanoTime()-start;
        String timing = String.format( "%.3fms", (float)delta / 1000000d );
        sender.sendMessage(color + "Mass edit finished in " + timing + ".");
        
        if (selCommand.verb!=null)
            sender.sendMessage(color + selCommand.verb + " " + count + " chip(s)");
        
    }
    
    private void selectById(Player p, String[] args) {
        UserSession session = rc.getUserSession(p, true);
        List<Circuit> selection = new ArrayList<Circuit>();
        for (int i=1; i<args.length; i++) {
            Circuit c = rc.getCircuitManager().getCircuitById(args[i]);
            if (c!=null) {
                selection.add(c);
            } else {
                p.sendMessage(ChatColor.DARK_PURPLE + "Can't find chip " + args[i]);                
            }
        }
        
        session.getSelection().addAll(selection);
        p.sendMessage(color + "Added " + selection.size() + " chip(s) to selection. Selection contains " + session.getSelection().size() + " chip(s).");
    }
        
    private void printSelMessage(Player p, UserSession s) {
        p.sendMessage(color + "Selection contains " + s.getSelection().size() + " active chip(s).");
    }
    
    private void massActivate(Player p, String[] args, UserSession session) {
        if (session.getCuboid()==null) {
            p.sendMessage(rc.getPrefs().getErrorColor() + "You must define a cuboid before using this command.");
            return;
        }
            
        MaterialData inputBlockType = null, outputBlockType = null, interfaceBlockType = null;

        try {
            if (args.length>=2)
                inputBlockType = PrefsManager.findMaterial(args[1]);
            if (args.length>=3)
                outputBlockType = PrefsManager.findMaterial(args[2]);
            if (args.length>=4)
                interfaceBlockType = PrefsManager.findMaterial(args[2]);
        } catch (IllegalArgumentException ie) {
            p.sendMessage(ie.getMessage());
            return;
        }

        if (inputBlockType==null) inputBlockType = rc.getPrefs().getInputBlockType();
        if (outputBlockType==null) outputBlockType = rc.getPrefs().getOutputBlockType();
        if (interfaceBlockType==null) interfaceBlockType = rc.getPrefs().getInterfaceBlockType();
        
        Location[] cuboid = session.getCuboid();
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
                        if (RCactivate.activate(b, inputBlockType, outputBlockType, interfaceBlockType, p, false, rc))
                            count++;
                    }
                }
            }
        }

        p.sendMessage(color + "Activated " + count + " circuit(s).");        
    }
    
}
