
package org.redstonechips.util;

/**
 *
 * @author taleisenberg
 */
public class BooleanSubset {

    final boolean[] array;
    final int start, length;
    
    public BooleanSubset(boolean[] array, int start, int length) {
        if (start+length>array.length) throw new IndexOutOfBoundsException("" + (start+ length));
        if ((start < 0) || (length < 0)) throw new IllegalArgumentException("Negative start or length arguments.");
        
        this.array = array;
        this.start = start;
        this.length = length;
    }
    
    public boolean get(int index) {
        return array[start+index];
    }
    
    public BooleanSubset subset(int start, int length) {
        return new BooleanSubset(array, this.start+start, length);
    }
    
    public boolean[] copy() {
        return copy(0, this.length);
    }
    
    public boolean[] copy(int start, int length) {
        if (start>=this.length || start+length>this.length)
            throw new IndexOutOfBoundsException();
        
        boolean[] ret = new boolean[length];
        System.arraycopy(array, this.start+start, ret, 0, length);
        
        return ret;
    }
    
    public long toUnsignedInt() {
        return toUnsignedInt(0, length);
    }
    
    public long toUnsignedInt(int start, int length) {
        if (this.start+start>=this.length || this.start+start+length>this.length)
            throw new IndexOutOfBoundsException();
        
        return BooleanArrays.toUnsignedInt(array, this.start+start, length);
    }    

    public String toBinaryString() {
        return BooleanArrays.toString(array, start, length);
    }

    public int length() {
        return length;
    }
}
