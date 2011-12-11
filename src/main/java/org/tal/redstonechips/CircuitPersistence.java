package org.tal.redstonechips;

import java.io.BufferedWriter;
import org.tal.redstonechips.circuit.Circuit;
import java.io.File;
import java.io.FileInputStream;
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
import org.tal.redstonechips.util.ChunkLocation;
import org.tal.redstonechips.channel.BroadcastChannel;
import org.tal.redstonechips.circuit.IOBlock;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.circuit.InterfaceBlock;
import org.tal.redstonechips.circuit.OutputPin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A bunch of static methods for saving and loading circuit states.
 *
 * @author Tal Eisenberg
 */
public class CircuitPersistence {
    private RedstoneChips rc;

    public final static String circuitsFileExtension = ".circuits";
    public final static String circuitsFileName = "redstonechips"+circuitsFileExtension;
    public final static String channelsFileExtension = ".channels";
    public final static String channelsFileName = "redstonechips"+channelsFileExtension;
    private final static String backupFileExtension = ".BACKUP";
    
    private final static String classKey = "class";
    private final static String worldKey = "world";
    private final static String activationBlockKey = "activationBlock";
    private final static String chunkKey = "chunk";
    private final static String inputsKey = "inputs";
    private final static String outputsKey = "outputs";
    private final static String interfacesKey = "interfaces";
    private final static String structureKey = "structure";
    private final static String argsKey = "signArgs";
    private final static String stateKey = "state";
    private final static String idKey = "id";
    private final static String nameKey = "name";
    private final static String disabledKey = "disabled";

    private final static String channelNameKey = "name";
    private final static String channelOwnersKey = "owners";
    private final static String channelUsersKey = "users";    
    
    private List<String> madeBackup = new ArrayList<String>();

    /**
     * Used to prevent saving state more than once per game tick.
     */
    private List<World> dontSaveCircuits = new ArrayList<World>();
    private List<World> loadedWorlds = new ArrayList<World>();
    
    private Runnable dontSaveCircuitsReset = new Runnable() {
        @Override
        public void run() {
            dontSaveCircuits.clear();
        }
    };

    public CircuitPersistence(RedstoneChips plugin) {
        rc = plugin;
    }

    /**
     * Attempts to load the old circuits file (redstonechips.circuits). 
     * This is only used in case the old file name is found in the plugin folder.
     * File is renamed to redstonechips.circuits.old.
     * 
     * @return true if the old file exists. false otherwise.
     */
    public boolean loadOldFile() {
        File file = getCircuitsFile();
        if (file.exists()) {
            rc.log(Level.INFO, "Reading old circuits file "+file.getName()+"...");
            try {
                loadCircuitsFromFile(file);
            } catch (IOException ex) {
                rc.log(Level.SEVERE, "Circuits file '" + file + "' threw error "+ex.toString()+".");
            }                
            
            file.renameTo(new File(file.getParentFile(),circuitsFileName+".old"));
            return true;
        } else return false;
    }

    public void loadCircuits(World world) {
        File file = new File(rc.getDataFolder(), world.getName()+circuitsFileExtension);
        if (file.exists()) {
            rc.log(Level.INFO, "Loading chips for world '" + world.getName() + "'...");
            try {
                loadCircuitsFromFile(file);
                loadedWorlds.add(world);
            } catch (IOException ex) {
                rc.log(Level.SEVERE, "Circuits file '" + file + "' threw error "+ex.toString()+".");
            }                
        }        
    }
        
    protected void loadCircuitsFromFile(File file) throws IOException {        
        Yaml yaml = new Yaml();

        FileInputStream fis = new FileInputStream(file);
        List<Map<String, Object>> circuitsList = (List<Map<String, Object>>) yaml.load(fis);
        fis.close();

        if (circuitsList!=null) {
            for (Map<String,Object> circuitMap : circuitsList) {
                try {

                    compileCircuitFromMap(circuitMap);

                } catch (IllegalArgumentException ie) {
                    rc.log(Level.WARNING, ie.getMessage() + ". Ignoring circuit.");
                    backupCircuitsFile(file.getName());
                    ie.printStackTrace();
                } catch (InstantiationException ex) {
                    rc.log(Level.WARNING, ex.toString() + ". Ignoring circuit.");
                    backupCircuitsFile(file.getName());
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    rc.log(Level.WARNING, ex.toString() + ". Ignoring circuit.");
                    backupCircuitsFile(file.getName());
                    ex.printStackTrace();
                } catch (Throwable t) {
                    rc.log(Level.SEVERE, t.toString() + ". Ignoring circuit.");
                    backupCircuitsFile(file.getName());
                    t.printStackTrace();
                }
            }
        }
    }

    public void loadChannels() {
        File channelsFile = new File(rc.getDataFolder(), channelsFileName);
        if (channelsFile.exists()) {
            loadChannelsFromFile(channelsFile);
        }        
    }
    
    public void loadChannelsFromFile(File file) {
        try {
            Yaml yaml = new Yaml();

            FileInputStream fis = new FileInputStream(file);
            List<Map<String, Object>> channelsList = (List<Map<String, Object>>) yaml.load(fis);
            fis.close();

            if (channelsList!=null) {
                for (Map<String,Object> channelMap : channelsList) {
                    configureChannelFromMap(channelMap);
                }
            }
        } catch (IOException ex) {
            rc.log(Level.SEVERE, "Channels file threw error "+ex.toString()+".");
        }
    }

    public void saveCircuits() {
      rc.log(Level.INFO, "Saving chip data of all worlds...");
      for(World wrld : rc.getServer().getWorlds())
        saveCircuits(wrld);
    }

    public void saveCircuits(World world) {
        if (dontSaveCircuits.contains(world)) return;
        
        File file = getCircuitsFile(world.getName()+circuitsFileExtension);
        
        if (rc.getCircuitManager().getCircuits(world).isEmpty()) {
            if (file.delete()) 
                rc.log(Level.INFO, "Deleted empty world file - " + file.getName());
            return;
        }
        
        rc.getCircuitManager().checkCircuitsIntegrity(world);

        Map<Integer, Circuit> circuits = rc.getCircuitManager().getCircuits(world);
        dontSaveCircuits.add(world);
        rc.getServer().getScheduler().scheduleAsyncDelayedTask(rc, dontSaveCircuitsReset, 1);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        List<Map<String,Object>> circuitMaps = new ArrayList<Map<String,Object>>();

        for (Circuit c : circuits.values()) {
            c.save();
            circuitMaps.add(this.circuitToMap(c));
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(file);
            yaml.dump(circuitMaps, new BufferedWriter(new OutputStreamWriter(fos, "UTF-8")));
            fos.flush();
            fos.close();
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.getMessage());
        }
        
        circuitMaps = new ArrayList<Map<String,Object>>();
        for (BroadcastChannel channel : rc.broadcastChannels.values()) {
            if (channel.isProtected()) {
                circuitMaps.add(this.channelToMap(channel));
            }
        }
        
        if (!circuitMaps.isEmpty()) {
            try {
                File channelsFile = new File(rc.getDataFolder(), channelsFileName);
                FileOutputStream fosChannels = new FileOutputStream(channelsFile);
                yaml.dump(circuitMaps, new BufferedWriter(new OutputStreamWriter(fosChannels, "UTF-8")));
                fosChannels.flush();
                fosChannels.close();
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.getMessage());
            }
        }
    }

    private Map<String, Object> circuitToMap(Circuit c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(classKey, c.getCircuitClass());
        map.put(worldKey, c.world.getName());
        map.put(activationBlockKey, makeBlockList(c.activationBlock));
        map.put(chunkKey, makeChunksList(c.circuitChunks));
        map.put(inputsKey, makeIOBlockList(c.inputs));
        map.put(outputsKey, makeIOBlockList(c.outputs));
        map.put(interfacesKey, makeIOBlockList(c.interfaceBlocks));
        map.put(structureKey, makeBlockListsList(c.structure));
        map.put(argsKey, c.args);
        map.put(stateKey, c.getInternalState());
        map.put(idKey, c.id);
        map.put(nameKey, c.name);
        map.put(disabledKey, c.isDisabled());
        return map;
    }

    private Map<String, Object> channelToMap(BroadcastChannel c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(channelNameKey, c.name);
        map.put(channelOwnersKey, c.owners);
        map.put(channelUsersKey, c.users);
        return map;
    }

    private Circuit compileCircuitFromMap(Map<String,Object> map) throws InstantiationException, IllegalAccessException {

        String className = (String)map.get(classKey);
        World world = findWorld((String)map.get(worldKey));
        Circuit c = rc.getCircuitLoader().getCircuitInstance(className);
        c.world = world;
        c.activationBlock = getLocation(world, (List<Integer>)map.get(activationBlockKey));
        c.structure = getLocationArray(world, (List<List<Integer>>)map.get(structureKey));
        
        IOBlock[] inIO = getIOBlockArray((List<List<Integer>>)map.get(inputsKey), c, IOBlock.Type.INPUT);        
        IOBlock[] outIO = getIOBlockArray((List<List<Integer>>)map.get(outputsKey), c, IOBlock.Type.OUTPUT);
        IOBlock[] interfaceIO = getIOBlockArray((List<List<Integer>>)map.get(interfacesKey), c, IOBlock.Type.INTERFACE);

        c.inputs = new InputPin[inIO.length];
        c.outputs = new OutputPin[outIO.length];
        c.interfaceBlocks = new InterfaceBlock[interfaceIO.length];        
        
        for (int i=0; i<inIO.length; i++) c.inputs[i] = (InputPin)inIO[i];
        for (int i=0; i<outIO.length; i++) c.outputs[i] = (OutputPin)outIO[i];
        for (int i=0; i<interfaceIO.length; i++) c.interfaceBlocks[i] = (InterfaceBlock)interfaceIO[i];
        
        if (map.containsKey(chunkKey)) {
            c.circuitChunks = getChunkLocations(world, (List<List<Integer>>)map.get(chunkKey));
        } else {
            c.circuitChunks = rc.getCircuitManager().findCircuitChunks(c);
        }

        List<String> argsList = (List<String>)map.get(argsKey);
        c.args = argsList.toArray(new String[argsList.size()]);

        if (map.containsKey(nameKey)) c.name = (String)map.get(nameKey);

        int id = -1;
        if (map.containsKey(idKey)) id = (Integer)map.get(idKey);
        if (rc.getCircuitManager().activateCircuit(c, null, id)>=0) {
            if (map.containsKey(stateKey))
                c.setInternalState((Map<String, String>)map.get(stateKey));
            
            if (map.containsKey(disabledKey)) c.setDisabled((Boolean)map.get(disabledKey));            
            
            return c;
            
        } else return null;
    }

    private void configureChannelFromMap(Map<String,Object> map) {
        BroadcastChannel channel;
        channel = rc.getChannelByName((String)map.get(channelNameKey));
        channel.owners = (List<String>)map.get(channelOwnersKey);
        channel.users = (List<String>)map.get(channelUsersKey);
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

    private Object makeIOBlockList(IOBlock[] blocks) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        for (IOBlock b : blocks)
            list.add(makeBlockList(b.getLocation()));
        return list;        
    }    
    
    private Object makeBlockListsList(Location[] vs) {
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        if(vs!=null)
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
        if(list!=null)
          for (List<Integer> coords : list)
            locations.add(getLocation(w, coords));

        return locations.toArray(new Location[locations.size()]);
    }

    private IOBlock[] getIOBlockArray(List<List<Integer>> list, Circuit c, IOBlock.Type type) {
        List<IOBlock> io = new ArrayList<IOBlock>();
        for (int i=0; i<list.size(); i++) {
            List<Integer> coords = list.get(i);
            IOBlock ib = IOBlock.makeIOBlock(type, c, new Location(c.world, coords.get(0), coords.get(1), coords.get(2)), i);
            io.add(ib);
        }

        return io.toArray(new IOBlock[io.size()]);
    }
    
    private void backupCircuitsFile(String filename) {
        if (madeBackup.contains(filename)) return;

        try {
            File original = getCircuitsFile(filename);
            File backup = getBackupFileName(original.getParentFile(),filename);

            rc.log(Level.INFO, "An error occurred while loading redstone chips. To make sure you won't lose any data, a backup copy of "
                + circuitsFileName + " is being created. The backup can be found at " + backup.getPath());
            copy(original, backup);
        } catch (IOException ex) {
            rc.log(Level.SEVERE, "Error while trying to write backup file: " + ex);
        }
        madeBackup.add(filename);
    }

    private File getCircuitsFile() {
        return getCircuitsFile(circuitsFileName);
    }
    
    private File getCircuitsFile(String name) {
        return new File(rc.getDataFolder(), name);
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

    private File getBackupFileName(File parentFile,String filename) {
        File backup;
        int idx = 0;

        do {
            backup = new File(parentFile, filename + backupFileExtension + idx);
            idx++;
        } while (backup.exists());
        return backup;
    }

    boolean isWorldChipLoaded(World w) {
        return loadedWorlds.contains(w);
    }

    void clearLoadedWorldsList() {
        loadedWorlds.clear();
    }

    void removeLoadedWorld(World unloadedWorld) {
        loadedWorlds.remove(unloadedWorld);
    }
}
