package org.tal.redstonechips;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.tal.redstonechips.circuit.CircuitIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.channel.BroadcastChannel;
import org.tal.redstonechips.channel.ReceivingCircuit;
import org.tal.redstonechips.channel.TransmittingCircuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;
import org.tal.redstonechips.command.RCCommand;
import org.tal.redstonechips.command.RCactivate;
import org.tal.redstonechips.command.RCarg;
import org.tal.redstonechips.command.RCbreak;
import org.tal.redstonechips.command.RCchannels;
import org.tal.redstonechips.command.RCclasses;
import org.tal.redstonechips.command.RCdebug;
import org.tal.redstonechips.command.RCdestroy;
import org.tal.redstonechips.command.RCfixioblocks;
import org.tal.redstonechips.command.RChelp;
import org.tal.redstonechips.command.RCinfo;
import org.tal.redstonechips.command.RClist;
import org.tal.redstonechips.command.RCload;
import org.tal.redstonechips.command.RCp;
import org.tal.redstonechips.command.RCpin;
import org.tal.redstonechips.command.RCprefs;
import org.tal.redstonechips.command.RCreset;
import org.tal.redstonechips.command.RCsave;
import org.tal.redstonechips.command.RCsel;
import org.tal.redstonechips.command.RCtype;
import org.tal.redstonechips.command.RCprotect;
import org.tal.redstonechips.util.ChunkLocation;


/**
 * RedstoneChips Bukkit JavaPlugin implementation. The main entry point of the plugin.
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {
    private static final Logger logg = Logger.getLogger("Minecraft");
    
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

    public RCsel rcsel = new RCsel();
    public RClist rclist = new RClist();
    public RCCommand[] commands = new RCCommand[] {
        new RCactivate(), new RCarg(), new RCbreak(), new RCchannels(), new RCclasses(), new RCdebug(), new RCdestroy(),
        new RCfixioblocks(), new RChelp(), new RCinfo(), rclist, new RCpin(), new RCprefs(), new RCreset(), rcsel,
        new RCtype(), new RCload(), new RCsave(), new RCp(), new RCprotect(), new org.tal.redstonechips.command.RedstoneChips()
    };

    @Override
    public void onEnable() {
        prefsManager = new PrefsManager(this);
        circuitManager = new CircuitManager(this);
        circuitPersistence = new CircuitPersistence(this);
        circuitLoader = new CircuitLoader(this);

        PluginDescriptionFile desc = this.getDescription();

        // initalize registered circuit libraries giving them a reference to the plugin.
        for (CircuitIndex lib : circuitLibraries) {
            lib.onRedstoneChipsEnable(this);
        }

        // load circuit classes
        for (CircuitIndex lib : circuitLibraries) {
            String libMsg = desc.getName() + ": Loading " + lib.getName() + " " + lib.getVersion() + " > ";
            Class[] classes = lib.getCircuitClasses();
            if (classes != null && classes.length>0) {
                for (Class c : classes)
                    libMsg += c.getSimpleName() + ", ";

                libMsg = libMsg.substring(0, libMsg.length()-2) + ".";
                logg.info(libMsg);
                
                this.addCircuitClasses(classes);
            } else {
                libMsg += "No circuit classes were loaded.";
                logg.info(libMsg);
            }
        }

        prefsManager.loadPrefs();
        
        registerEvents();
        registerCommands();

        String msg = desc.getName() + " " + desc.getVersion() + " enabled.";
        logg.info(msg);

        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    circuitPersistence.loadCircuits();
                }})==-1) {

            log(Level.WARNING, "Couldn't schedule circuit loading. Multiworld support might not work.");
            circuitPersistence.loadCircuits();
        }

    }

    @Override
    public void onDisable() {
        circuitPersistence.saveCircuits();

        circuitManager.shutdownCircuits();

        PluginDescriptionFile desc = this.getDescription();
        String msg = desc.getName() + " " + desc.getVersion() + " disabled.";
        logg.info(msg);
    }

    private void registerEvents() {
        if (rcBlockListener==null) rcBlockListener = new BlockListener() {
            @Override
            public void onBlockRedstoneChange(BlockRedstoneEvent event) {
                circuitManager.redstoneChange(event);
            }

            @Override
            public void onBlockBreak(BlockBreakEvent event) {
                if (!event.isCancelled()) {
                    if (!circuitManager.checkCircuitDestroyed(event.getBlock(), event.getPlayer())) event.setCancelled(true);
                    circuitManager.checkCircuitInputChanged(event.getBlock(), event.getPlayer(), true);
                }
            }

            @Override
            public void onBlockPlace(BlockPlaceEvent event) {
                if (!event.isCancelled()) {
                    circuitManager.checkCircuitInputChanged(event.getBlock(), event.getPlayer(), false);
                }
            }

            @Override
            public void onBlockBurn(BlockBurnEvent event) {
                if (!event.isCancelled()) {
                    circuitManager.checkCircuitDestroyed(event.getBlock(), null);
                }
            }
        };

        if (rcEntityListener==null) rcEntityListener = new EntityListener() {

            @Override
            public void onEntityExplode(EntityExplodeEvent event) {
                if (event.isCancelled()) return;

                for (Block b : event.blockList())
                    circuitManager.checkCircuitDestroyed(b, null);
            }
        };

        if (rcPlayerListener==null) rcPlayerListener = new PlayerListener() {

            @Override
            public void onPlayerQuit(PlayerQuitEvent event) {
                circuitManager.checkDebuggerQuit(event.getPlayer());
            }

            @Override
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.isCancelled()) return;

                if ((event.getAction()==Action.LEFT_CLICK_BLOCK && !prefsManager.getRightClickToActivate()) ||
                        (event.getAction()==Action.RIGHT_CLICK_BLOCK && prefsManager.getRightClickToActivate()))
                    circuitManager.checkForCircuit(event.getClickedBlock(), event.getPlayer(),
                            prefsManager.getInputBlockType(), prefsManager.getOutputBlockType(), prefsManager.getInterfaceBlockType());

                if (event.getAction()==Action.RIGHT_CLICK_BLOCK && (!event.getPlayer().getItemInHand().getType().isBlock() || event.getPlayer().getItemInHand().getType()==Material.AIR)) {
                    rcsel.cuboidLocation(event.getPlayer(), event.getClickedBlock().getLocation());
                }
            }
        };

        if (rcWorldListener==null) rcWorldListener = new WorldListener() {

            @Override
            public void onChunkLoad(ChunkLoadEvent event) {
                circuitManager.updateOnChunkLoad(ChunkLocation.fromChunk(event.getChunk()));
            }

            @Override
            public void onChunkUnload(ChunkUnloadEvent event) {
                if (!event.isCancelled())
                    circuitManager.updateOnChunkUnload(ChunkLocation.fromChunk(event.getChunk()));
            }

            @Override
            public void onWorldSave(WorldSaveEvent event) {
                if (event.getWorld()==getServer().getWorlds().get(0)) {
                    circuitPersistence.saveCircuits();
                }
            }
        };

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
    }

    private void registerCommands() {
        for (RCCommand cmd : commands) {
            cmd.setRCInstance(this);
            getCommand(cmd.getClass().getSimpleName().toLowerCase()).setExecutor(cmd);
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

    public void log(Level level, String message) {
        String logMsg = this.getDescription().getName() + ": " + message;
        logg.log(level, logMsg);
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
    public BroadcastChannel registerReceiver(ReceivingCircuit r, String channelName) {
        BroadcastChannel channel = getChannelByName(channelName);
        channel.addReceiver(r);

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

    public boolean removeTransmitter(TransmittingCircuit t) {
        BroadcastChannel channel = t.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeTransmitter(t);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }

    public boolean removeReceiver(ReceivingCircuit r) {
        BroadcastChannel channel = r.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeReceiver(r);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }
}
