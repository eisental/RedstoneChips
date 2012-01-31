package org.tal.redstonechips.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.CircuitListener;

/**
 * An abstract Circuit debugger.
 * 
 * @author Tal Eisenberg
 */
public abstract class Debugger extends CircuitListener {
    public enum Flag { IO }
    
    protected Map<Circuit, List<Flag>> flags;
    
    protected List<Circuit> circuits;
    protected UserSession session;
    private boolean paused = false;
    
    /**
     * 
     * @param session The user session that uses this debugger.
     */
    public Debugger(UserSession session) {
        circuits = new ArrayList<Circuit>();
        flags = new HashMap<Circuit, List<Flag>>();
        this.session = session;
    }
    
    /**
     * Adds a circuit to the debugger list.
     * 
     * @param c a Circuit.
     */
    public void addCircuit(Circuit c) {        
        if (!circuits.contains(c)) {
            c.addListener(this);
            circuits.add(c);
        }
    }
    
    /**
     * Removes a circuit from the debugger list.
     * 
     * @param c The circuit to remove.
     * @return true if the circuit was removed.
     */
    public boolean removeCircuit(Circuit c) {
        boolean removed = circuits.remove(c);
        if (removed) { 
            c.removeListener(this);
            flags.remove(c);
        }
        return removed;
    }

    /**
     * Clear the debugger circuit list.
     */
    public void clear() {
        for (Circuit c : circuits) c.removeListener(this);
        circuits.clear();
        flags.clear();
    }
    
    /**
     * Pause or unpause the debugger. When paused the debugger doesn't send any information.
     * 
     * @param pause 
     */
    public void setPaused(boolean pause) {
        if (paused==pause) return;
        
        if (pause) for (Circuit c : circuits) c.removeListener(this);
        else for (Circuit c : circuits) c.addListener(this);
        
        paused = pause;
    }
    
    /**
     * 
     * @return true if the debugger is paused.
     */
    public boolean isPaused() { return paused; }

    /**
     * 
     * @return a List of all circuits this debugger is listening to.
     */
    public List<Circuit> getCircuits() {
        return circuits;
    }

    /**
     * 
     * @param c a Circuit
     * @return true if the debugger is listening to the specified circuit.
     */
    public boolean isDebugging(Circuit c) {
        return circuits.contains(c);
    }

    /**
     * @param c a Circuit.
     * @return a List of debug flags for this circuit or null if none are set.
     */
    public List<Flag> getFlags(Circuit c) {
        return flags.get(c);
    }
    
    /**
     * Sets a debug flag for a debugged circuit.
     * @param c a Circuit.
     * @param f a Flag.
     */
    public void addFlag(Circuit c, Flag f) {
        if (!flags.containsKey(c))
            flags.put(c, new ArrayList<Flag>());
        
        flags.get(c).add(f);
    }
    
    /**
     * Unsets a debug flag of a debugged circuit.
     * @param c a Circuit.
     * @param f The Flag to remove.
     * @return true if the flag was removed.
     */
    public boolean removeFlag(Circuit c, Flag f) {
        if (!flags.containsKey(c)) return false;
        return flags.get(c).remove(f);
    }
    
    /**
     * @param c a debugged Circuit.
     * @param f a debug Flag.
     * @return True if the debug flag is set for this circuit.
     */
    public boolean isDebugFlagSet(Circuit c, Flag f) {
        if (!flags.containsKey(c)) return false;
        else return flags.get(c).contains(f);
    }
}
