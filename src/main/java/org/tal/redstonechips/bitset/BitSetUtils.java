
package org.tal.redstonechips.bitset;

import java.math.BigInteger;
import java.util.Map;

/**
 *
 * @author Tal Eisenberg
 */
public class BitSetUtils {
    
    /**
     * An empty BitSet7 object. Useful for clearing all outputs without creating new objects.
     */
    public final static BitSet7 clearBitSet = new BitSet7();

    /**
     * Convert a BitSet to an unsigned integer.
     *
     * @param b BitSet to convert.
     * @param startBit LSB bit of the integer.
     * @param length Number of bits to read.
     * @return an unsigned integer number.
     */
    public static int bitSetToUnsignedInt(BitSet7 b, int startBit, int length) {
        int val = 0;
        int bitval = 1;
        for (int i=0; i<length; i++) {
            if (b.get(i+startBit)) val += bitval;
            // Computationally-cheap addition by itself to multiply by two
            bitval += bitval;
        }

        return val;
    }    
    
    /**
     * Convert a BitSet to a signed integer using two's complement encoding.
     *
     * @param b BitSet to convert.
     * @param startBit LSB bit of the integer.
     * @param length Number of bits to read.
     * @return a signed integer number.
     */
    public static int bitSetToSignedInt(BitSet7 b, int startBit, int length) {
        // treats the bit set as a two's complement encoding binary number.
        int signed = -(b.get(startBit+length-1)?1:0) * (int)Math.pow(2, length-1);
        int bitval = 1;
        for (int i=0; i<length-1; i++) {
            if (b.get(startBit+i)) signed += bitval;
            // Computationally-cheap addition by itself to multiply by two
            bitval += bitval;
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
    public static BigInteger bitSetToBigInt(BitSet7 b, int offset, int length) {
        BigInteger val = BigInteger.ZERO;
        BigInteger bitval = BigInteger.ONE;
        for (int i=0; i<length; i++) {
            if (b.get(i+offset)) val = val.add(bitval);
            // Computationally-cheap addition by itself to multiply by two
            bitval = bitval.add(bitval);
        }

        return val;
    }

    /**
     * Convert a BitSet to an unsigned BigInteger.
     * @param b BitSet to convert.
     * @return 
     */
    public static BigInteger bitSetToBigInt(BitSet7 b) {
        return bitSetToBigInt(b, 0, b.length());
    }
    
    /**
     * BigInteger object representing the number 2.
     */
    public final static BigInteger BigTwo = new BigInteger("2");
    
    /**
     * Convert a BigInteger into a BitSet.
     * @param i BigInteger to convert.
     * @return 
     */
    public static BitSet7 bigIntToBitSet(BigInteger i) {
        int bitLength = i.bitLength();
        BitSet7 bits = new BitSet7(bitLength);
        for (int index = 0; index < bitLength; index++) {
            bits.set(i.testBit(index));
        }
        
        return bits;
}
   
    /**
     * Convert an integer to BitSet.
     * @param value Value to convert.
     * @return 
     */
    public static BitSet7 intToBitSet(int value) {
        BitSet7 bits = new BitSet7();
        int index = 0;
        while (value != 0) {
            if (value & 1 != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }

        return bits;
        
    }
    
    /**
     * Convert a BitSet to a binary representation string.
     * Each bit is converted into "0" or "1" and added to the string.
     * The result is a binary number with its most significant bit on the left.
     * 
     * @param b BitSet to convert
     * @param startBit Start converting from this bit. Treat it as the least significant bit.
     * @param length Number of bits to read from the BitSet after the startBit.
     */    
    public static String bitSetToBinaryString(BitSet7 b, int startBit, int length) {
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
    public static String bitSetToBinaryString(BitSet7 b, int startBit, int length, int wordlength) {
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
    public static String bitSetToString(BitSet7 bits, int length) {
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
     */
    public static BitSet7 stringToBitSet(String sbits) {
        BitSet7 bits = new BitSet7(sbits.length());

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
    public static BitSet7 shiftLeft(BitSet7 s, int length) {
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
    public static BitSet7 shiftRight(BitSet7 s, int length, boolean logical) {
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
    public static BitSet7 intToBitSet(int value, int length) {
        BitSet7 bits = new BitSet7(length);
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
    public static Map<String,String> bitSetToMap(Map<String,String> map, String key, BitSet7 bits, int length) {
        map.put(key, bitSetToString(bits, length));
        return map;
    }

    /**
     * Parses a string representation of a BitSet into a BitSet7 object. Used for loading circuit state from file.
     * @param map The map to read the BitSet string from.
     * @param key The map key that points to the BitSet string.
     * @return The parsed BitSet7 object.
     */
    public static BitSet7 mapToBitSet(Map<String, String> map, String key) {
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
    public static BitSet7 boolToBitSet(boolean[] bits, int offset, int length) {
        BitSet7 bitset = new BitSet7(length-offset);
        for (int i=offset; i<length; i++) bitset.set(i-offset,bits[i]);
        
        return bitset;
    }

}
