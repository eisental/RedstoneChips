package org.redstonechips.command.filters;

import java.util.Collection;
import org.bukkit.command.CommandSender;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public interface ChipFilter {    
    public abstract void parse(CommandSender s, String[] string) throws IllegalArgumentException;    
    public abstract Collection<Chip> filter(Collection<Chip> circuits);    
}
