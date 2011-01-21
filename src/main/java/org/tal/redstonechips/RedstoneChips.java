package org.tal.redstonechips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author Tal Eisenberg
 */
public class RedstoneChips extends JavaPlugin {

    private final PluginDescriptionFile desc;

    public final static String circuitsFileName = "redstonechips.dat";

    private Material blockType = Material.SANDSTONE;
    private Material inputBlockType = Material.IRON_BLOCK;
    private Material outputBlockType = Material.GOLD_BLOCK;

    private static final Logger log = Logger.getLogger("Minecraft");
    private BlockListener rcBlockListener;
    private EntityListener rcEntityListener;

    private List<Circuit> circuits;

    private Map<String,Class> circuitClasses = new HashMap<String,Class>();
    private Map<Block, Object[]> inputLookupMap = new HashMap<Block, Object[]>();
    private Map<Block, Circuit> structureLookupMap = new HashMap<Block, Circuit>();

    public RedstoneChips(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        this.desc = desc;

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
        log.info(desc.getName() + " " + desc.getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        loadProperties();
        loadCircuits();


        pm.registerEvent(Type.REDSTONE_CHANGE, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_RIGHTCLICKED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_DAMAGED, rcBlockListener, Priority.Monitor, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, rcEntityListener, Priority.Monitor, this);

        log.info(desc.getName() + " " + desc.getVersion() + " enabled.");
    }

    @Override
    public boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equals("redchips-active")) {
            listActiveCircuits(player);
            return true;
        } else if (cmd.getName().equals("redchips-classes")) {
            listCircuitClasses(player);
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
            log.warning("While trying to add " + c.getCanonicalName() + " to circuit pool: Another circuit class named " + name + " was found. ");
        } else if (!Circuit.class.isAssignableFrom(c)) {
            log.warning("While trying to add " + c.getCanonicalName() + ": Class does not extend org.tal.redstonechips.circuits.Circuit");
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

    private void loadProperties() {
        Properties props = new Properties();
        File propFile = new File(desc.getName().toLowerCase() + ".properties");
        if (!propFile.exists()) { // create empty file if doesn't already exist
            try {
                props.store(new FileOutputStream(propFile), "");
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }

        try {

            log.info(desc.getName() + ": properties loaded.");

            if (props.containsKey("inputBlockType")) {
                inputBlockType = Material.getMaterial(props.getProperty("inputBlockType").toUpperCase());
            } else props.setProperty("inputBlockType", inputBlockType.name());

            if (props.containsKey("outputBlockType")) {
                outputBlockType = Material.getMaterial(props.getProperty("outputBlockType").toUpperCase());
            } else props.setProperty("outputBlockType", outputBlockType.name());

            if (props.containsKey("blockType")) {
                blockType = Material.getMaterial(props.getProperty("blockType"));
            } else props.setProperty("blockType", blockType.name());

            props.store(new FileOutputStream(propFile), "");
        } catch (IOException ex) {
            log.log(Level.WARNING, null, ex);
        }
    }

    private void loadCircuits() {
        Properties props = new Properties();
        File propFile = new File(circuitsFileName);
        if (!propFile.exists()) { // create empty file if doesn't already exist
            try {
                props.store(new FileOutputStream(propFile), "");
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }

        circuits = new ArrayList<Circuit>();

        try {
            props.load(new FileInputStream(propFile));
            for (String id : props.stringPropertyNames()) {
                String circuitString = props.getProperty(id);
                Circuit c = RCPersistence.stringToCircuit(circuitString, this);
                if (c==null) log.warning(desc.getName() + ": Error while loading circuit: " + circuitString);
                else {
                    circuits.add(c);
                    addInputLookup(c);
                    addStructureLookup(c);
                }
            }
            log.info(desc.getName() + ": Loaded " + circuits.size() + " circuits");
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }

    }

    private void saveCircuits() {
        Properties props = new Properties();
        File propFile = new File(circuitsFileName);

        for (Circuit c : circuits) {
            props.setProperty(""+circuits.indexOf(c), RCPersistence.toFileString(c, this));
        }

        try {
            props.store(new FileOutputStream(propFile), "");
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
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
            for (String name : names) {
                list += name + ", ";
                if (list.length()>20) {
                    p.sendMessage(list.substring(0, list.length()-2));
                    list = "";
                }
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
                    player.sendMessage("Circuit is already activated.");
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

        do {
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
        } while(curBlock.getType()==blockType);

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
                    player.sendMessage("Activated " + c.getClass().getSimpleName() + " with " + inputs.size() + " inputs and " + outputs.size() + " outputs.");
                    return true;
                } else {
                    player.sendMessage(c.getClass().getSimpleName() + " was not activated.");
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                // unknown circuit name
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                log.warning(ex.toString());
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                log.warning(ex.toString());
            }
        }

        // if we reached this point the circuit wasn't created.
        return false;
    }

    private void checkCircuitDestroyed(Block b, Player p) {
        Circuit destroyed = structureLookupMap.get(b);

        if (destroyed!=null) {
            if (p!=null) p.sendMessage("You destroyed the " + destroyed.getClass().getSimpleName() + " chip.");
            destroyed.circuitDestroyed();
            circuits.remove(destroyed);
            removeInputLookup(destroyed);
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
}
