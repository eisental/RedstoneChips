package org.redstonechips.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.Chip;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Manages player specific data.
 * @author Tal Eisenberg
 */
public class UserSession {

    /** User session operation mode */
    public enum Mode { 
        /** Chip selection by clicking on a chip block. */
        SELECTION , 
        
        /** User is defining a cuboid. */
        CUBOID_DEFINE,
        
        /** Normal operation mode */
        NORMAL
        
    }
    
    private Player player;
    
    private Debugger debugger;
    private final RedstoneChips rc;    
    private final Map<Material, Tool> tools;
    private final String username; // use uuid instead of username!!
    private List<Chip> selection;
    private Mode mode;
    private Location[] cuboid = null;
    private Map<String, Object> playerData;
    
    /**
     * 
     * @param username Player name.
     * @param rc a Plugin reference.
     */
    public UserSession(String username, RedstoneChips rc) {
        this.username = username;
        this.rc = rc;
        selection = new ArrayList<>();
        playerData = new HashMap<>();
        tools = new EnumMap<>(Material.class);
        
        player = rc.getServer().getPlayer(username);
        
        debugger = new ChatDebugger(this);
    }

    /**
     * 
     * @return the username of this session.
     */
    public String getUsername() { return username; }
    
    /**
     * 
     * @return The player this session points to.
     */
    public Player getPlayer() {
        if (player==null)
            return rc.getServer().getPlayer(username);
        else return player;
    }
    
    /**
     * Register a Tool. Tool.setItem() must be called before calling this method.
     * @param t 
     */
    public void addTool(Tool t) {
        t.setSession(this);
        tools.put(t.getItem(), t);
    }

    /**
     * @return All registered tools for this session.
     */
    public Map<Material, Tool> getTools() { return tools; }
        
    /**
     * @return Current session chip selection.
     */
    public List<Chip> getSelection() { return selection; }

    /**
     * Replaces any selected circuits with a new selection list.
     * 
     * @param selection a list of Circuits.
     */
    public void setSelection(List<Chip> selection) {
        this.selection = selection;
    }

    /**
     * @return Current player operation mode.
     */
    public Mode getMode() { return mode; }
    
    /**
     * @param m Sets player operation mode.
     */
    public void setMode(Mode m) {
        this.mode = m;
    }

    /**
     * Called when the player tied to this session quits the server. Player data is saved to file.
     */
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

    /**
     * Called when the session player joins the server. Player data is loaded from its player file if it exists.
     * @param p The new Player object of the session player.
     */
    public void playerJoined(Player p) {
        player = p;
        try {
            load();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            rc.log(Level.INFO, "Error while loading player file: " + ex.getMessage());
        }
    }
    
    /**
     * Uses the tool in the player's hand on the specified Block.
     * @param block used Block.
     * @return true if a Tool was used.
     */
    public boolean useToolInHand(Block block) {
        Player p = getPlayer();
        if (p==null) return false;
        Tool t = tools.get(p.getItemInHand().getType());
        if (t==null) return false;
        
        t.use(block);
        return true;
    }
    
    /**
     * Adds or removes a Circuit from the chip selection and notifies the player.
     * 
     * @param c a Circuit.
     */
    public void selectChip(Chip c) {
        if (c==null) return;
        ChatColor infoColor = RCPrefs.getInfoColor();
        Player p = getPlayer();
        if (selection.contains(c)) {
            selection.remove(c);
            p.sendMessage(infoColor + "Removed " + ChatColor.YELLOW + c.toString() + infoColor + " from selection.");
        } else {
            selection.add(c);
            p.sendMessage(infoColor + "Added " + ChatColor.YELLOW + c.toString() + infoColor + " to selection.");
        }
    }    
    
    /**
     * Clears the session cuboid and returns to Mode.NORMAL if the player was defining a cuboid.
     */
    public void clearCuboid() {
        if (mode==Mode.CUBOID_DEFINE) mode = Mode.NORMAL;
        cuboid = null;
    }

    /**
     * Sets the session cuboid.
     * 
     * @param cuboid Location array of two opposite cuboid corners.
     */
    public void setCuboid(Location[] cuboid) {
        this.cuboid = cuboid;
    }

    /**
     * @return The players WorldEdit selection cuboid if defined, the session cuboid if defined or null.
     */
    public Location[] getCuboid() {
        if (cuboid==null && WorldEditHook.isWorldEditInstalled(rc)) {
            cuboid = WorldEditHook.getWorldEditSelection(getPlayer(), rc);
        }
        return cuboid;
    }
    
    /**
     * Switch to Mode.CUBOID_DEFINE mode.
     */
    public void defineCuboid() {
        cuboid = new Location[2];
        mode = Mode.CUBOID_DEFINE;
    }

    /**
     * Adds a cuboid corner location. When the cuboid has two locations it's set as the session cuboid and
     * all chips inside it are added to the selection. the selection is cleared of any other chips.
     * 
     * @param location a Location.
     */
    public void addCuboidLocation(Location location) {
        Player p = getPlayer();
        
        if (cuboid[0]==null) {
            cuboid[0] = location;
            p.sendMessage(RCPrefs.getInfoColor() + "1st corner selected: " + location.getBlockX() + ", " + 
                    location.getBlockY() + ", " + location.getBlockZ());
        } else {
            cuboid[1] = location;
            
            selectChipsInCuboid(cuboid, false);
            p.sendMessage(RCPrefs.getInfoColor() + "2nd corner selected: " + location.getBlockX() + ", " + 
                    location.getBlockY() + ", " + location.getBlockZ());            
            p.sendMessage(RCPrefs.getInfoColor() + "Selected " + selection.size() + " active chips.");
            
            mode = Mode.NORMAL;
        }
    }
    
    /**
     * Select all chips inside a cuboid region.
     * @param sel an array with two opposite corner Locations of the cuboid.
     * @param add When true chips are added to the current selection, otherwise the previous selection is cleared.
     */
    public void selectChipsInCuboid(Location[] sel, boolean add) {
        List<Chip> chips = findChipsInCuboid(sel, rc);
        if (add) selection.addAll(chips);
        else selection = chips;
    }
    
    /**
     * 
     * @param cuboid an array with two opposite corner Locations of the cuboid.
     * @param rc Plugin reference.
     * @return a List of all Circuits found inside the cuboid.
     */
    public static List<Chip> findChipsInCuboid(Location[] cuboid, RedstoneChips rc) {
        int lowx = Math.min(cuboid[0].getBlockX(), cuboid[1].getBlockX());
        int highx = Math.max(cuboid[0].getBlockX(), cuboid[1].getBlockX());

        int lowy = Math.min(cuboid[0].getBlockY(), cuboid[1].getBlockY());
        int highy = Math.max(cuboid[0].getBlockY(), cuboid[1].getBlockY());

        int lowz = Math.min(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());
        int highz = Math.max(cuboid[0].getBlockZ(), cuboid[1].getBlockZ());

        List<Chip> result = new ArrayList<>();

        for (Chip c : rc.chipManager().getAllChips().values()) {
            Location l = c.activationBlock;
            if (l.getBlockX()>=lowx && l.getBlockX()<=highx
                    && l.getBlockY()>=lowy && l.getBlockY()<=highy
                    && l.getBlockZ()>=lowz && l.getBlockZ()<=highz) {
                result.add(c);
            }
        }

        return result;
    }

    /**
     * @return This session's debugger.
     */
    public Debugger getDebugger() { return debugger; }
    
    /**
     * Sets the session debugger.
     * 
     * @param d a Debugger.
     */
    public void setDebugger(Debugger d) { debugger = d; }
    
    /**
     * Adds custom persistent player data. Player data is saved when the player quits and reloaded when he 
     * rejoins the server.
     * 
     * @param key Map key.
     * @param data 
     */
    public void putPlayerData(String key, Object data) {
        playerData.put(key, data);
    }
    
    /**
     * @param key Map key.
     * @return Player data for this key or null.
     */
    public Object getPlayerData(String key) {
        return playerData.get(key);
    }

    /**
     * Save all session data to file. This currently includes tools settings and custom player data.
     * 
     * @throws IOException 
     */
    public void save() throws IOException {
        DumperOptions opt = new DumperOptions();
        opt.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(opt);
        Map<String, Object> map = new HashMap<>();
        map.put("tools", saveTools());
        map.put("data", playerData);
        yaml.dump(map, new FileWriter(getPlayerFile()));        
    }
    
    /**
     * Loads session data from file.
     * 
     * @throws InstantiationException When a tool can't be reconstructed.
     * @throws IllegalAccessException When a tool can't be reconstructed.
     * @throws ClassNotFoundException When a tool can't be reconstructed.
     */
    public void load() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        File f = getPlayerFile();

        try {
            Yaml yaml = new Yaml();
            Map<String,Object> map = (Map<String, Object>)yaml.load(new FileInputStream(f));

            if (map.containsKey("tools")) loadTools((Map<Material, String>)map.get("tools"));
            if (map.containsKey("data")) playerData = (Map<String, Object>)map.get("data");
        } catch (FileNotFoundException ex) {
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
        Map<Material, String> map = new EnumMap<>(Material.class);
        for (Material m : tools.keySet()) {
            map.put(m, tools.get(m).getClass().getCanonicalName());
        }
        
        return map;
    }
    
    /**
     * @return the File that this session saves data to.
     */
    public File getPlayerFile() {
        return new File(rc.getDataFolder(), username + ".player");        
    }
    
    /**
     * @param name a player name.
     * @param folder Where player files are stored.
     * @return The session file for this player name.
     */
    public static File getPlayerFileFor(String name, File folder) {
        return new File(folder, name + ".player");
    }
    
    /**
     * @param p a Player.
     * @param folder Where player files are stored.
     * @return The session file for this Player.
     */
    public static File getPlayerFileFor(Player p, File folder) {
        return new File(folder, p.getName() + ".player");
    }
    
    @Override
    public String toString() {
        return "<UserSession: " + this.getUsername() + " mode=" + this.getMode() + " data=" + this.playerData + " selection=" + this.getSelection() + ">";
    }
}
