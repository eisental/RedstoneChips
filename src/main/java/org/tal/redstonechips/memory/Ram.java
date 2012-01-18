
package org.tal.redstonechips.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 * Random access memory implementation. 
 * @author Tal Eisenberg
 */
public class Ram extends Memory {
    private Map<BitSet7, BitSet7> words;
    private List<RamListener> listeners = new ArrayList<RamListener>();
        
    @Override
    public BitSet7 read(BitSet7 address) {
        BitSet7 data = words.get(address);
        if (data==null) data = new BitSet7();
        return data;
    }

    public BitSet7 read(int address) {
        BitSet7 data = words.get(BitSetUtils.intToBitSet(address, 32));
        if (data==null) data = new BitSet7();
        return data;
    }
    
    @Override
    public void write(BitSet7 address, BitSet7 data) {
        words.put(address, data);
        
        for (RamListener l : listeners) l.dataChanged(this, address, data);        
    }

    public void write(int address, BitSet7 data) {
        write(BitSetUtils.intToBitSet(address, 32), data);
    }
    
    @Override
    public void init(String id) {
        super.init(id);
        words = new HashMap<BitSet7, BitSet7>();
    }
    
    public void init() {
        init(Memory.getFreeMemId());
    }

    @Override
    protected Map getData() {
        return words;
    }

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
    
    public void addListener(RamListener l) { 
        if (!listeners.contains(l)) listeners.add(l);
    }
    
    public List<RamListener> getListeners() { return listeners; }
    
    private BitSet7 convert(Object obj) {
        if (obj instanceof BitSet7) return (BitSet7)obj;
        else if (obj instanceof Integer) return BitSetUtils.intToBitSet((Integer)obj, 32);
        else if (obj instanceof BigInteger) return BitSetUtils.bigIntToBitSet((BigInteger)obj);
        else throw new IllegalArgumentException("Unsupported memory data class: " + obj.getClass().getCanonicalName());
    }
}
