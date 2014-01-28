package org.redstonechips.chip.io;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.redstonechips.chip.Chip;

/**
 * Represents an IO chip block. Currently, this is either an input, output or interface block.
 * 
 * @author Tal Eisenberg
 */
public abstract class IOBlock {

    /** IOBlock type */
    public static enum Type {
        OUTPUT(OutputPin.class),
        INPUT(InputPin.class),
        INTERFACE(InterfaceBlock.class);
        
        Class<? extends IOBlock> clazz;
        
        Type(Class<? extends IOBlock> clazz) {
            this.clazz = clazz;
        }
        
        public Class<? extends IOBlock> getIOClass() { return clazz; }
    }
    
    protected Chip chip = null;
    protected Location loc = null;
    protected int index = -1;
    
    /**
     * Creates an IOBlock instance of the desired type.
     * 
     * @param type IO block type.
     * @param c owner circuit of the block.
     * @param l block location.
     * @param index the block pin index in the circuit.
     * @return new IOBlock instance.
     */
    public static IOBlock makeIOBlock(Type type, Chip c, Location l, int index) {
        for (Type t : Type.values()) {
            if (type==t) {
                try {
                    return t.getIOClass().getConstructor(Chip.class, Location.class, int.class).newInstance(c,l,index);
                } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
                    Logger.getLogger(IOBlock.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return null;
    }
    
    public IOBlock(Chip c, Location l, int index) {
        this.chip = c;
        this.loc = l;
        this.index = index;
    }
    
    /**
     *
     * @return The circuit of this input pin.
     */
    public Chip getChip() { return chip; }

    /**
     *
     * @return The location of the io block.
     */
    public Location getLocation() { return loc; }

    /**
     *
     * @return The index of the io block in its circuit.
     */
    public int getIndex() { return index; }
    
    protected boolean isPartOfStructure(Location b) {
        for (Location l : chip.structure) {
            if (b.equals(l))
                return true;
        }

        return false;
    }

    /**
     * @param blocks An array of IOBlocks
     * @return A Location array containing the locations of each IOBlock in the same order.
     */
    public static Location[] locationsOf(IOBlock[] blocks) {
        Location[] locs = new Location[blocks.length];

        for (int i=0; i<locs.length; i++)
            locs[i] = blocks[i].getLocation();
        
        return locs;
    }
    
}
