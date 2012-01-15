package org.tal.redstonechips.user;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class UserSession {

    public void playerQuit() {
        player = null;
    }

    public void playerJoined(Player p) {
        player = p;
    }

    public enum Mode { SELECTION , NORMAL, CUBOID_DEFINE}
    
    private Player player;
    
    private RedstoneChips rc;    
    private Map<Material, Tool> tools;
    private String username;
    private List<Circuit> selection;
    private Mode mode;
    private Location[] cuboid = null;
    
    public UserSession(String username, RedstoneChips rc) {
        this.username = username;
        this.rc = rc;
        selection = new ArrayList<Circuit>();
        
        tools = new EnumMap<Material, Tool>(Material.class);
        
        player = rc.getServer().getPlayer(username);
    }

    public String getUsername() { return username; }
    
    public Player getPlayer() {
        if (player==null)
            return rc.getServer().getPlayer(username);
        else return player;
    }
    
    public void addTool(Tool t) {
        t.setSession(this);
        tools.put(t.getItem(), t);
    }

    public RedstoneChips getPlugin() {
        return rc;
    }
    
    public List<Circuit> getSelection() { return selection; }

    public void setSelection(List<Circuit> selection) {
        this.selection = selection;
    }

    public Mode getMode() { return mode; }
    
    public void setMode(Mode m) {
        this.mode = m;
    }
    
    public boolean useToolInHand(Block block) {
        Player p = getPlayer();
        if (p==null) return false;
        Tool t = tools.get(p.getItemInHand().getType());
        if (t==null) return false;
        
        t.use(block);
        return true;
    }
    
    public void selectChip(Circuit c) {
        if (c==null) return;
        ChatColor infoColor = rc.getPrefs().getInfoColor();
        Player p = getPlayer();
        if (selection.contains(c)) {
            selection.remove(c);
            p.sendMessage(infoColor + "Removed " + ChatColor.YELLOW + c.getChipString() + infoColor + " from selection.");
        } else {
            selection.add(c);
            p.sendMessage(infoColor + "Added " + ChatColor.YELLOW + c.getChipString() + infoColor + " to selection.");
        }
    }    
    
    public void clearCuboid() {
        if (mode==Mode.CUBOID_DEFINE) mode = Mode.NORMAL;
        cuboid = null;
    }

    public void setCuboid(Location[] cuboid) {
        this.cuboid = cuboid;
    }

    public Location[] getCuboid() {
        if (cuboid==null && WorldEditHook.isWorldEditInstalled(rc)) {
            cuboid = WorldEditHook.getWorldEditSelection(getPlayer(), rc);
        }
        return cuboid;
    }
    
    public void defineCuboid() {
        cuboid = new Location[2];
        mode = Mode.CUBOID_DEFINE;
    }

    public void addCuboidLocation(Location location) {
        Player p = getPlayer();
        
        if (cuboid[0]==null) {
            cuboid[0] = location;
            p.sendMessage(rc.getPrefs().getInfoColor() + "1st corner selected: " + location.getBlockX() + ", " + 
                    location.getBlockY() + ", " + location.getBlockZ());
        } else {
            cuboid[1] = location;
            
            selectChipsInCuboid(cuboid, false);
            p.sendMessage(rc.getPrefs().getInfoColor() + "2nd corner selected: " + location.getBlockX() + ", " + 
                    location.getBlockY() + ", " + location.getBlockZ());            
            p.sendMessage(rc.getPrefs().getInfoColor() + "Selected " + selection.size() + " active chips.");
            
            mode = Mode.NORMAL;
        }
    }
    
    public void selectChipsInCuboid(Location[] sel, boolean add) {
        List<Circuit> chips = findChipsInCuboid(sel, rc);
        if (add) selection.addAll(chips);
        else selection = chips;
    }
    
    public static List<Circuit> findChipsInCuboid(Location[] cuboid, RedstoneChips rc) {
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

        return result;
    }
    
}
