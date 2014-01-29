
package org.redstonechips.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.redstonechips.util.BitSetUtils;
import org.redstonechips.util.BooleanArrays;

/**
 * Random access memory implementation. 
 * @author Tal Eisenberg
 */
public class Ram extends Memory {
    private final Map<Long, boolean[]> words = new HashMap<>(); // actual data storage.
            
    /**
     * Read data from memory.
     * 
     * @param address Address to read from.
     * @return The data at the specified address.
     */
    public boolean[] read(long address) {
        boolean[] b = words.get(address);
        if (b==null) return BooleanArrays.zero;
        else return b;
    }
    
    public long readInt(long address) {
        return BooleanArrays.toUnsignedInt(read(address));
    }    
    
    /**
     * Write a BitSet to the specified address.
     * @param address Memory address.
     * @param data Bits to write.
     */
    public void write(long address, boolean[] data) {
        if (data==null) data = BooleanArrays.zero;
        words.put(address, data);

        for (RamListener l : listeners) l.dataChanged(this, address, data);        
    }

    /**
     * Writes a long integer to the specified address.
     * @param address Memory address.
     * @param data long value to write.
     */    
    public void write(long address, int data) {
        write(address, BooleanArrays.fromInt(data));
    }
    
    /**
     * Writes a BigInteger to the specified address.
     * 
     * @param address Memory address.
     * @param data BigInteger value to write.
     */
    public void write(long address, BigInteger data) {
        write(address, BooleanArrays.fromBigInt(data));
    }
            
    // -- Overrides for Serialization --
    
    /**
     * 
     * @return a Map<BitSet, BitSet> containing all memory data.
     */
    @Override
    protected Map getData() {
        Map<BitSet, BitSet> data = new HashMap<>();
        for (Long address : words.keySet()) {
            BitSet a = BitSet.valueOf(new long[] {address});
            BitSet d = BooleanArrays.toBitSet(words.get(address));
            if (!d.isEmpty())
                data.put(a, d);
        }
        return data;
    }

    /**
     * Replace the memory data with a data Map. Tries to convert non BitSet Map values or keys.
     * @param data New memory data.
     */
    @Override
    protected void setData(Map data) {
        words.clear();
        if (data==null) return;
        for (Object key : data.keySet()) {            
            Object value = data.get(key);
            
            long address = convertToLong(key);
            boolean[] word = convert(value);
            words.put(address, word);
        }
    }
    
    private long convertToLong(Object obj) {
        if (obj instanceof boolean[]) return BooleanArrays.toUnsignedInt((boolean[])obj);
        else if (obj instanceof BitSet) return BitSetUtils.bitSetToUnsignedInt((BitSet)obj, 0, ((BitSet)obj).length());
        else if (obj instanceof BigInteger) return ((BigInteger)obj).longValue();
        else return (Long)obj;
    }
    
    private boolean[] convert(Object obj) {
        if (obj instanceof boolean[]) return (boolean[])obj;
        else if (obj instanceof BitSet) return BooleanArrays.fromBitSet((BitSet)obj);
        else if (obj instanceof Integer) return BooleanArrays.fromInt((Integer)obj, 32);
        else if (obj instanceof BigInteger) return BooleanArrays.fromBigInt((BigInteger)obj);
        else throw new IllegalArgumentException("Unsupported memory data class: " + obj.getClass().getCanonicalName());
    }
   
    // -- Ram Listener Mechanics --
    
    private final List<RamListener> listeners = new ArrayList<>();
    
    /**
     * Registers a RamListener with this Ram.
     * @param l Ram listener.
     */
    public void addListener(RamListener l) { 
        if (!listeners.contains(l)) listeners.add(l);
    }
    
    /**
     * @return All RamListeners listening to this Ram.
     */
    public List<RamListener> getListeners() { return listeners; }    
        
}

