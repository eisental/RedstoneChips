
package org.redstonechips.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

/**
 * A static class for working with boolean arrays as binary numbers. Each boolean 
 * element represents one bit. The first element is always the LSB (least significant
 * bit).
 * 
 * Consists of various conversion methods between boolean[] and different types, as well as bitwise
 * operations on boolean[]s.
 * 
 * @author taleisenberg
 */
public class BooleanArrays {
    /**
     * Boolean array representing 0: {false}.
     */
    public static boolean[] zero = new boolean[1];

    // -- String conversion --
    
    /**
     * {@link #toString(boolean[], int, int) toString}, printing the whole array.
     */    
    public static String toString(boolean[] bits) {
        return toString(bits, bits.length);        
    }
    
    /**
     * {@link #toString(boolean[], int, int) toString} starting with the 1st
     * array element.
     */
    public static String toString(boolean[] bits, int length) {
        return toString(bits, 0, length);
    }
    
    /**
     * {@link #toPrettyString(boolean[], int, int, int) toPrettyString} without
     * spaces between words. Useful for serialization.
     */
    public static String toString(boolean[] bits, int start, int length) {
        return toPrettyString(bits, start, length, -1);
    }

    /**
     * {@link #toPrettyString(boolean[], int, int, int) toPrettyString} with a 
     * word-length of 4, printing the whole array.
     */    
    public static String toPrettyString(boolean[] bits) {
        return toPrettyString(bits, 0, bits.length);
    }

    /**
     * {@link #toPrettyString(boolean[], int, int, int) toPrettyString} with a 
     * word-length of 4, starting with the 1st array element.
     */
    public static String toPrettyString(boolean[] bits, int length) {
        return toPrettyString(bits, 0, length);
    }
    
    /**
     * {@link #toPrettyString(boolean[], int, int, int) toPrettyString} with a word-length of 4.
     */
    public static String toPrettyString(boolean[] bits, int start, int length) {
        return toPrettyString(bits, start, length, 4);
    }
    
    /**
     * Creates a printable binary representation of the array. MSB is on the left.
     * 
     * @param bits A bits array to print.
     * @param start First array index to represent.
     * @param length Number of array elements to represent.
     * @param wordlength When greater than 0, a space is inserted between each word, as in 0101 0010 1111 for a word-length of 4.
     * @return
     */
    public static String toPrettyString(boolean[] bits, int start, int length, int wordlength) {
        StringBuilder ret = new StringBuilder();

        for (int i=length-1; i>=0; i--) {
            if (i < bits.length)
                ret.append(bits[i+start]?"1":"0");
            else
                ret.append('0');
            
            if (wordlength>0 && i>0 && i % wordlength==0)
                ret.append(' ');
        }

        return ret.toString();
    }
    
    /**
     * Parses a String representing a binary number into a boolean array. The 
     * input number is expected to start with the MSB and contain 1s and 0s. 
     * Whitespace is ignored. For ex. "1100" is converted to {false, false, true, true}.
     * 
     * @param sbits Binary string to parse.
     * @return Parsed boolean array.
     */
    public static boolean[] fromString(String sbits) {
        sbits = sbits.trim().replaceAll("\\s", "");
        boolean[] bits = new boolean[sbits.length()];
        int lastbit = sbits.length()-1;        
        for (int i=lastbit; i>=0; i--) {
            bits[lastbit-i] = (sbits.charAt(i)=='1');
        }

        return bits;
        
    }

    // -- (long) Integer conversion --
    
    /**
     * {@link #toSignedInt(boolean[], int, int) toSignedInt} over the whole array.
     */
    public static long toSignedInt(boolean[] bits) {
        return toSignedInt(bits, 0, bits.length);
    }
    
    /**
     * Convert a boolean array to signed integer using two's complement. Supports
     * up to 64 bits.
     * 
     * @param bits Array to convert.
     * @param start First array element.
     * @param length Number of elements.
     * @return Signed integer representation of the boolean array subset.
     */
    public static long toSignedInt(boolean[] bits, int start, int length) {
        long signed = -(bits[start+length-1]?1:0) * (1 << length-1);
        
        for (int i=0, bitval = 1; i<length-1; i++, bitval+=bitval) 
            if (bits[start+i]) signed += bitval;        
        
        return signed;        
    }
    
    /**
     * {@link #toUnsignedInt(boolean[], int, int) toUnsignedInt} over the whole array.
     */
    public static long toUnsignedInt(boolean[] bits) {
        return toUnsignedInt(bits, 0, bits.length);
    }
    
    /**
     * Convert a boolean array to unsigned integer. Supports up to 64 bits.
     * 
     * @param bits Array to convert.
     * @param start First array element.
     * @param length Number of elements.
     * @return Unsigned integer representation of the boolean array subset.
     */
    public static long toUnsignedInt(boolean[] bits, int start, int length) {
        long val = 0;

        for (int i=0, bitval = 1; i<length; i++, bitval+=bitval) 
            if (bits[start+i]) val += bitval;        
                
        return val;
    }

    /**
     * {@link #fromInt(long, int) fromInt} with automatically calculating the 
     * necessary bit length for holding the value.
     */
    public static boolean[] fromInt(long value) {
        int length = requiredBitsForUnsigned(value);
        return fromInt(value, length);
    }
    
    /**
     * Convert a long value to it's corresponding two's complement boolean array.
     * 
     * @param value long value to convert.
     * @param length The length of the new boolean array.
     * @return A new boolean array.
     */
    public static boolean[] fromInt(long value, int length) {
        if (value>=0) {
            boolean[] bits = new boolean[length];
            int index = 0;
            while (value != 0 && index < length) {
                if (value % 2 != 0) {
                    bits[index] = true;
                }
                ++index;
                value = value >>> 1;
            }
            return bits;
        } else {
            boolean[] bits = fromInt(-value, length);
            
            return add(not(bits, bits), 1, length);
            
        }
    }

    // -- BitSet conversion --
    
    /**
     * {@link #toBitSet(boolean[], int, int) toBitSet}, converting the whole array.
     */
    public static BitSet toBitSet(boolean[] bits) {
        return toBitSet(bits, 0, bits.length);
    }
    
    /**
     * Convert a boolean array to {@link BitSet}
     * 
     * @param bits Array to convert.
     * @param start First array element. 
     * @param length Number of array elements.
     * @return a BitSet with the same value of bits.
     */
    public static BitSet toBitSet(boolean[] bits, int start, int length) {
        BitSet buf = new BitSet(length);
        
        for (int i=0; i<length; i++)
            buf.set(i, bits[start+i]);
        
        return buf;
    }
    
    /**
     * {@link #fromBitSet(java.util.BitSet, int, int)} over the whole BitSet.
     */
    public static boolean[] fromBitSet(BitSet bitset) {
        return fromBitSet(bitset, 0, bitset.length());
    }
    
    /**
     * Convert a {@link BitSet} to a boolean array.
     * @param bits BitSet to convert.
     * @param start First bit to convert from.
     * @param length Number of bits to convert.
     * @return A new boolean array.
     */
    public static boolean[] fromBitSet(BitSet bits, int start, int length) {
        boolean[] ret = new boolean[length];
        
        for (int i=start; i<start+length; i++)
            ret[i-start] = bits.get(i);
        
        return ret;
    }
    
    /**
     * Convert a boolean array to {@link BigInteger}
     * @param bits The array to convert.
     * @return A new BigInteger representing the value of bits.
     */
    public static BigInteger toBigInt(boolean[] bits) {
        return new BigInteger(toByteArray(bits));
    }
    
    /**
     * Convert a {@link BigInteger} to boolean array.
     * @param bigInt The BigInteger to convert.
     * @return A new boolean array.
     */
    public static boolean[] fromBigInt(BigInteger bigInt) {
        return fromByteArray(bigInt.toByteArray());
    }
 
    /**
     * Convert a boolean array to an array of bytes.
     * @param bits The array to convert.
     * @return A new byte array representing the value of bits.
     */
    public static byte[] toByteArray(boolean[] bits) {
        byte[] bytes = new byte[bits.length/8+1];
        for (int i=0; i<bits.length; i++) {
            if (bits[i]) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }    
    
    /**
     * Convert a byte array to a boolean array.
     * @param bytes The array to convert.
     * @return A new boolean array representing the value of bytes.
     */
    public static boolean[] fromByteArray(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bits.length; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits[i] = true;
            }
        }
        return bits;
    }
    
    // -- Gates --
    
    /**
     * A bitwise {@code and} of array a and array b. When array lengths are not 
     * equal, the smallest array determines the length of the operation.
     * @param dest An array used to store the result.
     * @param a First operand.
     * @param b Second operand.
     * @return dest.
     */
    public static boolean[] and(boolean[] dest, boolean[] a, boolean[] b) {
        for (int i=0; i<min3(dest.length, a.length, b.length); i++)
            dest[i] = a[i] & b[i];
        
        return dest;
    }

    /**
     * A bitwise {@code or} of array a and array b. When array lengths are not 
     * equal, the smallest array determines the length of the operation..
     * @param dest An array used to store the result.
     * @param a First operand.
     * @param b Second operand.
     * @return dest.
     */    
    public static boolean[] or(boolean[] dest, boolean[] a, boolean[] b) {
        for (int i=0; i<min3(dest.length, a.length, b.length); i++)
            dest[i] = a[i] | b[i];
        
        return dest;
    }
    
    /**
     * A bitwise {@code xor} of array a and array b. When array lengths are not 
     * equal, the smallest array determines the length of the operation.
     * @param dest An array used to store the result.
     * @param a First operand.
     * @param b Second operand.
     * @return dest.
     */
    public static boolean[] xor(boolean[] dest, boolean[] a, boolean[] b) {
        for (int i=0; i<min3(dest.length, a.length, b.length); i++)
            dest[i] = a[i] ^ b[i];
        
        return dest;
    }

    /**
     * A bitwise {@code not} of src array. When array lengths are not equal, the
     * smallest array determines the length of the operation.
     * @param dest An array used to store the result.
     * @param src The not operand.
     * @return dest.
     */
    public static boolean[] not(boolean[] dest, boolean[] src) {
        for (int i=0; i<Math.min(dest.length, src.length); i++)
            dest[i] = !src[i];
        
        return dest;
    }

    // -- Binary Arithmetic --
    
    /**
     * A bitwise {@code left shift} of src array. When array lengths are not equal, the
     * smallest array determines the length of the operation.
     * @param dest An array used to store the result.
     * @param src The shift operand.
     * @return dest.
     */    
    public static boolean[] shiftLeft(boolean[] dest, boolean[] src) {
        for (int i=src.length; i>0; i--) {
            dest[i] = src[i-1];
        }
        dest[0] = false;

        return dest;
    }
    
    /**
     * A bitwise {@code right shift} of src array. When array lengths are not equal, the
     * smallest array determines the length of the operation.
     * @param dest An array used to store the result.
     * @param src The shift operand.
     * @param logical When true, the method calculates a logical right shift, clearing the last bit.
     * @return dest.
     */    
    public static boolean[] shiftRight(boolean[] dest, boolean[] src, boolean logical) {
        for (int i=0; i<src.length-1; i++) {
            dest[i] = src[i+1];
        }

        if (logical) dest[src.length-1] = false;

        return dest;
    }

    public static boolean[] add(long a, long b, int length) {
        return fromInt(a+b, length);
    }

    public static boolean[] add(boolean[] a, long b, int length) {
        return add(toUnsignedInt(a), b, length);
    }
    public static boolean[] add(boolean[] a, boolean[] b, int length) {
        return add(toUnsignedInt(a), toUnsignedInt(b), length);
    }
        
    public static boolean[] negate(boolean[] set, int length) {
        long n = toSignedInt(set, 0, length);
        return fromInt(-n, length);
    }
    
    // -- misc --
    
    /**
     * Compare two boolean arrays based on numeric value. Arrays need not
     * be the same length to be equal.
     * 
     * @param a
     * @param b
     * @return 
     */
    public static boolean equals(boolean[] a, boolean[] b) {
        if (a.length==b.length) return Arrays.equals(a, b);
        else {
            for (int i=0; i<Math.min(a.length, b.length); i++)
                if (a[i]!=b[i]) return false;
            if (a.length>b.length) {
                for (int i=b.length; i<a.length; i++)
                    if (a[i]==true) return false;
            } else {
                for (int i=a.length; i<b.length; i++)
                    if (b[i]==true) return false;
            }
            
            return true;
        }
    }
    
    /**
     * Calculates the minimum number of bits required to store a long value.
     * @param value 
     * @return The bit length.
     */
    public static int requiredBitsForUnsigned(long value) {
        if (value==0) return 1;
        
        int count = 0;
        while (value > 0) {
            count++;
            value = value >> 1;
        }
        
        return count;
    }
    
    /**
     * 
     * @param bits The boolean array to test.
     * @return True when all elements of bits are false.
     */
    public static boolean isZero(boolean[] bits) {
        for (int i=0; i<bits.length; i++)
            if (bits[i]) return false;
        
        return true;
    }
    
    /**
     * minimum function for 3 values.
     * 
     * @param a
     * @param b
     * @param c
     * @return the smallest value.
     */
    public static int min3(int a, int b, int c) {
        if (a==b && b==c) return a;
        else if (a<b && a<c) return a;
        else if (b<a && b<c) return b;
        else return c;        
    }
    
    private BooleanArrays() {} // prevent instantiation.
}
