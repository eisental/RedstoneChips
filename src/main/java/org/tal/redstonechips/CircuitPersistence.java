package org.tal.redstonechips;

import java.io.BufferedWriter;
import org.tal.redstonechips.circuit.Circuit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.util.ChunkLocation;
import org.tal.redstonechips.channel.BroadcastChannel;
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

    /**
     * Used to prevent saving state more than once per game tick.
     */
    private boolean dontSaveCircuits = false;

    private Runnable dontSaveCircuitsReset = new Runnable() {
        @Override
        public void run() {
            dontSaveCircuits = false;
        }
    };

    public CircuitPersistence(RedstoneChips plugin) {
        rc = plugin;
    }

    public void loadCircuits() {
        File file = getCircuitsFile();
        if (!file.exists()) { // create empty file if doesn't already exist
            try {
                file.createNewFile();
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.getMessage());
            }
        }

        Yaml yaml = new Yaml();

        try {
            rc.log(Level.INFO, "Reading circuits file...");
            List<Map<String, Object>> circuitsList = (List<Map<String, Object>>) yaml.load(new FileInputStream(file));
            List<Map<String, Object>> broadcastChannels = new ArrayList<Map<String, Object>>();

            rc.log(Level.INFO, "Activating circuits...");
            if (circuitsList!=null) {
                for (Map<String,Object> circuitMap : circuitsList) {
                    try {
                        if (!((String)circuitMap.get("class")).equals("BC")) {
                            compileCircuitFromMap(circuitMap);
                        } else {
                            broadcastChannels.add(circuitMap);
                        }
                        
                    } catch (IllegalArgumentException ie) {
                        rc.log(Level.WARNING, ie.getMessage() + ". Ignoring circuit.");
                        backupCircuitsFile();
                        ie.printStackTrace();
                    } catch (InstantiationException ex) {
                        rc.log(Level.WARNING, ex.toString() + ". Ignoring circuit.");
                        backupCircuitsFile();
                        ex.printStackTrace();
                    } catch (IllegalAccessException ex) {
                        rc.log(Level.WARNING, ex.toString() + ". Ignoring circuit.");
                        backupCircuitsFile();
                        ex.printStackTrace();
                    } catch (Throwable t) {
                        rc.log(Level.SEVERE, t.toString() + ". Ignoring circuit.");
                        backupCircuitsFile();
                        t.printStackTrace();
                    }
                }
                
                if (!broadcastChannels.isEmpty()) {
                    for (Map<String,Object> channelMap : broadcastChannels) {
                        configureChannelFromMap(channelMap);
                    }
                }
            }

            rc.log(Level.INFO, "Done. Loaded " + rc.getCircuitManager().getCircuits().size() + " chips.");

        } catch (FileNotFoundException ex) {
            rc.log(Level.SEVERE, "Circuits file '" + file + "' was not found.");
        }

        madeBackup = false;
    }

    public void saveCircuits() {
        if (dontSaveCircuits) return;
        
        rc.getCircuitManager().checkCircuitsIntegrity();

        Map<Integer, Circuit> circuits = rc.getCircuitManager().getCircuits();
        rc.log(Level.INFO, "Saving " + circuits.size() + " circuits state to file...");
        dontSaveCircuits = true;
        rc.getServer().getScheduler().scheduleAsyncDelayedTask(rc, dontSaveCircuitsReset, 1);

        File file = new File(rc.getDataFolder(), circuitsFileName);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        List<Map<String,Object>> circuitMaps = new ArrayList<Map<String,Object>>();

        for (Circuit c : circuits.values()) {
            circuitMaps.add(this.circuitToMap(c));
            c.save();
        }
        
        for (BroadcastChannel channel : rc.broadcastChannels.values()) {
            if (channel.isProtected()) {
                circuitMaps.add(this.channelToMap(channel));
            }
        }
        
        try {
            yaml.dump(circuitMaps, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.getMessage());
        }
    }

    private Map<String, Object> circuitToMap(Circuit c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("class", c.getCircuitClass());
        map.put("world", c.world.getName());
        map.put("activationBlock", makeBlockList(c.activationBlock));
        map.put("chunk", makeChunksList(c.circuitChunks));
        map.put("inputs", makeInputPinsList(c.inputs));
        map.put("outputs", makeBlockListsList(c.outputs));
        map.put("interfaces", makeBlockListsList(c.interfaceBlocks));
        map.put("structure", makeBlockListsList(c.structure));
        map.put("signArgs", c.args);
        map.put("state", c.getInternalState());
        map.put("id", c.id);

        return map;
    }

    private Map<String, Object> channelToMap(BroadcastChannel c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("class", "BC");
        map.put("name", c.name);
        map.put("owners", c.owners);
        map.put("users", c.users);
        return map;
    }

    private Circuit compileCircuitFromMap(Map<String,Object> map) throws InstantiationException, IllegalAccessException {

        String className = (String)map.get("class");
        World world = findWorld((String)map.get("world"));
        Circuit c = rc.getCircuitLoader().getCircuitInstance(className);
        c.world = world;
        c.activationBlock = getLocation(world, (List<Integer>)map.get("activationBlock"));
        c.outputs = getLocationArray(world, (List<List<Integer>>)map.get("outputs"));
        c.interfaceBlocks = getLocationArray(world, (List<List<Integer>>)map.get("interfaces"));
        c.structure = getLocationArray(world, (List<List<Integer>>)map.get("structure"));
        c.inputs = getInputPinsArray((List<List<Integer>>)map.get("inputs"), c);
        
        if (map.containsKey("chunks")) {
            c.circuitChunks = getChunkLocations(world, (List<List<Integer>>)map.get("chunks"));
        } else {
            c.circuitChunks = rc.getCircuitManager().findCircuitChunks(c);
        }

        List<String> argsList = (List<String>)map.get("signArgs");
        String[] signArgs = argsList.toArray(new String[argsList.size()]);

        int id = -1;
        if (map.containsKey("id")) id = (Integer)map.get("id");

        if (rc.getCircuitManager().activateCircuit(c, null, signArgs, id)>0) {
            if (map.containsKey("state"))
                c.setInternalState((Map<String, String>)map.get("state"));
            
            return c;
        }
        else return null;
    }

    private void configureChannelFromMap(Map<String,Object> map) {
        BroadcastChannel channel;
        channel = rc.getChannelByName((String)map.get("name"));
        channel.owners = (List<String>)map.get("owners");
        channel.users = (List<String>)map.get("users");
    }

    private List<Integer> makeBlockList(Location l) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(l.getBlockX());
        list.add(l.getBlockY());
        list.add(l.getBlockZ());

        return list;
    }

    private Object makeChunksList(ChunkLocation[] locs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (ChunkLocation l : locs) {
            List<Integer> loc = new ArrayList<Integer>();
            loc.add(l.getX());
            loc.add(l.getZ());
            list.add(loc);
        }

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

    private ChunkLocation[] getChunkLocations(World world, List<List<Integer>> locs) {
        List<ChunkLocation> ret = new ArrayList<ChunkLocation>();

        for (List<Integer> loc : locs) {
            ret.add(new ChunkLocation(loc.get(0), loc.get(1), world));
        }

        return ret.toArray(new ChunkLocation[ret.size()]);
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
