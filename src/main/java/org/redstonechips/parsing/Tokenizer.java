/*
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tal Eisenberg
 */
public class Tokenizer {
    private static char[] stringNesters = { '\'', '"'};
    private static char[][] parens = new char[][] { {'[',']'}, {'(', ')'}, {'{', '}'} };
    private String[] tokens;
    private char delimiter;
    private int currenttoken = 0;
    
    public Tokenizer(char delimiter) {
        this.delimiter = delimiter;
    }
    
    public Tokenizer(String str, char delimiter) {
        this(delimiter);
        parse(str);
    }
    
    public void parse(String str) throws IllegalArgumentException {
        List<Integer> delimiters = new ArrayList<Integer>();

        str = str.trim();
        
        //1st stage: find every delimiter which is not between balanced parenthesis or string nesters.
        int idx = 0;
        while (idx<str.length()) {
            char chr = str.charAt(idx);
            
            //jump over string nesters.
            for (char stringNester : stringNesters) {
                if (chr==stringNester) {
                    //find next stringNester in str and jump to there
                    idx = str.indexOf(stringNester, idx+1);
                    break;
                }
            }
            
            //jump over parenthesis.
            for (char[] par : parens) {
                if (chr==par[0]) {
                    //found an opening parenthesis. jump to closing parenthesis.
                    int nidx = balanced(str, idx, par[0], par[1]);            
                    if (nidx==-1) { //balancing parenthesis was not found. error!
                        int start = 0, end = str.length();
                        boolean preDots = false, postDots = false;
                        if ((end-idx)>30) { end = idx + 30; postDots = true; }
                        if (idx>20) { start = idx - 20; preDots = true; }
                        String loc = (preDots?"...":"") + str.substring(start, idx) + " -->" + chr + str.substring(idx+1, end) + (postDots?"...":"");
                        throw new IllegalArgumentException("Expected " + par[1] + " after: " + loc);
                    }
                    idx = nidx;
                    break;
                } else if (chr==par[1]) {
                    //found a closing parenthesis. error!
                    int start = 0, end = str.length();
                    boolean preDots = false, postDots = false;
                    if ((end-idx)>30) { end = idx + 30; postDots = true; }
                    if (idx>20) { start = idx - 20; preDots = true; }                    
                    String loc = (preDots?"...":"") + str.substring(start, idx) + " -->" + chr + str.substring(idx+1, end) + (postDots?"...":"");
                    throw new IllegalArgumentException("Expected " + par[0] + " before: " + loc);
                }
            }

            //if current char is a delimiter, add it to the list.
            if (chr==delimiter)
                delimiters.add(idx);
            
            //increment by 1 and work on next character
            idx++;
        }
        
        //2nd stage: take the delimiters list and split around it into tokens.
        List<String> tokensList = new ArrayList<String>();
        if (delimiters.size()>0) {
            int splitFrom = 0;
            for (int i=0; i<delimiters.size(); i++) {
                int thisDelim = (Integer)delimiters.get(i);
                tokensList.add(str.substring(splitFrom, thisDelim).trim());
                splitFrom = thisDelim + 1;
            }
            if (splitFrom<str.length()) {
                String lastToken = str.substring(splitFrom).trim();
                tokensList.add(lastToken);
            }
        } else {
            tokensList.add(str);
        }
        
        tokens = tokensList.toArray(new String[0]);        
    }

    public static int balanced(String str, int idx, char openpar, char closepar) {
        int numofstarts = 0, numofends = 0;
        if (idx>=0) {
            int i=idx; 
            while (i<str.length()) {
                for (char stringNester : stringNesters) {
                    if (str.charAt(i)==stringNester) {
                        i = str.indexOf(stringNester, i+1);
                        if (i==-1) { // end string not found.
                            int start = 0, end = str.length();
                            boolean preDots = false, postDots = false;
                            if ((end-idx)>30) { end = idx + 30; postDots = true; }
                            if (idx>20) { start = idx - 20; preDots = true; }                    
                            String loc = (preDots?"...":"") + str.substring(start, idx) + " -->" + str.substring(idx+1, end) + (postDots?"...":"");
                            throw new IllegalArgumentException("Unterminated string after: " + loc);
                        }
                    }
                }
                if (str.charAt(i)==openpar) numofstarts++;
                if (str.charAt(i)==closepar) numofends++;
                if (numofstarts==numofends) // we found it
                    return i;
                i++;
            }
        }
        return -1;
    }
    
    /**
     * Returns a String[] array containing all the tokens in the tokenizer.
     */
    public String[] getTokens() {
        return tokens;
    }

    /**
     * Returns the number of tokens in the tokenizer.
     */
    public int countTokens() { return tokens.length; }
    
    /**
     * Returns false if the last token was reached when calling nextToken()
     */
    public boolean hasMoreTokens() { return (currenttoken<(tokens.length)); }

    /**
     * Returns the tokens of this tokenizer sequentially.
     */
    public String nextToken() {
        if (currenttoken<tokens.length) {
            String token = tokens[currenttoken];
            currenttoken++;
            return token;
        } else return null;
    }

    @Override
    public String toString() {
        if (tokens!=null) return this.tokens.toString();
        else return "<not parsed>";
    }
}
