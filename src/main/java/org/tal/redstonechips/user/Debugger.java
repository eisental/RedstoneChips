package org.tal.redstonechips.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.CircuitListener;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class Debugger extends CircuitListener {
    public enum Flag { IO }
    
    protected Map<Circuit, List<Flag>> flags;
    
    protected List<Circuit> circuits;
    protected UserSession session;
    private boolean paused = false;
    
    public Debugger(UserSession session) {
        circuits = new ArrayList<Circuit>();
        flags = new HashMap<Circuit, List<Flag>>();
        this.session = session;
    }
    
    public void addCircuit(Circuit c) {        
        if (!circuits.contains(c)) {
            c.addListener(this);
            circuits.add(c);
        }
    }
    
    public boolean removeCircuit(Circuit c) {
        boolean removed = circuits.remove(c);
        if (removed) { 
            c.removeListener(this);
            flags.remove(c);
        }
        return removed;
    }

    public void clear() {
        for (Circuit c : circuits) c.removeListener(this);
        circuits.clear();
        flags.clear();
    }
    
    public void setPaused(boolean p) {
        if (paused==p) return;
        
        if (p) for (Circuit c : circuits) c.removeListener(this);
        else for (Circuit c : circuits) c.addListener(this);
        
        paused = p;
    }
    
    public boolean isPaused() { return paused; }

    public List<Circuit> getCircuits() {
        return circuits;
    }

    public boolean isDebugging(Circuit c) {
        return circuits.contains(c);
    }

    public List<Flag> getFlags(Circuit c) {
        return flags.get(c);
    }
    
    public void addFlag(Circuit c, Flag f) {
        if (!flags.containsKey(c))
            flags.put(c, new ArrayList<Flag>());
        
        flags.get(c).add(f);
    }
    
    public boolean removeFlag(Circuit c, Flag f) {
        if (!flags.containsKey(c)) return false;
        return flags.get(c).remove(f);
    }
    
    public boolean isDebugFlagSet(Circuit c, Flag f) {
        if (!flags.containsKey(c)) return false;
        else return flags.get(c).contains(f);
    }
}
