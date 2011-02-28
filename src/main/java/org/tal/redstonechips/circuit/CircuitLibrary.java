/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.RedstoneChips;

/**
 * A Bukkit JavaPlugin implementation for circuit libraries. Provides invisible inter-plugin communication with RedstoneChips and
 * by implementing the CircuitIndex interface provides a mechanism to register new circuit classes.
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitLibrary implements CircuitIndex {

    public static final String rcName = "RedstoneChips";

    /**
     * Local reference to the RedstoneChips plugin. It's value is set in the plugin's onEnable() method.
     */
    public RedstoneChips redstoneChips;

    public CircuitLibrary() {
        RedstoneChips.addCircuitLibrary(this);
    }

    @Override
    public void onRedstoneChipsEnable() { }

    @Override
    public void setRedstoneChipsInstance(RedstoneChips instance) {
        this.redstoneChips = instance;
    }
}
