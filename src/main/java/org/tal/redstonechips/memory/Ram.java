
package org.tal.redstonechips.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 * Random access memory implementation. 
 * @author Tal Eisenberg
 */
public class Ram extends Memory {
    private Map<BitSet7, BitSet7> words;
    private final List<RamListener> listeners = new ArrayList<RamListener>();
        
    /**
     * Read data from memory.
     * 
     * @param address Address to read from.
     * @return The data at the specified address.
     */
    public BitSet7 read(BitSet7 address) {
        BitSet7 data = words.get(address);
        if (data==null) data = new BitSet7();
        return data;
    }

    /**
     * Read data from memory.
     * 
     * @param address Address to read from.
     * @return The data at the specified address.
     */
    public BitSet7 read(int address) {
        BitSet7 data = words.get(BitSetUtils.intToBitSet(address, 32));
        if (data==null) data = new BitSet7();
        return data;
    }
    
    /**
     * Write a BitSet to the specified address.
     * @param address Memory address.
     * @param data Bits to write.
     */
    public void write(BitSet7 address, BitSet7 data) {
        if (address==null || data==null) throw new IllegalArgumentException("Can't write memory. Data or address is null.");
        words.put(address, data);

        for (RamListener l : listeners) l.dataChanged(this, address, data);        
    }

    /**
     * Writes a BitSet to the specified address.
     * @param address Memory address.
     * @param data Bits to write.
     */
    public void write(long address, BitSet7 data) {
        write(BitSet7.valueOf(new long[] {address}), data);
    }

    /**
     * Writes a long integer to the specified address.
     * @param address Memory address.
     * @param data long value to write.
     */    
    public void write(long address, long data) {
        write(address, BitSet7.valueOf(new long[] {data}));
    }
    
    /**
     * Writes a BigInteger to the specified address.
     * 
     * @param address Memory address.
     * @param data BigInteger value to write.
     */
    public void write(BigInteger address, BigInteger data) {
        write(BitSetUtils.bigIntToBitSet(address), BitSetUtils.bigIntToBitSet(data));
    }

    public void writeInt(int address, int value) {
        write(address, value);
    }
        
    public int readInt(int address) {
        BitSet7 val = read(address);
        return BitSetUtils.bitSetToUnsignedInt(val, 0, val.length());
    }    
    
    @Override
    public void init(String id) {
        super.init(id);
        words = new HashMap<BitSet7, BitSet7>();
    }

    /**
     * 
     * @return a Map<BitSet, BitSet> containing all memory data.
     */
    @Override
    protected Map getData() {
        return words;
    }

    /**
     * Replace the memory data with a data Map. Tries to convert non BitSet Map values.
     * @param data New memory data.
     */
    @Override
    protected void setData(Map data) {
        words.clear();
        if (data==null) return;
        for (Object key : data.keySet()) {            
            Object value = data.get(key);
            
            BitSet7 address = convert(key);
            BitSet7 word = convert(value);
            words.put(address, word);
        }
    }
    
   private BitSet7 convert(Object obj) {
        if (obj instanceof BitSet7) return (BitSet7)obj;
        else if (obj instanceof Integer) return BitSetUtils.intToBitSet((Integer)obj, 32);
        else if (obj instanceof BigInteger) return BitSetUtils.bigIntToBitSet((BigInteger)obj);
        else throw new IllegalArgumentException("Unsupported memory data class: " + obj.getClass().getCanonicalName());
    }
   
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
