package org.tal.redstonechips;

import org.tal.redstonechips.circuit.CircuitIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.channel.*;
import org.tal.redstonechips.command.*;

/**
 * RedstoneChips Bukkit JavaPlugin implementation. The main entry point of the plugin.
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {
    
    private static final Logger log = Logger.getLogger("Minecraft");
    
    private BlockListener rcBlockListener;
    private EntityListener rcEntityListener;
    private PlayerListener rcPlayerListener;
    private WorldListener rcWorldListener;

    private PrefsManager prefsManager;
    private CircuitManager circuitManager;
    private CircuitPersistence circuitPersistence;
    private CircuitLoader circuitLoader;

    public static List<CircuitIndex> circuitLibraries = new ArrayList<CircuitIndex>();

    public Map<Location, rcTypeReceiver> rcTypeReceivers = new HashMap<Location, rcTypeReceiver>();
    public Map<String, BroadcastChannel> broadcastChannels = new HashMap<String, BroadcastChannel>();
    private Map<String, Material> playerChipProbe = new HashMap<String, Material>();
    
    public RCsel rcsel = new RCsel();
    
    public RCCommand[] commands = new RCCommand[] {
        new RCactivate(), new RCarg(), new RCbreak(), new RCchannels(), new RCclasses(), new RCdebug(), new RCdestroy(),
        new RCfixioblocks(), new RChelp(), new RCinfo(), new RClist(), new RCpin(), new RCprefs(), new RCreset(), rcsel,
        new RCtype(), new RCload(), new RCsave(), new RCp(), new RCprotect(), new RCtool(), 
        new RCname(), new RCenable(), new RCdisable(), new org.tal.redstonechips.command.RedstoneChips()
    };

    @Override
    public void onEnable() {
        initManagers();
        callLibraryRedstoneChipsEnable();
        loadClasses();
        prefsManager.loadPrefs();        
        registerEvents();
        registerCommands();

        String msg = getDescription().getName() + " " + getDescription().getVersion() + " enabled.";
        log.info(msg);

        // schedule loading channel and old circuits file (if exists) until after server startup is complete.
        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() { postStartup();} })==-1) {

            // couldn't schedule task. Try running it before server startup is finished (could fail).
            postStartup();
        }

    }

    private void postStartup() {
        if (!circuitPersistence.loadOldFile()) {
            for (World w : getServer().getWorlds()) {
                if (!circuitPersistence.isWorldChipLoaded(w)) 
                    circuitPersistence.loadCircuits(w);
            }
        }
        
        circuitPersistence.loadChannels();
        log(Level.INFO, (isEnabled()?"Enabled.":"Disabled.") + " Running " + circuitManager.getCircuits().size() + " active chip(s).");        
    }
    
    @Override
    public void onDisable() {
        circuitPersistence.saveCircuits();
        circuitManager.shutdownAllCircuits();
        circuitPersistence.clearLoadedWorldsList();
        
        String msg = getDescription().getName() + " " + getDescription().getVersion() + " disabled.";
        log.info(msg);
    }

    private void initManagers() {
        prefsManager = new PrefsManager(this);
        circuitManager = new CircuitManager(this);
        circuitPersistence = new CircuitPersistence(this);
        circuitLoader = new CircuitLoader(this);        
    }                

    private void registerCommands() {
        for (RCCommand cmd : commands) {
            cmd.setRCInstance(this);
            getCommand(cmd.getClass().getSimpleName().toLowerCase()).setExecutor(cmd);
        }
    }
    
    private void registerEvents() {
        if (rcBlockListener==null) rcBlockListener = new RCBlockListener(this);
        if (rcEntityListener==null) rcEntityListener = new RCEntityListener(this);
        if (rcPlayerListener==null) rcPlayerListener = new RCPlayerListener(this);
        if (rcWorldListener==null) rcWorldListener = new RCWorldListener(this);

        if (this.isEnabled()) {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
            pm.registerEvent(Type.BLOCK_BREAK, rcBlockListener, Priority.Monitor, this);
            pm.registerEvent(Type.BLOCK_PLACE, rcBlockListener, Priority.Monitor, this);
            pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);
            pm.registerEvent(Type.BLOCK_BURN, rcBlockListener, Priority.Monitor, this);
            pm.registerEvent(Type.PLAYER_QUIT, rcPlayerListener, Priority.Monitor, this);
            pm.registerEvent(Type.PLAYER_INTERACT, rcPlayerListener, Priority.Monitor, this);
            pm.registerEvent(Type.CHUNK_LOAD, rcWorldListener, Priority.Monitor, this);
            pm.registerEvent(Type.CHUNK_UNLOAD, rcWorldListener, Priority.Monitor, this);
            pm.registerEvent(Type.WORLD_SAVE, rcWorldListener, Priority.Monitor, this);
            pm.registerEvent(Type.WORLD_LOAD, rcWorldListener, Priority.Monitor, this);
            pm.registerEvent(Type.WORLD_UNLOAD, rcWorldListener, Priority.Monitor, this);
        }
    }

    /**
     * Tells the plugin to add a list of circuit classes to the circuit loader.
     * @param circuitClasses An array of Class objects that extend the Circuit class.
     */
    public void addCircuitClasses(Class... circuitClasses) {
        for (Class c : circuitClasses) circuitLoader.addCircuitClass(c);
    }

    /**
     * Removes a list of circuit classes from the circuit loader.
     * @param circuitClasses An array of Class objects to be removed.
     */
    public void removeCircuitClasses(Class... circuitClasses) {
        for (Class c : circuitClasses) circuitLoader.removeCircuitClass(c);
    }

    /**
     * Tells the plugin to load circuit classes from this circuit library when enabled.
     *
     * @param lib Any object implementing the CircuitIndex interface.
     */
    public static void addCircuitLibrary(CircuitIndex lib) {
        circuitLibraries.add(lib);
    }

    /** 
     * Sends a RedstoneChips log message to the console.
     * 
     * @param level
     * @param message 
     */
    public void log(Level level, String message) {
        String logMsg = "[" + this.getDescription().getName() + "] " + message;
        log.log(level, logMsg);
    }

    /**
     * Returns the plugin's preference manager. The object responsible for loading, saving and editing the plugin preferences.
     * @return A reference to the plugin's PrefsManager object.
     */
    public PrefsManager getPrefs() {
        return prefsManager;
    }

    /**
     * Returns the plugin's circuit loader. The object responsible for creating new instances of Circuit classes.
     * @return A reference to the plugin's CircuitLoader object.
     */
    public CircuitLoader getCircuitLoader() {
        return circuitLoader;
    }

    /**
     * Returns the plugin's circuit manager. The object responsible for creating and managing active circuits.
     * @return A reference to the plugin's CircuitManager object.
     */
    public CircuitManager getCircuitManager() {
        return circuitManager;
    }

    /**
     * Returns the plugin's circuit loader. The object responsible for saving and loading the active circuit list from storage.
     * @return A reference to the plugin's CircuitPresistence object.
     */
    public CircuitPersistence getCircuitPersistence() {
        return circuitPersistence;
    }

    /**
     * Registers a typingBlock to be used by the rcTypeReceiver. When a player points towards the typingBlock and uses
     * the /rctype command the rcTypeReceiver circuit will receive the typed text.
     * 
     * @param typingBlock The block to point towards while typing.
     * @param circuit The circuit that will receive the typed text.
     */
    public void registerRcTypeReceiver(Location typingBlock, rcTypeReceiver circuit) {
        rcTypeReceivers.put(typingBlock, circuit);
    }

    /**
     * The rcTypeReceiver will no longer receive /rctype commands.
     * @param circuit The rcTypeReceiver to remove.
     */
    public void removeRcTypeReceiver(rcTypeReceiver circuit) {
        List<Location> toremove = new ArrayList<Location>();

        for (Location l : rcTypeReceivers.keySet()) {
            if (rcTypeReceivers.get(l)==circuit)
                toremove.add(l);
        }

        for (Location l : toremove)
            rcTypeReceivers.remove(l);
    }

    /**
     * Adds the receiving circuit to listen on a channel and returns the BroadcastChannel object that the receiver
     * was added to. If a BroadcastChannel by that name was not found a new one is created.
     * 
     * @param r The receiving circuit.
     * @param channelName Name of the receiver's channel.
     * @return The channel that the receiver was added to.
     */
    public BroadcastChannel registerReceiver(final ReceivingCircuit r, String channelName) {
        final BroadcastChannel channel = getChannelByName(channelName);
        channel.addReceiver(r);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                channel.sendAllForReceiver(r);
            }
        });

        return channel;
    }

    /**
     * Adds the transmitter circuit to a channel and returns the BroadcastChannel object that the transmitter
     * was added to. If a BroadcastChannel by that name was not found a new one is created.
     *
     * @param r The receiving circuit.
     * @param channelName Name of the receiver's channel.
     * @return The channel that the receiver was added to.
     */
    public BroadcastChannel registerTransmitter(TransmittingCircuit t, String channelName) {
        BroadcastChannel channel = getChannelByName(channelName);
        channel.addTransmitter(t);

        return channel;
    }

    /**
     * Removes this transmitter from the list and removes its channel if it's deserted.
     * 
     * @param t a transmitting circuit.
     * @return true if the transmitter was actually removed.
     */
    public boolean removeTransmitter(TransmittingCircuit t) {
        BroadcastChannel channel = t.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeTransmitter(t);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }

    /**
     * Removes this receiver from the list and removes its channel if it's deserted.
     * 
     * @param r a receiving circuit.
     * @return true if the receiver was actually removed.
     */    
    public boolean removeReceiver(ReceivingCircuit r) {
        BroadcastChannel channel = r.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeReceiver(r);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }

    /**
     * If the named channel doesn't exist, a new channel is created.
     * 
     * @param Channel name
     * @return a BroadcastChannel instance representing the named channel. 
     */
    public BroadcastChannel getChannelByName(String name) {
        BroadcastChannel channel;
        if (broadcastChannels.containsKey(name))
            channel = broadcastChannels.get(name);
        else {
            channel = new BroadcastChannel(name);
            broadcastChannels.put(name, channel);
        }

        return channel;
    }
    
    /**
     * Sets the chip probe item for this player
     * 
     * @param player 
     * @param item 
     */
    public void setChipProbe(Player player, Material item) {
        if (item.isBlock()) throw new IllegalArgumentException("Blocks can't be used as a chip probe.");
        playerChipProbe.put(player.getName(), item);
    }

    /**
     * @return a list containing player names and their respective chip probe material, if one is defined.
     */
    public Map<String, Material> getPlayerChipProbe() {
        return playerChipProbe;
    }
    
    /**
     * Prints a chip block info. 
     * When block points to a chip pin, the player receives an /rcpin message of this pin.
     * When block points to an activation block, debug mode is toggled for this player.
     * When block points to any other structure block the chip info is sent.
     * 
     * @param player The player to send the info message to.
     * @param block Queried block.
     */
    public void probeChipBlock(Player player, Block block) {
        try {
            RCpin.printPinInfo(block, player, this);
        } catch (IllegalArgumentException ie) {
            // not probing a pin
            Circuit c = circuitManager.getCircuitByStructureBlock(block);
            
            if (c!=null) {
                if (c.activationBlock.equals(block.getLocation()))
                    player.performCommand("rcdebug");
                else RCinfo.printCircuitInfo(player, c, this);
            }

        }
    }

    /**
     * @return Running instance of the /rcsel command.
     */
    public RCsel getRCsel() {
        return rcsel;
    }
        
    private void loadClasses() {
        String prefix = "[" + getDescription().getName() + "] Loading ";
        
        for (CircuitIndex lib : circuitLibraries) {
            String libMsg = prefix + lib.getName() + " " + lib.getVersion() + " > ";
            Class[] classes = lib.getCircuitClasses();
            
            if (classes != null && classes.length>0) {
                for (Class c : classes)
                    libMsg += c.getSimpleName() + ", ";

                libMsg = libMsg.substring(0, libMsg.length()-2) + ".";
                log.info(libMsg);
                
                this.addCircuitClasses(classes);
            } else {
                libMsg += "No circuit classes were loaded.";
                log.info(libMsg);
            }
        }        
    }

    private void callLibraryRedstoneChipsEnable() {
        for (CircuitIndex lib : circuitLibraries) {
            lib.onRedstoneChipsEnable(this);
        }        
    }
}
