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
    private RedstoneChips rc;
    private DumperOptions prefDump;
    private Map<String,Object> prefs;

    private Material chipBlockType = Material.SANDSTONE;
    private Material inputBlockType = Material.IRON_BLOCK;
    private Material outputBlockType = Material.GOLD_BLOCK;
    private Material interactionBlockType = Material.LAPIS_BLOCK;

    private ChatColor infoColor = ChatColor.GREEN;
    private ChatColor errorColor = ChatColor.RED;
    private ChatColor debugColor = ChatColor.AQUA;

    public PrefsManager(RedstoneChips plugin) {
        this.rc = plugin;

        prefDump = new DumperOptions();
        prefDump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    public void loadPrefs() {
        if (!rc.getDataFolder().exists()) rc.getDataFolder().mkdir();

        File propFile = new File(rc.getDataFolder(), "preferences.yml");
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

            if (prefs.containsKey("inputBlockType")) {
                Material i = findMaterial(prefs.get("inputBlockType"));
                if (i==null) rc.log(Level.WARNING, "Unknown material: " + prefs.get("inputBlockType"));
                else inputBlockType = i;
            } else prefs.put("inputBlockType", inputBlockType.name());

            if (prefs.containsKey("outputBlockType")) {
                Material i = findMaterial(prefs.get("outputBlockType"));
                if (i==null) rc.log(Level.WARNING, "Unknown material: " + prefs.get("outputBlockType"));
                else outputBlockType = i;
            } else prefs.put("outputBlockType", outputBlockType.name());

            if (prefs.containsKey("chipBlockType")) {
                Material i = findMaterial(prefs.get("chipBlockType"));
                if (i==null) rc.log(Level.WARNING, "Unknown material: " + prefs.get("chipBlockType"));
                else chipBlockType = i;
            } else prefs.put("chipBlockType", chipBlockType.name());

            if (prefs.containsKey("interactionBlockType")) {
                Material i = findMaterial(prefs.get("interactionBlockType"));
                if (i==null) rc.log(Level.WARNING, "Unknown material: " + prefs.get("interactionBlockType"));
                else interactionBlockType = i;
            } else prefs.put("interactionBlockType", interactionBlockType.name());

            yaml.dump(prefs, new FileWriter(propFile));
        } catch (IOException ex) {
            rc.log(Level.WARNING, ex.toString());
        }
    }

    public void savePrefs() {
        if (!rc.getDataFolder().exists()) rc.getDataFolder().mkdir();

        File prefsFile = new File(rc.getDataFolder(), "preferences.yml");
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

    public Material getInteractionBlockType() {
        return interactionBlockType;
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

    private static Material findMaterial(Object m) {
        if (m instanceof String)
            return Material.getMaterial(((String)m).toUpperCase());
        else if (m instanceof Integer)
            return Material.getMaterial((Integer)m);
        else return null;
    }

    public Map<String, Object> getPrefs() {
        return prefs;
    }

}
