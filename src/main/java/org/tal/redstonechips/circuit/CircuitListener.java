package org.tal.redstonechips.circuit;

import org.bukkit.command.CommandSender;

/**
 * A class for listening to circuit events.
 * 
 * @author Tal Eisenberg
 */
public class CircuitListener {
    /** 
     * Called whenever an input pin changes its state.
     * @param c The affected circuit.
     * @param idx Input index.
     * @param state New input state.
     */
    public void inputChanged(Circuit c, int idx, boolean state) { }

    /**
     * Called whenever an output pin changes its state.
     * @param c The affected circuit.
     * @param idx Output index.
     * @param state New output state.
     */
    public void outputChanged(Circuit c, int idx, boolean state) { }
    
    /**
     * Called when the circuit is disabled.
     * 
     * @param c The affected circuit.
     */
    public void circuitDisabled(Circuit c) { }
    
    /**
     * Called when the circuit is enabled.
     * 
     * @param c The affected circuit.
     */
    public void circuitEnabled(Circuit c) { }
    
    /**
     * Called when the circuit is shutdown.
     * 
     * @param c The affected circuit.
     */
    public void circuitShutdown(Circuit c) { }
    
    /**
     * Called when the circuit is destroyed.
     * @param c The affected circuit
     * @param destroyer The circuit destroyer.
     */
    public void circuitDestroyed(Circuit c, CommandSender destroyer) { }
    
    /**
     * Called when the circuit is sending a debug message.
     * 
     * @param c The affected circuit.
     * @param msg A circuit message.
     */
    public void circuitMessage(Circuit c, String msg) { }
}
