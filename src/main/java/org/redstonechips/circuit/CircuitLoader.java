
package org.redstonechips.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.redstonechips.RedstoneChips;

/**
 * @author Tal Eisenberg
 */
public class CircuitLoader {
    /**
     * Contains all circuit classes from every loaded circuit library.
     */
    private static final Map<String,Class<? extends Circuit>> circuitClasses = new HashMap<>();
    
    /**
     * A list of installed circuit libraries.
     */
    private static final List<CircuitIndex> libraries = new ArrayList<>();
    
    /**
     * Maximum number of characters for class names to fit into the 1st line of a sign excluding 2 color chars (13).
     */
    public final static int maxClassNameLength = 13;

    /**
     * Adds all classes in the circuit library to the circuit pool allowing players to create circuits of this class.
     *
     * @param lib
     */
    public static void addCircuitIndex(CircuitIndex lib) {
        for (Class c : lib.getCircuitClasses()) {
            String name = c.getSimpleName();

            if (name.length()>maxClassNameLength) {
                RedstoneChips.inst().log(Level.WARNING, "While trying to add " + c.getCanonicalName() + " to circuit pool: Class name is longer than " + maxClassNameLength + " characters.");
            } else if (circuitClasses.containsKey(name)) {
                RedstoneChips.inst().log(Level.WARNING, "While trying to add " + c.getCanonicalName() + " to circuit pool: Another circuit class named " + name + " was found. ");
            } else if (!Circuit.class.isAssignableFrom(c)) {
                RedstoneChips.inst().log(Level.WARNING, "While trying to add " + c.getCanonicalName() + ": Class does not extend " + Circuit.class.getCanonicalName());
            } else {
                circuitClasses.put(name, c);
            }
        }
        
        libraries.add(lib);
    }

    /**
     * Removes a circuit library from the circuit pool.
     *
     * @param lib
     * @return result of Map.remove() method.
     */
    public static Object removeCircuitIndex(CircuitIndex lib) {
        for (Class<? extends Circuit> c : lib.getCircuitClasses()) {
            circuitClasses.remove(c.getSimpleName());
        }
        return libraries.remove(lib);
    }

    /**
     *
     * @param name Circuit class name.
     * @return A new empty instance of a circuit of the specified class.
     * @throws InstantiationException if thrown by Constructor.newInstance().
     * @throws IllegalAccessException if thrown by Constructor.newInstance().
     */
    public static Circuit getCircuitInstance(String name) throws InstantiationException, IllegalAccessException {
        Class cls = circuitClasses.get(name);
        if (cls==null) throw new IllegalArgumentException("Unknown circuit type: " + name);
        else {
            Circuit c = (Circuit) cls.newInstance();
            return c;
        }
    }

    /**
     *
     * @return a Map containing all registered circuit classes. The map's key is set to the circuit class name.
     */
    public static Map<String, Class<? extends Circuit>> getCircuitClasses() {
        return circuitClasses;
    }

    /**
     * 
     * @return a List of installed circuit libraries.
     */
    public static List<CircuitIndex> getCircuitLibraries() {
        return libraries;
    }

}
