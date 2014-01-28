/*
 * Parsing.java
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

package org.redstonechips.parsing;

import java.text.DecimalFormat;
import java.util.regex.Pattern;


/**
 * A Utility class that deals with String parsing and analysis.
 *
 * @author Tal Eisenberg
 */
public class Parsing {
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
     * @return 
     */
    public static boolean isNumber(String value) {
        return (NUMBER_PATTERN.matcher(value).matches());
    }
    
    /**
     * Returns true if the string can be parsed as an integer number. Currently the method
     * tries to parse the String using Integer.parseInteger() and returns true if it succeeds.
     * 
     * @param value The String that is checked.
     * @return 
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
     * @return 
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
    
    public static String arrayToString(String[] args, String delimiter) {
        StringBuilder b = new StringBuilder();
        for (String arg : args) { b.append(arg); b.append(delimiter); }

        return b.substring(0, b.length()-delimiter.length());
    }

    /**
     * http://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns
     * 
     * @param str
     * @return 
     */
    public static String convertGlobToRegex(String str) {
        str = str.trim();
        int strLen = str.length();
        StringBuilder sb = new StringBuilder(strLen);
        // Remove beginning and ending * globs because they're useless
        if (str.startsWith("*")) {
            str = str.substring(1);
            strLen--;
        }
        if (str.endsWith("*")) {
            str = str.substring(0, strLen - 1);
            strLen--;
        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : str.toCharArray()) {
            switch (currentChar) {
                case '*':
                    if (escaping) {
                        sb.append("\\*");
                    } else {
                        sb.append(".*");
                    }
                    escaping = false;
                    break;
                case '?':
                    if (escaping) {
                        sb.append("\\?");
                    } else {
                        sb.append('.');
                    }
                    escaping = false;
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(currentChar);
                    escaping = false;
                    break;
                case '\\':
                    if (escaping) {
                        sb.append("\\\\");
                        escaping = false;
                    } else {
                        escaping = true;
                    }
                    break;
                case '{':
                    if (escaping) {
                        sb.append("\\{");
                    } else {
                        sb.append('(');
                        inCurlies++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inCurlies > 0 && !escaping) {
                        sb.append(')');
                        inCurlies--;
                    } else if (escaping) {
                        sb.append("\\}");
                    } else {
                        sb.append("}");
                    }
                    escaping = false;
                    break;
                case ',':
                    if (inCurlies > 0 && !escaping) {
                        sb.append('|');
                    } else if (escaping) {
                        sb.append("\\,");
                    } else {
                        sb.append(",");
                    }
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
        }
        return sb.toString();
    }
    
    public static String indexToOrdinal(int idx) {
        int ord = idx+1;
        
        switch (idx) {
            case 0: return "1st";
            case 1: return "2nd";
            case 2: return "3rd";
            default: return ord + "th";
        }
                        
                    
    }
}
