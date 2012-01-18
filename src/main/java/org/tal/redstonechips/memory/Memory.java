
package org.tal.redstonechips.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSet7Constructor;
import org.tal.redstonechips.util.BitSet7Representer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Represents a memory capable of reading and writing bit sets.
 *
 * @author Tal Eisenberg
 */
public abstract class Memory {
    public static File memoryFolder;
    public static final String memoryFolderName = "memory";
    
    public static final String oldMemoryFolderName = "sram";
    
    public static final Pattern MemIDPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    
    public static Map<String,Memory> memories = new HashMap<String,Memory>();

    private String id;

    public abstract BitSet7 read(BitSet7 address);

    public abstract void write(BitSet7 address, BitSet7 data);

    public void init(String id) {
        this.id = id;
        memories.put(id, this);
    }

    public String getId() { return id; }

    public void store(File file) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new BitSet7Representer(), options);
        yaml.dump(getData(), new FileWriter(file));
    }

    public void load(File file) throws FileNotFoundException {
        Yaml yaml = new Yaml(new BitSet7Constructor());
        setData((Map)yaml.load(new FileInputStream(file)));
    }

    protected abstract Map getData();

    protected abstract void setData(Map data);
    
    public static boolean isValidId(String string) {
        return MemIDPattern.matcher(string).matches();
    }
    
    public static File getMemoryFile(String id) {
        return new File(memoryFolder, id + ".mem");
    }
    
    public static String getFreeMemId() {
        File file;
        int idx = 0;

        do {
            file = getMemoryFile(Integer.toString(idx));
            idx++;
        } while (file.exists());
        return Integer.toString(idx);
    }
    
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
                System.out.println("checking " + f.getName());
                if (isMemoryFile(f)) {
                    f.renameTo(new File(memoryFolder, renameMem(f)));
                    System.out.println("ismemoryfile!");
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
    
    public boolean delete() {
        return getFile().delete();
    }

    public File getFile() {
        return getMemoryFile(getId());
    }        
    
    public void save() throws IOException {
        store(getMemoryFile(getId()));
    }

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
            
            return memory;
        } else return Memory.memories.get(memId);
        
    }
    
    public static Memory getMemory(Class<? extends Memory> type) throws IOException {
        return Memory.getMemory(getFreeMemId(), type);
    }
    
}
