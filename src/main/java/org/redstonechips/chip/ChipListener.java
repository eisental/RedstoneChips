
package org.redstonechips.chip;

import org.bukkit.command.CommandSender;

/**
 * An interface for receiving notifications from a {@link org.redstonechips.chip.Chip Chip} object.
 * 
 * @author taleisenberg
 */
public interface ChipListener {
    /** 
     * Called whenever an input pin changes its state.
     * @param c Source.
     * @param idx Input index.
     * @param state New input state.
     */
    public void inputChanged(Chip c, int idx, boolean state);

    /**
     * Called whenever an output pin changes its state.
     * @param c Source.
     * @param idx Output index.
     * @param state New output state.
     */
    public void outputChanged(Chip c, int idx, boolean state);
    
    /**
     * Called when the chip is disabled.
     * 
     * @param c Source.
     */
    public void chipDisabled(Chip c);
    
    /**
     * Called when the chip is enabled.
     * 
     * @param c Source.
     */
    public void chipEnabled(Chip c);
    
    /**
     * Called when the chip is shutdown.
     * 
     * @param c Source.
     */
    public void chipShutdown(Chip c);
    
    /**
     * Called when the chip is destroyed.
     * @param c Source.
     * @param destroyer The circuit destroyer.
     */
    public void chipDestroyed(Chip c, CommandSender destroyer);
    
    /**
     * Called when the circuit is sending a debug message.
     * 
     * @param c The affected circuit.
     * @param msg A circuit message.
     */
    public void circuitMessage(Chip c, String msg);
}
