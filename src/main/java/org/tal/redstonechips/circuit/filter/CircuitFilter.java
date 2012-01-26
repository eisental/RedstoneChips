package org.tal.redstonechips.circuit.filter;

import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.RedstoneChips;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class CircuitFilter {
    protected RedstoneChips rc;
    
    public CircuitFilter setPlugin(RedstoneChips rc) { this.rc = rc; return this; }
    
    public abstract void parse(CommandSender s, String[] string) throws IllegalArgumentException;
    
    public abstract Collection<Circuit> filter(Collection<Circuit> circuits);    
}
