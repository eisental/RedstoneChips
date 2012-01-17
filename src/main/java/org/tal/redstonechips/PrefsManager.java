
package org.tal.redstonechips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.circuit.ChipScanner;
import org.tal.redstonechips.circuit.RecursiveChipScanner;
import org.tal.redstonechips.circuit.ScanParameters;
import org.tal.redstonechips.circuit.io.IOBlock;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Tal Eisenberg
 */
public class PrefsManager {
    private static final String defaultsFileName = "/defaultprefs.yml";
    private static final String prefsFileName = "preferences.yml";

    /**
     * enum of all the default preferences keys.
     */
    public enum Prefs { inputBlockType, outputBlockType, interfaceBlockType, infoColor, errorColor, debugColor,
        signColor, enableDestroyCommand, maxInputChangesPerTick, usePermissions;
    };

    private RedstoneChips rc;
    private DumperOptions prefDump;

    private MaterialData inputBlockType;
    private MaterialData outputBlockType;
    private MaterialData interfaceBlockType;

    private ChatColor infoColor;
    private ChatColor errorColor;
    private ChatColor debugColor;

    private String signColor;

    private int maxInputChangesPerTick;
    private boolean usePermissions;

    private Map<String,Object> prefs;
    private Map<String, Object> defaults;

    /**
     * PrefsManager constructor. Loads the defaults from file to the defaults Map.
     *
     * @param plugin reference to the RedstoneChips instance.
     */
    public PrefsManager(RedstoneChips plugin) {
        this.rc = plugin;

        prefDump = new DumperOptions();
        prefDump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml();

        URL res = getClass().getResource(defaultsFileName);
        InputStream stream;
        try {
            stream = res.openStream();
            defaults = (Map<String, Object>) yaml.load(stream);
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.toString());
        }   
        prefs = new HashMap<String, Object>();
    }

    /**
     * Loads the preferences from the preferences yaml file.
     */
    public void loadPrefs() {
        if (!rc.getDataFolder().exists()) rc.getDataFolder().mkdir();

        File propFile = new File(rc.getDataFolder(), prefsFileName);
        if (!propFile.exists()) { // create empty file if doesn't already exist
            try {
                propFile.createNewFile();
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.toString());
            }
        }

        try {
            Yaml yaml = new Yaml(prefDump);
            Map<String,Object> loadedPrefs = (Map<String, Object>)yaml.load(new FileInputStream(propFile));
            if (loadedPrefs==null) loadedPrefs = new HashMap<String, Object>();
            loadMissingPrefs(loadedPrefs);

            applyPrefs(loadedPrefs);

            yaml.dump(prefs, new FileWriter(propFile));
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.toString() + ". Disabling plugin. Fix preferences.yml and try again.");
            rc.getPluginLoader().disablePlugin(rc);
        } catch (IllegalArgumentException ie) {
            rc.log(Level.SEVERE, "While loading preferences: " + ie.toString() + ". Disabling plugin. Fix preferences.yml and try again.");
            rc.getPluginLoader().disablePlugin(rc);
        }
    }

    /**
     * Saves the current preferences values from the prefs Map to the preferences yaml file.
     */
    public void savePrefs() {
        if (!rc.getDataFolder().exists()) rc.getDataFolder().mkdir();

        File prefsFile = new File(rc.getDataFolder(), prefsFileName);
        if (!prefsFile.exists()) { // create empty file if doesn't already exist
            try {
                prefsFile.createNewFile();
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.toString());
            }
        }

        Yaml yaml = new Yaml(prefDump);
        try {
            yaml.dump(prefs, new FileWriter(prefsFile));
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.toString());
        }
    }

    /**
     * Adds a chunk of yaml to the preferences Map. Parses the yaml into a Map object and sets the preference values according
     * to the map. Only changing the value of EXISTING preferences keys is allowed.
     *
     * @param yaml a String of yaml code.
     * @return Map object containing the parsed yaml values.
     * @throws IllegalArgumentException If one of the yaml keys was not found in the preferences map.
     */
    public Map<String, Object> setYaml(String yaml) throws IllegalArgumentException {
        Yaml y = new Yaml(prefDump);
        Map<String,Object> map = (Map<String,Object>)y.load(yaml);
        for (String key : map.keySet()) {
            if (!prefs.containsKey(key)) 
                throw new IllegalArgumentException("Unknown preferences key: " + key);
        }

        applyPrefs(map);
        
        return map;
    }

    /**
     * Sends a map object as a yaml dump to the sender.
     * @param sender The sender that will receive the yaml dump messages.
     * @param map The map to dump into yaml code.
     */
    public void printYaml(CommandSender sender, Map<String, Object> map) {

        Yaml yaml = new Yaml(prefDump);
        String[] split = yaml.dump(map).split("\\n");
        sender.sendMessage("");
        sender.sendMessage(getInfoColor() + rc.getDescription().getName() + " " + rc.getDescription().getVersion() + " preferences:");
        sender.sendMessage(getInfoColor() + "-----------------------------");
        for (String line : split)
            sender.sendMessage(line);
        sender.sendMessage(getInfoColor() + "-----------------------------");
        sender.sendMessage("");
    }

    /**
     *
     * @return The current input block type preference value.
     */
    public MaterialData getInputBlockType() {
        return inputBlockType;
    }

    /**
     *
     * @return The current output block type preference value.
     */
    public MaterialData getOutputBlockType() {
        return outputBlockType;
    }

    /**
     *
     * @return The current interface block type preference value.
     */
    public MaterialData getInterfaceBlockType() {
        return interfaceBlockType;
    }

    /**
     *
     * @return The current error chat message color preference value.
     */
    public ChatColor getErrorColor() {
        return errorColor;
    }

    /**
     *
     * @return The current info chat message color preference value.
     */
    public ChatColor getInfoColor() {
        return infoColor;
    }

    /**
     * 
     * @return The current sign activation color code. A hex value between 0-f.
     */
    public String getSignColor() {
        return signColor;
    }

    /**
     * 
     * @return The current maxInputChangesPerTick preference value.
     */
    public int getMaxInputChangesPerTick() {
        return maxInputChangesPerTick;
    }

    /**
     * 
     * @return The current permissions preference value.
     */
    public boolean getUsePermissions() {
        return usePermissions;
    }

    /**
     *
     * @return The current debug chat message color preference value.
     */
    public ChatColor getDebugColor() {
        return debugColor;
    }

    /**
     *
     * @return a Map containing all preference keys.
     */
    public Map<String, Object> getPrefs() {
        return prefs;
    }

    /**
     * Tries to find a material according to search string m.
     * The string can be either a material name or a name and data value combination such as: wool:orange or wood:1.
     * 
     * @param m 
     * @return a MaterialData that matches the search string.
     * @throws IllegalArgumentException when parameter m is invalid.
     */
    public static MaterialData findMaterial(String m) throws IllegalArgumentException {
        try {
            // try to parse as int type id.
            int i = Integer.decode(m);
            MaterialData material = new MaterialData(Material.getMaterial(i), (byte)-1);
            if (material==null) throw new IllegalArgumentException("Unknown material type id: " + m);
            else return material;
        } catch (NumberFormatException ne) {
            // try material:data
            int colonIdx = m.indexOf(':');
            if (colonIdx!=-1) {
                String smat = m.substring(0, colonIdx);
                String sdata = m.substring(colonIdx+1);
                Material material = findMaterial(smat).getItemType();

                try {
                    byte data = Byte.decode(sdata);
                    return new MaterialData(material, data);
                } catch (NumberFormatException le) {
                    if (material==Material.WOOL) {
                        // try as dye color
                        DyeColor color = DyeColor.valueOf(sdata.toUpperCase());
                        return new MaterialData(material, color.getData());
                    } else throw new IllegalArgumentException("Bad data value: " + m);
                }
            }
            // try as material name
            for (Material material : Material.values()) {
                if (material.name().equals(m.toUpperCase()))
                    return new MaterialData(material, (byte)-1);
                else if(material.name().replaceAll("_", "").equals(m.toUpperCase()))
                    return new MaterialData(material, (byte)-1);
            }

            throw new IllegalArgumentException("Unknown material name: " + m);
        }
    }

    private void loadMissingPrefs(Map<String,Object> loadedPrefs) {
        for (String key : defaults.keySet()) {
            if (!loadedPrefs.containsKey(key))
                loadedPrefs.put(key, defaults.get(key));
        }
    }

    /**
     * Allows circuit libraries to register their own preferences keys. This method should be called
     * in the CircuitIndex onRedstoneChipsEnable() method to ensure the key is added before RedstoneChips reads the preferences file.
     * The preferences key is set in the format of class-name.key
     *
     * @param circuitClass The circuit class that uses this preference key.
     * @param key The new preference key.
     * @param defaultValue The preference default value.
     */
    public void registerCircuitPreference(Class circuitClass, String key, Object defaultValue) {
        key = circuitClass.getSimpleName() + "." + key;

        // add default value
        defaults.put(key, defaultValue);

        // check if pref is missing
        if (prefs!=null && !prefs.containsKey(key))
            prefs.put(key, defaultValue);
    }

    private void applyPrefs(Map<String, Object> loadedPrefs) {
        Map toapply = new HashMap<String, Object>();
        toapply.putAll(prefs);
        toapply.putAll(loadedPrefs);

        inputBlockType = findMaterial(toapply.get(Prefs.inputBlockType.name()).toString());
        outputBlockType = findMaterial(toapply.get(Prefs.outputBlockType.name()).toString());
        interfaceBlockType = findMaterial(toapply.get(Prefs.interfaceBlockType.name()).toString());

        infoColor = ChatColor.valueOf((String)toapply.get(Prefs.infoColor.name()));
        errorColor = ChatColor.valueOf((String)toapply.get(Prefs.errorColor.name()));
        debugColor = ChatColor.valueOf((String)toapply.get(Prefs.debugColor.name()));

        signColor = toapply.get(Prefs.signColor.name()).toString().toLowerCase();
        
        usePermissions = Boolean.parseBoolean(toapply.get(Prefs.usePermissions.name()).toString());

        maxInputChangesPerTick = Integer.parseInt(toapply.get(Prefs.maxInputChangesPerTick.name()).toString());

        prefs.putAll(toapply);
    }

}
