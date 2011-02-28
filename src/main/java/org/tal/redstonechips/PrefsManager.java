/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
        enableDestroyCommand;
    };

    private RedstoneChips rc;
    private DumperOptions prefDump;

    private Material inputBlockType;
    private Material outputBlockType;
    private Material interfaceBlockType;

    private ChatColor infoColor;
    private ChatColor errorColor;
    private ChatColor debugColor;

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

        defaults = (Map<String, Object>) yaml.load(getClass().getResourceAsStream(defaultsFileName));
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
            prefs = (Map<String, Object>)yaml.load(new FileInputStream(propFile));
            if (prefs==null) prefs = new HashMap<String, Object>();
            loadMissingPrefs();

            try {
                inputBlockType = findMaterial(prefs.get(Prefs.inputBlockType.name()));
                outputBlockType = findMaterial(prefs.get(Prefs.outputBlockType.name()));
                interfaceBlockType = findMaterial(prefs.get(Prefs.interfaceBlockType.name()));
            } catch (IllegalArgumentException ie) {
                rc.log(Level.SEVERE, "While loading preferences: " + ie.getMessage());
            }


            try {
                infoColor = ChatColor.valueOf((String)prefs.get(Prefs.infoColor.name()));
                errorColor = ChatColor.valueOf((String)prefs.get(Prefs.errorColor.name()));
                debugColor = ChatColor.valueOf((String)prefs.get(Prefs.debugColor.name()));
            } catch (IllegalArgumentException ie) {
                rc.log(Level.SEVERE, "While loading preferences: " + ie.getMessage());
            }

            yaml.dump(prefs, new FileWriter(propFile));
        } catch (IOException ex) {
            rc.log(Level.WARNING, ex.toString());
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

        loadPrefs();
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
            if (prefs.containsKey(key)) prefs.put(key, map.get(key));
            else throw new IllegalArgumentException("Unknown preferences key: " + key);
        }
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
    public Material getInputBlockType() {
        return inputBlockType;
    }

    /**
     *
     * @return The current output block type preference value.
     */
    public Material getOutputBlockType() {
        return outputBlockType;
    }

    /**
     *
     * @return The current interface block type preference value.
     */
    public Material getInterfaceBlockType() {
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
     * @return The current debug chat message color preference value.
     */
    public ChatColor getDebugColor() {
        return debugColor;
    }

    /**
     *
     * @return a Map containing every preference keys currently set.
     */
    public Map<String, Object> getPrefs() {
        return prefs;
    }

    private static Material findMaterial(Object m) throws IllegalArgumentException {
        if (m instanceof String) {
            Material material = findMaterial((String)m);
            if (material==null) throw new IllegalArgumentException("Unknown material name: " + m);
            else return material;
        } else if (m instanceof Integer) {
            Material material = Material.getMaterial((Integer)m);
            if (material==null) throw new IllegalArgumentException("Unknown material type id: " + m);
            else return material;
        }  else
            throw new IllegalArgumentException("Invalid material: " + m);
    }

    public static Material findMaterial(String m) throws IllegalArgumentException {
        try {
            // try to parse as int type id.
            int i = Integer.decode(m);
            Material material = Material.getMaterial(i);
            if (material==null) throw new IllegalArgumentException("Unknown material type id: " + m);
            else return material;
        } catch (NumberFormatException ne) {
            // try as material name
            for (Material material : Material.values()) {
                if (material.name().equals(m.toUpperCase()))
                    return material;
                else if(material.name().replaceAll("_", "").equals(m.toUpperCase()))
                    return material;
            }

            throw new IllegalArgumentException("Unknown material name: " + m);
        }
    }

    private void loadMissingPrefs() {
        for (String key : defaults.keySet()) {
            if (!prefs.containsKey(key))
                prefs.put(key, defaults.get(key));
        }
    }

    /**
     * Allows circuit libraries to register their own preferences keys. This method should be called
     * in the CircuitIndex onRedstoneChipsEnable() method to insure the key is added before RedstoneChips reads the preferences file.
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

}
