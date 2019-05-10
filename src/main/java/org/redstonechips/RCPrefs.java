
package org.redstonechips;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.redstonechips.chip.io.InputPin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Tal Eisenberg
 */
public class RCPrefs {
    private static final String defaultsFileName = "/defaultprefs.yml";
    private static final String prefsFileName = "preferences.yml";

    /**
     * enum of all the default preferences keys.
     */
    public enum Prefs { inputBlockType, outputBlockType, interfaceBlockType, infoColor, errorColor, debugColor,
        signColor, enableDestroyCommand, maxInputChangesPerTick, usePermissions, useDenyPermissions, checkForUpdates;
    };

    private static final DumperOptions prefDump;
    static {
        prefDump = new DumperOptions();
        prefDump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    private static Material inputBlockType;
    private static Material outputBlockType;
    private static Material interfaceBlockType;

    private static ChatColor infoColor;
    private static ChatColor errorColor;
    private static ChatColor debugColor;

    private static String signColor;

    private static int maxInputChangesPerTick;
    private static boolean usePermissions;
    private static boolean useDenyPermissions;
    private static boolean checkForUpdates;
    
    private static Map<String,Object> prefs;
    private static Map<String, Object> defaults;

    private RCPrefs() {}
    
    /**
     * Initializes PrefsManager. Loads the defaults from file to the defaults Map.
     *
     * @param plugin reference to the RedstoneChips instance.
     * @throws java.io.IOException
     */
    public static void initialize() throws IOException {
        Yaml yaml = new Yaml();

        URL res = RedstoneChips.inst().getClass().getResource(defaultsFileName);
        InputStream stream;

        stream = res.openStream();
        defaults = (Map<String, Object>) yaml.load(stream);
        
        prefs = new HashMap<>();
    }

    /**
     * Loads the preferences from the preferences yaml file.
     */
    public static void loadPrefs() {
        RedstoneChips rc = RedstoneChips.inst();
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
            if (loadedPrefs==null) loadedPrefs = new HashMap<>();
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
    public static void savePrefs() {
        RedstoneChips rc = RedstoneChips.inst();
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
    public static Map<String, Object> setYaml(String yaml) throws IllegalArgumentException {
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
    public static void printYaml(CommandSender sender, Map<String, Object> map) {
        RedstoneChips rc = RedstoneChips.inst();
        
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
    public static Material getInputBlockType() {
        return inputBlockType;
    }

    /**
     *
     * @return The current output block type preference value.
     */
    public static Material getOutputBlockType() {
        return outputBlockType;
    }

    /**
     *
     * @return The current interface block type preference value.
     */
    public static Material getInterfaceBlockType() {
        return interfaceBlockType;
    }

    /**
     *
     * @return The current error chat message color preference value.
     */
    public static ChatColor getErrorColor() {
        return errorColor;
    }

    /**
     *
     * @return The current info chat message color preference value.
     */
    public static ChatColor getInfoColor() {
        return infoColor;
    }

    /**
     * 
     * @return The current sign activation color code. A hex value between 0-f.
     */
    public static String getSignColor() {
        return signColor;
    }

    /**
     * 
     * @return The current maxInputChangesPerTick preference value.
     */
    public static int getMaxInputChangesPerTick() {
        return maxInputChangesPerTick;
    }

    /**
     * 
     * @return The current permissions preference value.
     */
    public static boolean getUsePermissions() {
        return usePermissions;
    }

    /**
     * 
     * @return Whether to check for *.deny permissions or not.
     */
    public static boolean getUseDenyPermissions() {
        return useDenyPermissions;
    }

    /**
     * 
     * @return The current value of checkForUpdates preference.
     */
    public static boolean getCheckForUpdates() {
        return checkForUpdates;
    }
    
    /**
     *
     * @return The current debug chat message color preference value.
     */
    public static ChatColor getDebugColor() {
        return debugColor;
    }

    /** 
     * 
     * @param p Preference name
     * @return The value of preference p.
     */
    public static Object getPref(String p) {
        return prefs.get(p);
    }
    
    /**
     *
     * @return a Map containing all preference keys.
     */
    public static Map<String, Object> getPrefs() {
        return prefs;
    }

    private static void loadMissingPrefs(Map<String,Object> loadedPrefs) {
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
    public static void registerCircuitPreference(Class circuitClass, String key, Object defaultValue) {
        key = circuitClass.getSimpleName() + "." + key;

        // add default value
        defaults.put(key, defaultValue);

        // check if pref is missing
        if (prefs!=null && !prefs.containsKey(key))
            prefs.put(key, defaultValue);
    }

    private static void applyPrefs(Map<String, Object> loadedPrefs) {
        Map<String, Object> toapply = new HashMap<>();
        toapply.putAll(prefs);
        toapply.putAll(loadedPrefs);

        inputBlockType = Material.matchMaterial(toapply.get(Prefs.inputBlockType.name()).toString());
        outputBlockType = Material.matchMaterial(toapply.get(Prefs.outputBlockType.name()).toString());
        interfaceBlockType = Material.matchMaterial(toapply.get(Prefs.interfaceBlockType.name()).toString());

        infoColor = ChatColor.valueOf((String)toapply.get(Prefs.infoColor.name()));
        errorColor = ChatColor.valueOf((String)toapply.get(Prefs.errorColor.name()));
        debugColor = ChatColor.valueOf((String)toapply.get(Prefs.debugColor.name()));

        signColor = toapply.get(Prefs.signColor.name()).toString().toLowerCase();
        
        usePermissions = Boolean.parseBoolean(toapply.get(Prefs.usePermissions.name()).toString());
        useDenyPermissions = Boolean.parseBoolean(toapply.get(Prefs.useDenyPermissions.name()).toString());
           
        checkForUpdates = Boolean.parseBoolean(toapply.get(Prefs.checkForUpdates.name()).toString());
        
        maxInputChangesPerTick = Integer.parseInt(toapply.get(Prefs.maxInputChangesPerTick.name()).toString());
        InputPin.maxInputChangesPerTick = maxInputChangesPerTick;
        
        prefs.putAll(toapply);
    }

}
