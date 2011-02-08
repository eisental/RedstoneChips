package org.tal.redstonechips;

import org.tal.redstonechips.circuit.CircuitIndex;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


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

    public RedstoneChips(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        prefsManager = new PrefsManager(this);
        circuitManager = new CircuitManager(this);
        circuitPersistence = new CircuitPersistence(this);
        circuitLoader = new CircuitLoader(this);
        commandHandler = new CommandHandler(this);

        rcBlockListener = new BlockListener() {

            @Override
            public void onBlockRedstoneChange(BlockFromToEvent event) {
                if (!event.isCancelled());
                    circuitManager.redstoneChange((BlockRedstoneEvent)event);
            }

            @Override
            public void onBlockRightClick(BlockRightClickEvent event) {
                circuitManager.checkForCircuit(event.getBlock(), event.getPlayer());
            }

            @Override
            public void onBlockDamage(BlockDamageEvent event) {
                if (!event.isCancelled()) {
                    if (event.getDamageLevel()==BlockDamageLevel.BROKEN)
                        circuitManager.checkCircuitDestroyed(event.getBlock(), event.getPlayer());
                }
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
            public void onPlayerQuit(PlayerEvent event) {
                circuitManager.checkDebuggerQuit(event.getPlayer());
            }
        };

        rcWorldListener = new WorldListener() {

            @Override
            public void onChunkLoaded(ChunkLoadEvent event) {
                circuitManager.checkUpdateOutputLevers(event.getChunk());
            }

        };
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

    @Override
    public void onDisable() {
        log(Level.INFO, "Saving circuits state to file.");
        circuitPersistence.saveCircuits(circuitManager.getCircuits());
        circuitManager.destroyCircuits();

        PluginDescriptionFile desc = this.getDescription();
        logg.info(desc.getName() + " " + desc.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile desc = this.getDescription();
        logg.info(desc.getName() + " " + desc.getVersion() + " enabled.");

        // load circuit classes
        for (CircuitIndex lib : RedstoneChips.circuitLibraries) {
            lib.setRedstoneChipsInstance(this);
            
            String libMsg = desc.getName() + ": Loading " + lib.getClass().getSimpleName() + " > ";
            Class[] classes = lib.getCircuitClasses();
            if (classes != null && classes.length>0) {
                for (Class c : classes)
                    libMsg += c.getSimpleName() + ", ";

                logg.info(libMsg.substring(0, libMsg.length()-2) + ".");
                
                this.addCircuitClasses(classes);
            } else
                logg.info(libMsg + "No circuit classes were loaded.");
        }

        for (CircuitIndex lib : this.circuitLibraries) {
            lib.onRedstoneChipsEnable();
        }

        prefsManager.loadPrefs();
        circuitManager.setCircuitList(circuitPersistence.loadCircuits());

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_RIGHTCLICKED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_DAMAGED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_BURN, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_QUIT, rcPlayerListener, Priority.Monitor, this);
        pm.registerEvent(Type.CHUNK_LOADED, rcWorldListener, Priority.Monitor, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redchips-active")) {
            commandHandler.listActiveCircuits(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-classes")) {
            commandHandler.listCircuitClasses(sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-prefs")) {
            commandHandler.prefsCommand(args, sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-debug")) {
            commandHandler.debugCommand(sender, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-pin")) {
            commandHandler.pinCommand(sender);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-destroy")) {
            boolean enableDestroyCommand = (Boolean)prefsManager.getPrefs().get(PrefsManager.Prefs.enableDestroyCommand.name());
            if (enableDestroyCommand) {
                commandHandler.destroyCommand(sender);
            } else sender.sendMessage(prefsManager.getErrorColor()+"/redchips-destroy is disabled. You can enable it using /redchips-prefs enableDestroyCommand true");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-deactivate")) {
            commandHandler.deactivateCommand(sender, args);
            return true;
        } else return false;
    }

    void log(Level level, String message) {
        logg.log(level, this.getDescription().getName() + ": " + message);
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

}
