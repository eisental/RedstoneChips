
package org.redstonechips.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.redstonechips.util.BitSetConstructor;
import org.redstonechips.util.BitSetRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents an abstract memory that can be saved to file.
 *
 * @author Tal Eisenberg
 */
public abstract class Memory {

    /** Memory id string. */
    private String id;

    private int allocatorsCount;
    
    /** Returns the memory id string
     * @return  */
    public String getId() { return id; }

    /**
     * Saves this memory to its memory file (according to its id).
     * @throws IOException 
     */
    public void store() throws IOException {
        store(getMemoryFile(getId()));
    }
    
    /**
     * Saves the data in this memory. Any data returned by getData() is stored.
     * 
     * @param file The file to save to.
     * @throws IOException When an error occurs wile saving the file.
     */
    public void store(File file) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new BitSetRepresenter(), options);
        yaml.dump(getData(), new FileWriter(file));
    }
    
    /**
     * Loads and update memory data from a file.
     * @param file The file to read from.
     * @throws FileNotFoundException When the file is not found.
     */
    public void load(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml(new BitSetConstructor());
        setData((Map)yaml.load(new FileInputStream(file)));
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
     * Decreases the allocators count by 1 and release it once the count reaches 0. 
     * Once there are no more allocators, data is stored in the memory file and all data is cleared for garbage collection.
     */
    public void release() {
        if (allocatorsCount>0) allocatorsCount--;
        else {
            allocatorsCount=0;
            try {
                store();
            } catch (IOException ex) {
                Logger.getLogger(Memory.class.getName()).log(Level.SEVERE, null, "While storing memory: " + ex.getMessage());
            }
            Map data = getData();
            if (data!=null) data.clear();
            if (id!=null) Memory.memories.remove(id);            
        }
    }
    
    /** 
     * Initializes the memory and adds it to the memory list.
     * @param id The memory id of this Memory.
     */
    protected void init(String id) {
        this.id = id;
        memories.put(id, this);
    }
    
    /**
     * Increases the alloc count of this memory by 1.
     */
    private void alloc() {
        allocatorsCount++;
    }
        
    protected abstract Map getData();

    protected abstract void setData(Map data);
            
    // -- Static Part --

    /** The folder to which memory files are saved. */
    public static File memoryFolder;
    
    /** Default name for the memory folder. */
    public static final String memoryFolderName = "memory";
        
    /** Regex pattern for a valid memory id */
    public static final Pattern MemIDPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    /** A map containing all loaded memories indexed by their id */
    public static final Map<String,Memory> memories = new HashMap<>();
    
    /**
     * Retrieves the Memory object for this id or creates and configures a new one if it's not used. If the 
     * memory file exists data is loaded from it.
     * 
     * @param memId The memory id.
     * @param type Memory class (Ram.class for ex.). Ignored when the memory already exists.
     * @return A Memory object
     * @throws IOException 
     */
    public static Memory getMemory(String memId, Class<? extends Memory> type ) throws IOException, IllegalArgumentException {
        if (!isValidId(memId)) throw new IllegalArgumentException("Invalid memory id: " + memId);
        
        if (!Memory.memories.containsKey(memId)) {
            Memory memory;
            try {
                memory = type.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
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
    public static Memory getAnonymousMemory(Class<? extends Memory> type) throws IOException {
        return Memory.getMemory("anon" + getFreeMemId(), type);
    }
        
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
     * @throws java.io.IOException
     */
    public static boolean setupDataFolder(File pluginFolder) throws IOException {
        memoryFolder = new File(pluginFolder, memoryFolderName);
        if (!memoryFolder.exists()) {
            if (!memoryFolder.mkdirs()) 
                throw new IOException("Can't make folder " + memoryFolder.getAbsolutePath());            
            return true;
        } else return false;        
    }    
}
