/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

/**
 *
 * @author Tal Eisenberg
 */
public interface CircuitIndex {
    /**
     * Called by the RedstoneChips plugin. Every circuit class in the class array is added to the circuit class pool.
     * @return An array of Class objects. Each class must extend the Circuit class.
     */
    public Class[] getCircuitClasses();

    /**
     * Called by the RedstoneChips plugin immediately after loading circuit classes from every circuit library.
     */
    public void onRedstoneChipsEnable();
}
