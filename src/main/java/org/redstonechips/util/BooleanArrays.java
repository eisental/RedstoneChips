
package org.redstonechips.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

/**
 *
 * @author taleisenberg
 */
public class BooleanArrays {
    public static boolean[] zero = new boolean[1];

    public static String asString(boolean[] bits) {
        return asString(bits, bits.length);        
    }
    
    public static String asString(boolean[] bits, int length) {
        return asString(bits, 0, length);
    }
    
    public static String asString(boolean[] bits, int start, int length) {
        return toPrettyString(bits, start, length, -1);
    }

    public static String toPrettyString(boolean[] bits) {
        return toPrettyString(bits, 0, bits.length);
    }

    public static String toPrettyString(boolean[] bits, int length) {
        return toPrettyString(bits, 0, length);
    }
    
    public static String toPrettyString(boolean[] bits, int start, int length) {
        return toPrettyString(bits, start, length, 4);
    }
    
    public static String toPrettyString(boolean[] bits, int start, int length, int wordlength) {
        StringBuilder ret = new StringBuilder();

        for (int i=length-1+start; i>=start; i--) {
            ret.append(bits[i]?"1":"0");
            
            if (wordlength>0 && i>0 && i % wordlength==0)
                ret.append(' ');
        }

        return ret.toString();
    }
    
    public static boolean[] fromString(String sbits) {
        boolean[] bits = new boolean[sbits.length()];
        int lastbit = sbits.length()-1;
        
        for (int i=lastbit; i>=0; i--) {
            bits[lastbit-i] = (sbits.charAt(i)=='1');
        }

        return bits;
        
    }

    public static int toSignedInt(boolean[] bits) {
        return toSignedInt(bits, 0, bits.length);
    }
    
    public static int toSignedInt(boolean[] bits, int start, int length) {
        int signed = -(bits[start+length-1]?1:0) * (1 << length-1);
        int bitval = 1;
        for (int i=0; i<length-1; i++) {
            if (bits[start+i]) signed += bitval;
            bitval+=bitval;
        }
        
        return signed;        
    }
    
    public static int toUnsignedInt(boolean[] bits) {
        return toUnsignedInt(bits, 0, bits.length);
    }
    
    public static int toUnsignedInt(boolean[] bits, int start, int length) {
        int val = 0;
        int bitval = 1;
        for (int i=0; i<length; i++) {
            if (bits[start+i]) val += bitval;
            bitval+=bitval;                    
        }
                
        return val;
    }

    public static boolean[] fromInt(int value) {
        int length = requiredBitsForUnsigned(value);
        return fromInt(value, length);
    }
    
    public static boolean[] fromInt(int value, int length) {
        boolean[] bits = new boolean[length];
        int index = 0;
        while (value != 0) {
            if (value % 2 != 0) {
                bits[index] = true;
            }
            ++index;
            value = value >>> 1;
        }

        return bits;
    }

    public static boolean[] fromBitSet(BitSet bits, int start, int length) {
        boolean[] ret = new boolean[length];
        
        for (int i=start; i<start+length; i++)
            ret[i-start] = bits.get(i);
        
        return ret;
    }

    public static boolean[] fromBitSet(BitSet bitset) {
        return fromBitSet(bitset, 0, bitset.length());
    }
    
    public static boolean isZero(boolean[] inputs) {
        for (int i=0; i<inputs.length; i++)
            if (inputs[i]) return false;
        
        return true;
    }

    public static void and(boolean[] dest, boolean[] a, boolean[] b) {
        for (int i=0; i<Math.min(a.length, b.length); i++)
            dest[i] = a[i] & b[i];
    }
    
    public static void or(boolean[] dest, boolean[] a, boolean[] b) {
        for (int i=0; i<Math.min(a.length, b.length); i++)
            dest[i] = a[i] | b[i];
    }
    
    public static void xor(boolean[] dest, boolean[] a, boolean[] b) {
        for (int i=0; i<Math.min(a.length, b.length); i++)
            dest[i] = a[i] ^ b[i];
    }

    public static void not(boolean[] dest, boolean[] src) {
        for (int i=0; i<Math.min(dest.length, src.length); i++)
            dest[i] = !src[i];
    }
    
    public static int requiredBitsForUnsigned(int value) {
        if (value==0) return 1;
        
        int count = 0;
        while (value > 0) {
            count++;
            value = value >> 1;
        }
        
        return count;
    }

    public static boolean[] shiftLeft(boolean[] bits, int length) {
        for (int i=length; i>0; i--) {
            bits[i] = bits[i-1];
        }
        bits[0] = false;

        return bits;
    }

    public static boolean[] shiftRight(boolean[] bits, int length, boolean logical) {
        for (int i=0; i<length-1; i++) {
            bits[i] = bits[i+1];
        }

        if (logical) bits[length-1] = false;

        return bits;
    }
    
    public static boolean[] flip(boolean[] bits, int start, int length) {
        for (int i=start; i<start+length; i++) 
            bits[i] = !bits[i];
        return bits;
    }

    public static BitSet toBitSet(boolean[] bits, int start, int length) {
        BitSet buf = new BitSet(length);
        
        for (int i=0; i<length; i++)
            buf.set(i, bits[start+i]);
        
        return buf;
    }

    public static BitSet toBitSet(boolean[] bits) {
        return toBitSet(bits, 0, bits.length);
    }

    public static boolean[] fromBigInt(BigInteger bigInt) {
        return fromByteArray(bigInt.toByteArray());
    }
 
    public static boolean[] fromByteArray(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bits.length; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits[i] = true;
            }
        }
        return bits;
    }

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
    
    public int min3(int a, int b, int c) {
        if (a==b && b==c) return a;
        else if (a<b && a<c) return a;
        else if (b<a && b<c) return b;
        else return c;
    }
}
