package org.tal.redstonechips;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
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
    private CircuitLoader circuitLoader;
    private CommandHandler commandHandler;

    public RedstoneChips(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        prefsManager = new PrefsManager(this);
        circuitManager = new CircuitManager(this);
        circuitLoader = new CircuitLoader(this);
        commandHandler = new CommandHandler(this);
        
        rcBlockListener = new BlockListener() {

            @Override
            public void onBlockRedstoneChange(BlockFromToEvent event) {
                circuitManager.redstoneChange((BlockRedstoneEvent)event);
            }

            @Override
            public void onBlockRightClick(BlockRightClickEvent event) {
                circuitManager.checkForCircuit(event.getBlock(), event.getPlayer());
            }

            @Override
            public void onBlockDamage(BlockDamageEvent event) {
                if (event.getDamageLevel()==BlockDamageLevel.BROKEN)
                    circuitManager.checkCircuitDestroyed(event.getBlock(), event.getPlayer());
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
        circuitManager.saveCircuits();

        PluginDescriptionFile desc = this.getDescription();
        logg.info(desc.getName() + " " + desc.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        prefsManager.loadPrefs();
        circuitManager.loadCircuits();


        pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_RIGHTCLICKED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_DAMAGED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);

        PluginDescriptionFile desc = this.getDescription();
        logg.info(desc.getName() + " " + desc.getVersion() + " enabled.");
    }

    @Override
    public boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redchips-active")) {
            commandHandler.listActiveCircuits(player, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-classes")) {
            commandHandler.listCircuitClasses(player);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-prefs")) {
            commandHandler.prefsCommand(args, player);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-debug")) {
            commandHandler.debugCommand(player, args);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-pin")) {
            commandHandler.pinCommand(player);
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
}
