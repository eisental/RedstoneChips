
package org.redstonechips.circuit;

import org.redstonechips.RedstoneChips;

/**
 * An interface that enables a circuit library to inform RedstoneChips of all the Circuit classes it provides.
 * 
 * @author Tal Eisenberg
 */
public interface CircuitIndex {
    /**
     * Called by the RedstoneChips plugin. Every circuit class in the class array is added to the circuit class pool.
     * @return An array of Class objects. Each class must extend the Circuit class.
     */
    public Class<? extends Circuit>[] getCircuitClasses();

    /**
     * Called by the RedstoneChips plugin before starting to load circuit classes.
     * @param instance
     */
    public void onRedstoneChipsEnable(RedstoneChips instance);

    /**
     * 
     * @return the index name
     */
    public String getIndexName();

    /**
     *
     * @return the index version
     */
    public String getVersion();
    
}
