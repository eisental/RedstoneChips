package org.redstonechips.command.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.redstonechips.circuit.CircuitLoader;
import org.redstonechips.chip.Chip;

/**
 *
 * @author Tal Eisenberg
 */
public class TypeFilter implements ChipFilter {
    String cclass;

    @Override
    public void parse(CommandSender sender, String[] string) throws IllegalArgumentException {
        if (string.length!=1) {
            StringBuilder sb = new StringBuilder();
            for (String s : string) sb.append(s);

            throw new IllegalArgumentException("Bad class filter: " + sb.toString() + ". Expecting 'class: <chip class>'.");
        } else {
            for (String sclass : CircuitLoader.getCircuitClasses().keySet()) {
                if (sclass.startsWith(string[0])) {
                    cclass = sclass;
                    break;
                }
            }

            if (cclass==null)
                throw new IllegalArgumentException("Unknown chip class: " + string[0]);
        }
    }

    @Override
    public Collection<Chip> filter(Collection<Chip> chips) {
        List<Chip> filtered = new ArrayList<>();

        for (Chip chip : chips) {
            if (chip.getType().equalsIgnoreCase(cclass)) filtered.add(chip);
        }

        return filtered;
    }
    
}
