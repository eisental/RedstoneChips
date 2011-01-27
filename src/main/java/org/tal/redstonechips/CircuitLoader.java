/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import org.tal.redstonechips.circuit.Circuit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitLoader {
    private Map<String,Class> circuitClasses = new HashMap<String,Class>();
    private RedstoneChips rc;

    public CircuitLoader(RedstoneChips plugin) {
        rc = plugin;
    }

    /**
     * Adds the class to the circuit pool, allowing players to create circuits of this class.
     *
     * @param c The class to add. must extend org.tal.redstonechips.Circuit.
     */
    public void addCircuitClass(Class c) {
        String name = c.getSimpleName();
        if (circuitClasses.containsKey(name)) {
            rc.log(Level.WARNING, "While trying to add " + c.getCanonicalName() + " to circuit pool: Another circuit class named " + name + " was found. ");
        } else if (!Circuit.class.isAssignableFrom(c)) {
            rc.log(Level.WARNING, "While trying to add " + c.getCanonicalName() + ": Class does not extend org.tal.redstonechips.circuits.Circuit");
        } else {
            circuitClasses.put(name, c);
        }
    }

    /**
     * Removes a class from the circuit pool
     *
     * @param c Class to remove.
     * @return result of Map.remove() method.
     */
    public Object removeCircuitClass(Class c) {
        return circuitClasses.remove(c.getSimpleName());
    }

    public Circuit getCircuitInstance(String name) throws InstantiationException, IllegalAccessException {
        Class c = this.circuitClasses.get(name);
        if (c==null) throw new IllegalArgumentException("Unknown circuit type: " + name);
        else return (Circuit) c.newInstance();
    }

    Map<String, Class> getCircuitClasses() {
        return circuitClasses;
    }

}
