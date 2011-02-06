/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A Bukkit JavaPlugin implementation for circuit libraries. Provides invisible inter-plugin communication with RedstoneChips and
 * by implementing the CircuitIndex interface provides a mechanism to register new circuit classes.
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitLibrary extends JavaPlugin implements CircuitIndex {

    private static final String rcName = "RedstoneChips";

    /**
     * Local reference to the minecraft logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");

    /**
     * Local reference to the RedstoneChips plugin. It's value is set in the plugin's onEnable() method.
     */
    protected RedstoneChips redstoneChips;

    public CircuitLibrary(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        RedstoneChips.addCircuitLibrary(this);
    }

    @Override
    public void onDisable() {
        logger.info(getDescription().getName() + " " + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        Plugin p = getServer().getPluginManager().getPlugin(rcName);
        if (p==null) {
            logger.warning(getDescription().getName() + " " + getDescription().getVersion() + ": Required plugin " + rcName + " is missing.");
        }

        redstoneChips = (RedstoneChips)p;
    }

    @Override
    public void onRedstoneChipsEnable() { }
}
