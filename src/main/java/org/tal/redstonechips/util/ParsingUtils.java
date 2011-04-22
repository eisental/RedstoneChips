/*
 * ParsingAid.java
 *
 * Created on May 21, 2007, 4:42 PM
 *
 *  Copyright (C) 2010 Tal Eisenberg
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.tal.redstonechips.util;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.regex.Pattern;
import javax.swing.KeyStroke;


/**
 * A Utility class that deals with String parsing and analysis.
 *
 * @author Tal Eisenberg
 */
public class ParsingUtils {
    public final static Pattern NUMBER_PATTERN = Pattern.compile("^[-+]?\\d*\\.?\\d*");
    public final static Pattern INTEGER_PATTERN = Pattern.compile("^[-+]?\\d*");
    public final static Pattern SYMBOLNAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_$]*$");
    public final static Pattern notePattern = Pattern.compile("\\@[^@]*\\@");
    public final static Pattern unitsPattern = Pattern.compile("\\:[^:]*\\:");

    public final static DecimalFormat floatFormat = new DecimalFormat(".##");
    public final static DecimalFormat intFormat = new DecimalFormat("#");

    public static boolean checkNesting(String str, char open, char close) {
        return (str.charAt(0)==open) && (Tokenizer.balanced(str, 0, open, close)==str.length()-1);
    }

    public static String fillString(String fill, int length) {
        String ret = "";
        while (ret.length()<length) {
            ret += fill;
        }
        return ret.substring(0, length);
    }

    /**
     * Returns true if the string can be parsed as a number. Currently the method
     * tries to parse the String using Double.parseDouble() and returns true if it succeeds.
     *
     * @param value The String that is checked.
     */
    public static boolean isNumber(String value) {
        return (NUMBER_PATTERN.matcher(value).matches());
    }
    
    /**
     * Returns true if the string can be parsed as an integer number. Currently the method
     * tries to parse the String using Integer.parseInteger() and returns true if it succeeds.
     * 
     * @param value The String that is checked.
     */
    public static boolean isInt(String value) {
        return (INTEGER_PATTERN.matcher(value).matches());
    }
    
    /**
     * Returns true if the character in the index parameter is nested inside balanced characters. 
     * for example, using the String "a(b,c),(d,e[f]),g'abcdefg'":
     * isNested(str, 0, "(['",")]'") will return false while the same call with an index of 3 will 
     * return true. The length of openBalance must be exactly the same as the length of closeBalance.
     * each character in openBalance is paired with the character of the same index in closeBalance.
     * For example if openBalance="([" closeBalance should be ")]" if the same type of brackets are to be paired.
     * 
     * @param str   The String that will be parsed
     * @param index The index of the character that will be checked.
     * @param openBalance   A String containing all the balance opening characters.
     * @param closeBalance  A String containing all the balance closing characters.
     */
    public static boolean isNested(String str, int index, String openBalance, String closeBalance) throws IllegalArgumentException {
        if (index==0) return false; // first char can't be nested...
        char[] openers = openBalance.toCharArray();
        char[] closers = closeBalance.toCharArray();
        if (closers.length!=openers.length) throw new IllegalArgumentException("There must be the same number of balance opener and balance closer characters.");
        for (int i=0; i<openers.length; i++) {
            //make sure the balance is 0 before index
            int count = 0;
            for (int x=0; x<index; x++) {
                if (str.charAt(x)==openers[i]) count++;
                else if (str.charAt(x)==closers[i]) count--;
            }
            if (closers[i]==openers[i]) {
                if (count%2!=0) return true;
            } else if (count!=0) return true;
        }
        return false;
    }
    
    public static int indexOfUnnested(String str, char chr, String openBalance, String closeBalance) {
        return indexOfUnnested(str, chr, 0, openBalance, closeBalance);
    }
    
    public static int indexOfUnnested(String str, char chr, int fromIdx, String openBalance, String closeBalance) throws IllegalArgumentException {
        int idx = fromIdx-1; boolean nested;
        do {
           idx = str.indexOf(chr, idx+1);
           if (idx==-1) return -1;
           else
               nested = isNested(str, idx, openBalance, closeBalance);
       } while (nested);
       return idx;
    }
    
    public static int lastIndexOfUnnested(String str, char chr, String openBalance, String closeBalance) throws IllegalArgumentException {
        return lastIndexOfUnnested(str, chr, str.length()-1, openBalance, closeBalance);
    }
    
    public static int lastIndexOfUnnested(String str, char chr, int fromIdx, String openBalance, String closeBalance) throws IllegalArgumentException {
        int idx = fromIdx+1; boolean nested;
        do {
            idx = str.lastIndexOf(chr, idx-1);
            if (idx==-1) return -1;
            else 
                nested = isNested(str, idx, openBalance, closeBalance);
        } while (nested);
        return idx;
    }
    
    public static KeyStroke parseKeyStroke(String keyName) throws IllegalArgumentException {
        if (keyName.equals("none")) return null;
        keyName = keyName.trim();
        int modifiers = 0;
        int plusIdx = keyName.indexOf("+");
        while (plusIdx!=-1) {
            String mod = keyName.substring(0, plusIdx);
            keyName = keyName.substring(plusIdx+1);
            if (mod.equals("alt")) modifiers |= InputEvent.ALT_MASK;
            else if (mod.equals("ctrl")) modifiers |= InputEvent.CTRL_MASK;
            else if (mod.equals("meta")) modifiers |= InputEvent.META_MASK;
            else if (mod.equals("shift")) modifiers |= InputEvent.SHIFT_MASK;
            plusIdx = keyName.indexOf("+");
        }
        for (Field f : KeyEvent.class.getDeclaredFields()) {
            if (f.getName().startsWith("VK_")) {
                String name = f.getName().substring(3).toLowerCase();
                if (name.equals(keyName))
                    try {
                        KeyStroke s = KeyStroke.getKeyStroke(f.getInt(null), modifiers);
                        return s;
                    } catch (IllegalAccessException ex) {
                        throw new IllegalArgumentException("While parsing key stroke: " + ex.getMessage());
                    }
            }
        }
        throw new IllegalArgumentException("Unknown key name: " + keyName);
    }

    public static String keyStrokeToName(KeyStroke stroke) throws IllegalArgumentException {
        if (stroke==null) return "none";
        int keyCode = stroke.getKeyCode();
        int modifiers = stroke.getModifiers();
        String mods = "";
        if ((modifiers & InputEvent.ALT_MASK)!=0)
            mods += "alt+";
        if ((modifiers & InputEvent.CTRL_MASK)!=0)
            mods += "ctrl+";
        if ((modifiers & InputEvent.SHIFT_MASK)!=0)
            mods += "meta+";

        for (Field f : KeyEvent.class.getDeclaredFields()) {
            if (f.getName().startsWith("VK_")) {
                String name = f.getName().substring(3).toLowerCase();
                try {
                    if (f.getInt(null) == keyCode) {
                        return mods+name;
                    }
                } catch (IllegalAccessException ex) {
                    throw new IllegalArgumentException("While looking for key name: " + ex.getMessage());
                }
            }
        }
        throw new IllegalArgumentException("Unknown key code: " + keyCode);
    }
    public static String parseEscapeCharacters(String str) {
        return str.replaceAll("\\$gt\\.", ">")
            .replaceAll("\\$lt\\.", "<")
            .replaceAll("\\$amp\\.", "&")
            .replaceAll("\\$sc\\.", ";")
            .replaceAll("\\$eq\\.","=")
            .replaceAll("\\$com\\.", ",")
            .replaceAll("\\$apos\\.", "'")
            .replaceAll("\\$curl-open\\.", "{")
            .replaceAll("\\$curl-close\\.", "}")
            .replaceAll("\\$under\\.", "_")
            .replaceAll("\\$tab\\.", "\t")
            .replaceAll("\\$nline\\.", System.getProperty("line.separator"));
    }

    public static int parseInteger(String i) throws IllegalArgumentException {
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException n) {
            throw new IllegalArgumentException("Invalid int value " + i);
        }
    }
    
    public static double parseDouble(String i) {
        try {
            return Double.parseDouble(i);
        } catch (NumberFormatException n) {
            throw new IllegalArgumentException("Invalid float value " + i);
        }

    }

    private final static int COLUMNS = 80;
    
    public static String makeCaption(String message) {
        int eqCount = COLUMNS - message.length();
        String out = "";
        if (eqCount>=2) {
            for (int i=0; i<(int)(eqCount/2); i++) 
                out += "=";
        } out += message.toUpperCase();
        int l = out.length();
        for (int i=0; i<(COLUMNS-l); i++)
            out += "=";
        return "\n" + out + "\n";
    }
    
    public static String makeDots(String message, int tab) {
        int columns = COLUMNS - tab;
        if (message.length()>columns)
            return "..." + message.substring(message.length()-(columns-4));
        else return message;
    }
    
    private static boolean backSpcSign = true;

    public static char makePrefixSign() {
        if (backSpcSign) {
            backSpcSign = false;
            return ':';
        } else {
            backSpcSign = true;            
            return '|';
        }
    }

    public static String arrayToString(String[] args, String delimiter) {
        StringBuilder b = new StringBuilder();
        for (String arg : args) { b.append(args); b.append(delimiter); }

        return b.substring(0, b.length()-delimiter.length());
    }

}
