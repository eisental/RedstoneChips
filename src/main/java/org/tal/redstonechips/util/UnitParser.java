package org.tal.redstonechips.util;

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



import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Main
 */
public class UnitParser {

    public final static Pattern NUMBERUNIT_PATTERN = Pattern.compile("^[-+]?\\d*\\.?\\d*[a-zA-Z]*");
    
    public enum Unit {
        NANO("nano", "n", " ") {
            public double toVal(double val) {
                return val * 1.0e-9;
            }
            
            public double apply(double val) {
                return val * 1.0e9;
            }
        },
        MICRO("micro", "mic", " ") {
            public double toVal(double val) {
                return val * 1.0e-6;
            }
            
            public double apply(double val) {
                return val * 1.0e6;
            }
        },
        MILLI("milli", "m", " ") {
            public double toVal(double val) {
                return val / 1000.0;
            }
            
            public double apply(double val) {
                return val * 1000.0;
            }
        },
        CENT("cent", "c", " ") {
            public double toVal(double val) {
                return val / 100.0;
            }
            
            public double apply(double val) {
                return val * 100.0;
            }
        },
        KILO("kilo", "k", " ") {
            public double toVal(double val) {
                return val * 1000.0;
            }
            
            public double apply(double val) {
                return val / 1000.0;
            }
        },
        MEGA("mega", "M", " ") {
            public double toVal(double val) {
                return val * 1000000.0;
            }
            
            public double apply(double val) {
                return val / 1000000.0;
            }
        },
        GIGA("giga", "G", " ") {
            public double toVal(double val) {
                return val * 1000000000.0;
            }
            
            public double apply(double val) {
                return val / 1000000000.0;
            }
        },
        TERA("tera", "T", " ") {
            public double toVal(double val) {
                return val * 1000000000000.0;
            }
            
            public double apply(double val) {
                return val / 1000000000000.0;
            }
        },
        SECOND("sec", "s", "s") {
            public double toVal(double val) {
                return val * 1000.0;
            }
            
            public double apply(double val) {
                return val / 1000.0;
            }
        }, 
        MINUTE("min", " ", "s") {
            public double toVal(double val) {
                return val * 60000.0;
            }
            
            public double apply(double val) {
                return val / 60000.0;
            }
        }, HOUR("hour", "hr", "s") {
            public double toVal(double val) {
                return val * 3600000.0;
            }
            
            public double apply(double val) {
                return val / 3600000.0;
            }
        }, DAY("day", " ", "s") {
            public double toVal(double val) {
                return val * 86400000.0;
            }
            
            public double apply(double val) {
                return val / 86400000.0;
            }
        }, WEEK("week", "wk", "s") {
            public double toVal(double val) {
                return val * 604800000.0;
            }
            
            public double apply(double val) {
                return val / 604800000.0;
            }
        }, YEAR("year", "yr", "s") {
            public double toVal(double val) {
                return val * 31536000000.0;
            }
            
            public double apply(double val) {
                return val / 31536000000.0;
            }
        }, BPM("bpm", " ", " ") {
            public double toVal(double val) {
                return 60000.0/val;
            }
            
            public double apply(double val) {
                return 60000.0/val;
            }
        }, HERTZ("hertz", "hz", " ") {
            public double toVal(double val) {
                return 1000.0/val;
            }
            
            public double apply(double val) {
                return 1000.0/val;
            }
        };
        
        private String name, abb, plural;
        
        Unit(String name, String abb, String plural) {
            this.name = name;
            this.abb = abb;
            this.plural = plural;
        }
        
        public abstract double toVal(double val);
        public abstract double apply(double val);

        public boolean matches(String l) {
            l = l.trim();
            if (l.length()>1) {
                l = l.toLowerCase();
                if (l.equals(name) || l.equals(abb)) return true;
                else if (l.equals(name + plural)) return true;
                else if (abb.length()>1 && l.equals(abb + plural)) return true;
                else return false;
            } else return (l.equals(name) || l.equals(abb));
        }
                
    }
    
    public static Unit findUnit(String unit) throws IllegalArgumentException {
        for (Unit u : Unit.values()) {
            if (u.matches(unit)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Unknown unit name: " + unit);
    }

    public static double parse(String val) throws IllegalArgumentException {
        val = val.trim();
        // check if val contains anything that doesn't match numberunit pattern
        if (!NUMBERUNIT_PATTERN.matcher(val).matches())
            throw new NumberFormatException();
        
        //look for compound time
        StringTokenizer compound = new StringTokenizer(val, "-");
        if (compound.countTokens()>1) {
            double retval = 0;
            while(compound.hasMoreTokens())
                retval += parse(compound.nextToken());
            return retval;
        }
        Matcher m = ParsingAid.NUMBER_PATTERN.matcher(val);
        double value = -1;
        String unit = "";
        
        if (!m.find()) { throw new IllegalArgumentException("Bad unit value syntax: " + val); }
        value = Double.parseDouble(val.substring(m.start(), m.end()).trim());
        if (m.end()<val.length()) unit = val.substring(m.end()).trim();
        
        return UnitsToVal(value, parseUnitStack(unit));

        
    }
    
    public static Unit[] parseUnitStack(String stack) throws IllegalArgumentException {
        List units = new ArrayList();
        //parses in the format of unit.unit.....unit and (a)(a)(unit) (giga.years)
        StringTokenizer tokens = new StringTokenizer(stack, ".");
        while (tokens.hasMoreElements()) {
            String token = tokens.nextToken();
            try { 
                units.add(findUnit(token));
            } catch (IllegalArgumentException ie) { 
                String abbstack = token;
                while (true) {
                    String abbUnit = abbstack.substring(0, 1);
                    try {
                        units.add(findUnit(abbstack));
                        break;
                    } catch (IllegalArgumentException ixe) { 
                        try {
                            units.add(findUnit(abbUnit));
                            abbstack = abbstack.substring(1);                            
                        } catch (IllegalArgumentException xxe) {
                            String uniterror;
                            if (abbstack.equals(abbUnit)) uniterror = abbstack;
                            else uniterror = abbstack + " or " + abbUnit;
                            throw new IllegalArgumentException("Unknown unit name: " + uniterror);
                        }
                    }
                }
            }
            
        }
        return (Unit[])units.toArray(new Unit[0]);
    }
    
    public static double UnitsToVal(double value, Unit[] units) {
        double ret = value;
        for (Unit u : units) {
            ret = u.toVal(ret);
        }
        return ret;
    }
    
    public static double applyUnits(double value, Unit[] units) {
        double ret = value;
        for (Unit u : units) {
            ret = u.apply(value);
        }
        return ret;
    }
    
}
