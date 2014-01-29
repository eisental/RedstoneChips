
package org.redstonechips.circuit;

import org.bukkit.plugin.java.JavaPlugin;
import org.redstonechips.RedstoneChips;

/**
 * A JavaPlugin implementation for circuit libraries. Provides invisible inter-plugin communication with RedstoneChips and,
 * by implementing the CircuitIndex interface provides a mechanism to register new circuit classes.
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitLibrary extends JavaPlugin implements CircuitIndex {
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
    public String getIndexName() {
        return getName();
    }
    
    @Override
    public String getVersion() {
        return this.getDescription().getVersion();
    }
}
