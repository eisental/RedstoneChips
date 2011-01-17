package org.tal.redstonechips.circuits;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RCPlugin;





/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public abstract class Circuit {
    public final static int signPostType = 63;
    public final static int wallSignType = 68;
    public final static int redstoneWire = 55;

    public String[] args;
    public Block[] inputs;
    public Block[] outputs;
    public Block[] structure;
    public Block activationBlock;
    public Block lastLineBlock;
    public Block outputBlock;

    private World world;

    protected BitSet inputBits, outputBits;

    public final boolean initCircuit(World w, Player player, String[] args) {
        inputBits = new BitSet(inputs.length);
        outputBits = new BitSet(outputs.length);
        this.args = args;
        if (w!=null) this.world = w;
        else this.world = player.getWorld();

        return init(player, args);
    }

    public boolean redstoneChange(Block block, boolean newVal) {
        for (int i=0; i<inputs.length; i++) {
            if (block.equals(inputs[i])) {
                inputBits.set(i, newVal);
                inputChange(i, newVal);
                return true;
            }
        }

        return false;
    }


    public abstract void inputChange(int inIdx, boolean newLevel);

    /**
     * Called when right-clicking the sign or when the plugin read circuit data from file.
     * @param player The player that right-clicked the sign
     * @param args Any words on the sign after the circuit type.
     * @return true if the init was successful, false if an error occurred.
     */
    protected abstract boolean init(Player player, String[] args);

    protected Map<String,String> saveState() { return new HashMap<String,String>(); }

    protected void loadState(Map<String,String> state) {}

    public void circuitDestroyed() {}

    /**
     * Sets the physical state of one of the outputs.
     * 
     * @param outIdx Output index. 0 for first output and so on.
     * @param on The new state of the output.
     */
    public void sendOutput(int outIdx, boolean on) {
        outputBits.set(outIdx, on);
        outputs[outIdx].setType((on?Material.REDSTONE_TORCH_ON:Material.REDSTONE_WIRE));
    }

    protected void sendInt(int startOutIdx, int length, int value) {
        BitSet bits = intToBitSet(value, length);
        sendBitSet(startOutIdx, length, bits);
    }

    protected void sendBitSet(int startOutIdx, int length, BitSet bits) {        
        for (int i=length-1; i>=0; i--) {
            sendOutput(startOutIdx+i, bits.get(i));
        }
    }

    protected void sendBitSet(BitSet bits) {
        sendBitSet(0, outputs.length, bits);
    }

    protected static int bitSetToUnsignedInt(BitSet b, int startBit, int length) {
        int val = 0;
        for (int i=0; i<length; i++) {
            if (b.get(i+startBit)) val += Math.pow(2,i);
        }
        
        return val;
    }

    protected static int bitSetToSignedInt(BitSet b, int startBit, int length) {
        // treats the bit set as a two's complement encoding binary number.
        int signed = -(b.get(startBit+length-1)?1:0) * (int)Math.pow(2, length-1);
        for (int i=0; i<length-1; i++) {
            if (b.get(startBit+i)) signed += Math.pow(2, i);
        }

        return signed;
    }

    protected static BitSet shiftLeft(BitSet s, int length) {
        for (int i=length; i>0; i--) {
            s.set(i, s.get(i-1));
        }
        s.set(0, false);

        return s;
    }

    protected static BitSet shiftRight(BitSet s, int length, boolean logical) {
        for (int i=0; i<length-1; i++) {
            s.set(i, s.get(i+1));
        }

        if (logical) s.set(length-1, 0);

        return s;
    }

    protected static BitSet intToBitSet(int value, int size) {
        BitSet bits = new BitSet(size);
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

    public static Circuit getInstance(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class c = Circuit.class.getClassLoader().loadClass(name);
        if (Circuit.class.isAssignableFrom(c)) {
            Circuit ret = (Circuit)c.newInstance();
            return ret;
        } else throw new ClassNotFoundException("Unknown circuit type: " + name + " (Not a circuit class).");
    }

    public static Circuit fromFileString(String circuitString, World world) throws IllegalArgumentException, InstantiationException, IllegalAccessException {
        StringTokenizer t = new StringTokenizer(circuitString, "|");
        String name = t.nextToken();
        String sActivation = t.nextToken();
        String sLast = t.nextToken();
        String sOutputBlock = t.nextToken();
        String sInputs = t.nextToken();
        String sOutputs = t.nextToken();
        String sStructure = t.nextToken();
        String sArgs = t.nextToken();
        String sState = t.nextToken();

        Circuit ret;
        try {
            ret = Circuit.getInstance(RCPlugin.circuitPackage + "." + name);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Circuit.class.getName()).log(Level.SEVERE, "Circuit class " + name + " was not found.", ex);
            return null;
        }
        ret.activationBlock = stringToBlock(world, sActivation);
        ret.lastLineBlock = stringToBlock(world, sLast);
        ret.outputBlock = stringToBlock(world, sOutputBlock);
        ret.inputs = stringToBlockArray(world, sInputs);
        ret.outputs = stringToBlockArray(world, sOutputs);
        ret.structure = stringToBlockArray(world, sStructure);

        if (ret.initCircuit(world, null, stringArgsToArray(sArgs))) {
            ret.loadState(stateStringToMap(sState));
            return ret;
        }
        else return null;

    }

    public String toFileString() {
        // name|activatioBlock|lastLineBlock|inputs|outpus|structure|args
        Map<String,String> circuitState = saveState();
        String name = this.getClass().getSimpleName();
        String sActivation = blockToString(activationBlock);
        String sLast = blockToString(lastLineBlock);
        String sOutputBlock = blockToString(outputBlock);
        String sInputs = blockArrayToString(inputs);
        String sOutputs = blockArrayToString(outputs);
        String sSturcture = blockArrayToString(structure);
        String sArgs = argsArrayToString(args);
        String sState = stateMapToString(circuitState);
        return name + "|" + sActivation + "|" + sLast + "|" + sOutputBlock + "|" 
                + sInputs + "|" + sOutputs + "|" + sSturcture + "|" + sArgs + "|" + sState;
    }

    protected static String blockToString(Block b) {
        return b.getX()+","+b.getY()+","+b.getZ();
    }

    protected static Block stringToBlock(World w, String s) {
        StringTokenizer t = new StringTokenizer(s, ",");
        int x = Integer.parseInt(t.nextToken());
        int y = Integer.parseInt(t.nextToken());
        int z = Integer.parseInt(t.nextToken());
        return w.getBlockAt(x, y, z);
    }

    protected static Block[] stringToBlockArray(World w, String s) {
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

    protected static String blockArrayToString(Block[] blocks) {
        if (blocks.length==0) return "empty";
        
        String ret = "";
        for (Block b : blocks) {
            ret += blockToString(b) + "/";
        }
        return ret.substring(0, ret.length()-1);
    }

    protected static String[] stringArgsToArray(String s) {
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

    protected static String argsArrayToString(String[] ss) {
        if (ss==null || ss.length==0) return "empty";
        String ret = "";
        for (String s : ss) {
            ret = ret.concat(s + "$");
        }
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

    protected static String stateMapToString(Map<String,String> state) {
        // x=y$...$x=y
        if (state==null || state.isEmpty()) return "empty";
        String ret ="";
        for (String key : state.keySet()) {
            ret += key + "=" + state.get(key) + "$";
        }
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

    protected static Map<String,String> stateStringToMap(String state) {
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

    protected static Map<String,String> storeBitSet(Map<String,String> map, String key, BitSet bits, int length) {
        String sbits = "";
        for (int i=0; i<length; i++)
            sbits += (bits.get(i)?"1":"0");

        map.put(key, sbits);
        return map;
    }

    protected static BitSet loadBitSet(Map<String, String> map, String key, int length) {
        BitSet bits = new BitSet(length);
        String sbits = map.get(key);
        if (sbits==null) return null;

        for (int i=0; i<length; i++) {
            bits.set(i, (sbits.charAt(i)=='1'));
        }
        return bits;
    }
}
