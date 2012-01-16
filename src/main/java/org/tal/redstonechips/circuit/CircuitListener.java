package org.tal.redstonechips.circuit;

import org.bukkit.command.CommandSender;

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
    
    public void circuitDestroyed(Circuit c, CommandSender destroyer) { }
    
    public void circuitMessage(Circuit c, String msg) { }
}
