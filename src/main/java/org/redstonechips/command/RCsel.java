package org.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.user.UserSession;
import org.redstonechips.user.UserSession.Mode;
import org.redstonechips.user.WorldEditHook;

/**
 *
 * @author Tal Eisenberg
 */
public class RCsel extends RCCommand {
    private static final ChatColor color = ChatColor.AQUA;

    private enum SelCommand {
        WORLD(null), CUBOID(null), ID(null), TARGET(null), ACTIVATE("Activated"), RESET("Reset"), BREAK("Deactivated"), LIST(null), 
        DESTROY("Destroyed"), FIXIOBLOCKS("Fixed"), CLEAR(null), ENABLE("Enabled"), DISABLE("Disabled");
        
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
    public boolean isPlayerRequired() { return true; }
    
    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        
        SelCommand selCommand;
        
        if (args.length==0) {
            UserSession session = rc.getUserSession(p, true);
            
            if (session.getMode()==Mode.SELECTION) {
                session.setMode(Mode.NORMAL);
                p.sendMessage(color + "Stopped selecting chips.");
            } else {
                session.setMode(Mode.SELECTION);
                p.sendMessage(RCPrefs.getInfoColor() + "rcselection");
                p.sendMessage(RCPrefs.getInfoColor() + "-----------------");
                p.sendMessage(color + "Right-click a chip block to select it. Run " + ChatColor.YELLOW + "/rcsel" + color + " again to stop selecting.");
                p.sendMessage(color + "Run " + ChatColor.YELLOW + "/rchelp rcsel" + color + " for more selection commands.");
            }
            
            printSelMessage(p, session);
            return;
        } else {
            selCommand = SelCommand.startsWith(args[0]);
            if (selCommand==null) {
                error(sender, "Unknown selection command: " + args[0]);            
                return;
            }
        }

        if (selCommand == SelCommand.CUBOID) {
            UserSession session = rc.getUserSession(p, true);
            Location[] sel = null;
            if (WorldEditHook.isWorldEditInstalled(rc)) {
                sel = WorldEditHook.getWorldEditSelection(p, rc);
            } 
            
            if (sel!=null) {
                info(sender, "Using WorldEdit selection.");
                session.selectChipsInCuboid(sel, false);
                session.setCuboid(sel);
                printSelMessage(p, session);
            } else {
                if (session.getMode()==Mode.CUBOID_DEFINE) {
                    session.clearCuboid();
                }
                
                session.defineCuboid();
                
                info(sender, "Right-click 2 blocks at opposite corners of your cuboid.");                
            }
            
        } else if (selCommand == SelCommand.ID) {
            selectById(p, args);
        } else if (selCommand == SelCommand.TARGET) {
            selectTarget(p);
        } else if (selCommand == SelCommand.WORLD) {
            selectWorld(p, args);
        } else if (selCommand == SelCommand.ACTIVATE) {
            UserSession session = rc.getUserSession(p, true);
            massActivate(p, args, session);            
        } else {
            UserSession session = rc.getUserSession(p, false);
            if (session == null || session.getSelection().isEmpty()) {
                error(sender, "Selection is empty.");
                return;
            }

            if (selCommand==SelCommand.LIST) {
                RClist.printCircuitList(p, session.getSelection(), "Selection (" + session.getSelection().size() + ")");
                
            } else if (selCommand==SelCommand.CLEAR) {
                session.getSelection().clear();
                info(sender, "Cleared chip selection.");
            } else {
                massCommand(selCommand, p, session);
            }    
        }
    }      
    
    private void massCommand(SelCommand selCommand, CommandSender sender, UserSession session) {
        long start = System.nanoTime();
        int count = 0;
        
        for (Chip c : session.getSelection()) {
            switch (selCommand) {
                case RESET:
                    if (rc.chipManager().resetChip(c, sender)) count++;
                    break;
                case BREAK:
                    if (rc.chipManager().destroyChip(c, sender, false)) count++;        
                    break;
                case DESTROY:
                    if (rc.chipManager().destroyChip(c, sender, true)) count++;
                    break;
                case FIXIOBLOCKS:
                    if (rc.chipManager().fixIOBlocks(c)>0) count++;
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
                    break;
                default: break;
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
        List<Chip> selection = new ArrayList<>();
        for (int i=1; i<args.length; i++) {
            Chip c = rc.chipManager().getAllChips().getById(args[i]);
            if (c!=null) {
                selection.add(c);
            } else {
                p.sendMessage(ChatColor.DARK_PURPLE + "Can't find chip " + args[i]);                
            }
        }
        
        session.getSelection().addAll(selection);
        p.sendMessage(color + "Added " + selection.size() + " chip(s) to selection. Selection contains " + session.getSelection().size() + " chip(s).");
    }
     
    private void selectWorld(Player p, String[] args) {
        UserSession session = rc.getUserSession(p, true);
        session.getSelection().clear();
        
        Map<Integer, Chip> clist;
        if (args.length<2) {
            clist = rc.chipManager().getAllChips().getInWorld(p.getWorld());
            if (clist!=null) session.getSelection().addAll(clist.values());
        } else {
            for (int i=1; i<args.length; i++) {
                World world = rc.getServer().getWorld(args[i]);
                if (world==null) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "Unknown world name: " + args[i]);
                } else {
                    clist = rc.chipManager().getAllChips().getInWorld(world);
                    if (clist!=null) session.getSelection().addAll(clist.values());
                }
            }
        }
        
        p.sendMessage(color + "Selected " + session.getSelection().size() + " chip(s).");
    }
    
    private void selectTarget(Player p) {
        Chip c = CommandUtils.findTargetChip(p, 20, true);
        
        if (c!=null) {
            UserSession session = rc.getUserSession(p, true);
            if (!session.getSelection().contains(c)) {
                session.getSelection().add(c);
                p.sendMessage(color + "Selected " + ChatColor.YELLOW + c + color + ".");
            } else {
                session.getSelection().remove(c);
                p.sendMessage(color + "Removed " + ChatColor.YELLOW + c + color + " from selection.");
            }
            
            
        }
    }
    
    private void printSelMessage(Player p, UserSession s) {
        p.sendMessage(color + "Selection contains " + s.getSelection().size() + " active chip(s).");
    }
    
    private void massActivate(Player p, String[] args, UserSession session) {
        if (session.getCuboid()==null) {
            p.sendMessage(RCPrefs.getErrorColor() + "You must define a cuboid before using this command.");
            return;
        }
            
        Material inputBlockType = null, outputBlockType = null, interfaceBlockType = null;

        try {
            if (args.length>=2)
                inputBlockType = Material.matchMaterial(args[1]);
            if (args.length>=3)
                outputBlockType = Material.matchMaterial(args[2]);
            if (args.length>=4)
                interfaceBlockType = Material.matchMaterial(args[2]);
        } catch (IllegalArgumentException ie) {
            p.sendMessage(ie.getMessage());
            return;
        }

        if (inputBlockType==null) inputBlockType = RCPrefs.getInputBlockType();
        if (outputBlockType==null) outputBlockType = RCPrefs.getOutputBlockType();
        if (interfaceBlockType==null) interfaceBlockType = RCPrefs.getInterfaceBlockType();
        
        Location[] cuboid = session.getCuboid();
        int lowx = Math.min(cuboid[0].getBlockX(), cuboid[1].getBlockX());
        int highx = Math.max(cuboid[0].getBlockX(), cuboid[1].getBlockX());

        int lowy = Math.min(cuboid[0].getBlockY(), cuboid[1].getBlockY());
        int highy = Math.max(cuboid[0].getBlockY(), cuboid[1].getBlockY());

        int lowz = Math.min(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());
        int highz = Math.max(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());

        int count = 0;

        for (int x=lowx; x<=highx; x++) {
            for (int y=lowy; y<=highy; y++) {
                for (int z=lowz; z<=highz; z++) {
                    Block b = cuboid[0].getWorld().getBlockAt(x, y, z);
                    if (b.getBlockData() instanceof WallSign) {
                        MaybeChip mChip = RCactivate.activate(p, b, 0, inputBlockType, outputBlockType, interfaceBlockType);
                        if (mChip==MaybeChip.AChip) count++;
                        else if (mChip==MaybeChip.ChipError) error(p, mChip.getError());
                    }
                }
            }
        }

        p.sendMessage(color + "Activated " + count + " circuit(s).");        
    }
    
}
