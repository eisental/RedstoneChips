package org.tal.redstonechips;

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
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.CircuitIndex;
import org.tal.redstonechips.circuit.RCTypeReceiver;
import org.tal.redstonechips.command.*;
import org.tal.redstonechips.memory.Memory;
import org.tal.redstonechips.user.UserSession;

/**
 * RedstoneChips Bukkit JavaPlugin implementation. The main entry point of the plugin.
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {
    
    private static final List<CircuitIndex> preloadedLibs = new ArrayList<CircuitIndex>();
    
    private PrefsManager prefsManager;
    private CircuitManager circuitManager;
    private CircuitPersistence circuitPersistence;
    private CircuitLoader circuitLoader;
    private ChannelManager channelManager;
    
    /** List of registered /rctype receivers. */
    public Map<Location, RCTypeReceiver> rcTypeReceivers = new HashMap<Location, RCTypeReceiver>();
    
    private final Map<String, UserSession> sessions = new HashMap<String, UserSession>();

    /** All plugin commands */
    public RCCommand[] commands = new RCCommand[] {
        new RCactivate(), new RCarg(), new RCbreak(), new RCchannels(), new RCclasses(), new RCdebug(), new RCdestroy(),
        new RCfixioblocks(), new RChelp(), new RCinfo(), new RClist(), new RCpin(), new RCprefs(), new RCreset(),
        new RCtype(), new RCload(), new RCsave(), new RCp(), new RCprotect(), new RCtool(), new RCsend(),
        new RCname(), new RCenable(), new RCdisable(), new org.tal.redstonechips.command.RedstoneChips(), new RCsel()
    };

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        initManagers();
        loadLibraries();
        notifyEnabledToLibraries();
        prefsManager.loadPrefs();        
        getServer().getPluginManager().registerEvents(new RCBukkitEventHandler(this), this);

        registerCommands();

        try {
            Memory.setupDataFolder(getDataFolder());
        } catch (RuntimeException e) {
            log(Level.WARNING, e.getMessage());
        }
        
        // delay some tasks until after the server startup is complete.
        getServer().getScheduler().runTaskLater(this, new Runnable() { 
            @Override
            public void run() { postStartup();}            
        }, 1);
    }

    private void postStartup() {
        if (!circuitPersistence.loadOldFile()) {
            for (World w : getServer().getWorlds()) {
                if (!circuitPersistence.isWorldLoaded(w)) 
                    circuitPersistence.loadCircuits(w);
            }
        }
        
        circuitPersistence.loadChannels();
        log(Level.INFO, "Processing " + circuitManager.getCircuits().size() + " active chip(s).");
        
        if (prefsManager.getCheckForUpdates()) {
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
        
        circuitPersistence.saveCircuits();
        circuitManager.shutdownAllCircuits();
        circuitPersistence.clearLoadedWorldsList();        
    }

    private void initManagers() {
        prefsManager = new PrefsManager(this);
        circuitManager = new CircuitManager(this);
        circuitPersistence = new CircuitPersistence(this);
        circuitLoader = new CircuitLoader(this);        
        channelManager = new ChannelManager(this);
    }                

    private void registerCommands() {
        for (RCCommand cmd : commands) {
            cmd.setRCInstance(this);
            getCommand(cmd.getClass().getSimpleName().toLowerCase()).setExecutor(cmd);
        }
    }
    
    /**
     * Called by CircuitLibrary to register the library with the plugin.
     *
     * @param lib
     */
    public static void addCircuitLibrary(CircuitIndex lib) {
        preloadedLibs.add(lib);
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
     * @return the preference manager. The object responsible for loading, saving and editing the plugin preferences.
     */
    public PrefsManager getPrefs() {
        return prefsManager;
    }

    /**
     * @return the circuit loader. The object responsible for creating new instances of Circuit classes.
     */
    public CircuitLoader getCircuitLoader() {
        return circuitLoader;
    }

    /**
     * @return the circuit manager. The object responsible for creating and managing active circuits.
     */
    public CircuitManager getCircuitManager() {
        return circuitManager;
    }

    /**
     * @return the channel manager. The object responsible for handling wireless broadcast channels.
     */
    public ChannelManager getChannelManager() {
        return channelManager;
    }
    
    /**
     * @return the circuit persistence handler. The object responsible for saving and loading the active circuit list from storage.
     */
    public CircuitPersistence getCircuitPersistence() {
        return circuitPersistence;
    }

    /**
     * Returns the UserSession object tied to this username.
     * @param username The player name.
     * @param create Whether to create a new UserSession if none exists yet or not.
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
     * @param player The requested UserSession player.
     * @param create Whether to create a new UserSession if none exists yet or not.
     * @return The player UserSession or null if none was found and create if false.
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
        List<Location> toremove = new ArrayList<Location>();

        for (Location l : rcTypeReceivers.keySet()) {
            if (rcTypeReceivers.get(l)==circuit)
                toremove.add(l);
        }

        for (Location l : toremove)
            rcTypeReceivers.remove(l);
    }
        
    private void loadLibraries() {
        for (CircuitIndex lib : preloadedLibs) {
            String libMsg = "Loading " + lib.getIndexName() + " " + lib.getVersion() + " > ";
            Class<? extends Circuit>[] classes = lib.getCircuitClasses();
            
            if (classes != null && classes.length>0) {
                for (Class c : classes)
                    libMsg += c.getSimpleName() + ", ";

                libMsg = libMsg.substring(0, libMsg.length()-2) + ".";
                getLogger().info(libMsg);

                circuitLoader.addCircuitIndex(lib);
            } else {
                libMsg += "No circuit classes were loaded.";
                getLogger().info(libMsg);
            }
        }        
    }

    private void notifyEnabledToLibraries() {
        for (CircuitIndex lib : circuitLoader.getCircuitLibraries()) {
            lib.onRedstoneChipsEnable(this);
        }        
    }
}
