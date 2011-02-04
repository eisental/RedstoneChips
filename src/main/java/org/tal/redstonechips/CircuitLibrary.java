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
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitLibrary extends JavaPlugin implements CircuitIndex {
    public static final String rcName = "RedstoneChips";
    private static final Logger logger = Logger.getLogger("Minecraft");

    protected RedstoneChips redstoneChips;

    public CircuitLibrary(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
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

}
