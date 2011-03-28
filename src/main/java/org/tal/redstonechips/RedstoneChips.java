package org.tal.redstonechips;

import java.io.File;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.tal.redstonechips.circuit.CircuitIndex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.tal.redstonechips.circuit.ReceivingCircuit;
import org.tal.redstonechips.circuit.TransmittingCircuit;
import org.tal.redstonechips.circuit.rcTypeReceiver;


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
    private CommandHandler commandHandler;

    private static List<CircuitIndex> circuitLibraries = new ArrayList<CircuitIndex>();

    public Map<Location, rcTypeReceiver> rcTypeReceivers = new HashMap<Location, rcTypeReceiver>();
    public List<TransmittingCircuit> transmitters = new ArrayList<TransmittingCircuit>();
    public List<ReceivingCircuit> receivers = new ArrayList<ReceivingCircuit>();

    /**
     * Used to prevent saving state more than once per game tick.
     */
    private boolean dontSaveCircuits = false;

    private Runnable dontSaveCircuitsReset = new Runnable() {
        @Override
        public void run() {
            dontSaveCircuits = false;
        }
    };

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

    @Override
    public void onDisable() {
        saveCircuits();

        circuitManager.destroyCircuits();

        PluginDescriptionFile desc = this.getDescription();
        String msg = desc.getName() + " " + desc.getVersion() + " disabled.";
        logg.info(msg);
    }

    @Override
    public void onEnable() {
        prefsManager = new PrefsManager(this);
        circuitManager = new CircuitManager(this);
        circuitPersistence = new CircuitPersistence(this);
        circuitLoader = new CircuitLoader(this);
        commandHandler = new CommandHandler(this);

        rcBlockListener = new BlockListener() {
            @Override
            public void onBlockRedstoneChange(BlockRedstoneEvent event) {
                circuitManager.redstoneChange(event);
            }

            @Override
            public void onBlockBreak(BlockBreakEvent event) {
                if (!event.isCancelled())
                    circuitManager.checkCircuitDestroyed(event.getBlock(), event.getPlayer());
            }

            @Override
            public void onBlockBurn(BlockBurnEvent event) {
                if (!event.isCancelled()) {
                    circuitManager.checkCircuitDestroyed(event.getBlock(), null);
                }
            }
        };

        rcEntityListener = new EntityListener() {

            @Override
            public void onEntityExplode(EntityExplodeEvent event) {
                for (Block b : event.blockList())
                    circuitManager.checkCircuitDestroyed(b, null);
            }
        };

        rcPlayerListener = new PlayerListener() {

            @Override
            public void onPlayerQuit(PlayerQuitEvent event) {
                circuitManager.checkDebuggerQuit(event.getPlayer());
            }

            @Override
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getAction()==Action.RIGHT_CLICK_BLOCK)
                    circuitManager.checkForCircuit(event.getClickedBlock(), event.getPlayer());
            }
        };

        rcWorldListener = new WorldListener() {

            @Override
            public void onChunkLoad(ChunkLoadEvent event) {
                circuitManager.checkUpdateOutputLevers(event.getChunk());
            }

            @Override
            public void onWorldSave(WorldSaveEvent event) {
                if (event.getWorld()==getServer().getWorlds().get(0)) {
                    saveCircuits();
                }
            }
        };

        PluginDescriptionFile desc = this.getDescription();

        // initalize registered circuit libraries giving them a reference to the plugin.
        for (CircuitIndex lib : circuitLibraries) {
            lib.onRedstoneChipsEnable(this);
        }

        // load circuit classes
        for (CircuitIndex lib : circuitLibraries) {
            String libMsg = desc.getName() + ": Loading " + lib.getClass().getSimpleName() + " > ";
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

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_BREAK, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_BURN, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_QUIT, rcPlayerListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_INTERACT, rcPlayerListener, Priority.Monitor, this);
        pm.registerEvent(Type.CHUNK_LOAD, rcWorldListener, Priority.Monitor, this);
        pm.registerEvent(Type.WORLD_SAVE, rcWorldListener, Priority.Monitor, this);

        String msg = desc.getName() + " " + desc.getVersion() + " enabled.";
        logg.info(msg);

        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                circuitManager.setCircuitMap(circuitPersistence.loadCircuits());
            }})==-1) {
            logg.warning("Couldn't schedule circuit loading. Multiworld support might not work.");
            circuitManager.setCircuitMap(circuitPersistence.loadCircuits());
        }

    }

    public void saveCircuits() {
        if (!dontSaveCircuits) {
            log(Level.INFO, "Saving circuits state to file.");
            dontSaveCircuits = true;
            circuitPersistence.saveCircuits(circuitManager.getCircuits());
            getServer().getScheduler().scheduleSyncDelayedTask(this, dontSaveCircuitsReset, 1);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rc-list")) {
            commandHandler.listActiveCircuits(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-classes")) {
            commandHandler.listCircuitClasses(sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-prefs")) {
            commandHandler.prefsCommand(args, sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-debug")) {
            commandHandler.debugCommand(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-pin")) {
            commandHandler.pinCommand(sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-destroy")) {
            boolean enableDestroyCommand = (Boolean)prefsManager.getPrefs().get(PrefsManager.Prefs.enableDestroyCommand.name());
            if (enableDestroyCommand) {
                commandHandler.destroyCommand(sender);
            } else sender.sendMessage(prefsManager.getErrorColor()+"/rc-destroy is disabled. You can enable it using /rc-prefs enableDestroyCommand true");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-break")) {
            commandHandler.deactivateCommand(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-type")) {
            commandHandler.handleRcType(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-reset")) {
            commandHandler.resetCircuit(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-info")) {
            commandHandler.printCircuitInfo(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-channels")) {
            commandHandler.listBroadcastChannels(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-save")) {
            if (sender.isOp())
                saveCircuits();
            else sender.sendMessage(prefsManager.getErrorColor() + "Only ops (admins) are allowed to use this command.");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-load")) {
            if (sender.isOp())
                circuitManager.setCircuitMap(circuitPersistence.loadCircuits());
            else sender.sendMessage(prefsManager.getErrorColor() + "Only ops (admins) are allowed to use this command.");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("rc-help")) {
            commandHandler.commandHelp(sender, args);
            return true;
        } else return false;
    }

    void log(Level level, String message) {
        String logMsg = this.getDescription().getName() + ": " + message;
        logg.log(level, logMsg);
    }

    /**
     * Returns the plugin's preference manager. The object responsible for loading, saving and editing the plugin preferences.
     * @return A reference to the plugin's PrefsManager object.
     */
    public PrefsManager getPrefsManager() {
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
    CircuitPersistence getCircuitPersistence() {
        return circuitPersistence;
    }

    /**
     * Registers a typingBlock to be used by the rcTypeReceiver. When a player points towards the typingBlock and uses
     * the /rc-type command the rcTypeReceiver circuit will receive the typed text.
     * 
     * @param typingBlock The block to point towards while typing.
     * @param circuit The circuit that will receive the typed text.
     */
    public void registerRcTypeReceiver(Location typingBlock, rcTypeReceiver circuit) {
        rcTypeReceivers.put(typingBlock, circuit);
    }

    /**
     * The rcTypeReceiver will no longer receive /rc-type commands.
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
     * Registers a ReceivingCircuit so other transmitters can find it.
     *
     * @param r A ReceivingCircuit
     */
    public void addReceiver(ReceivingCircuit r) {
        receivers.add(r);

        // find already existing transmitters for this channel
        for (TransmittingCircuit t : transmitters) {
            if (t.getChannel()!=null && t.getChannel().equals(r.getChannel())) t.addReceiver(r);
        }

    }

    /**
     * Removes the ReceivingCircuit from the receiver list.
     *
     * @param r A ReceivingCircuit
     */
    public void removeReceiver(ReceivingCircuit r) {
        receivers.remove(r);
    }

    /**
     *
     * @return a List of all transmitters on the server.
     */
    public List<TransmittingCircuit> getTransmitters() {
        return transmitters;
    }

    /**
     *
     * @return a List of all receivers on the server.
     */
    public List<ReceivingCircuit> getReceivers() {
        return receivers;
    }

    /**
     * Registers a TransmittingCircuit so other receivers can fin
     * @param t
     */
    public void addTransmitter(TransmittingCircuit t) {
        transmitters.add(t);
    }

    public void removeTransmitter(TransmittingCircuit t) {
        transmitters.remove(t);
    }

    @Override
    public File getDataFolder() {
        return new File(super.getDataFolder().getParentFile(), getDescription().getName());
    }

}
