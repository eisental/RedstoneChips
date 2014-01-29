
package org.redstonechips;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.redstonechips.chip.io.IOBlock;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author taleisenberg
 */
public abstract class Serializer {

    public enum Key { 
        CLASS("c", "class"), 
        WORLD("w", "world"), 
        SIGN("sign", "activationBlock"),
        INPUTS("inp", "inputs"),
        OUTPUTS("out", "outputs"),
        INTERFACES("int", "interfaces"),
        STRUCTURE("str", "structure"),
        ARGS("args", "signArgs"),
        STATE("state", "state"),
        ID("id", "id"),
        NAME("name", "name"),
        DISABLED("dis", "disabled"),
        OUTPUT_BITS("bits", "outputBits"),
        CHAN_NAME("name", null),
        CHAN_STATE("state", null),
        CHAN_OWNERS("owners", null),
        CHAN_USERS("users", null);
        
        public String key, longKey; // longKey is for backwards compatibility. It's not used anymore.    
        Key(String key, String longKey) {
            this.key = key;
            this.longKey = longKey;
        }        
    }
    
    public static List<Integer> makeBlockList(Location l) {
        List<Integer> list = new ArrayList<>();
        list.add(l.getBlockX());
        list.add(l.getBlockY());
        list.add(l.getBlockZ());

        return list;
    }

    public static List<List<Integer>> makeIOBlockList(IOBlock[] blocks) {
        List<List<Integer>> list = new ArrayList<>();
        for (IOBlock b : blocks)
            list.add(makeBlockList(b.getLocation()));
        return list;        
    }    
    
    public static List<List<Integer>> makeBlockListsList(Location[] vs) {
        List<List<Integer>> list = new ArrayList<>();
        if(vs!=null)
          for (Location l : vs)
            list.add(makeBlockList(l));
        return list;
    }    
        
    public static boolean[] mapToBooleanArray(Map<String, String> map, String key) {
        String sbits = map.get(key);
        if (sbits==null) return null;
        else return BooleanArrays.fromString(sbits);        
    }

    public static Map<String, String> booleanArrayToMap(Map<String, String> map, String key, boolean[] bits) {    
        return booleanArrayToMap(map, key, bits, bits.length);
    }
    
    public static Map<String, String> booleanArrayToMap(Map<String, String> map, String key, boolean[] bits, int length) {
        map.put(key, BooleanArrays.toString(bits, length));
        return map;
    }
    
    public abstract Map<String, Object> serialize(Object o);
    public abstract Object deserialize(Map<String, Object> m);
}
