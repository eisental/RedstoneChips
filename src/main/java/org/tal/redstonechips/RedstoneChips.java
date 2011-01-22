package org.tal.redstonechips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockDamageLevel;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.material.Lever;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


/**
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {

    private final PluginDescriptionFile desc;

    public final static String circuitsFileName = "redstonechips.dat";

    private Material chipBlockType = Material.SANDSTONE;
    private Material inputBlockType = Material.IRON_BLOCK;
    private Material outputBlockType = Material.GOLD_BLOCK;

    private static final Logger logg = Logger.getLogger("Minecraft");
    private BlockListener rcBlockListener;
    private EntityListener rcEntityListener;

    private List<Circuit> circuits;

    private Map<String,Class> circuitClasses = new HashMap<String,Class>();
    private Map<Block, Object[]> inputLookupMap = new HashMap<Block, Object[]>();
    private Map<Block, Circuit> structureLookupMap = new HashMap<Block, Circuit>();

    private DumperOptions prefDump;
    private Map<String,Object> prefs;

    public RedstoneChips(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        this.desc = desc;

        prefDump = new DumperOptions();
        prefDump.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        rcBlockListener = new BlockListener() {

            @Override
            public void onBlockRedstoneChange(BlockFromToEvent event) {
                redstoneChange((BlockRedstoneEvent)event);
            }

            @Override
            public void onBlockRightClick(BlockRightClickEvent event) {
                checkForCircuit(event);
            }

            @Override
            public void onBlockDamage(BlockDamageEvent event) {
                if (event.getDamageLevel()==BlockDamageLevel.BROKEN)
                    checkCircuitDestroyed(event.getBlock(), event.getPlayer());
            }

        };

        rcEntityListener = new EntityListener() {

            @Override
            public void onEntityExplode(EntityExplodeEvent event) {
                for (Block b : event.blockList())
                    checkCircuitDestroyed(b, null);
            }
        };
    }

    @Override
    public void onDisable() {
        saveCircuits();
        logg.info(desc.getName() + " " + desc.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        loadPrefs();
        loadCircuits();


        pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_RIGHTCLICKED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_DAMAGED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);
        logg.info(desc.getName() + " " + desc.getVersion() + " enabled.");
    }

    @Override
    public boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("redchips-active")) {
            listActiveCircuits(player);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("redchips-classes")) {
            listCircuitClasses(player);
        } else if (cmd.getName().equalsIgnoreCase("redchips-prefs")) {
            if (args.length==0) { // list preferences
                printYaml(player, prefs);
                player.sendMessage(ChatColor.GREEN + "Type /redchips-prefs <name> <value> to make changes.");
            } else if (args.length==1) { // show one key value pair
                Object o = prefs.get(args[0]);
                if (o==null) player.sendMessage(ChatColor.RED + "Unknown preferences key: " + args[0]);
                else {
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put(args[0], o);

                    printYaml(player, map);
                }
            } else if (args.length==2) { // set value
                if (!player.isOp()) {
                    player.sendMessage(ChatColor.RED + "Unauthorized command: " + cmd.getName() + " " + args[0] + " " + args[1]);
                    return true;
                }
                Yaml yaml = new Yaml(prefDump);
                Map<String,Object> map = (Map<String,Object>)yaml.load(args[0] + ": " + args[1]);
                for (String key : map.keySet()) {
                    if (prefs.containsKey(key)) prefs.put(key, map.get(key));
                    else {
                        player.sendMessage(ChatColor.RED + "Unknown preferences key: " + key);
                        return false;
                    }
                }
                printYaml(player, map);
                player.sendMessage(ChatColor.GREEN + "Saving changes...");
                savePrefs();
            }
        } return false;
    }

    /**
     * Adds this class to the circuit pool, allowing players to create circuits of this class
     * 
     * @param c The class to add. must extend org.tal.redstonechips.Circuit.
     */
    public void addCircuitClass(Class c) {
        String name = c.getSimpleName();
        if (circuitClasses.containsKey(name)) {
            logg.warning("While trying to add " + c.getCanonicalName() + " to circuit pool: Another circuit class named " + name + " was found. ");
        } else if (!Circuit.class.isAssignableFrom(c)) {
            logg.warning("While trying to add " + c.getCanonicalName() + ": Class does not extend org.tal.redstonechips.circuits.Circuit");
        } else {
            circuitClasses.put(name, c);
        }
    }

    /**
     * Removes class from the circuit pool
     *
     * @param c Class to remove.
     */
    public void removeCircuitClass(Class c) {
        circuitClasses.remove(c.getSimpleName());
    }

    private void loadPrefs() {
        if (!this.getDataFolder().exists()) getDataFolder().mkdir();

        File propFile = new File(this.getDataFolder(), "preferences.yml");
        if (!propFile.exists()) { // create empty file if doesn't already exist
            try {
                propFile.createNewFile();
            } catch (IOException ex) {
                logg.log(Level.SEVERE, null, ex);
            }
        }

        try {
            Yaml yaml = new Yaml(prefDump);
            prefs = (Map<String, Object>)yaml.load(new FileInputStream(propFile));
            if (prefs==null) prefs = new HashMap<String, Object>();

            if (prefs.containsKey("inputBlockType")) {
                Material i = findMaterial(prefs.get("inputBlockType"));
                if (i==null) logg.warning("Unknown material: " + prefs.get("inputBlockType"));
                else inputBlockType = i;
            } else prefs.put("inputBlockType", inputBlockType.name());

            if (prefs.containsKey("outputBlockType")) {
                Material i = findMaterial(prefs.get("outputBlockType"));
                if (i==null) logg.warning("Unknown material: " + prefs.get("outputBlockType"));
                else outputBlockType = i;
            } else prefs.put("outputBlockType", outputBlockType.name());

            if (prefs.containsKey("chipBlockType")) {
                Material i = findMaterial(prefs.get("chipBlockType"));
                if (i==null) logg.warning("Unknown material: " + prefs.get("chipBlockType"));
                else chipBlockType = i;
            } else prefs.put("chipBlockType", chipBlockType.name());

            yaml.dump(prefs, new FileWriter(propFile));
        } catch (IOException ex) {
            logg.log(Level.WARNING, null, ex);
        }
    }

    private void savePrefs() {
        if (!this.getDataFolder().exists()) getDataFolder().mkdir();

        File prefsFile = new File(this.getDataFolder(), "preferences.yml");
        if (!prefsFile.exists()) { // create empty file if doesn't already exist
            try {
                prefsFile.createNewFile();
            } catch (IOException ex) {
                logg.log(Level.SEVERE, null, ex);
            }
        }

        Yaml yaml = new Yaml(prefDump);
        try {
            yaml.dump(prefs, new FileWriter(prefsFile));
        } catch (IOException ex) {
            logg.log(Level.SEVERE, null, ex);
        }

        loadPrefs();
    }

    private void loadCircuits() {
        Properties props = new Properties();
        File propFile = new File(this.getDataFolder(), circuitsFileName);
        if (!propFile.exists()) { // create empty file if doesn't already exist
            try {
                props.store(new FileOutputStream(propFile), "");
            } catch (IOException ex) {
                logg.log(Level.SEVERE, null, ex);
            }
        }

        circuits = new ArrayList<Circuit>();

        try {
            props.load(new FileInputStream(propFile));
            for (String id : props.stringPropertyNames()) {
                String circuitString = props.getProperty(id);
                Circuit c = RCPersistence.stringToCircuit(circuitString, this);
                if (c==null) logg.warning(desc.getName() + ": Error while loading circuit: " + circuitString);
                else {
                    circuits.add(c);
                    addInputLookup(c);
                    addStructureLookup(c);
                }
            }
            logg.info(desc.getName() + ": " + circuits.size() + " active circuits");
        } catch (Exception ex) {
            logg.log(Level.SEVERE, null, ex);
        }

    }

    private void saveCircuits() {
        Properties props = new Properties();
        File propFile = new File(getDataFolder(), circuitsFileName);

        for (Circuit c : circuits) {
            props.setProperty(""+circuits.indexOf(c), RCPersistence.toFileString(c, this));
        }

        try {
            props.store(new FileOutputStream(propFile), "");
        } catch (IOException ex) {
            logg.log(Level.SEVERE, null, ex);
        }
    }

    private void listActiveCircuits(Player p) {
        if (circuits.isEmpty()) p.sendMessage(ChatColor.GREEN + "There are no active circuits.");
        else {
            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "Active redstone circuits: ");
            p.sendMessage(ChatColor.GREEN + "----------------------");
            for (Circuit c : circuits)
                p.sendMessage(circuits.indexOf(c) + ": " + ChatColor.YELLOW + c.getClass().getSimpleName() + ChatColor.WHITE + " @ " + c.activationBlock.getX() + ", " + c.activationBlock.getY() + ", " + c.activationBlock.getZ());
            p.sendMessage(ChatColor.GREEN + "----------------------");
            p.sendMessage("");
        }

    }

    private void listCircuitClasses(Player p) {
        if (circuitClasses.isEmpty()) p.sendMessage(ChatColor.GREEN + "There are no circuit classes installed.");
        else {
            List<String> names = Arrays.asList(circuitClasses.keySet().toArray(new String[circuitClasses.size()]));
            Collections.sort(names);
            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "Installed circuit classes:");
            p.sendMessage(ChatColor.GREEN + "----------------------");
            String list = "";
            ChatColor color = ChatColor.WHITE;
            for (String name : names) {
                list += color + name + ", ";
                if (list.length()>50) {
                    p.sendMessage(list.substring(0, list.length()-2));
                    list = "";
                }
                if (color==ChatColor.WHITE)
                    color = ChatColor.YELLOW;
                else color = ChatColor.WHITE;
            }
            p.sendMessage(ChatColor.GREEN + "----------------------");
            p.sendMessage("");
        }
    }

    private void redstoneChange(BlockRedstoneEvent e) {

        boolean newVal = (e.getNewCurrent()>0);
        boolean oldVal = (e.getOldCurrent()>0);
        if (newVal==oldVal) return; // not a change

        Object[] o = inputLookupMap.get(e.getBlock());
        if (o!=null) {
            final Circuit c = (Circuit)o[0];
            final int i = (Integer)o[1];
            if (e.getBlock().getType()==Material.STONE_BUTTON) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                            c.redstoneChange(i, false);
                        } catch (InterruptedException ex) {}
                    }
                }.start();
            }
            c.redstoneChange(i, newVal);
        }
    }

    private void checkForCircuit(BlockRightClickEvent event) {
        Block b = event.getBlock();
        Player player = event.getPlayer();

        if (b.getType()==Material.WALL_SIGN) {

            // first check if its already registered
            for (Circuit c : circuits) {
                if (c.activationBlock.equals(b)) {
                    player.sendMessage(ChatColor.GREEN + "Circuit is already activated.");
                    return;
                }
            }

            // try to detect a circuit in any possible orientation (N,W,S or E)
            if (b.getData()==0x2) this.detectCircuit(b, BlockFace.WEST, player);
            else if (b.getData()==0x3) this.detectCircuit(b, BlockFace.EAST, player);
            else if (b.getData()==0x4) this.detectCircuit(b, BlockFace.SOUTH, player);
            else if (b.getData()==0x5) this.detectCircuit(b, BlockFace.NORTH, player);
        }
    }

    private boolean detectCircuit(Block blockClicked, BlockFace direction, Player player) {
        List<Block> inputs = new ArrayList<Block>();
        List<Block> outputs = new ArrayList<Block>();
        List<Block> structure = new ArrayList<Block>();
        Block curPlus1, curMinus1, curBlock;
        boolean xAxis = (direction==BlockFace.SOUTH || direction==BlockFace.NORTH);
        structure.add(blockClicked);
        curBlock = blockClicked.getFace(direction);
        curPlus1 = curBlock.getRelative((xAxis?0:1), 0, (xAxis?1:0));
        curMinus1 = curBlock.getRelative((xAxis?0:-1), 0, (xAxis?-1:0));


        while(curBlock.getType()==chipBlockType) {
            if (curPlus1.getType()==inputBlockType || curPlus1.getType()==outputBlockType) {
                structure.add(curPlus1);
                Block jack = curPlus1.getRelative((xAxis?0:1), 0, (xAxis?1:0));
                if (curPlus1.getType()==inputBlockType) {
                    inputs.add(jack);
                    //player.sendMessage("Found input at " + jack.toString());
                } else {
                    outputs.add(jack);
                    structure.add(jack);
                    //player.sendMessage("Found output at " + jack.toString());
                }
            }

            if (curMinus1.getType()==inputBlockType || curMinus1.getType()==outputBlockType) {
                structure.add(curMinus1);
                Block jack = curMinus1.getRelative((xAxis?0:-1), 0, (xAxis?-1:0));
                if (curMinus1.getType()==inputBlockType) {
                    inputs.add(jack);
                    //player.sendMessage("Found input at " + jack.toString());
                } else {
                    outputs.add(jack);
                    structure.add(jack);
                    //player.sendMessage("Found output at " + jack.toString());
                }
            }

            structure.add(curBlock); // a line block.

            // iterate forward
            curBlock = curBlock.getFace(direction);
            curPlus1 = curBlock.getRelative((xAxis?0:1), 0, (xAxis?1:0));
            curMinus1 = curBlock.getRelative((xAxis?0:-1), 0, (xAxis?-1:0));
        } 

        Block outputBlock = curBlock;
        Block lastLineBlock = structure.get(structure.size()-1);


        for (Block o : outputs) {
            if (o.getType()!=Material.LEVER) {
                return false;
            }
        }

        if (outputs.size()>0 || inputs.size()>0) {
            // we have a circuit
            Sign sign = (Sign)blockClicked.getState();
            String sargs = sign.getLine(1) + " " + sign.getLine(2) + " " + sign.getLine(3);
            StringTokenizer t = new StringTokenizer(sargs);
            String[] args = new String[t.countTokens()];
            int i = 0;
            while (t.hasMoreElements()) {
                args[i] = t.nextToken();
                i++;
            }

            try {
                Circuit c = getCircuitInstance(sign.getLine(0).trim());
                c.activationBlock = blockClicked;
                c.outputBlock = outputBlock;
                c.inputs = inputs.toArray(new Block[inputs.size()]);
                c.outputs = outputs.toArray(new Block[outputs.size()]);
                c.structure = structure.toArray(new Block[structure.size()]);
                c.lastLineBlock = lastLineBlock;
                c.args = args;

                if (c.initCircuit(player, args)) {
                    circuits.add(c);
                    addInputLookup(c);
                    addStructureLookup(c);
                    saveCircuits();
                    player.sendMessage(ChatColor.GREEN + "Activated " + c.getClass().getSimpleName() + " with " + inputs.size() + " inputs and " + outputs.size() + " outputs.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + c.getClass().getSimpleName() + " was not activated.");
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                // unknown circuit name
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                logg.warning(ex.toString());
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                logg.warning(ex.toString());
            }
        }

        // if we reached this point the circuit wasn't created.
        return false;
    }

    private void checkCircuitDestroyed(Block b, Player p) {
        Circuit destroyed = structureLookupMap.get(b);

        if (destroyed!=null && circuits.contains(destroyed)) {
            if (p!=null) p.sendMessage(ChatColor.RED + "You destroyed the " + destroyed.getClass().getSimpleName() + " chip.");
            destroyed.circuitDestroyed();
            circuits.remove(destroyed);
            removeInputLookup(destroyed);
            removeStructureLookup(destroyed);
            saveCircuits();
        }
    }

    /**
     *
     * @param name
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Circuit getCircuitInstance(String name) throws InstantiationException, IllegalAccessException {
        Class c = this.circuitClasses.get(name);
        if (c==null) throw new IllegalArgumentException("Unknown circuit type: " + name);
        else return (Circuit) c.newInstance();
    }

    private void addInputLookup(Circuit c) {
        for (int i=0; i<c.inputs.length; i++) {
            Block input = c.inputs[i];
            inputLookupMap.put(input, new Object[]{c, i});
        }
    }

    private void addStructureLookup(Circuit c) {
        for (int i=0; i<c.structure.length; i++)
            structureLookupMap.put(c.structure[i], c);
    }

    private void removeStructureLookup(Circuit c) {
        for (Block block : c.structure)
            structureLookupMap.remove(block);
    }

    private void removeInputLookup(Circuit c) {
        for (Block input : c.inputs) {
            inputLookupMap.remove(input);
        }
    }

    private void printYaml(Player player, Map<String, Object> map) {
        Yaml yaml = new Yaml(prefDump);
        String[] split = yaml.dump(map).split("\\n");
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + desc.getName() + " " + desc.getVersion() + " preferences:");
        player.sendMessage(ChatColor.GREEN + "-----------------------------");
        for (String line : split)
            player.sendMessage(line);
        player.sendMessage(ChatColor.GREEN + "-----------------------------");
        player.sendMessage("");
    }

    private Material findMaterial(Object m) {
        if (m instanceof String)
            return Material.getMaterial(((String)m).toUpperCase());
        else if (m instanceof Integer)
            return Material.getMaterial((Integer)m);
        else return null;
    }
}
