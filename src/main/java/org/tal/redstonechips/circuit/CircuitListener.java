package org.tal.redstonechips.circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitListener {
    public void inputChanged(Circuit c, int idx, boolean state) { }
    
    public void outputChanged(Circuit c, int idx, boolean state) { }
    
    public void circuitDisabled(Circuit c) { }
    
    public void circuitEnabled(Circuit c) { }
    
    public void circuitShutdown(Circuit c) { }
    
    public void circuitDestroyed(Circuit c) { }
}
