package org.tal.redstonechips.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.Circuit;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Tal Eisenberg
 */
public class UserSession {

    public enum Mode { SELECTION , NORMAL, CUBOID_DEFINE}
    
    private Player player;
    
    private Debugger debugger;
    private RedstoneChips rc;    
    private Map<Material, Tool> tools;
    private String username;
    private List<Circuit> selection;
    private Mode mode;
    private Location[] cuboid = null;
    private Map<String, Object> playerData;
    
    public UserSession(String username, RedstoneChips rc) {
        this.username = username;
        this.rc = rc;
        selection = new ArrayList<Circuit>();
        playerData = new HashMap<String, Object>();
        tools = new EnumMap<Material, Tool>(Material.class);
        
        player = rc.getServer().getPlayer(username);
        
        debugger = new ChatDebugger(this);
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

    public void playerQuit() {
        try {
            save();
        } catch (IOException ex) {
            rc.log(Level.WARNING, "Error while saving player file: " + ex.getMessage());
        }
        
        if (debugger!=null) debugger.clear();
        rc.removeUserSession(username);
        player = null;
    }

    public void playerJoined(Player p) {
        player = p;
        try {
            load();
        } catch (ClassNotFoundException ex) {
            rc.log(Level.INFO, "Error while loading player file: " + ex.getMessage());
        } catch (InstantiationException ex) {
            rc.log(Level.INFO, "Error while loading player file: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            rc.log(Level.INFO, "Error while loading player file: " + ex.getMessage());
        }
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

    public Debugger getDebugger() { return debugger; }
    public void setDebugger(Debugger d) { debugger = d; }
    
    public void putPlayerData(String key, Object data) {
        playerData.put(key, data);
    }
    
    public Object getPlayerData(String key) {
        return playerData.get(key);
    }

    public void save() throws IOException {
        DumperOptions opt = new DumperOptions();
        opt.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(opt);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("tools", saveTools());
        map.put("data", playerData);
        yaml.dump(map, new FileWriter(getPlayerFile()));        
    }
    
    public void load() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        File f = getPlayerFile();

        try {
            Yaml yaml = new Yaml();
            Map<String,Object> map = (Map<String, Object>)yaml.load(new FileInputStream(f));

            if (map.containsKey("tools")) loadTools((Map<Material, String>)map.get("tools"));
            if (map.containsKey("data")) playerData = (Map<String, Object>)map.get("data");
        } catch (IOException ex) {
        } 
        
    }
    
    private void loadTools(Map<Material, String> tools) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.tools.clear();
        for (Material m : tools.keySet()) {
            Tool t = (Tool)Class.forName(tools.get(m)).newInstance();
            t.setItem(m);
            t.setSession(this);
            this.tools.put(m, t);
        }
    }
    
    private Map<Material, String> saveTools() {
        Map<Material, String> map = new HashMap<Material, String>();
        for (Material m : tools.keySet()) {
            map.put(m, tools.get(m).getClass().getCanonicalName());
        }
        
        return map;
    }
    
    public File getPlayerFile() {
        return new File(rc.getDataFolder(), username + ".player");        
    }
    
    public static File getPlayerFileFor(String name, File folder) {
        return new File(folder, name + ".player");
    }
    
    public static File getPlayerFileFor(Player p, File folder) {
        return new File(folder, p.getName() + ".player");
    }
}
