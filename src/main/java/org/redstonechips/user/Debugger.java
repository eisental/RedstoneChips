package org.redstonechips.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.ChipListenerImpl;

/**
 * An abstract Chip debugger.
 * 
 * @author Tal Eisenberg
 */
public abstract class Debugger extends ChipListenerImpl {
    public enum Flag { IO }
    
    protected Map<Chip, List<Flag>> flags;
    
    protected List<Chip> chips;
    protected UserSession session;
    private boolean paused = false;
    
    /**
     * 
     * @param session The user session that uses this debugger.
     */
    public Debugger(UserSession session) {
        chips = new ArrayList<>();
        flags = new HashMap<>();
        this.session = session;
    }
    
    /**
     * Adds a chip to the debugger list.
     * 
     * @param c.
     */
    public void addChip(Chip c) {        
        if (!chips.contains(c)) {
            c.addListener(this);
            chips.add(c);
        }
    }
    
    /**
     * Removes a chip from the debugger list.
     * 
     * @param c The chip to remove.
     * @return true if the chip was removed.
     */
    public boolean removeChip(Chip c) {
        boolean removed = chips.remove(c);
        if (removed) { 
            c.removeListener(this);
            flags.remove(c);
        }
        return removed;
    }

    /**
     * Clear the debugger chip list.
     */
    public void clear() {
        for (Chip c : chips) c.removeListener(this);
        chips.clear();
        flags.clear();
    }
    
    /**
     * Pause or unpause the debugger. When paused the debugger doesn't send any information.
     * 
     * @param pause 
     */
    public void setPaused(boolean pause) {
        if (paused==pause) return;
        
        if (pause) for (Chip c : chips) c.removeListener(this);
        else for (Chip c : chips) c.addListener(this);
        
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
    public List<Chip> getChips() {
        return chips;
    }

    /**
     * 
     * @param c
     * @return true if the debugger is listening to the specified chip.
     */
    public boolean isDebugging(Chip c) {
        return chips.contains(c);
    }

    /**
     * @param c 
     * @return a List of debug flags for this chip or null if none are set.
     */
    public List<Flag> getFlags(Chip c) {
        return flags.get(c);
    }
    
    /**
     * Sets a debug flag for a debugged chip.
     * @param c 
     * @param f
     */
    public void addFlag(Chip c, Flag f) {
        if (!flags.containsKey(c))
            flags.put(c, new ArrayList<Flag>());
        
        flags.get(c).add(f);
    }
    
    /**
     * Unsets a debug flag of a debugged chip.
     * @param c 
     * @param f The Flag to remove.
     * @return true if the flag was removed.
     */
    public boolean removeFlag(Chip c, Flag f) {
        if (!flags.containsKey(c)) return false;
        return flags.get(c).remove(f);
    }
    
    /**
     * @param c a debugged chip.
     * @param f a debug Flag.
     * @return True if the debug flag is set for this chip.
     */
    public boolean isDebugFlagSet(Chip c, Flag f) {
        if (!flags.containsKey(c)) return false;
        else return flags.get(c).contains(f);
    }
}
