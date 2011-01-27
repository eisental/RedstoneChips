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
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Tal Eisenberg
 */
public class PrefsManager {
    private static final String defaultsFileName = "/defaultprefs.yml";
    private static final String prefsFileName = "preferences.yml";

    private static final String cBTKey = "chipBlockType";
    private static final String inpBTKey = "inputBlockType";
    private static final String oBTKey = "outputBlockType";
    private static final String intBTKey = "interfaceBlockType";

    private static final String iCKey = "infoColor";
    private static final String eCKey = "errorColor";
    private static final String dCKey = "debugColor";

    private RedstoneChips rc;
    private DumperOptions prefDump;
    private Map<String,Object> prefs;

    private Material chipBlockType;
    private Material inputBlockType;
    private Material outputBlockType;
    private Material interfaceBlockType;

    private ChatColor infoColor;
    private ChatColor errorColor;
    private ChatColor debugColor;

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
            loadDefaultsIfNeeded(inpBTKey, oBTKey, cBTKey, iCKey, eCKey, dCKey);

            try {
                inputBlockType = findMaterial(prefs.get(inpBTKey));
                outputBlockType = findMaterial(prefs.get(oBTKey));
                interfaceBlockType = findMaterial(prefs.get(intBTKey));
                chipBlockType = findMaterial(prefs.get(cBTKey));
            } catch (IllegalArgumentException ie) {
                rc.log(Level.SEVERE, "While loading preferences: " + ie.getMessage());
            }


            try {
                infoColor = ChatColor.valueOf((String)prefs.get(iCKey));
                errorColor = ChatColor.valueOf((String)prefs.get(eCKey));
                debugColor = ChatColor.valueOf((String)prefs.get(dCKey));
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

    public void printYaml(Player player, Map<String, Object> map) {

        Yaml yaml = new Yaml(prefDump);
        String[] split = yaml.dump(map).split("\\n");
        player.sendMessage("");
        player.sendMessage(getInfoColor() + rc.getDescription().getName() + " " + rc.getDescription().getVersion() + " preferences:");
        player.sendMessage(getInfoColor() + "-----------------------------");
        for (String line : split)
            player.sendMessage(line);
        player.sendMessage(getInfoColor() + "-----------------------------");
        player.sendMessage("");
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


    public void loadDefaultsIfNeeded(String... keys) {
        for (String key : keys) {
            if (!prefs.containsKey(key))
                prefs.put(key, defaults.get(key));
        }
    }

}
