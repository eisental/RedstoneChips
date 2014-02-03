
package org.redstonechips.util;

/**
 *
 * @author taleisenberg
 */
public class BooleanSubset {

    final boolean[] array;
    final int start, length;
    
    public BooleanSubset(boolean[] array, int start, int length) {
        if (start+length>array.length) throw new IndexOutOfBoundsException("" + (start + length));
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
    
    /**
     * Copy the subset into another boolean array.
     * 
     * @param dest Destination array.
     * @param start The first index of dest that will be overwritten.
     * @return Number of elements written.
     */
    public int copyInto(boolean[] dest, int start) {        
        int len = Math.min(length, dest.length-start);
        System.arraycopy(array, this.start, dest, start, len);
        return len;
    }
    
    public long toUnsignedInt() {
        return toUnsignedInt(0, length);
    }
    
    public long toUnsignedInt(int start, int length) {
        if (this.start+start>=this.length || this.start+start+length>this.length)
            throw new IndexOutOfBoundsException();
        
        return BooleanArrays.toUnsignedInt(array, this.start+start, length);
    }    

    @Override
    public String toString() {
        return BooleanArrays.toString(array, start, length);
    }

    public String toPrettyString() { 
        return BooleanArrays.toPrettyString(array, start, length);
    }
    
    public String toPrettyString(int wordlength) {
        return BooleanArrays.toPrettyString(array, start, length, wordlength);
    }
    
    public int length() {
        return length;
    }
}
