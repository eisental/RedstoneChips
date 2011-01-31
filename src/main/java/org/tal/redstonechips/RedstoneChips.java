package org.tal.redstonechips;

import java.io.File;
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
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {
    private static final Logger logg = Logger.getLogger("Minecraft");

    private BlockListener rcBlockListener;
    private EntityListener rcEntityListener;

    private PrefsManager prefsManager;
    private CircuitManager circuitManager;
    private CircuitPersistence circuitPersistence;
    private CircuitLoader circuitLoader;
    private CommandHandler commandHandler;

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
    }

    @Override
    public void onDisable() {
        circuitPersistence.saveCircuits(circuitManager.getCircuits());
        circuitManager.destroyCircuits();

        PluginDescriptionFile desc = this.getDescription();
        logg.info(desc.getName() + " " + desc.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        prefsManager.loadPrefs();
        circuitManager.setCircuitList(circuitPersistence.loadCircuits());


        pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_RIGHTCLICKED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_DAMAGED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_BURN, rcBlockListener, Priority.Monitor, this);

        PluginDescriptionFile desc = this.getDescription();
        logg.info(desc.getName() + " " + desc.getVersion() + " enabled.");
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
        } else return false;
    }

    public void addCircuitClasses(Class... circuitClasses) {
        for (Class c : circuitClasses) circuitLoader.addCircuitClass(c);
    }

    void log(Level level, String message) {
        logg.log(level, this.getDescription().getName() + ": " + message);
    }

    public PrefsManager getPrefsManager() {
        return prefsManager;
    }

    public CircuitLoader getCircuitLoader() {
        return circuitLoader;
    }

    public CircuitManager getCircuitManager() {
        return circuitManager;
    }

    CircuitPersistence getCircuitPersistence() {
        return circuitPersistence;
    }
}
