/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import org.tal.redstonechips.RedstoneChips;

/**
 * An interface used for enabling the circuit library to inform RedstoneChips of all the Circuit classes it provides.
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
     * Called by the RedstoneChips plugin immediately after finishing loading circuit classes from every circuit library.
     */
    public void onRedstoneChipsEnable();

    /**
     * Called by the RedstoneChips plugin on the beginning of its onEnable() method.
     * @param instance Plugin reference.
     */
    public void setRedstoneChipsInstance(RedstoneChips instance);
}
