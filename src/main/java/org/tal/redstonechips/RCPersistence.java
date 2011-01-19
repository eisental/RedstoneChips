package org.tal.redstonechips;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A bunch of static methods for saving and loading circuit states.
 *
 * @author Tal Eisenberg
 */
public class RCPersistence {

    public static Circuit stringToCircuit(String circuitString, RedstoneChips plugin) throws IllegalArgumentException, InstantiationException, IllegalAccessException {
        StringTokenizer t = new StringTokenizer(circuitString, "|");
        String name = t.nextToken();
        String sWorld = t.nextToken();
        String sActivation = t.nextToken();
        String sLast = t.nextToken();
        String sOutputBlock = t.nextToken();
        String sInputs = t.nextToken();
        String sOutputs = t.nextToken();
        String sStructure = t.nextToken();
        String sArgs = t.nextToken();
        String sState = t.nextToken();

        Circuit ret;

        ret = plugin.getCircuitInstance(name);

        World world = stringToWorld(plugin.getServer(), sWorld);
        ret.world = world;
        ret.activationBlock = stringToBlock(world, sActivation);
        ret.lastLineBlock = stringToBlock(world, sLast);
        ret.outputBlock = stringToBlock(world, sOutputBlock);
        ret.inputs = stringToBlockArray(world, sInputs);
        ret.outputs = stringToBlockArray(world, sOutputs);
        ret.structure = stringToBlockArray(world, sStructure);

        if (ret.initCircuit(null, stringArgsToArray(sArgs))) {
            ret.loadState(stateStringToMap(sState));
            return ret;
        }
        else return null;

    }

    /**
     * Creates a String representation of this circuit. Used when storing circuits to disk.
     *
     * @return String representation of this Circuit object.
     */
    public static String toFileString(Circuit c, RedstoneChips plugin) {
        // name|activatioBlock|lastLineBlock|inputs|outpus|structure|args
        Map<String,String> circuitState = c.saveState();
        String name = c.getClass().getSimpleName();
        String world = worldToString(c.activationBlock.getWorld(), plugin.getServer());
        String sActivation = blockToString(c.activationBlock);
        String sLast = blockToString(c.lastLineBlock);
        String sOutputBlock = blockToString(c.outputBlock);
        String sInputs = blockArrayToString(c.inputs);
        String sOutputs = blockArrayToString(c.outputs);
        String sSturcture = blockArrayToString(c.structure);
        String sArgs = argsArrayToString(c.args);
        String sState = stateMapToString(circuitState);
        return name + "|" + world + "|" + sActivation + "|" + sLast + "|" + sOutputBlock + "|"
                + sInputs + "|" + sOutputs + "|" + sSturcture + "|" + sArgs + "|" + sState;
    }

    private static Map<String,String> stateStringToMap(String state) {
        Map<String,String> ret = new HashMap<String,String>();
        if (state==null || state.equals("empty")) return ret;

        StringTokenizer dollar = new StringTokenizer(state, "$");
        while(dollar.hasMoreTokens()) {
            String equal = dollar.nextToken();
            int eqIdx = equal.indexOf("=");
            if (eqIdx==-1) throw new IllegalArgumentException("Bad syntax in state key=value set '" + equal + "'.");

            String key = equal.substring(0, eqIdx);
            String value = equal.substring(eqIdx+1);

            ret.put(key,value);
        }

        return ret;
    }

    private static String blockToString(Block b) {
        return b.getX()+","+b.getY()+","+b.getZ();
    }

    private static Block stringToBlock(World w, String s) {
        StringTokenizer t = new StringTokenizer(s, ",");
        int x = Integer.parseInt(t.nextToken());
        int y = Integer.parseInt(t.nextToken());
        int z = Integer.parseInt(t.nextToken());
        return w.getBlockAt(x, y, z);
    }

    private static Block[] stringToBlockArray(World w, String s) {
        if (s.trim().length()==0 || s.equals("empty")) return new Block[0];
        StringTokenizer t = new StringTokenizer(s, "/");
        Block[] blocks = new Block[t.countTokens()];
        int idx = 0;
        while(t.hasMoreTokens()) {
            blocks[idx] = stringToBlock(w, t.nextToken());
            idx++;
        }
        return blocks;
    }

    private static String blockArrayToString(Block[] blocks) {
        if (blocks.length==0) return "empty";

        String ret = "";
        for (Block b : blocks) {
            ret += blockToString(b) + "/";
        }
        return ret.substring(0, ret.length()-1);
    }

    private static String[] stringArgsToArray(String s) {
        if (s.trim().length()==0 || s.equals("empty")) return new String[0];
        StringTokenizer t = new StringTokenizer(s, "$");
        String[] ret = new String[t.countTokens()];
        int idx = 0;
        while (t.hasMoreTokens()) {
            ret[idx] = t.nextToken();
            idx++;

        }
        return ret;
    }

    private static World stringToWorld(Server s, String sWorld) {
        int idx = Integer.decode(sWorld);
        return s.getWorlds()[idx];
    }

    private static String worldToString(World world, Server s) {
        World[] worlds = s.getWorlds();
        for (int i=0; i<worlds.length; i++)
            if (worlds[i].getId()==world.getId()) return ""+i;

        return "ERR";
    }

    private static String argsArrayToString(String[] ss) {
        if (ss==null || ss.length==0) return "empty";
        String ret = "";
        for (String s : ss) {
            ret = ret.concat(s + "$");
        }
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

    private static String stateMapToString(Map<String,String> state) {
        // x=y$...$x=y
        if (state==null || state.isEmpty()) return "empty";
        String ret ="";
        for (String key : state.keySet()) {
            ret += key + "=" + state.get(key) + "$";
        }
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

}
