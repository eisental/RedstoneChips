
package org.tal.redstonechips.memory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.tal.redstonechips.bitset.BitSet7Constructor;
import org.tal.redstonechips.bitset.BitSet7Representer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents an abstract memory that can be saved to file.
 *
 * @author Tal Eisenberg
 */
public abstract class Memory {
    /** The folder to which memory files are saved. */
    public static File memoryFolder;
    
    /** Default name for the memory folder. */
    public static final String memoryFolderName = "memory";
    
    /** Old memory folder name. */ 
    public static final String oldMemoryFolderName = "sram";
    
    /** Regex pattern for a valid memory id */
    public static final Pattern MemIDPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    /** A map containing all loaded memories indexed by their id */
    public static Map<String,Memory> memories = new HashMap<String,Memory>();

    /** Memory id string. */
    private String id;

    private int allocatorsCount;
    
    /** 
     * Initializes the memory and adds it to the memory list.
     * @param id The memory id of this Memory.
     */
    public void init(String id) {
        this.id = id;
        memories.put(id, this);
    }

    /** Returns the memory id string */
    public String getId() { return id; }

    /**
     * Saves the data in this memory. Any data returned by getData() is stored.
     * 
     * @param file The file to save to.
     * @throws IOException When an error occurs wile saving the file.
     */
    public void store(File file) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new BitSet7Representer(), options);
        yaml.dump(getData(), new FileWriter(file));
    }

    /**
     * Loads and update memory data from a file.
     * @param file The file to read from.
     * @throws FileNotFoundException When the file is not found.
     */
    public void load(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml(new BitSet7Constructor());
        setData((Map)yaml.load(new FileInputStream(file)));
    }

    protected abstract Map getData();

    protected abstract void setData(Map data);
    
    /**
     * 
     * @param id Memory id to test.
     * @return true if the id string is valid according to MemIDPattern.
     */
    public static boolean isValidId(String id) {
        return MemIDPattern.matcher(id).matches();
    }
    
    /**
     * @param id Memory id 
     * @return A memory File based on the id string.
     */
    public static File getMemoryFile(String id) {
        return new File(memoryFolder, id + ".mem");
    }
    
    /**
     * @return an unused memory id.
     */
    public static String getFreeMemId() {
        File file;
        int idx = 0;

        do {
            file = getMemoryFile(Integer.toString(idx));
            idx++;
        } while (file.exists());
        return Integer.toString(idx);
    }
    
    /**
     * Finds and creates the memory folder.
     * 
     * @param pluginFolder The plugin data folder.
     * @return true if the folder was created.
     */
    public static boolean setupDataFolder(File pluginFolder) {
        boolean res;
        memoryFolder = new File(pluginFolder, memoryFolderName);
        if (!memoryFolder.exists()) {
            if (!memoryFolder.mkdirs()) 
                throw new RuntimeException("Can't make folder " + memoryFolder.getAbsolutePath());            
            res = true;
        } else {
            res = false;
        }    
        
        // move any sram files in rc data folder.
/*        if (pluginFolder.listFiles()!=null) {
            for (File f : pluginFolder.listFiles()) {
                if (isMemoryFile(f))
                    f.renameTo(new File(memoryFolder, renameMem(f)));                
            }
        }*/
        
        // move any sram files from old sram folder.
        File oldFolder = new File(pluginFolder, oldMemoryFolderName);
        if (oldFolder.listFiles()!=null) {
            for (File f : oldFolder.listFiles())  {
                if (isMemoryFile(f)) {
                    f.renameTo(new File(memoryFolder, renameMem(f)));
                }
            }
        }
        
        return res;
        
    }
    
    private static String renameMem(File f) {
        String name = f.getName();
        if (name.startsWith("sram-") && name.endsWith(".data")) {
            String id = name.substring(5, name.length()-5);
            return id + ".mem";
        } else return f.getName();
        
        
    }

    private static boolean isMemoryFile(File f) {
        if (!f.isFile()) return false;
        return (f.getName().startsWith("sram-") && f.getName().endsWith(".data")) || f.getName().endsWith(".mem");
        
    }
    
    /**
     * Deletes the memory file.
     * @return true if the file was actually deleted.
     */
    public boolean delete() {
        return getFile().delete();
    }

    /**
     * @return The memory file of this Memory.
     */
    public File getFile() {
        return getMemoryFile(getId());
    }        
    
    /**
     * Saves this memory to its memory file (according to its id).
     * @throws IOException 
     */
    public void save() throws IOException {
        store(getMemoryFile(getId()));
    }

    /**
     * Retrieves the Memory object for this id or creates and configures a new one if it's not used. If the 
     * memory file exists data is loaded from it.
     * 
     * @param memId The memory id.
     * @param type Memory class (Ram.class for ex.). Ignored when the memory already exists.
     * @return A Memory object
     * @throws IOException 
     */
    public static Memory getMemory(String memId, Class<? extends Memory> type ) throws IOException {
        if (!isValidId(memId)) throw new IllegalArgumentException("Invalid memory id: " + memId);
        
        if (!Memory.memories.containsKey(memId)) {
            Memory memory;
            try {
                memory = type.newInstance();
            } catch (InstantiationException ex) {
                return null;
            } catch (IllegalAccessException ex) {
                return null;
            }
            memory.init(memId);

            File file = Memory.getMemoryFile(memId);
            
            if (file.exists()) {
                memory.load(file);
            } else {
                file.createNewFile();
            }
            
            memory.alloc();
            return memory;
        } else {
            Memory m = Memory.memories.get(memId);
            m.alloc();
            return m;
        }
        
    }
    
    /**
     * Creates a new memory using an unused id.
     * 
     * @param type Memory class (Ram.class for ex.).
     * @return A Memory object
     * @throws IOException 
     */
    public static Memory getMemory(Class<? extends Memory> type) throws IOException {
        return Memory.getMemory(getFreeMemId(), type);
    }
    
    /**
     * Increases the alloc count of this memory by 1.
     */
    public void alloc() {
        allocatorsCount++;
    }
    
    /**
     * Decreases the alloc count of this memory by 1 and release it when the count reaches 0.
     */
    public void release() {
        if (allocatorsCount>0) allocatorsCount--;
        else allocatorsCount=0;
        
        if (allocatorsCount==0) {
            Map data = getData();
            if (data!=null) data.clear();
            if (id!=null) Memory.memories.remove(id);
        }
    }
}
