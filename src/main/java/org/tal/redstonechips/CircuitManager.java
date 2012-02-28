
package org.tal.redstonechips;

import java.util.*;
import java.util.logging.Level;
import net.eisental.common.parsing.ParsingUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Redstone;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.CircuitListener;
import org.tal.redstonechips.circuit.io.InputPin;
import org.tal.redstonechips.circuit.io.InputPin.SourceType;
import org.tal.redstonechips.circuit.io.InterfaceBlock;
import org.tal.redstonechips.circuit.io.OutputPin;
import org.tal.redstonechips.circuit.scan.ChipScanner.ChipScanException;
import org.tal.redstonechips.circuit.scan.IOChipScanner;
import org.tal.redstonechips.circuit.scan.RecursiveChipScanner;
import org.tal.redstonechips.circuit.scan.ScanParameters;
import org.tal.redstonechips.circuit.scan.SingleBlockChipScanner;
import org.tal.redstonechips.util.ChunkLocation;
import org.tal.redstonechips.wireless.Wireless;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitManager implements Listener {

    private RedstoneChips rc;
    
    private HashMap<Integer, Circuit> circuits = new HashMap<Integer, Circuit>();

    private Map<ChunkLocation, List<Circuit>> chunkLookupMap = new HashMap<ChunkLocation, List<Circuit>>();
    private Map<Location, List<InputPin>> sourceLookupMap = new HashMap<Location, List<InputPin>>();
    private Map<Location, InputPin> inputPinLookupMap = new HashMap<Location, InputPin>();
    private Map<Location, OutputPin> outputPinLookupMap = new HashMap<Location, OutputPin>();
    private Map<Location, List<OutputPin>> outputLookupMap = new HashMap<Location, List<OutputPin>>();
    private Map<Location, Circuit> structureLookupMap = new HashMap<Location, Circuit>();
    private Map<Location, Circuit> activationLookupMap = new HashMap<Location, Circuit>();

    private List<ChunkLocation> processedChunks = new ArrayList<ChunkLocation>();
    
    public CircuitManager(RedstoneChips plugin) { 
        rc = plugin; 
    }

    /**
     * Redstone change event handler. Checks if this event reports an input change in any circuit's input pins.
     * When the new redstone state is different than the current one, the input pin is updated and the circuit is notified.
     *
     * @param e A redstone change event.
     */
    public void onBlockRedstoneChange(Block b, int newCurrent, int oldCurrent) { 
        boolean newVal = (newCurrent>0);
        boolean oldVal = (oldCurrent>0);
        if (newVal==oldVal) return; // not a change

        List<InputPin> inputList = sourceLookupMap.get(b.getLocation());
        if (inputList==null) return;
        for (InputPin inputPin : inputList)
            inputPin.updateValue(b, newVal, SourceType.REDSTONE);
        
    }

    /**
     * Scans a chip starting at params.signBlock, debug level set to 0.
     * 
     * @param params An initialized ScanParameters object.
     * @param sender The circuit activator
     * @return The new chip id or -1 if a chip was not found or -2 if an error occurred.
     */
    public int checkForCircuit(ScanParameters params, CommandSender sender) {
        return checkForCircuit(params, sender, 0);
    }

    /**
     * Scans a chip starting at params.signBlock.
     * 
     * @param params An initialized ScanParameters object.
     * @param sender The circuit activator.
     * @param debugLevel Scanning debug level. 0 - no debug messages.
     * @return The new chip id or -1 if a chip was not found or -2 if an error occurred.
     */
    public int checkForCircuit(ScanParameters params, CommandSender sender, int debugLevel) {
        Block signBlock = params.signBlock;        

        BlockState state = signBlock.getState();
        if (!(state instanceof Sign)) return -1;

        Sign sign = (Sign)signBlock.getState();
        String signClass = getClassFromSign(sign);

        // check if the sign text points to a known circuit type.
        if (!rc.getCircuitLoader().getCircuitClasses().containsKey(signClass)) return -1;
        
        // check if it belongs to an active chip.
        Circuit check = this.getCircuitByActivationBlock(signBlock.getLocation());
        if (check!=null) {
            if (sender!=null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Chip is already active - " + check.getChipString() + ".");
            return -2;
        }
        
        if (!checkChipPermission(sender, signClass, true)) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to create circuits of type " + signClass + ".");
            return -2;
        }

        IOChipScanner scanner = new RecursiveChipScanner();
        scanner.setDebugger(sender, debugLevel);
        
        if (!scanner.isTypeAllowed(params, params.chipMaterial, params.origin.getData())) {
            try {
                scanner = new SingleBlockChipScanner();
                scanner.setDebugger(sender, debugLevel);
                scanner.scan(params);
            } catch (ChipScanException e) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "You can't build a redstone chip using this material ("
                        + params.chipMaterial.name() + ").");
            }
        } else {        
            try {
                params = scanner.scan(params);
            } catch (ChipScanException e) {
                if (sender!=null) sender.sendMessage(rc.getPrefs().getErrorColor() + e.getMessage());
            }
        }
        
        if (params==null || (params.outputs.isEmpty() && params.inputs.isEmpty() && params.interfaces.isEmpty())) return -1;

        for (Block b : params.structure) {
            Circuit c = this.getCircuitByStructureBlock(b.getLocation());
            if (c!=null) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + "One of the chip blocks (" + 
                        rc.getPrefs().getInfoColor() + b.getType().name().toLowerCase() + 
                        rc.getPrefs().getErrorColor() + ") already belongs to another chip: " + 
                        rc.getPrefs().getInfoColor() + c.getChipString());
                return -2;
            }
        }
        
        Circuit c;
        try {
            c = rc.getCircuitLoader().getCircuitInstance(signClass);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
            rc.log(Level.WARNING, ex.toString());
            return -2;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            rc.log(Level.WARNING, ex.toString());
            return -2;
        } catch (IllegalArgumentException ex) {
            // unknown circuit name. shouldn't happen at this stage.
            return -2;
        }

        c.world = signBlock.getWorld();

        c.activationBlock = signBlock.getLocation();

        c.structure = new Location[params.structure.size()];
        for (int i=0; i<params.structure.size(); i++) {
            c.structure[i] = params.structure.get(i).getLocation();
        }

        c.inputs = new InputPin[params.inputs.size()];
        for (int i=0; i<params.inputs.size(); i++) {
            Location l = params.inputs.get(i).getLocation();
            InputPin ip = new InputPin(c, l, i);
            c.inputs[i] = ip;
        }

        c.outputs = new OutputPin[params.outputs.size()];
        for (int i=0; i<params.outputs.size(); i++) {
            Location l = params.outputs.get(i).getLocation();
            OutputPin op = new OutputPin(c, l, i);
            c.outputs[i] = op;
        }

        c.interfaceBlocks = new InterfaceBlock[params.interfaces.size()];
        for (int i=0; i<params.interfaces.size(); i++) {
            InterfaceBlock ib = new InterfaceBlock(c, params.interfaces.get(i).getLocation(), i);
            c.interfaceBlocks[i] = ib; 
        }

        c.circuitChunks = findCircuitChunks(c);

        c.args = getArgsFromSign(sign);

        return this.activateCircuit(c, sender, -1);        
    }
    
    /**
     * Activates an already scanned circuit.
     *
     * @param c The circuit to activate
     * @param sender The activator.
     * @param id The desired circuit id. When less than 0, a new id is generated.
     * @return The circuit's id or -2 if an error occurred.
     */
    public int activateCircuit(Circuit c, CommandSender sender, int id) {
        int res;
        
        List<ChunkLocation> chunksToUnload = new ArrayList<ChunkLocation>();
        
        for (ChunkLocation chunk : c.circuitChunks) {
            if (!chunk.isChunkLoaded()) {
                chunksToUnload.add(chunk);
                workOnChunk(chunk);
            }
        }
        
        if (c.initCircuit(sender, rc)) {
            this.addCircuitLookups(c);

            if (id<0)
                c.id = generateId();
            else
                c.id = id;
            circuits.put(c.id, c);

            if (sender != null) {
                ChatColor ic = rc.getPrefs().getInfoColor();
                ChatColor dc = rc.getPrefs().getDebugColor();
                sender.sendMessage(ic + "Activated " + ChatColor.YELLOW + c.getChipString() + ic + ":");
                sender.sendMessage(dc + "> " + ChatColor.WHITE + c.inputs.length + dc + " input"
                        + (c.inputs.length!=1?"s":"") + ", " + ChatColor.YELLOW + c.outputs.length + dc + " output"
                        + (c.outputs.length!=1?"s":"") + " and " + ChatColor.AQUA + c.interfaceBlocks.length + dc
                        + " interface block" + (c.interfaceBlocks.length!=1?"s":"") + ".");
            }

            c.updateCircuitSign(true);

            res = c.id;
        } else {
            if (sender!=null)
                sender.sendMessage(rc.getPrefs().getErrorColor() + c.getClass().getSimpleName() + " was not activated.");
            res = -2;
        }
        
        for (ChunkLocation chunk : chunksToUnload) {
            releaseChunk(chunk);
        }

        return res;
    }

    /**
     * Checks whether the block b is part of a circuit and if so deactivates the circuit.
     *
     * @param b The block that was broken.
     * @param s The breaker. Can be null.
     */
    public boolean checkCircuitDestroyed(Block b, CommandSender s) {
        Circuit destroyed = structureLookupMap.get(b.getLocation());

        if (destroyed!=null && circuits.containsValue(destroyed)) {
            if (destroyCircuit(destroyed, s, false)) {
                if (s!=null) s.sendMessage(rc.getPrefs().getErrorColor() + "You destroyed " + destroyed.getChipString() + ".");
            } else {
                return false;
            }
        }
        return true;
    }

    private final static Class redstoneClass = Redstone.class;

    /**
     * Called on block place and block break to see if any circuit input pin state is affected by the change.
     *
     * @param block The block that was placed or broken.
     * @param player The player who placed or broke the block.
     * @param isBroken True if the block was broken and false if it was placed.
     * @return true if an input source block was placed or broken.
     */
    boolean checkCircuitInputBlockChanged(Block block, Player player, boolean isBroken) {
        Class<? extends MaterialData> dataClass = block.getType().getData();
        if (dataClass!=null && redstoneClass.isAssignableFrom(dataClass)) {
            List<InputPin> inputs = sourceLookupMap.get(block.getLocation());
            if (inputs!=null) {
                for (InputPin pin : inputs) {
                    if (isBroken) pin.updateValue(block, false, SourceType.REDSTONE);
                    else pin.updateValue(block, pin.findSourceBlockState(block.getLocation()), SourceType.REDSTONE);
                }
                return true;
            }

        }
        
        return false;
    }

    /**
     * Called on block place to see if any circuit output pin needs to refresh.
     * 
     * @param block The block that was placed.
     * @param player The player who placed the block.
     * @return true if an output block was placed.
     */
    boolean checkCircuitOutputBlockPlaced(Block block, Player player) {
        if (OutputPin.isOutputMaterial(block.getType())) {
            final List<OutputPin> outputs = outputLookupMap.get(block.getLocation());
            if (outputs!=null && !outputs.isEmpty()) {
/*                rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, 
                        new Runnable() {
                            @Override
                            public void run() {*/
                                for (OutputPin pin : outputs) {
                                    pin.refreshOutputs();
                                }
                           /* }
                        }
                    );*/
                return true;
            }

        }
        
        return false;
    }
    
    /**
     * Deactivates the specified circuit, possibly changing all of its structure blocks into air.
     *
     * @param destroyed The circuit that was destroyed.
     * @param destroyer The circuit's destroyer.
     * @param destroyBlocks True if the circuit's blocks should turn into air.
     * @return true if successful.
     */
    public boolean destroyCircuit(Circuit destroyed, CommandSender destroyer, boolean destroyBlocks) {
        if (destroyBlocks) {
            boolean enableDestroyCommand = (Boolean)rc.getPrefs().getPrefs().get(PrefsManager.Prefs.enableDestroyCommand.name());
            if (!enableDestroyCommand) {
                if (destroyer!=null) destroyer.sendMessage(rc.getPrefs().getErrorColor()+"/rcdestroy is disabled. You can enable it using /rcprefs enableDestroyCommand true");
                return false;
            }
        }

        if (!checkChipPermission(destroyer, destroyed.getClass().getSimpleName(), false)) {
            if (destroyer!=null) destroyer.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to destroy circuits of type " + destroyed.getClass().getSimpleName() + ".");
            return false;
        }
        
        if (destroyer!=null) {
            List<Wireless> list = rc.getChannelManager().getCircuitWireless(destroyed);
            for (Wireless w : list) {
                if (w.getChannel()!=null && !(w.getChannel().checkChanPermissions(destroyer, false))) {
                    destroyer.sendMessage(rc.getPrefs().getErrorColor()+"You do not have permissions to use channel " + w.getChannel().name + ".");
                    return false;
                }
            }
        }

        destroyed.destroyCircuit(destroyer);

        circuits.remove(destroyed.id);
        removeCircuitLookups(destroyed);
        
        if (destroyBlocks) {
            for (Location l : destroyed.structure)
                destroyed.world.getBlockAt(l).setType(Material.AIR);
        } else {
            destroyed.updateCircuitSign(false);
        }        

        return true;
    }

    /**
     * Resets a circuit. First the circuit is destroyed and then reactivated. Any listeners of the circuit are copied over
     * to the new circuit.
     *
     * @param c The circuit to reset.
     * @param reseter The reseter.
     * @return true if the circuit was reactivated.
     */
    public boolean resetCircuit(Circuit c, CommandSender reseter) {
        Block activationBlock = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
        List<CircuitListener> listeners = c.getListeners();
        Map<String,Object> data = c.getResetData();
		
        int id = c.id;
        String name = c.name;
        
        if (!rc.getCircuitManager().destroyCircuit(c, reseter, false)) return false;
        int newId = checkForCircuit(ScanParameters.generateDefaultParams(activationBlock, rc), reseter);
        Circuit newCircuit = rc.getCircuitManager().getCircuits().get(newId);

        if (newCircuit!=null) {

            newCircuit.id = id;
            newCircuit.name = name;
			newCircuit.setResetData(data);
            newCircuit.getListeners().addAll(listeners);

            if (reseter!=null) reseter.sendMessage(rc.getPrefs().getInfoColor() + "Successfully reactivated " + ChatColor.YELLOW + newCircuit.getChipString() + rc.getPrefs().getInfoColor() + ".");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called on every chunk load event. Finds any circuits in the loaded chunk and calls their .circuitChunkLoaded() method.
     * 
     * @param chunk The loaded chunk.
     */
    public void updateOnChunkLoad(ChunkLocation chunk) {
        if (processedChunks.contains(chunk)) return;
        List<Circuit> circuitsInChunk = chunkLookupMap.get(chunk);

        if (circuitsInChunk!=null) {
            for (Circuit c : circuitsInChunk) {
                c.circuitChunkLoaded();
            }
        }
    }

    /**
     * Check each active circuit to see if all of its blocks are in place. See Circuit.checkIntegrity().
     * Any unloaded circuit chunks are first loaded and then unloaded after the check is over.
     */
    public void checkCircuitsIntegrity(World world) {
        if (circuits==null) return;

        List<Integer> invalidIds = new ArrayList<Integer>();

        List<ChunkLocation> unloadedChunks = new ArrayList<ChunkLocation>();
        
        for (Circuit c : circuits.values()) {
            if(c.world.equals(world)) {
                for (ChunkLocation chunk : c.circuitChunks) {
                    if (!chunk.isChunkLoaded() && !unloadedChunks.contains(chunk))
                        unloadedChunks.add(chunk);
                }

                // we also might need to load/unload some chunks that don't have i/o blocks in them
                for (Location s : c.structure) {
                    ChunkLocation chunk = ChunkLocation.fromLocation(s);
                    if (!chunk.isChunkLoaded() && !unloadedChunks.contains(chunk))
                        unloadedChunks.add(chunk);
                }
            }
        }

        for (ChunkLocation c : unloadedChunks)
            workOnChunk(c);
        
        for (Circuit c : circuits.values()) {
            if(c.world.equals(world)) {
                if (!c.checkIntegrity()) {
                    invalidIds.add(c.id);
                }
            }
        }

        String msg = "";
        if (!invalidIds.isEmpty()) {
            String ids = "";

            for (int i : invalidIds) {
                Circuit c = circuits.get(i);
                String details = "("  + c.getCircuitClass() + " @ " + c.activationBlock.getBlockX() + ", " + c.activationBlock.getBlockY() + ", " + c.activationBlock.getBlockZ() + " on " + c.world.getName() + ")";
                this.destroyCircuit(circuits.get(i), null, false);
                ids += i + " " + details + ", ";
            }

            ids = ids.substring(0, ids.length()-2);

            msg = "Deactivated " + invalidIds.size() + " damaged circuits: " + ids;
        }

        for (ChunkLocation chunk : unloadedChunks) {
            releaseChunk(chunk);
        }

        if (!invalidIds.isEmpty()) rc.log(Level.INFO, "Done checking circuits. " + msg);
    }

    /**
     * Finds the circuit's chunks according to its activation block, output power blocks, input power blocks and interface blocks.
     *
     * @param c The circuit to check.
     * @return All chunks used by this circuit.
     */
    public ChunkLocation[] findCircuitChunks(Circuit c) {
        List<ChunkLocation> circuitChunks = new ArrayList<ChunkLocation>();

        circuitChunks.add(ChunkLocation.fromLocation(c.activationBlock));

        for (int i=0; i<c.outputs.length; i++) {
            for (Location out : c.outputs[i].getOutputBlocks()) {
                ChunkLocation chunk = ChunkLocation.fromLocation(out);

                if (!circuitChunks.contains(chunk))
                    circuitChunks.add(chunk);
            }
        }

        for (int i=0; i<c.inputs.length; i++) {
            for (Location in : c.inputs[i].getSourceBlocks()) {
                ChunkLocation chunk = ChunkLocation.fromLocation(in);

                if (!circuitChunks.contains(chunk))
                    circuitChunks.add(chunk);
            }
        }

        for (int i=0; i<c.interfaceBlocks.length; i++) {
            ChunkLocation chunk = ChunkLocation.fromLocation(c.interfaceBlocks[i].getLocation());

            if (!circuitChunks.contains(chunk))
                circuitChunks.add(chunk);
        }

        return circuitChunks.toArray(new ChunkLocation[circuitChunks.size()]);
    }

    /**
     * Calls Circuit.circuitShutdown on every activated circuit.
     */
    public void shutdownAllCircuits() {
        for (Circuit c : circuits.values()) c.shutdownCircuit();
    }

    /**
     * @return a map of all active circuits. The map keys are circuit ids.
     */
    public HashMap<Integer, Circuit> getCircuits() {
        return circuits;
    }

    /**
     * @return a map of all active circuits in the specified world. The map keys are circuit ids.
     */
    public HashMap<Integer, Circuit> getCircuits(World world) {
        HashMap<Integer, Circuit> worldCircuits = new HashMap<Integer, Circuit>();
        for (Integer id : circuits.keySet()) {
          Circuit c = circuits.get(id);
          if (c.world.equals(world)) {
            worldCircuits.put(id,c);
          }
        }
        
        return worldCircuits;
    }

    /**
     *
     * @param structureBlock Any block that belongs to a chip.
     * @return The circuit that the block belongs to or null if a circuit was not found.
     */
    public Circuit getCircuitByStructureBlock(Location structureBlock) {
        return this.structureLookupMap.get(structureBlock);
    }

    /**
     *
     * @param activationBlock An activation sign of a chip.
     * @return The circuit that uses this activation sign or null if a circuit was not found.
     */
    public Circuit getCircuitByActivationBlock(Location activationBlock) {
        return this.activationLookupMap.get(activationBlock);
    }

    /**
     * 
     * @param outputBlock An output block of a chip.
     * @return The output pin represented by this block or null if none was found.
     */
    public OutputPin getOutputPin(Location outputBlock) {
        return this.outputPinLookupMap.get(outputBlock);
    }

    /**
     * 
     * @param inputBlock An input block of a chip.
     * @return The input pin represented by this block or null if none was found.
     */    
    public InputPin getInputPin(Location inputBlock) {
        return this.inputPinLookupMap.get(inputBlock);
    }
    
    /**
     * 
     * @param sourceBlock A signal source block surrounding an input block.
     * @return A list of input pins that can receive a signal from this block or null if none was found.
     */
    public List<InputPin> getInputPinBySource(Location sourceBlock) {
        return this.sourceLookupMap.get(sourceBlock);
    }

    /**
     * 
     * @param outputBlock A block that receives signal from an output pin.
     * @return A list of output pins that can change the state of the output block or null if none was found.
     */
    public List<OutputPin> getOutputPinByOutputBlock(Location outputBlock) {
        return this.outputLookupMap.get(outputBlock);
    }
    
    /**
     * 
     * @param id A chip id number of name.
     * @return The chip that has this id or null if none was found.
     */
    public Circuit getCircuitById(String id) {  
        if (id==null) return null;

        if (ParsingUtils.isInt(id)) { // as id number
            return circuits.get(Integer.decode(id));
        } else { // as name
            for (Circuit c : circuits.values()) {
                if (c.name!=null && c.name.equals(id)) return c;
            }
        }
        
        return null;
    }
    
    /**
     * Sets the map of active circuits to a new one.
     *
     * @param circuits A new active circuits map. The map keys are circuit ids.
     */
    void setCircuitMap(HashMap<Integer, Circuit> circuits) {
        this.circuits = circuits;
    }

    /**
     * Unloads all chip in the specified world.
     * 
     * @param unloadedWorld 
     */
    public void unloadWorldChips(World unloadedWorld) {
        HashMap<Integer, Circuit> unloadedCircuits = this.getCircuits(unloadedWorld);
        for (Circuit c : unloadedCircuits.values()) {
            c.shutdownCircuit();
            
            circuits.remove(c.id);
        }
        
        rc.getCircuitPersistence().removeLoadedWorld(unloadedWorld);
    }

    /**
     * Generates a circuit id.
     * 
     * @return a new unused circuit id.
     */
    public int generateId() {
        int i = 0;

        if (circuits!=null)
            while(circuits.containsKey(i)) i++;

        return i;
    }
    
    private String[] getArgsFromSign(Sign sign) {
        String sargs = (sign.getLine(1) + " " + sign.getLine(2) + " " + sign.getLine(3)).replaceAll("\\xA7\\d", "");
        StringTokenizer t = new StringTokenizer(sargs);
        String[] args = new String[t.countTokens()];
        int i = 0;
        while (t.hasMoreElements()) {
            args[i] = t.nextToken();
            i++;
        }

        return args;
    }

    private String getClassFromSign(Sign sign) {
        String line = sign.getLine(0);
        line = line.replaceAll("\\xA7\\d", "");
        return line.trim();
    }

    private void addCircuitLookups(Circuit c) {
        for (int i=0; i<c.structure.length; i++)
            structureLookupMap.put(c.structure[i], c);

        activationLookupMap.put(c.activationBlock, c);

        for (int i=0; i<c.inputs.length; i++) {
            for (Location l : c.inputs[i].getSourceBlocks()) {
                if (!sourceLookupMap.containsKey(l))
                    sourceLookupMap.put(l, new ArrayList<InputPin>());

                sourceLookupMap.get(l).add(c.inputs[i]);
            }
            
            inputPinLookupMap.put(c.inputs[i].getLocation(), c.inputs[i]);
        }

        for (int i=0; i<c.outputs.length; i++) {
            for (Location l : c.outputs[i].getOutputBlocks()) {
                if (!outputLookupMap.containsKey(l))
                    outputLookupMap.put(l, new ArrayList<OutputPin>());
                
                outputLookupMap.get(l).add(c.outputs[i]);
            }
            
            outputPinLookupMap.put(c.outputs[i].getLocation(), c.outputs[i]);
        }
        
        for (ChunkLocation chunk : c.circuitChunks) {
            if (!chunkLookupMap.containsKey(chunk))
                chunkLookupMap.put(chunk, new ArrayList<Circuit>());
            chunkLookupMap.get(chunk).add(c);
        }
    }

    private void removeCircuitLookups(Circuit c) {
        for (Location l : c.structure)
            structureLookupMap.remove(l);

        for (OutputPin o : c.outputs) {
            outputPinLookupMap.remove(o.getLocation());
        }

        for (InputPin i : c.inputs) {
            inputPinLookupMap.remove(i.getLocation());
        }
        
        List<Location> inputBlocksToRemove = new ArrayList<Location>();
        for (Location l : sourceLookupMap.keySet()) {
            List<InputPin> pins = sourceLookupMap.get(l);
            List<InputPin> toRemove = new ArrayList<InputPin>();
            for (InputPin pin : pins) {
                if (pin.getCircuit()==c)
                    toRemove.add(pin);
            }

            pins.removeAll(toRemove);
            if (pins.isEmpty())
                inputBlocksToRemove.add(l);
        }
        for (Location l : inputBlocksToRemove)
            sourceLookupMap.remove(l);
        
        List<Location> outputBlocksToRemove = new ArrayList<Location>();
        for (Location l : outputLookupMap.keySet()) {
            List<OutputPin> pins = outputLookupMap.get(l);
            List<OutputPin> toRemove = new ArrayList<OutputPin>();
            for (OutputPin pin : pins) {
                if (pin.getCircuit()==c)
                    toRemove.add(pin);
            }
            
            pins.removeAll(toRemove);
            if (pins.isEmpty())
                outputBlocksToRemove.add(l);
        }
        for (Location l : outputBlocksToRemove)
            outputLookupMap.remove(l);
        
        activationLookupMap.remove(c.activationBlock);

        List<ChunkLocation> emptyChunks = new ArrayList<ChunkLocation>();

        for (ChunkLocation loc : c.circuitChunks) {
            if (chunkLookupMap.containsKey(loc)) {
                List<Circuit> ccircuits = chunkLookupMap.get(loc);

                if (ccircuits!=null) {
                    ccircuits.remove(c);
                    if (ccircuits.isEmpty())
                        emptyChunks.add(loc);
                }
            }
        }
        
        for (ChunkLocation loc : emptyChunks)
           chunkLookupMap.remove(loc);
    }
    
    /**
    * Checks if a player has permission to create or destroy a chip.
    * 
    * @return true if player has permission.
    */
    private boolean checkChipPermission(CommandSender sender, String classname, boolean create) {
        if (!rc.getPrefs().getUsePermissions()) return true;
        if (!(sender instanceof Player)) return true;
        
        Player player = (Player)sender;
        if (player.isOp()) return true;
        
        if (player.hasPermission("redstonechips.circuit." + (create?"create":"destroy") + ".deny") || 
                player.hasPermission("redstonechips.circuit." + (create?"create.":"destroy.")  + classname + ".deny")) 
            return false;
        else if (player.hasPermission("redstonechips.circuit." + (create?"create":"destroy") + ".*") || 
                player.hasPermission("redstonechips.circuit." + (create?"create.":"destroy.") + classname)) 
            return true;
        else return false;
    }

    /**
     * Mark a chunk as a processed chunk. While marked the chunk would not be allowed to unload.
     * Call releaseChunk(chunk) to release it.
     * 
     * @param chunk The chunk to keep alive.
     */
    public void workOnChunk(ChunkLocation chunk) {
        if (!processedChunks.contains(chunk)) {
            processedChunks.add(chunk);
            chunk.loadChunk();
        }
    }
    
    /**
     * Release a used chunk and allow it to unload.
     * 
     * @param chunk The chunk to release
     */
    public void releaseChunk(ChunkLocation chunk) {
        if (processedChunks.remove(chunk))
            chunk.unloadChunk();
    }

    /**
     * 
     * @param chunk The chunk to test.
     * @return true if the chunk was loaded, or is kept loaded, for processing.
     */
    public boolean isProcessingChunk(ChunkLocation chunk) {
        return processedChunks.contains(chunk);
    }
    

}
