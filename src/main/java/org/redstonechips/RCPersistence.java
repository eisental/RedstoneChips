package org.redstonechips;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.chip.ChipSerializer;
import org.redstonechips.wireless.BroadcastChannel;
import org.redstonechips.wireless.ChannelSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A bunch of static methods for saving and loading circuit states.
 *
 * @author Tal Eisenberg
 */
public class RCPersistence {
    public final static String circuitsFileExtension = ".circuits";
    public final static String channelsFileExtension = ".channels";
    public final static String channelsFileName = "redstonechips"+channelsFileExtension;
        
    /**
     * Used to prevent saving state more than once per game tick.
     */
    private static final List<World> dontSaveCircuits = new ArrayList<>();
    
    private RCPersistence() {}

    // -- Chip persistence --
    
    /**
     * Loads a circuits file of one world.
     * @param world 
     */
    public static void loadChipsOf(World world) {
        RedstoneChips rc = RedstoneChips.inst();
        File file = new File(rc.getDataFolder(), world.getName()+circuitsFileExtension);
        if (file.exists()) {
            rc.log(Level.INFO, "Loading chips for world '" + world.getName() + "'...");
            try {
                loadChipsFromFile(file);
                WorldsObserver.addLoadedWorld(world);                
            } catch (IOException ex) {
                rc.log(Level.SEVERE, "Circuits file '" + file + "' threw error "+ex.toString()+".");
            }                
        }        
    }
        
    private static void loadChipsFromFile(File file) throws IOException {        
        RedstoneChips rc = RedstoneChips.inst();
        
        Yaml yaml = new Yaml();
        List<Map<String, Object>> circuitsList;
        try (FileInputStream fis = new FileInputStream(file)) {
            circuitsList = (List<Map<String, Object>>) yaml.load(fis);
        }
        
        Map<Chip, Map<String, String>> chipsAndState = new HashMap<>();

        if (circuitsList!=null) {
            ChipSerializer s = new ChipSerializer();

            for (Map<String,Object> circuitMap : circuitsList) {
                try {
                    MaybeChip mChip = s.deserialize(circuitMap);
                    if (mChip==MaybeChip.AChip) {
                        Map<String, String> state = (Map<String, String>)circuitMap.get(ChipSerializer.Key.STATE.key);
                        chipsAndState.put(mChip.getChip(), state);
                    } else 
                        rc.log(Level.WARNING, "Found bad chip entry in " + file.getName() + (mChip==MaybeChip.ChipError? ": " + mChip.getError():""));
                    
                } catch (Exception e) {
                    rc.log(Level.WARNING, e.getMessage() + ". Ignoring circuit.");
                    backupCircuitsFile(file.getName());
                    e.printStackTrace();
                }
            }
                        
            // Activate all compiled chips.
            for (Chip c : chipsAndState.keySet()) {
                if (rc.chipManager().activateChip(c, null, c.id)) {
                    Map<String, String> state = chipsAndState.get(c);
                    if (state!=null) c.circuit.setInternalState(state);
                } 
            }
        }
    }

    /**
     * Saves all the circuits on the server.
     */
    public static void saveAll() {
      for(World wrld : RedstoneChips.inst().getServer().getWorlds())
        saveChipsOf(wrld);
    }

    /**
     * Saves all the circuits in the specified world.
     * 
     * @param world 
     */
    public static void saveChipsOf(World world) {        
        RedstoneChips rc = RedstoneChips.inst();
        
        rc.log(Level.INFO, "Saving " + world.getName() + " chip data...");                
        
        if (dontSaveCircuits.contains(world)) return;
        
        File circuitsFile = getCircuitsFile(world.getName()+circuitsFileExtension);
        
        if (rc.chipManager().getAllChips().getInWorld(world).isEmpty()) {
            if (circuitsFile.delete()) 
                rc.log(Level.INFO, "Deleted empty world file - " + circuitsFile.getName());
            return;
        }
        
        rc.chipManager().checkChipsIntegrityIn(world);

        dontSaveCircuits.add(world);
        if (rc.isEnabled()) {
            rc.getServer().getScheduler().runTaskLaterAsynchronously(rc, new Runnable() {
                @Override
                public void run() {
                    dontSaveCircuits.clear();
                }
            }, 1);
        }

        Collection<Chip> chips = rc.chipManager().getAllChips().getInWorld(world).values();
        for (Chip c : chips) c.circuit.save();        
        serialize(chips, new ChipSerializer(), circuitsFile);
        
        File channelsFile = new File(rc.getDataFolder(), channelsFileName);
        Collection<BroadcastChannel> channels = rc.channelManager().getBroadcastChannels().values();
        removeUnprotectedChannels(channels);
        serialize(channels, new ChannelSerializer(), channelsFile);
        
        // if world is unloaded remove circuits.
        if (WorldsObserver.isWorldUnloading(world)) {
            int size = rc.chipManager().getAllChips().size();
            
            rc.chipManager().unloadWorldChips(WorldsObserver.getUnloadingWorld());
            rc.log(Level.INFO, "Unloaded " + (size-rc.chipManager().getAllChips().size()) + " chip(s).");
        }
        
    }
    
    private static void serialize(Collection values, Serializer serializer, File file) {
        List<Map<String,Object>> map = new ArrayList<>();
        for (Object o : values) {
            map.add(serializer.serialize(o));
        }
        
        if (!map.isEmpty()) 
            dumpYaml(map, file);
    }
    
    private static void dumpYaml(List<Map<String, Object>> circuitMap, File file) {        
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        Yaml yaml = new Yaml(options);
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            yaml.dump(circuitMap, new BufferedWriter(new OutputStreamWriter(fos, "UTF-8")));
            fos.flush();
        } catch (IOException ex) {
            RedstoneChips.inst().log(Level.SEVERE, ex.getMessage());
        }
    }
    
    private static File getCircuitsFile(String name) {
        return new File(RedstoneChips.inst().getDataFolder(), name);
    }
        
    // -- Wireless channels persistence --
    
    /**
     * Loads channel data from file.
     */
    public static void loadChannelsIfExists() {
        File channelsFile = new File(RedstoneChips.inst().getDataFolder(), channelsFileName);
        if (channelsFile.exists()) {
            loadChannelsFromFile(channelsFile);
        }        
    }
    
    private static void loadChannelsFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            
            
            List<Map<String, Object>> channelsList = (List<Map<String, Object>>) yaml.load(fis);

            ChannelSerializer ser = new ChannelSerializer();
            
            if (channelsList!=null) {
                for (Map<String,Object> channelMap : channelsList) {
                    ser.deserialize(channelMap);
                }
            }
        } catch (IOException ex) {
            RedstoneChips.inst().log(Level.SEVERE, "While reading channels file: "+ex.toString());
        }
    }
    
    // -- Backup --
    
    private final static String backupFileExtension = ".BACKUP";    
    private final static List<String> madeBackup = new ArrayList<>();

    private static void backupCircuitsFile(String filename) {
        if (madeBackup.contains(filename)) return;

        try {
            File original = getCircuitsFile(filename);
            File backup = getBackupFileName(original.getParentFile(),filename);

            RedstoneChips.inst().log(Level.INFO, "An error occurred while loading redstone chips. To make sure you won't lose any data, a backup copy is"
                    + " being created at " + backup.getPath());
            copy(original, backup);
        } catch (IOException ex) {
            RedstoneChips.inst().log(Level.SEVERE, "Error while trying to write backup file: " + ex);
        }
        madeBackup.add(filename);
    }
    
    private static File getBackupFileName(File parentFile,String filename) {
        File backup;
        int idx = 0;

        do {
            backup = new File(parentFile, filename + backupFileExtension + idx);
            idx++;
        } while (backup.exists());
        return backup;
    }
    
    private static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }          

    private static void removeUnprotectedChannels(Collection<BroadcastChannel> channels) {
        List<BroadcastChannel> toremove = new ArrayList<>();
        for (BroadcastChannel c : channels) if (!c.isProtected()) toremove.add(c);        
        for (BroadcastChannel c : toremove) channels.remove(c);
    }
}
