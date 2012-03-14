
package org.tal.redstonechips.circuit;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.tal.redstonechips.RedstoneChips;

/**
 * A JavaPlugin implementation for circuit libraries. Provides invisible inter-plugin communication with RedstoneChips and,
 * by implementing the CircuitIndex interface provides a mechanism to register new circuit classes.
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitLibrary extends JavaPlugin implements CircuitIndex {
    @Deprecated
    public static final Logger logger = Logger.getLogger("Minecraft");

    /**
     * Registers the library with RedstoneChips.
     */
    public CircuitLibrary() {
        RedstoneChips.addCircuitLibrary(this);
    }

    @Override
    public void onRedstoneChipsEnable(RedstoneChips instance) {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getCircuitName() {
        return this.getDescription().getName();
    }

    @Override
    public String getCircuitVersion() {
        return this.getDescription().getVersion();
    }
}
