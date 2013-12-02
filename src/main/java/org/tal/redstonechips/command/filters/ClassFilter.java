package org.tal.redstonechips.command.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class ClassFilter extends CircuitFilter {
    String cclass;

    @Override
    public void parse(CommandSender sender, String[] string) throws IllegalArgumentException {
        if (string.length!=1) {
            StringBuilder sb = new StringBuilder();
            for (String s : string) sb.append(s);

            throw new IllegalArgumentException("Bad class filter: " + sb.toString() + ". Expecting 'class: <chip class>'.");
        } else {
            for (String sclass : rc.getCircuitLoader().getCircuitClasses().keySet()) {
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
    public Collection<Circuit> filter(Collection<Circuit> circuits) {
        List<Circuit> filtered = new ArrayList<Circuit>();

        for (Circuit circuit : circuits) {
            if (circuit.getCircuitClass().equalsIgnoreCase(cclass)) filtered.add(circuit);
        }

        return filtered;
    }
    
}
