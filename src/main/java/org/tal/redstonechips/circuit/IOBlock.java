package org.tal.redstonechips.circuit;

import org.bukkit.Location;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class IOBlock {

    public static enum Type {
        OUTPUT(OutputPin.class),
        INPUT(InputPin.class),
        INTERFACE(InterfaceBlock.class);
        
        Class<? extends IOBlock> cls;
        
        Type(Class<? extends IOBlock> clazz) {
            this.cls = clazz;
        }
        
        public Class<? extends IOBlock> getIOClass() { return cls; }
    }
    
    protected Circuit circuit = null;
    protected Location loc = null;
    protected int index = -1;
    
    public static IOBlock makeIOBlock(Type type, Circuit c, Location l, int index) {
        switch (type) {
            case OUTPUT:
                return new OutputPin(c, l, index);
            case INPUT:
                return new InputPin(c, l, index);
            case INTERFACE:
                return new InterfaceBlock(c, l, index);
            default:
                return null;
        }
    }
    
    public IOBlock(Circuit c, Location l, int index) {
        this.circuit = c;
        this.loc = l;
        this.index = index;
    }
    
    /**
     *
     * @return The circuit of this input pin.
     */
    public Circuit getCircuit() { return circuit; }

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
        for (Location l : circuit.structure) {
            if (b.equals(l))
                return true;
        }

        return false;
    }
    
}
