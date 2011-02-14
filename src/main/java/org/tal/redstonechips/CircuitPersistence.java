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
import org.bukkit.util.BlockVector;
import org.tal.redstonechips.circuit.InputPin;
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
                    } catch (IllegalArgumentException ie) {
                        rc.log(Level.WARNING, ie.getMessage() + ". Ignoring circuit.");
                    } catch (InstantiationException ex) {
                        rc.log(Level.WARNING, ex.toString());
                    } catch (IllegalAccessException ex) {
                        rc.log(Level.WARNING, ex.toString());
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
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.getMessage());
        }
    }

    private Map<String, Object> circuitToMap(Circuit c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("class", c.getCircuitClass());
        map.put("world", c.world.getName());
        map.put("activationBlock", makeBlockList(c.activationBlock));
        map.put("inputs", makeInputPinsList(c.inputs));
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
        c.world = world;
        c.activationBlock = getBlockVector((List<Integer>)map.get("activationBlock"));
        c.outputs = getBlockVectorArray((List<List<Integer>>)map.get("outputs"));
        c.interfaceBlocks = getBlockVectorArray((List<List<Integer>>)map.get("interfaces"));
        c.structure = getBlockVectorArray((List<List<Integer>>)map.get("structure"));
        c.inputs = getInputPinsArray((List<List<Integer>>)map.get("inputs"), c);
        List<String> signArgs = (List<String>)map.get("signArgs");
        c.initCircuit(null, signArgs.toArray(new String[signArgs.size()]), rc);
        c.loadState((Map<String,String>)map.get("state"));
        return c;
    }

    private List<Integer> makeBlockList(BlockVector v) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(v.getBlockX());
        list.add(v.getBlockY());
        list.add(v.getBlockZ());

        return list;
    }

    private Object makeInputPinsList(InputPin[] inputs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (InputPin p : inputs)
            list.add(makeBlockList(p.getInputBlock()));
        return list;
    }

    private Object makeBlockListsList(BlockVector[] vs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (BlockVector v : vs)
            list.add(makeBlockList(v));
        return list;
    }

    private World findWorld(String worldName) {
        for (World w : rc.getServer().getWorlds())
            if (w.getName().equals(worldName)) return w;

        throw new IllegalArgumentException("World " + worldName + " was not found on the server.");
    }

    private BlockVector getBlockVector(List<Integer> coords) {
        return new BlockVector(coords.get(0), coords.get(1), coords.get(2));
    }

    private BlockVector[] getBlockVectorArray(List<List<Integer>> list) {
        List<BlockVector> vectors = new ArrayList<BlockVector>();
        for (List<Integer> coords : list)
            vectors.add(getBlockVector(coords));

        return vectors.toArray(new BlockVector[vectors.size()]);
    }

    private InputPin[] getInputPinsArray(List<List<Integer>> list, Circuit c) {
        List<InputPin> inputs = new ArrayList<InputPin>();
        for (int i=0; i<list.size(); i++) {
            List<Integer> coords = list.get(i);
            inputs.add(new InputPin(c, new BlockVector(coords.get(0), coords.get(1), coords.get(2)), i));
        }

        return inputs.toArray(new InputPin[inputs.size()]);
    }
}
