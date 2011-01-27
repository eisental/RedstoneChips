package org.tal.redstonechips;

import org.tal.redstonechips.circuit.Circuit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A bunch of static methods for saving and loading circuit states.
 *
 * @author Tal Eisenberg
 */
public class CircuitPersistence {
    private RedstoneChips rc;

    public final static String circuitsFileName = "redstonechips.circuits";

    public CircuitPersistence(RedstoneChips plugin) {
        rc = plugin;
    }

    public List<Circuit> loadCircuits() {
        File file = new File(rc.getDataFolder(), circuitsFileName);
        if (!file.exists()) { // create empty file if doesn't already exist
            try {
                file.createNewFile();
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.getMessage());
            }
        }

        Yaml yaml = new Yaml();

        List<Circuit> circuits = new ArrayList<Circuit>();

        try {
            List<Map<String, Object>> circuitsList = (List<Map<String, Object>>) yaml.load(new FileInputStream(file));
            if (circuitsList!=null) {
                for (Map<String,Object> circuitMap : circuitsList) {
                    try {
                        Circuit c = parseCircuitMap(circuitMap);
                        circuits.add(c);
                    } catch (InstantiationException ex) {
                        rc.log(Level.SEVERE, ex.toString());
                    } catch (IllegalAccessException ex) {
                        rc.log(Level.SEVERE, ex.toString());
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            rc.log(Level.SEVERE, "Circuits file '" + file + "' was not found.");
        }

        return circuits;
    }

    public void saveCircuits(List<Circuit> circuits) {
        File file = new File(rc.getDataFolder(), circuitsFileName);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        Yaml yaml = new Yaml(options);

        List<Map<String,Object>> circuitMaps = new ArrayList<Map<String,Object>>();

        for (Circuit c : circuits)
            circuitMaps.add(this.circuitToMap(c));
        
        try {
            yaml.dump(circuitMaps, new FileWriter(file));
            rc.log(Level.INFO, "Saved circuits state to file.");
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.getMessage());
        }
    }

    private Map<String, Object> circuitToMap(Circuit c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("class", c.getCircuitClass());
        map.put("world", c.activationBlock.getWorld().getName());
        map.put("activationBlock", makeBlockList(c.activationBlock));
        map.put("inputs", makeBlockListsList(c.inputs));
        map.put("outputs", makeBlockListsList(c.outputs));
        map.put("interfaces", makeBlockListsList(c.interfaceBlocks));
        map.put("structure", makeBlockListsList(c.structure));
        map.put("signArgs", c.args);
        map.put("state", c.saveState());
        return map;
    }

    private Circuit parseCircuitMap(Map<String,Object> map) throws InstantiationException, IllegalAccessException {
        String className = (String)map.get("class");
        World world = findWorld((String)map.get("world"));

        Circuit c = rc.getCircuitLoader().getCircuitInstance(className);
        c.activationBlock = getBlock((List<Integer>)map.get("activationBlock"), world);
        c.inputs = getBlockArray((List<List<Integer>>)map.get("inputs"), world);
        c.outputs = getBlockArray((List<List<Integer>>)map.get("outputs"), world);
        c.interfaceBlocks = getBlockArray((List<List<Integer>>)map.get("interfaces"), world);
        c.structure = getBlockArray((List<List<Integer>>)map.get("structure"), world);
        List<String> signArgs = (List<String>)map.get("signArgs");
        c.initCircuit(null, signArgs.toArray(new String[signArgs.size()]), rc);
        c.loadState((Map<String,String>)map.get("state"));
        return c;
    }

    private List<Integer> makeBlockList(Block b) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(b.getX());
        list.add(b.getY());
        list.add(b.getZ());

        return list;
    }

    private Object makeBlockListsList(Block[] inputs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (Block b : inputs)
            list.add(makeBlockList(b));
        return list;
    }

    private World findWorld(String worldName) {
        for (World w : rc.getServer().getWorlds())
            if (w.getName().equals(worldName)) return w;

        throw new IllegalArgumentException("World " + worldName + " was not found on the server.");
    }

    private Block getBlock(List<Integer> coords, World world) {
        return world.getBlockAt(coords.get(0), coords.get(1), coords.get(2));
    }

    private Block[] getBlockArray(List<List<Integer>> list, World world) {
        List<Block> blocks = new ArrayList<Block>();
        for (List<Integer> coords : list)
            blocks.add(getBlock(coords, world));

        return blocks.toArray(new Block[blocks.size()]);
    }
}
