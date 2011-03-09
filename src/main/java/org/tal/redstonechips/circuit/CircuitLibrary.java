/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.RedstoneChips;

/**
 * A Bukkit JavaPlugin implementation for circuit libraries. Provides invisible inter-plugin communication with RedstoneChips and
 * by implementing the CircuitIndex interface provides a mechanism to register new circuit classes.
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitLibrary extends JavaPlugin implements CircuitIndex {
    public static final Logger logger = Logger.getLogger("Minecraft");

    /**
     * Local reference to the RedstoneChips plugin. It's value is set in the plugin's onEnable() method.
     */
    public RedstoneChips redstoneChips;

    public CircuitLibrary() {
        RedstoneChips.addCircuitLibrary(this);
    }

    @Override
    public void setRedstoneChipsInstance(RedstoneChips instance) {
        this.redstoneChips = instance;
    }


    @Override
    public void onRedstoneChipsEnable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {

    }
}
