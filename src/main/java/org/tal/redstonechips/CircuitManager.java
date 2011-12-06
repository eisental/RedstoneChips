
package org.tal.redstonechips;

import org.tal.redstonechips.circuit.Circuit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Redstone;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.util.ChunkLocation;
import org.tal.redstonechips.util.Locations;
import org.tal.redstonechips.channel.WirelessCircuit;
import org.bukkit.material.Wool;
import org.bukkit.DyeColor;
import org.bukkit.World;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitManager {

    /**
     * Used for passing argument when scanning recursively for chips.
     */
    private class ScanParameters {
        /**
         * The chip block material of the scanned chip.
         */
        Material chipMaterial;
        DyeColor woolColor;

        /**
         * The input block material.
         */
        MaterialData inputBlockType;

        /**
         * The output block material.
         */
        MaterialData outputBlockType;

        /**
         * The interface block material.
         */
        MaterialData interfaceBlockType;

        /**
         * The 1st block right after the activation sign. The block to which the sign is attached.
         */
        Block origin;

        /**
         * The current scanning direction.
         */
        BlockFace direction;

        /**
         * List of discovered input blocks.
         */
        List<Block> inputs;

        /**
         * List of discovered output blocks.
         */
        List<Block> outputs;

        /**
         * List of discovered interface blocks.
         */
        List<Block> interfaces;

        /**
         * List of all discovered structure blocks. Includes any block that would break the circuit when broken.
         */
        List<Block> structure;
    }

    private RedstoneChips rc;
    
    private HashMap<Integer, Circuit> circuits = new HashMap<Integer, Circuit>();

    private Map<ChunkLocation, List<Circuit>> chunkLookupMap = new HashMap<ChunkLocation, List<Circuit>>();
    private Map<Location, List<InputPin>> inputLookupMap = new HashMap<Location, List<InputPin>>();
    private Map<Location, Object[]> outputLookupMap = new HashMap<Location, Object[]>();
    private Map<Location, Circuit> structureLookupMap = new HashMap<Location, Circuit>();
    private Map<Location, Circuit> activationLookupMap = new HashMap<Location, Circuit>();

    private List<CommandSender> pausedDebuggers;

    public CircuitManager(RedstoneChips plugin) {
        rc = plugin;

        pausedDebuggers = new ArrayList<CommandSender>();
    }

    /**
     * Checks if this redstone event reports an input change in any circuit's input pins.
     * When the new redstone state is different than the current the input pin is updated and the circuit is notified.
     *
     * @param e A redstone change event.
     */
    public void redstoneChange(BlockRedstoneEvent e) {
        boolean newVal = (e.getNewCurrent()>0);
        boolean oldVal = (e.getOldCurrent()>0);
        if (newVal==oldVal) return; // not a change

        List<InputPin> inputList = inputLookupMap.get(e.getBlock().getLocation());
        if (inputList==null) return;
        for (InputPin inputPin : inputList)
            inputPin.updateValue(e.getBlock(), newVal);
        
    }

    /**
     * Tries to detect a circuit starting at the specified activation sign block using the specified i/o block materials.
     *
     * @param signBlock The activation sign's block.
     * @param sender The circuit activator.
     * @param inputBlockType Input block material.
     * @param outputBlockType Output block material.
     * @param interfaceBlockType Interface block material.
     * @return The new circuit's id when a chip was activated, -1 when a reported error has occured or -2 when a circuit was not found.
     */
    public int checkForCircuit(Block signBlock, CommandSender sender,
            MaterialData inputBlockType, MaterialData outputBlockType, MaterialData interfaceBlockType) {

        if (signBlock.getType()!=Material.WALL_SIGN) return -1;

        BlockState state = signBlock.getState();
        if (!(state instanceof Sign)) return -1;

        Sign sign = (Sign)signBlock.getState();
        String signClass = getClassFromSign(sign);

        // first check if its already registered
        Circuit check = this.getCircuitByActivationBlock(signBlock);
        if (check!=null) {
            if (sender!=null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Circuit is already activated (" + check.id + ").");
            return -2;
        }

        // then check if the sign text points to a known circuit type
        if (!rc.getCircuitLoader().getCircuitClasses().containsKey(signClass)) return -2;
        
        if (!checkChipPermission(sender, signClass, true)) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permission to create circuits of type " + signClass + ".");
            return -2;
        }

        List<Block> inputs = new ArrayList<Block>();
        List<Block> outputs = new ArrayList<Block>();
        List<Block> structure = new ArrayList<Block>();
        List<Block> interfaceBlocks = new ArrayList<Block>();

        structure.add(signBlock);

        BlockFace direction = ((org.bukkit.material.Sign)signBlock.getState().getData()).getAttachedFace();
        Block firstChipBlock = signBlock.getRelative(direction);

        if (!isTypeAllowed(firstChipBlock.getType(), firstChipBlock.getData())) {
            if (sender!=null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You can't build a redstone chip using this material (" + firstChipBlock.getType().name() + "). It's either doesn't work as a chip block or it already has another function as an i/o block.");
            return -1;
        }

        Material chipMaterial = firstChipBlock.getType();
        structure.add(firstChipBlock);

        try {
            ScanParameters params = new ScanParameters();
            params.chipMaterial = chipMaterial;
            if(chipMaterial.equals(Material.WOOL))
                params.woolColor=((Wool)(firstChipBlock.getState().getData())).getColor();
            else
                params.woolColor=null;
            params.inputBlockType = inputBlockType;
            params.outputBlockType = outputBlockType;
            params.interfaceBlockType = interfaceBlockType;
            params.origin = firstChipBlock;
            params.inputs = inputs;
            params.outputs = outputs;
            params.interfaces = interfaceBlocks;
            params.structure = structure;
            params.direction = direction;
            scanBranch(params);
        } catch (IllegalArgumentException ie) {
            if (sender!=null) sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
            return -2;
        } catch (StackOverflowError se) {
            if (sender!=null) sender.sendMessage(rc.getPrefs().getErrorColor() + "If you're trying to build a redstone chip, your chip is way too big.");
            return -2;
        }

        if (outputs.isEmpty() && inputs.isEmpty() && interfaceBlocks.isEmpty()) return -1;

        String[] args = getArgsFromSign(sign);

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

        c.structure = new Location[structure.size()];
        for (int i=0; i<structure.size(); i++) {
            c.structure[i] = structure.get(i).getLocation();
        }

        c.inputs = new InputPin[inputs.size()];
        for (int i=0; i<inputs.size(); i++) {
            c.inputs[i] = new InputPin(c, inputs.get(i).getLocation(), i);
        }

        c.outputs = new Location[outputs.size()];
        for (int i=0; i<outputs.size(); i++) {
            c.outputs[i] = outputs.get(i).getLocation();
        }

        c.interfaceBlocks = new Location[interfaceBlocks.size()];
        for (int i=0; i<interfaceBlocks.size(); i++) {
            c.interfaceBlocks[i] = interfaceBlocks.get(i).getLocation();
        }

        c.circuitChunks = findCircuitChunks(c);

        c.args = args;

        return this.activateCircuit(c, sender, args, -1);
    }

    /**
     * Activates an already set-up circuit.
     *
     * @param c The circuit to activate
     * @param sender The activator.
     * @param signargs The circuit's sign arguments.
     * @param id The desired circuit id. When less than 0, a new id is generated.
     * @return The circuit's id or -2 if an error occurred.
     */
    public int activateCircuit(Circuit c, CommandSender sender, String[] signargs, int id) {
        if (c.initCircuit(sender, signargs, rc)) {
            this.addCircuitLookups(c);

            if (id<0)
                c.id = generateId();
            else
                c.id = id;
            circuits.put(c.id, c);

            if (sender != null) {
                ChatColor infoColor = rc.getPrefs().getInfoColor();
                ChatColor debugColor = rc.getPrefs().getDebugColor();
                sender.sendMessage(infoColor + "Activated " + ChatColor.YELLOW + c.getClass().getSimpleName() + " (" + c.id + ") " + infoColor + "circuit:");
                sender.sendMessage(debugColor + "> " + ChatColor.WHITE + c.inputs.length + debugColor + " input"
                        + (c.inputs.length!=1?"s":"") + ", " + ChatColor.YELLOW + c.outputs.length + debugColor + " output"
                        + (c.outputs.length!=1?"s":"") + " and " + ChatColor.AQUA + c.interfaceBlocks.length + debugColor
                        + " interface block" + (c.interfaceBlocks.length!=1?"s":"") + ".");
            }

            c.updateCircuitSign(true);

            return c.id;
        } else {
            if (sender!=null)
                sender.sendMessage(rc.getPrefs().getErrorColor() + c.getClass().getSimpleName() + " was not activated.");
            return -2;
        }
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
                if (s!=null) s.sendMessage(rc.getPrefs().getErrorColor() + "You destroyed the " + destroyed.getClass().getSimpleName() + "(" + destroyed.id + ") chip.");
            } else {
                return false;
            }
        }
        return true;
    }

    private final static Class redstoneClass = Redstone.class;

    /**
     * Called on block place and block break to see if any circuit's input pin's state is affected by the change.
     *
     * @param block The block that was placed or broken.
     * @param player The player who placed or broke the block.
     * @param isBroken True if the block was broken and false if it was placed.
     */
    void checkCircuitInputChanged(Block block, Player player, boolean isBroken) {
        Class<? extends MaterialData> dataClass = block.getType().getData();
        if (dataClass!=null && redstoneClass.isAssignableFrom(dataClass)) {
            List<InputPin> inputs = inputLookupMap.get(block.getLocation());
            if (inputs!=null) {
                for (InputPin pin : inputs) {
                    if (isBroken) pin.updateValue(block, false);
                    else pin.updateValue(block, InputPin.findBlockPowerState(block.getLocation()));                    
                }
            }

        }
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
        
        if (destroyed instanceof WirelessCircuit) {
            WirelessCircuit w = (WirelessCircuit)destroyed;
            if (w.getChannel()!=null && !(w.getChannel().checkChanPermissions(destroyer, false))) {
                if (destroyer!=null) destroyer.sendMessage(rc.getPrefs().getErrorColor()+"You do not have permissions to use channel " + ((WirelessCircuit)destroyed).getChannel().name + ".");
                return false;
            }
        }
        
        destroyed.circuitShutdown();
        destroyed.circuitDestroyed();
        circuits.remove(destroyed.id);
        removeCircuitLookups(destroyed);

        for (Location l : destroyed.outputs) {
            Block output = destroyed.world.getBlockAt(l);

            if (output.getType()==Material.LEVER) {
                // turn lever off
                output.setData((byte)(output.getData()&0x7));
            }
        }

        if (destroyBlocks) {
            for (Location l : destroyed.structure)
                destroyed.world.getBlockAt(l).setType(Material.AIR);
        } else {
            destroyed.updateCircuitSign(false);
        }

        String dName;
        if (destroyer==null) dName = "an unknown cause";
        if (destroyer!=null && destroyer instanceof Player) dName = ((Player)destroyer).getDisplayName();
        else dName = "an unknown command sender";
        for (CommandSender s : destroyed.getDebuggers()) {
            if (!s.equals(destroyer)) {
                s.sendMessage(rc.getPrefs().getDebugColor() + "A " + destroyed.getCircuitClass() + " circuit you were debugging was " +
                        rc.getPrefs().getErrorColor() + "deactivated " + rc.getPrefs().getDebugColor() + "by " + dName +
                        rc.getPrefs().getDebugColor() + " (@" + destroyed.activationBlock.getX() + "," + destroyed.activationBlock.getY() + "," + destroyed.activationBlock.getZ() + ").");
            }
        }

        return true;
    }

    /**
     * Resets the specified circuit. First the circuit is destroyed and then reactivated. Any debuggers of the chip are copied over
     * to the activated circuit.
     *
     * @param c The circuit to reset.
     * @param reseter The reseter.
     * @return true if the circuit was reactivated.
     */
    public boolean resetCircuit(Circuit c, CommandSender reseter) {
        Block activationBlock = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
        List<CommandSender> debuggers = c.getDebuggers();
        List<CommandSender> iodebuggers = c.getIODebuggers();
        int id = c.id;

        if (!rc.getCircuitManager().destroyCircuit(c, reseter, false)) return false;
        Block a = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
        rc.getCircuitManager().checkForCircuit(a, reseter,
                rc.getPrefs().getInputBlockType(), rc.getPrefs().getOutputBlockType(), rc.getPrefs().getInterfaceBlockType());
        Circuit newCircuit = rc.getCircuitManager().getCircuitByActivationBlock(activationBlock);

        if (newCircuit!=null) {
            newCircuit.id = id;
            for (CommandSender d : debuggers) newCircuit.addDebugger(d);
            for (CommandSender d : iodebuggers) newCircuit.addIODebugger(d);
            if (reseter!=null) reseter.sendMessage(rc.getPrefs().getInfoColor() + "The " + ChatColor.YELLOW + newCircuit.getCircuitClass() + " (" + + newCircuit.id + ")" + rc.getPrefs().getInfoColor() + " circuit is reactivated.");

            return true;
        } else {
            return false;
        }
    }

    /**
     * Called on every player quit event. Removes the quitting player from any debug lists.
     *
     * @param player The quitting player.
     */
    public void checkDebuggerQuit(Player player) {
        for (Circuit c : circuits.values()) {
            if (c.getDebuggers().contains(player)) {
                c.getDebuggers().remove(player);
            }

        }
    }

    /**
     * Called on every chunk load event. Finds any circuits in the loaded chunk and calls their .circuitChunkLoaded() method.
     * 
     * @param chunk The loaded chunk.
     */
    public void updateOnChunkLoad(ChunkLocation chunk) {
        List<Circuit> circuitsInChunk = chunkLookupMap.get(chunk);

        if (circuitsInChunk!=null) {
            for (Circuit c : circuitsInChunk) {
                c.circuitChunkLoaded();
            }
        }
    }

    /**
     * Called on every chunk unload event. Finds any circuits in the unloaded chunks. When all of the chunks of a circuit are
     * unloaded the circuits circuitChunksUnloaded() method is called.
     *
     * @param chunk The unloaded chunk.
     */
    public void updateOnChunkUnload(ChunkLocation chunk) {
        List<Circuit> circuitsInChunk = chunkLookupMap.get(chunk);
        if (circuitsInChunk!=null) {
            for (Circuit c : circuitsInChunk) {
                // if all of the circuit's chunks are unloaded we call the method.
                boolean call = true;
                for (ChunkLocation loc : c.circuitChunks)
                    if (loc.isChunkLoaded())
                        call = false;

                if (call) c.circuitChunksUnloaded();
            }
        }
    }

    /**
     * Check each active circuit to see if all its blocks are in place. See Circuit.checkIntegrity().
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

        for (ChunkLocation unloaded : unloadedChunks)
            unloaded.loadChunk();

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
            chunk.unloadChunk();
        }

        if (!invalidIds.isEmpty()) rc.log(Level.INFO, "Done checking circuits. " + msg);
    }

    /**
     * Finds the circuit's chunks according to its activation block, output blocks, input power blocks and interface blocks.
     *
     * @param c The circuit to check.
     * @return All chunks used by this circuit.
     */
    public ChunkLocation[] findCircuitChunks(Circuit c) {
        List<ChunkLocation> circuitChunks = new ArrayList<ChunkLocation>();

        circuitChunks.add(ChunkLocation.fromLocation(c.activationBlock));

        for (int i=0; i<c.outputs.length; i++) {
            ChunkLocation chunk = ChunkLocation.fromLocation(c.outputs[i]);

            if (!circuitChunks.contains(chunk))
                circuitChunks.add(chunk);
        }

        for (int i=0; i<c.inputs.length; i++) {
            for (Location in : c.inputs[i].getPowerBlocks()) {
                ChunkLocation chunk = ChunkLocation.fromLocation(in);

                if (!circuitChunks.contains(chunk))
                    circuitChunks.add(chunk);
            }
        }

        for (int i=0; i<c.interfaceBlocks.length; i++) {
            ChunkLocation chunk = ChunkLocation.fromLocation(c.interfaceBlocks[i]);

            if (!circuitChunks.contains(chunk))
                circuitChunks.add(chunk);
        }

        return circuitChunks.toArray(new ChunkLocation[circuitChunks.size()]);
    }

    /**
     * Calls Circuit.circuitShutdown on every activated circuit.
     */
    public void shutdownCircuits() {
        for (Circuit c : circuits.values()) c.circuitShutdown();
    }

    /**
     *
     * @return a map of all active circuits. The map keys are circuit ids.
     */
    public HashMap<Integer, Circuit> getCircuits() {
        return circuits;
    }

    /**
     *
     * @return a map of all active circuits in world world. The map keys are circuit ids.
     */
    public HashMap<Integer, Circuit> getCircuits(World world) {
        HashMap<Integer, Circuit> worldCircuits = new HashMap<Integer, Circuit>();
        for(Integer id : circuits.keySet()) {
          Circuit c = circuits.get(id);
          if(c.world.equals(world)) {
            worldCircuits.put(id,c);
          }
        }
        return worldCircuits;
    }

    /**
     *
     * @param structureBlock Any block that belongs to a chip.
     * @return The circuit that the block is part of its structure.
     */
    public Circuit getCircuitByStructureBlock(Block structureBlock) {
        return this.structureLookupMap.get(structureBlock.getLocation());
    }

    /**
     *
     * @param activationBlock An activation sign of a chip.
     * @return The circuit that uses this activation sign.
     */
    public Circuit getCircuitByActivationBlock(Block activationBlock) {
        return this.activationLookupMap.get(activationBlock.getLocation());
    }

    /**
     * Sets the map of active circuits to a new one.
     *
     * @param circuits A new active circuits map. The map keys are circuit ids.
     */
    void setCircuitMap(HashMap<Integer, Circuit> circuits) {
        this.circuits = circuits;
    }

    public List<InputPin> lookupInputBlock(Block inputBlock) {
        return this.inputLookupMap.get(inputBlock.getLocation());
    }

    public Object[] lookupOutputBlock(Block outputBlock) {
        return this.outputLookupMap.get(outputBlock.getLocation());
    }

    public boolean isDebuggerPaused(CommandSender s) {
        return pausedDebuggers.contains(s);
    }

    public void pauseDebugger(CommandSender s, boolean pause) {
        if (pause) {
            if (!pausedDebuggers.contains(s)) pausedDebuggers.add(s);
        } else {
            pausedDebuggers.remove(s);
        }
    }    
    
    public void unloadWorldChips(World unloadedWorld) {
        HashMap<Integer, Circuit> unloadedCircuits = this.getCircuits(unloadedWorld);
        for (Circuit c : unloadedCircuits.values())
            circuits.remove(c.id);
    }

    private void scanBranch(ScanParameters params) {
        // look in every horizontal direction for inputs, outputs or interface blocks.
        checkAttachedIO(params);

        // look in every direction, inculding up and down for more chip blocks.
        checkAttachedChipBlock(params);
    }

    private void checkAttachedChipBlock(ScanParameters params) {
        BlockFace direction = params.direction;
        Block origin = params.origin;

        // look for chip blocks to the right
        params.direction = Locations.getRightFace(direction);
        checkForChipBlockOnSideFace(params);

        // look for chip blocks to the left
        params.direction = Locations.getLeftFace(direction);
        checkForChipBlockOnSideFace(params);
        
        // look for chip blocks in original direction
        params.direction = direction;
        checkForChipBlockOnSideFace(params);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        params.direction = direction.getOppositeFace();
        checkForChipBlockOnSideFace(params);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getRelative(BlockFace.UP);

        if (!params.structure.contains(up) && ((!up.getType().equals(Material.WOOL) && up.getType()==params.chipMaterial) || (up.getType().equals(Material.WOOL) && ((Wool)(up.getState().getData())).getColor().equals(params.woolColor)))) {
            params.structure.add(up);
            params.direction = direction;
            params.origin = up;
            scanBranch(params);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getRelative(BlockFace.DOWN);

        if (!params.structure.contains(down) && ((!down.getType().equals(Material.WOOL) && down.getType()==params.chipMaterial) || (down.getType().equals(Material.WOOL) && ((Wool)(down.getState().getData())).getColor().equals(params.woolColor)))) {
            params.structure.add(down);
            params.direction = direction;
            params.origin = down;
            scanBranch(params);
        }

        params.direction = direction;
        params.origin = origin;
    }

    private void checkAttachedIO(ScanParameters params) {
        BlockFace face = params.direction;

        params.direction = Locations.getRightFace(face);
        checkForIO(params);

        params.direction = Locations.getLeftFace(face);
        checkForIO(params);

        params.direction = face;
        checkForIO(params);

        params.direction = face.getOppositeFace();
        checkForIO(params);

        params.direction = BlockFace.UP;
        checkForIO(params);

        params.direction = BlockFace.DOWN;
        checkForIO(params);

        params.direction = face;
    }

    private void checkForIO(ScanParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if (b.getType()==params.inputBlockType.getItemType()
                    && (b.getData()==params.inputBlockType.getData() || params.inputBlockType.getData()==-1)) {
                params.structure.add(b);
                params.inputs.add(b);
            } else if (b.getType()==params.outputBlockType.getItemType()
                    && (b.getData()==params.outputBlockType.getData() || params.outputBlockType.getData()==-1)) {
                params.structure.add(b);
                Block o = findLeverAround(b);
                params.structure.add(o);
                params.outputs.add(o);
            } else if (b.getType()==params.interfaceBlockType.getItemType()
                    && (b.getData()==params.interfaceBlockType.getData() || params.interfaceBlockType.getData()==-1)) {
                params.structure.add(b);
                params.interfaces.add(b);
            }
        }
    }

    private void checkForChipBlockOnSideFace(ScanParameters params) {
        Block b = params.origin.getRelative(params.direction);
        if (!params.structure.contains(b)) {
            if ((!b.getType().equals(Material.WOOL) && b.getType()==params.chipMaterial) || (b.getType().equals(Material.WOOL) && ((Wool)(b.getState().getData())).getColor().equals(params.woolColor))) {
                params.structure.add(b);
                Block origin = params.origin;
                params.origin = b;

                scanBranch(params);

                params.origin = origin;
            }
        }
    }

    private Block findLeverAround(Block b) {
        Block up = b.getRelative(BlockFace.UP);
        Block north = b.getRelative(BlockFace.NORTH);
        Block east = b.getRelative(BlockFace.EAST);
        Block south = b.getRelative(BlockFace.SOUTH);
        Block west = b.getRelative(BlockFace.WEST);

        int leverCount = 0;
        Block ret = null;

        if (isLeverAttached(up, b)) { leverCount++; ret = up; }
        if (isLeverAttached(north, b)) { leverCount++; ret = north; }
        if (isLeverAttached(east, b)) { leverCount++; ret = east; }
        if (isLeverAttached(south, b)) { leverCount++; ret = south; }
        if (isLeverAttached(west, b)) { leverCount++; ret = west; }

        if (leverCount>1) throw new IllegalArgumentException("An output block has more than one lever connected to it.");
        else if (leverCount==0) throw new IllegalArgumentException("A lever is missing from one or more outputs.");
        else return ret;
    }

    private boolean isLeverAttached(Block lever, Block origin) {
        if (lever.getType()!=Material.LEVER) return false;

        Lever l = (Lever)lever.getState().getData();
        BlockFace face = l.getAttachedFace();

        if (face==null) return true; // something is wrong with the map. assume it's attached...
        else return lever.getRelative(face).equals(origin);
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

        for (int i=0; i<c.outputs.length; i++) {
            outputLookupMap.put(c.outputs[i], new Object[]{c, i});
        }

        activationLookupMap.put(c.activationBlock, c);

        for (int i=0; i<c.inputs.length; i++) {
            for (Location l : c.inputs[i].getPowerBlocks()) {
                if (!inputLookupMap.containsKey(l))
                    inputLookupMap.put(l, new ArrayList<InputPin>());

                inputLookupMap.get(l).add(c.inputs[i]);
            }
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

        for (Location l : c.outputs)
            outputLookupMap.remove(l);

        List<Location> inputBlocksToRemove = new ArrayList<Location>();
        for (Location l : inputLookupMap.keySet()) {
            List<InputPin> pins = inputLookupMap.get(l);
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
            inputLookupMap.remove(l);
        
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

    private boolean isTypeAllowed(Material material, byte data) {
        return !matchMaterial(material, rc.getPrefs().getInputBlockType(), data) &&
                !matchMaterial(material, rc.getPrefs().getOutputBlockType(), data) &&
                !matchMaterial(material, rc.getPrefs().getInterfaceBlockType(), data) &&
                material.isBlock() && material!=Material.GRAVEL && material!=Material.SAND;
    }

    private boolean matchMaterial(Material m, MaterialData md, byte data) {
        if (m!=md.getItemType()) return false;
        else if (m==Material.WOOL) {
            return data==md.getData();
        } else return true;
        
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
}
