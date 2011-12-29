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
import org.tal.redstonechips.util.BitSetUtils;
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
    
    private final static String classKeyLong = "class";
    private final static String classKey = "c";
    private final static String worldKeyLong = "world";
    private final static String worldKey = "w";
    private final static String activationBlockKey = "activationBlock";
    private final static String signKey = "sign";
    private final static String inputsKeyLong = "inputs";
    private final static String inputsKey = "inp";
    private final static String outputsKeyLong = "outputs";
    private final static String outputsKey = "out";
    private final static String interfacesKeyLong = "interfaces";
    private final static String interfacesKey = "int";
    private final static String structureKeyLong = "structure";
    private final static String structureKey = "str";
    private final static String argsKeyLong = "signArgs";
    private final static String argsKey = "args";
    private final static String stateKey = "state";
    private final static String idKey = "id";
    private final static String nameKey = "name";
    private final static String disabledKey = "disabled";
    private final static String outputBitsKeyLong = "outputBits";
    private final static String outputBitsKey = "bits";

    private final static String channelNameKey = "name";
    private final static String channelStateKey = "state";
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

    /**
     * Loads all of the world chips from file.
     * @param world 
     */
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
        
        List<Circuit> circuits = new ArrayList<Circuit>();

        if (circuitsList!=null) {
            Map<Circuit, Map<String, String>> internalStates = new HashMap<Circuit, Map<String, String>>();
            
            for (Map<String,Object> circuitMap : circuitsList) {
                try {
                    Circuit c = compileCircuitFromMap(circuitMap, internalStates);
                    if (c!=null) circuits.add(c);
                    else rc.log(Level.WARNING, "Found bad chip entry in " + file.getName());

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
            
            for (Circuit c : circuits) {
                if (rc.getCircuitManager().activateCircuit(c, null, c.id)>=0) {
                    Map<String, String> state = internalStates.get(c);
                    if (state!=null) c.setInternalState(state);
                } 
            }
        }
    }

    /**
     * Loads channel data from file.
     */
    public void loadChannels() {
        File channelsFile = new File(rc.getDataFolder(), channelsFileName);
        if (channelsFile.exists()) {
            loadChannelsFromFile(channelsFile);
        }        
    }
    
    private void loadChannelsFromFile(File file) {
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

    /**
     * Saves all the circuits on the server.
     */
    public void saveCircuits() {
      rc.log(Level.INFO, "Saving chip data of all worlds...");
      for(World wrld : rc.getServer().getWorlds())
        saveCircuits(wrld);
    }

    /**
     * Saves all the circuits in the specified world.
     * 
     * @param world 
     */
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
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
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
        for (BroadcastChannel channel : rc.getChannelManager().getBroadcastChannels().values()) {
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
        map.put(signKey, makeBlockList(c.activationBlock));
        if (c.inputs!=null && c.inputs.length!=0) map.put(inputsKey, makeIOBlockList(c.inputs));
        if (c.outputs!=null && c.outputs.length!=0) map.put(outputsKey, makeIOBlockList(c.outputs));
        if (c.interfaceBlocks!=null && c.interfaceBlocks.length!=0) map.put(interfacesKey, makeIOBlockList(c.interfaceBlocks));
        map.put(structureKey, makeBlockListsList(c.structure));
        if (c.args!=null && c.args.length!=0) map.put(argsKey, c.args);
        
        Map<String,String> state = c.getInternalState();
        if (state!=null && !state.isEmpty()) map.put(stateKey, c.getInternalState());
        
        map.put(idKey, c.id);
        if (c.name!=null) map.put(nameKey, c.name);
        if (c.isDisabled()) map.put(disabledKey, c.isDisabled());
        if (c.outputs!=null && c.outputs.length!=0) map.put(outputBitsKey, BitSetUtils.bitSetToString(c.getOutputBits(), c.outputs.length));
        
        return map;
    }

    private Map<String, Object> channelToMap(BroadcastChannel c) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(channelNameKey, c.name);
        map.put(channelStateKey, BitSetUtils.bitSetToString(c.bits, c.getLength()));
        map.put(channelOwnersKey, c.owners);
        map.put(channelUsersKey, c.users);
        return map;
    }

    private Circuit compileCircuitFromMap(Map<String,Object> map, Map<Circuit, Map<String,String>> internalStates) throws InstantiationException, IllegalAccessException {
        String className;
        if (map.containsKey(classKeyLong)) className = (String)map.get(classKeyLong);
        else if (map.containsKey(classKey)) className = (String)map.get(classKey);
        else return null;
        
        World world;
        if (map.containsKey(worldKeyLong)) world = findWorld((String)map.get(worldKeyLong));
        else if (map.containsKey(worldKey)) world = findWorld((String)map.get(worldKey));
        else return null;
        
        Circuit c = rc.getCircuitLoader().getCircuitInstance(className);
        c.world = world;
        
        if (map.containsKey(activationBlockKey))
            c.activationBlock = getLocation(world, (List<Integer>)map.get(activationBlockKey));
        else if (map.containsKey(signKey))
            c.activationBlock = getLocation(world, (List<Integer>)map.get(signKey));
        else return null;
        
        if (map.containsKey(structureKeyLong))
            c.structure = getLocationArray(world, (List<List<Integer>>)map.get(structureKeyLong));
        else if (map.containsKey(structureKey))
            c.structure = getLocationArray(world, (List<List<Integer>>)map.get(structureKey));
        else return null;
        
        IOBlock[] inIO, outIO, interfaceIO;
        if (map.containsKey(inputsKeyLong))
            inIO = getIOBlockArray((List<List<Integer>>)map.get(inputsKeyLong), c, IOBlock.Type.INPUT);
        else if (map.containsKey(inputsKey))
            inIO = getIOBlockArray((List<List<Integer>>)map.get(inputsKey), c, IOBlock.Type.INPUT);
        else inIO = new IOBlock[0];
                
        if (map.containsKey(outputsKeyLong))
            outIO = getIOBlockArray((List<List<Integer>>)map.get(outputsKeyLong), c, IOBlock.Type.OUTPUT);
        else if (map.containsKey(outputsKey))
            outIO = getIOBlockArray((List<List<Integer>>)map.get(outputsKey), c, IOBlock.Type.OUTPUT);
        else outIO = new IOBlock[0];
        
        if (map.containsKey(interfacesKeyLong))
            interfaceIO = getIOBlockArray((List<List<Integer>>)map.get(interfacesKeyLong), c, IOBlock.Type.INTERFACE);
        else if (map.containsKey(interfacesKey))
            interfaceIO = getIOBlockArray((List<List<Integer>>)map.get(interfacesKey), c, IOBlock.Type.INTERFACE);
        else interfaceIO = new IOBlock[0];

        c.inputs = new InputPin[inIO.length];
        for (int i=0; i<inIO.length; i++) c.inputs[i] = (InputPin)inIO[i];
        
        c.outputs = new OutputPin[outIO.length];
        for (int i=0; i<outIO.length; i++) c.outputs[i] = (OutputPin)outIO[i];

        c.interfaceBlocks = new InterfaceBlock[interfaceIO.length];
        for (int i=0; i<interfaceIO.length; i++) c.interfaceBlocks[i] = (InterfaceBlock)interfaceIO[i];
        
        c.circuitChunks = rc.getCircuitManager().findCircuitChunks(c);
        
        List<String> argsList = null;
        if (map.containsKey(argsKeyLong))
            argsList = (List<String>)map.get(argsKeyLong);
        else if (map.containsKey(argsKey))
            argsList = (List<String>)map.get(argsKey);

        if (argsList!=null) c.args = argsList.toArray(new String[argsList.size()]);
        else c.args = new String[0];
        
        if (map.containsKey(nameKey)) c.name = (String)map.get(nameKey);

        if (map.containsKey(outputBitsKeyLong)) c.setOutputBits(BitSetUtils.stringToBitSet((String)map.get(outputBitsKeyLong)));
        else if (map.containsKey(outputBitsKey)) c.setOutputBits(BitSetUtils.stringToBitSet((String)map.get(outputBitsKey)));
        
        if (map.containsKey(idKey)) c.id = (Integer)map.get(idKey);
        if (map.containsKey(stateKey)) 
            internalStates.put(c, (Map<String, String>)map.get(stateKey));
        if (map.containsKey(disabledKey)) c.disabled = (Boolean)map.get(disabledKey);
        
        return c;
    }

    private void configureChannelFromMap(Map<String,Object> map) {
        BroadcastChannel channel;
        channel = rc.getChannelManager().getChannelByName((String)map.get(channelNameKey), true);
        if (map.containsKey(channelStateKey)) channel.bits = BitSetUtils.stringToBitSet((String)map.get(channelStateKey));        
        channel.owners = (List<String>)map.get(channelOwnersKey);
        channel.users = (List<String>)map.get(channelUsersKey);
        channel.transmit(channel.bits, 0, channel.getLength());
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

    boolean isWorldLoaded(World w) {
        return loadedWorlds.contains(w);
    }

    void clearLoadedWorldsList() {
        loadedWorlds.clear();
    }

    void removeLoadedWorld(World unloadedWorld) {
        loadedWorlds.remove(unloadedWorld);
    }
}
