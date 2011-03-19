package org.tal.redstonechips;

import org.tal.redstonechips.circuit.Circuit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
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
    private boolean madeBackup = false;

    public CircuitPersistence(RedstoneChips plugin) {
        rc = plugin;
    }

    public HashMap<Integer, Circuit> loadCircuits() {
        File file = getCircuitsFile();
        if (!file.exists()) { // create empty file if doesn't already exist
            try {
                file.createNewFile();
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.getMessage());
            }
        }

        Yaml yaml = new Yaml();

        HashMap<Integer, Circuit> circuits = new HashMap<Integer, Circuit>();

        try {
            List<Map<String, Object>> circuitsList = (List<Map<String, Object>>) yaml.load(new FileInputStream(file));
            if (circuitsList!=null) {
                for (Map<String,Object> circuitMap : circuitsList) {
                    try {
                        Circuit c = parseCircuitMap(circuitMap);
                        if (c.id==-1) c.id = circuits.size();
                        circuits.put(c.id, c);
                    } catch (IllegalArgumentException ie) {
                        rc.log(Level.WARNING, ie.getMessage() + ". Ignoring circuit.");
                        backupCircuitsFile();
                    } catch (InstantiationException ex) {
                        rc.log(Level.WARNING, ex.toString());
                        backupCircuitsFile();
                    } catch (IllegalAccessException ex) {
                        rc.log(Level.WARNING, ex.toString());
                        backupCircuitsFile();
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            rc.log(Level.SEVERE, "Circuits file '" + file + "' was not found.");
        }

        madeBackup = false;
        return circuits;
    }

    public void saveCircuits(HashMap<Integer, Circuit> circuits) {
        File file = new File(rc.getDataFolder(), circuitsFileName);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        Yaml yaml = new Yaml(options);

        List<Map<String,Object>> circuitMaps = new ArrayList<Map<String,Object>>();

        if (circuits==null) {
            rc.log(Level.WARNING, "No circuits were found. There was probably a loading error.");
            return;
        }
        for (Circuit c : circuits.values())
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
        map.put("id", c.id);

        return map;
    }

    private Circuit parseCircuitMap(Map<String,Object> map) throws InstantiationException, IllegalAccessException {
        String className = (String)map.get("class");
        World world = findWorld((String)map.get("world"));
        Circuit c = rc.getCircuitLoader().getCircuitInstance(className);
        c.world = world;
        c.activationBlock = getLocation(world, (List<Integer>)map.get("activationBlock"));
        c.outputs = getLocationArray(world, (List<List<Integer>>)map.get("outputs"));
        c.interfaceBlocks = getLocationArray(world, (List<List<Integer>>)map.get("interfaces"));
        c.structure = getLocationArray(world, (List<List<Integer>>)map.get("structure"));
        c.inputs = getInputPinsArray((List<List<Integer>>)map.get("inputs"), c);
        List<String> signArgs = (List<String>)map.get("signArgs");
        c.initCircuit(null, signArgs.toArray(new String[signArgs.size()]), rc);
        if (map.containsKey("id")) c.id = (Integer)map.get("id");
        c.loadState((Map<String,String>)map.get("state"));
        return c;
    }

    private List<Integer> makeBlockList(Location l) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(l.getBlockX());
        list.add(l.getBlockY());
        list.add(l.getBlockZ());

        return list;
    }

    private Object makeInputPinsList(InputPin[] inputs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (InputPin p : inputs)
            list.add(makeBlockList(p.getInputBlock()));
        return list;
    }

    private Object makeBlockListsList(Location[] vs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (Location l : vs)
            list.add(makeBlockList(l));
        return list;
    }

    private World findWorld(String worldName) {
        World w = rc.getServer().getWorld(worldName);

        if (w!=null) return w;
        else throw new IllegalArgumentException("World " + worldName + " was not found on the server.");
    }

    private Location getLocation(World w, List<Integer> coords) {
        return new Location(w, coords.get(0), coords.get(1), coords.get(2));
    }

    private Location[] getLocationArray(World w, List<List<Integer>> list) {
        List<Location> locations = new ArrayList<Location>();
        for (List<Integer> coords : list)
            locations.add(getLocation(w, coords));

        return locations.toArray(new Location[locations.size()]);
    }

    private InputPin[] getInputPinsArray(List<List<Integer>> list, Circuit c) {
        List<InputPin> inputs = new ArrayList<InputPin>();
        for (int i=0; i<list.size(); i++) {
            List<Integer> coords = list.get(i);
            inputs.add(new InputPin(c, new Location(c.world, coords.get(0), coords.get(1), coords.get(2)), i));
        }

        return inputs.toArray(new InputPin[inputs.size()]);
    }

    private void backupCircuitsFile() {
        if (madeBackup) return;

        try {
            File original = getCircuitsFile();
            File backup = getBackupFileName(original.getParentFile());

            rc.log(Level.INFO, "An error occurred while loading circuits state. To make sure you won't lose any circuit data, a backup copy of "
                + circuitsFileName + " is being created. The backup can be found at " + backup.getPath());
            copy(original, backup);
        } catch (IOException ex) {
            rc.log(Level.SEVERE, "Error while trying to write backup file: " + ex);
        }
        madeBackup = true;
    }

    private File getCircuitsFile() {
        return new File(rc.getDataFolder(), circuitsFileName);
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
}

    private File getBackupFileName(File parentFile) {
        String ext = ".BACKUP";
        File backup;
        int idx = 0;

        do {
            backup = new File(parentFile, circuitsFileName + ext + idx);
            idx++;
        } while (backup.exists());
        return backup;
    }
}
