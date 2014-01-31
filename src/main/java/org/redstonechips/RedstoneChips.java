package org.redstonechips;

import org.redstonechips.circuit.CircuitLoader;
import org.redstonechips.wireless.ChannelManager;
import org.redstonechips.chip.ChipManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.circuit.CircuitIndex;
import org.redstonechips.command.*;
import org.redstonechips.memory.Memory;
import org.redstonechips.user.UserSession;

/**
 * RedstoneChips Bukkit JavaPlugin implementation. The main entry point of the plugin.
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {
    private ChipManager circuitManager;
    private ChannelManager channelManager;
    
    /** List of registered /rctype receivers. */
    public Map<Location, RCTypeReceiver> rcTypeReceivers = new HashMap<>();
    
    private final Map<String, UserSession> sessions = new HashMap<>();

    private static final List<CircuitIndex> preloadedLibs = new ArrayList<>();    
    
    /** All plugin commands */
    public RCCommand[] commands = new RCCommand[] {
        new RCactivate(), new RCarg(), new RCbreak(), new RCchannels(), new RCclasses(), new RCdebug(), new RCdestroy(),
        new RCfixioblocks(), new RChelp(), new RCinfo(), new RClist(), new RCpin(), new RCprefs(), new RCreset(),
        new RCtype(), new RCload(), new RCsave(), new RCp(), new RCprotect(), new RCtool(), new RCsend(),
        new RCname(), new RCenable(), new RCdisable(), new org.redstonechips.command.RedstoneChips(), new RCsel()
    };
    

    @Override
    public void onEnable() {
        RedstoneChips.instance = this;
        setupFolders();        
        initManagers();
        loadCircuitLibraries();        
        RCPrefs.loadPrefs();        
        getServer().getPluginManager().registerEvents(new RCBukkitEventHandler(this), this);
        registerCommands();
        for (CircuitIndex lib : CircuitLoader.getCircuitLibraries()) lib.onRedstoneChipsEnable(this);
        
        // delay some tasks until after the server startup is complete.
        getServer().getScheduler().runTaskLater(this, new Runnable() { 
            @Override
            public void run() { postStartup();}            
        }, 1);        
    }

    private void postStartup() {
        for (World w : getServer().getWorlds()) {
            if (!WorldsObserver.isWorldLoaded(w)) 
                RCPersistence.loadChipsOf(w);
        }
        
        RCPersistence.loadChannelsIfExists();
        
        log(Level.INFO, "Processing " + circuitManager.getAllChips().size() + " active chip(s).");
        
        if (RCPrefs.getCheckForUpdates()) {
            Runnable updater = new Runnable() {

                @Override
                public void run() {
                    String ver;
                    try {
                        ver = UpdateChecker.checkUpdate(getDescription().getVersion());
                    } catch (IOException ex) {
                        log(Level.WARNING, "Couldn't check for an update (" + ex.getClass().getSimpleName() + ").");
                        return;
                    }
                    if (ver!=null) {
                        log(Level.INFO, "A new RedstoneChips version (" + ver + ") is available.\n"
                                + "To download the update go to: http://eisental.github.com/RedstoneChips");
                    }
                }
            };
            getServer().getScheduler().runTaskAsynchronously(this, updater);
        }
        
    }
    
    @Override
    public void onDisable() {
        for (UserSession s : sessions.values())
            s.playerQuit();
        
        RCPersistence.saveAll();
        circuitManager.shutdownAllChips();
        WorldsObserver.clearLoadedWorldsList();
        RedstoneChips.instance = null;
    }

    private void initManagers() {
        try {
            RCPrefs.initialize();
        } catch (IOException ex) {
            log(Level.SEVERE, ex.toString());
        }   

        circuitManager = new ChipManager(this);
        channelManager = new ChannelManager(this);
    }                

    private void registerCommands() {
        for (RCCommand cmd : commands) {
            cmd.setRCInstance(this);
            getCommand(cmd.getClass().getSimpleName().toLowerCase()).setExecutor(cmd);
        }
    }
    
    private void loadCircuitLibraries() {
        for (CircuitIndex lib : preloadedLibs) {
            String libMsg = "Loading " + lib.getIndexName() + " " + lib.getVersion() + " > ";
            Class<? extends Circuit>[] classes = lib.getCircuitClasses();
            
            if (classes != null && classes.length>0) {
                for (Class c : classes)
                    libMsg += c.getSimpleName() + ", ";

                libMsg = libMsg.substring(0, libMsg.length()-2) + ".";
                getLogger().info(libMsg);

                CircuitLoader.addCircuitIndex(lib);
            } else {
                libMsg += "No circuit classes were loaded.";
                getLogger().info(libMsg);
            }
        }        
    }
    
    private void setupFolders() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        try {
            Memory.setupDataFolder(getDataFolder());
        } catch (IOException e) {
            log(Level.WARNING, e.getMessage());
        }
    }
    
    /** 
     * Sends a RedstoneChips log message to the console.
     * 
     * @param level
     * @param message
     */
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    /**
     * @return the circuit manager. The object responsible for creating and managing active circuits.
     */
    public ChipManager chipManager() { return circuitManager; }

    /**
     * @return the channel manager. The object responsible for handling wireless broadcast channels.
     */
    public ChannelManager channelManager() { return channelManager; }
        
    /**
     * Called by CircuitLibrary to register the library with the plugin.
     *
     * @param lib
     */
    public static void addCircuitLibrary(CircuitIndex lib) {
        preloadedLibs.add(lib);
    }
    
    /**
     * Returns the UserSession object tied to this username.
     * @param username The player name.
     * @param create Whether to create a new UserSession if none exists yet.
     * @return The player UserSession or null if none was found and create is false.
     */
    public UserSession getUserSession(String username, boolean create) {
        UserSession s = sessions.get(username);
        if (s==null && create) {
            s = new UserSession(username, this);
            sessions.put(username, s);
        } 
        
        return s;
    }
    
    /**
     * Returns the UserSession object tied to this player.
     * @param player A Player object.
     * @param create Whether to create a new UserSession if none exists yet.
     * @return The player UserSession or null if none was found and create is false.
     */
    public UserSession getUserSession(Player player, boolean create) {
        return getUserSession(player.getName(), create);
    }

    /**
     * Removes the UserSession for the specified player.
     * 
     * @param player
     * @return The UserSession object that was removed or null if it was not found.
     */
    public UserSession removeUserSession(Player player) {
        return removeUserSession(player.getName());
    }
    
    /**
     * Removes the UserSession for the specified username.
     * 
     * @param name
     * @return The UserSession object that was removed or null if it was not found.
     */
    public UserSession removeUserSession(String name) {
        return sessions.remove(name);
    }
    
    /**
     * Registers a typingBlock to be used by an RCTypeReceiver. When a player points towards the typingBlock and uses
     * the /rctype command the RCTypeReceiver will receive the typed text.
     * 
     * @param typingBlock The block to point towards while typing.
     * @param circuit The circuit that will receive the typed text.
     */
    public void addRCTypeReceiver(Location typingBlock, RCTypeReceiver circuit) {
        rcTypeReceivers.put(typingBlock, circuit);
    }

    /**
     * The RCTypeReceiver will no longer receive /rctype commands.
     * @param circuit The rcTypeReceiver to remove.
     */
    public void removeRCTypeReceiver(RCTypeReceiver circuit) {
        List<Location> toremove = new ArrayList<>();

        for (Location l : rcTypeReceivers.keySet()) {
            if (rcTypeReceivers.get(l)==circuit)
                toremove.add(l);
        }

        for (Location l : toremove)
            rcTypeReceivers.remove(l);
    }
            
    private static RedstoneChips instance;
    
    /**
     * 
     * @return The plugin instance or null if it is disabled.
     */
    public static RedstoneChips inst() { return instance; }    
}
