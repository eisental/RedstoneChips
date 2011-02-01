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

    public enum Prefs { chipBlockType, inputBlockType, outputBlockType, interfaceBlockType, infoColor, errorColor, debugColor,
        enableDestroyCommand;
    };

    private RedstoneChips rc;
    private DumperOptions prefDump;

    private Material chipBlockType;
    private Material inputBlockType;
    private Material outputBlockType;
    private Material interfaceBlockType;

    private ChatColor infoColor;
    private ChatColor errorColor;
    private ChatColor debugColor;

    private Map<String,Object> prefs;
    private Map<String, Object> defaults;

    public PrefsManager(RedstoneChips plugin) {
        this.rc = plugin;

        prefDump = new DumperOptions();
        prefDump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml();

        defaults = (Map<String, Object>) yaml.load(getClass().getResourceAsStream(defaultsFileName));
    }

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
                chipBlockType = findMaterial(prefs.get(Prefs.chipBlockType.name()));
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

    public Map<String, Object> setYaml(String yaml) {
        Yaml y = new Yaml(prefDump);
        Map<String,Object> map = (Map<String,Object>)y.load(yaml);
        for (String key : map.keySet()) {
            if (prefs.containsKey(key)) prefs.put(key, map.get(key));
            else throw new IllegalArgumentException("Unknown preferences key: " + key);
        }
        return map;
    }

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

    public Material getInputBlockType() {
        return inputBlockType;
    }

    public Material getOutputBlockType() {
        return outputBlockType;
    }

    public Material getChipBlockType() {
        return chipBlockType;
    }

    public Material getInterfaceBlockType() {
        return interfaceBlockType;
    }

    public ChatColor getErrorColor() {
        return errorColor;
    }

    public ChatColor getInfoColor() {
        return infoColor;
    }

    public ChatColor getDebugColor() {
        return debugColor;
    }

    public Map<String, Object> getPrefs() {
        return prefs;
    }

    private static Material findMaterial(Object m) throws IllegalArgumentException{
        if (m instanceof String)
            return Material.getMaterial(((String)m).toUpperCase());
        else if (m instanceof Integer)
            return Material.getMaterial((Integer)m);
        else throw new IllegalArgumentException("Invalid material: " + m);
    }


    private void loadMissingPrefs() {
        for (String key : defaults.keySet()) {
            if (!prefs.containsKey(key))
                prefs.put(key, defaults.get(key));
        }
    }

    public void registerCircuitPreference(String key, Object defaultValue) {
        // add default value
        defaults.put(key, defaultValue);

        // check if pref is missing
        if (!prefs.containsKey(key))
            prefs.put(key, defaultValue);
    }

}
