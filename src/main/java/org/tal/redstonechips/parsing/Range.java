package org.tal.redstonechips.parsing;

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



/**
 *
 * @author Main
 */
public class Range {
    double num1 = 0;
    double num2 = 0;
    boolean has1 = false;
    boolean has2 = false;
    
    public enum Type {
        CLOSED_ONLY("closed-only"),
        OPEN_ALLOWED("open-allowed");
        
        private String name;
        Type(String name) { this.name = name; }
        
        public String getName() { return name; }

        public static Type findType(String name) {
            for (Type type : Type.values())
                if (type.getName().equals(name))
                    return type;
            throw new IllegalArgumentException("Unknown range type: " + name);
        }
    }

    public Range(double[] range) throws IllegalArgumentException {
        if (range.length!=2) throw new IllegalArgumentException("range array must have 2 elements: " + range);
        num1 = range[0];
        num2 = range[1];
        has1 = true;
        has2 = true;
    }

    public Range(String range, Type type) throws IllegalArgumentException {
        int indexOfDots = range.indexOf("..");
        if (indexOfDots==-1) throw new IllegalArgumentException("Invalid range format: " + range);
        String sFirst = range.substring(0, indexOfDots).trim();
        String sSecond = range.substring(indexOfDots+2).trim();
        
        try {
            if (sFirst.length()>0) {
                num1 = Double.parseDouble(sFirst);
                has1 = true;
            } else if (type==Type.CLOSED_ONLY)
                throw new IllegalArgumentException("Invalid range format:" + range + " (open range not allowed)");
            if (sSecond.length()>0) {
                num2 = Double.parseDouble(sSecond);
                has2 = true;
            } else if (type==Type.CLOSED_ONLY)
                throw new IllegalArgumentException("Invalid range format:" + range + " (open range not allowed)");
        } catch (NumberFormatException ne) {
            throw new IllegalArgumentException("Invalid range format: " + range);
        }
        
    }
    
    public boolean hasLowerLimit() { return has1; }
    public boolean hasUpperLimit() { return has2; }
    
    public double[] getOrderedRange() {
        if (has1 && has2 && num1>num2)
            return new double[] { num2, num1 };
        else return new double[] { num1, num2 };
    }
    
    
    public double[] getRange() {
        return new double[] {num1, num2 };
    }
    
    public int getDirection() {
        if (!has1 || !has2) return 0;
        else if (num1>num2) return -1;
        else return 1;
    }

    @Override
    public String toString() {
        return (has1?""+num1:"") + ".." + (has2?""+num2:"");
    }
}
