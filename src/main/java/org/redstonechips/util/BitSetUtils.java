
package org.redstonechips.util;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Map;

/**
 *
 * @author Tal Eisenberg
 */
public class BitSetUtils {
    
    /**
     * An empty BitSet7 object. Useful for clearing all outputs without creating new objects.
     */
    public final static BitSet clearBitSet = new BitSet();

    /**
     * Convert a BitSet to an unsigned integer.
     *
     * @param b BitSet to convert.
     * @param startBit LSB bit of the integer.
     * @param length Number of bits to read.
     * @return an unsigned integer number.
     */
    public static int bitSetToUnsignedInt(BitSet b, int startBit, int length) {
        int val = 0;
        int bitval = 1;
        for (int i=0; i<length; i++) {
            if (b.get(startBit+i)) val += bitval;
            bitval+=bitval;                    
        }
                
        return val;
    }
    
    /**
     * Convert a BitSet to a signed integer using two's complement encoding. The BitSet is treated as a two's complement encoding binary number.
     *
     * @param b BitSet to convert.
     * @param startBit LSB bit of the integer.
     * @param length Number of bits to read.
     * @return a signed integer number.
     */
    public static int bitSetToSignedInt(BitSet b, int startBit, int length) {
        int signed = -(b.get(startBit+length-1)?1:0) * (1 << length-1);
        int bitval = 1;
        for (int i=0; i<length-1; i++) {
            if (b.get(startBit+i)) signed += bitval;
            bitval+=bitval;
        }
        
        return signed;
    }
    /**
     * Convert a BitSet to an unsigned BigInteger.
     * 
     * @param b BitSet to convert.
     * @param offset starting bit of the integer.
     * @param length number of bits to convert.
     * @return 
     */
    public static BigInteger bitSetToBigInt(BitSet b, int offset, int length) {
        return new BigInteger(BitSetUtils.toByteArray(b.get(offset, offset+length)));
    }

    /**
     * Convert a BitSet to an unsigned BigInteger.
     * @param b BitSet to convert.
     * @return 
     */
    public static BigInteger bitSetToBigInt(BitSet b) {
        return new BigInteger(BitSetUtils.toByteArray(b));
    }
    
    /**
     * Convert a BigInteger into a BitSet.
     * @param i BigInteger to convert.
     * @return 
     */
    public static BitSet bigIntToBitSet(BigInteger i) {
        return BitSetUtils.fromByteArray(i.toByteArray());
    }
   
    /**
     * Convert an integer to BitSet.
     * @param value Value to convert.
     * @return 
     */
    public static BitSet intToBitSet(int value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0) {
            if (value % 2 != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }

        return bits;
        
    }

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length()/8+1];
        for (int i=0; i<bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }    
    /**
     * Convert a BitSet to a binary representation string.
     * Each bit is converted into "0" or "1" and added to the string.
     * The result is a binary number with its most significant bit on the left.
     * 
     * @param b BitSet to convert
     * @param startBit Start converting from this bit. Treat it as the least significant bit.
     * @param length Number of bits to read from the BitSet after the startBit.
     * @return String representation of b.
     */    
    public static String bitSetToBinaryString(BitSet b, int startBit, int length) {
        return bitSetToBinaryString(b, startBit, length, 4);
    }

    /**
     * Convert a BitSet to a binary representation string.
     * Converting each bit into "0" or "1".
     * The result is a binary number with its most significant bit on the left.
     * 
     * @param b BitSet to convert
     * @param startBit Start converting from this bit. Treat it as the least significant bit.
     * @param length Number of bits to read from the BitSet after the startBit.
     * @param wordlength When greater than 0, a space character is added each wordlength number of digits.
     * @return Binary representation String of the BitSet.
     */
    public static String bitSetToBinaryString(BitSet b, int startBit, int length, int wordlength) {
        StringBuilder ret = new StringBuilder();

        for (int i=length+startBit-1; i>=startBit; i--) {
            ret.append(b.get(i)?"1":"0");
            
            if (wordlength!=-1 && wordlength>0 && i>0 && i % wordlength==0)
                ret.append(' ');
        }

        return ret.toString();
    }

    /**
     * Converts a BitSet to String
     * @param bits
     * @param length
     * @return 
     */
    public static String bitSetToString(BitSet bits, int length) {
        String sbits = "";
        for (int i=length-1; i>=0; i--)
            sbits += (bits.get(i)?"1":"0");

        return sbits;
    }
    
    /**
     * Converts a string into a BitSet. 
     * The first character is the most significant bit. 
     * a '1' character is converted to true. 
     * Every other character is converted to false.
     * 
     * @param sbits The string to convert.
     * @return BitSet representing the binary value.
     */
    public static BitSet stringToBitSet(String sbits) {
        BitSet bits = new BitSet(sbits.length());

        for (int i=sbits.length()-1; i>=0; i--) {
            bits.set(sbits.length()-1-i, (sbits.charAt(i)=='1'));
        }

        return bits;
    }
    
    /**
     * Shifts bits of a BitSet object one place to the left.
     * Stores new value in the same BitSet.
     *
     * @param s Shifted BitSet object.
     * @param length Length of binary number.
     * @return BitSet s after shifting.
     */
    public static BitSet shiftLeft(BitSet s, int length) {
        for (int i=length; i>0; i--) {
            s.set(i, s.get(i-1));
        }
        s.set(0, false);

        return s;
    }

    /**
     * Shifts bits of a BitSet object one place to the right.
     * Stores new value in the same BitSet.
     *
     * @param s Shifted BitSet object.
     * @param length Length of binary number.
     * @param logical true for logical right shift; false for arithmetic right shift.
     * @return BitSet s after shifting.
     */
    public static BitSet shiftRight(BitSet s, int length, boolean logical) {
        for (int i=0; i<length-1; i++) {
            s.set(i, s.get(i+1));
        }

        if (logical) s.set(length-1, false);

        return s;
    }

    /**
     * Stores an integer number in a BitSet object.
     *
     * @param value integer number to store
     * @param length number of bits to use.
     * @return
     */
    public static BitSet intToBitSet(int value, int length) {
        BitSet bits = new BitSet(length);
        int index = 0;
        while (value != 0) {
            if (value % 2 != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }

        return bits;
    }

    /**
     * Stores a BitSet object as a string in a map. Used for state persistence.
     * Basically converts the BitSet into a string of "1" and "0" according to its bit values.
     *
     * @param map A Map object to store the bit set in.
     * @param key The map key in which to store the bit set.
     * @param bits The bit set that will be stored in the map.
     * @param length Number of bits to store.
     * @return The same map object as the parameter.
     */
    public static Map<String,String> bitSetToMap(Map<String,String> map, String key, BitSet bits, int length) {
        map.put(key, bitSetToString(bits, length));
        return map;
    }

    /**
     * Parses a string representation of a BitSet into a BitSet7 object. Used for loading circuit state from file.
     * @param map The map to read the BitSet string from.
     * @param key The map key that points to the BitSet string.
     * @return The parsed BitSet7 object.
     */
    public static BitSet mapToBitSet(Map<String, String> map, String key) {
        String sbits = map.get(key);
        if (sbits==null) return null;
        else return stringToBitSet(sbits);
    }

    /**
     * Converts a boolean array into a BitSet
     * @param bits Boolean array to convert.
     * @param offset Array index to start reading from.
     * @param length Number of bits to convert.
     * @return 
     */
    public static BitSet boolToBitSet(boolean[] bits, int offset, int length) {
        BitSet bitset = new BitSet(length-offset);
        for (int i=offset; i<length; i++) bitset.set(i-offset,bits[i]);
        
        return bitset;
    }

}
