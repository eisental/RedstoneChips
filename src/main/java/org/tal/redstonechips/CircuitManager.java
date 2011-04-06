/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import org.bukkit.Chunk;
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
import org.bukkit.material.MaterialData;
import org.tal.redstonechips.circuit.InputPin;
import org.tal.redstonechips.util.ChunkLocation;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitManager {
    private RedstoneChips rc;
    
    private HashMap<Integer, Circuit> circuits = new HashMap<Integer, Circuit>();

    private Map<ChunkLocation, List<Circuit>> chunkLookupMap = new HashMap<ChunkLocation, List<Circuit>>();
    private Map<Location, List<InputPin>> inputLookupMap = new HashMap<Location, List<InputPin>>();
    private Map<Location, Object[]> outputLookupMap = new HashMap<Location, Object[]>();
    private Map<Location, Circuit> structureLookupMap = new HashMap<Location, Circuit>();
    private Map<Location, Circuit> activationLookupMap = new HashMap<Location, Circuit>();

    public CircuitManager(RedstoneChips plugin) {
        rc = plugin;
    }

    public void redstoneChange(BlockRedstoneEvent e) {
        boolean newVal = (e.getNewCurrent()>0);
        boolean oldVal = (e.getOldCurrent()>0);
        if (newVal==oldVal) return; // not a change

        List<InputPin> inputList = inputLookupMap.get(e.getBlock().getLocation());
        if (inputList==null) return;
        for (InputPin inputPin : inputList) {
            inputPin.updateValue(e.getBlock(), newVal);
            inputPin.getCircuit().redstoneChange(inputPin.getIndex(), inputPin.getPinValue());
        }
    }

    public int checkForCircuit(Block signBlock, CommandSender sender,
            MaterialData inputBlockType, MaterialData outputBlockType, MaterialData interfaceBlockType) {
        if (signBlock.getType()==Material.WALL_SIGN) {
            BlockState state = signBlock.getState();
            if (!(state instanceof Sign)) return -1;

            Sign sign = (Sign)signBlock.getState();

            String signClass = getClassFromSign(sign);

            // first check if its already registered
            Circuit check = this.getCircuitByActivationBlock(signBlock);
            if (check!=null) {
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Circuit is already activated (" + check.id + ").");
                return -1;
            }

            // then check if the sign text points to a known circuit type
            if (!rc.getCircuitLoader().getCircuitClasses().containsKey(signClass)) return -1;

            List<Block> inputs = new ArrayList<Block>();
            List<Block> outputs = new ArrayList<Block>();
            List<Block> structure = new ArrayList<Block>();
            List<Block> interfaceBlocks = new ArrayList<Block>();

            structure.add(signBlock);
            
            BlockFace direction = findSignDirection(signBlock.getData());
            Block firstChipBlock = signBlock.getFace(direction);

            if (isTypeAllowed(firstChipBlock.getType())) {
                Material chipMaterial = firstChipBlock.getType();

                structure.add(firstChipBlock);
                try {
                    ScanParameters params = new ScanParameters();
                    params.chipMaterial = chipMaterial;
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
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + ie.getMessage());
                    return -1;
                } catch (StackOverflowError se) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "If you're trying to build a redstone chip, your chip is way too big.");
                    return -1;
                }

                if (outputs.size()>0 || inputs.size()>0 || interfaceBlocks.size()>0) {
                    String[] args = getArgsFromSign(sign);

                    try {
                        Circuit c = rc.getCircuitLoader().getCircuitInstance(signClass);
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


                        c.args = args;
                        
                        if (c.initCircuit(sender, args, rc)) {
                            this.addCircuitLookups(c);
                            c.id = generateId();
                            circuits.put(c.id, c);

                            ChatColor infoColor = rc.getPrefsManager().getInfoColor();
                            ChatColor debugColor = rc.getPrefsManager().getDebugColor();
                            sender.sendMessage(infoColor + "Activated " + ChatColor.YELLOW + c.getClass().getSimpleName() + " (" + c.id + ") " + infoColor + "circuit:");
                            sender.sendMessage(debugColor + "> " + ChatColor.WHITE + inputs.size() + debugColor + " input"
                                    + (inputs.size()!=1?"s":"") + ", " + ChatColor.YELLOW + outputs.size() + debugColor + " output"
                                    + (outputs.size()!=1?"s":"") + " and " + ChatColor.BLUE + interfaceBlocks.size() + debugColor
                                    + " interface block" + (interfaceBlocks.size()!=1?"s":"") + ".");

                            c.updateCircuitSign(true);

                            return c.id;
                        } else {
                            sender.sendMessage(rc.getPrefsManager().getErrorColor() + c.getClass().getSimpleName() + " was not activated.");
                            return -2;
                        }
                    } catch (IllegalArgumentException ex) {
                        // unknown circuit name. shouldn't happen at this stage.
                        return -1;
                    } catch (InstantiationException ex) {
                        ex.printStackTrace();
                        rc.log(Level.WARNING, ex.toString());
                        return -1;
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                        rc.log(Level.WARNING, ex.toString());
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

    public void shutdownCircuits() {
        for (Circuit c : circuits.values()) c.circuitShutdown();
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
        params.direction = getRightFace(direction);
        checkForChipBlockOnSideFace(params);

        // look for chip blocks to the left
        params.direction = getLeftFace(direction);
        checkForChipBlockOnSideFace(params);
        
        // look for chip blocks in original direction
        params.direction = direction;
        checkForChipBlockOnSideFace(params);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        params.direction = getOppositeFace(direction);
        checkForChipBlockOnSideFace(params);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getFace(BlockFace.UP);

        if (!params.structure.contains(up) && up.getType()==params.chipMaterial) {
            params.structure.add(up);
            params.direction = direction;
            params.origin = up;
            scanBranch(params);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getFace(BlockFace.DOWN);

        if (!params.structure.contains(down) && down.getType()==params.chipMaterial) {
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

        params.direction = getRightFace(face);
        checkForIO(params);

        params.direction = getLeftFace(face);
        checkForIO(params);

        params.direction = face;
        checkForIO(params);

        params.direction = getOppositeFace(face);
        checkForIO(params);

        params.direction = BlockFace.UP;
        checkForIO(params);

        params.direction = BlockFace.DOWN;
        checkForIO(params);

        params.direction = face;
    }

    private void checkForIO(ScanParameters params) {
        Block b = params.origin.getFace(params.direction);
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
        Block b = params.origin.getFace(params.direction);
        if (!params.structure.contains(b)) {
            if (b.getType()==params.chipMaterial) {
                params.structure.add(b);
                Block origin = params.origin;
                params.origin = b;

                scanBranch(params);

                params.origin = origin;
            }
        }
    }

    private Block findLeverAround(Block b) {
        Block up = b.getFace(BlockFace.UP);
        Block north = b.getFace(BlockFace.NORTH);
        Block east = b.getFace(BlockFace.EAST);
        Block south = b.getFace(BlockFace.SOUTH);
        Block west = b.getFace(BlockFace.WEST);

        int leverCount = 0;
        Block ret = null;
        if (up.getType()==Material.LEVER) { leverCount++; ret = up; }
        if (north.getType()==Material.LEVER) { leverCount++; ret = north; }
        if (east.getType()==Material.LEVER) { leverCount++; ret = east; }
        if (south.getType()==Material.LEVER) { leverCount++; ret = south; }
        if (west.getType()==Material.LEVER) { leverCount++; ret = west; }
        if (leverCount>1) throw new IllegalArgumentException("An output block has more than one lever connected to it.");
        else if (leverCount==0) throw new IllegalArgumentException("A lever is missing from one or more outputs.");
        else return ret;
    }

    private static BlockFace getLeftFace(BlockFace direction) {
        if (direction==BlockFace.WEST) return BlockFace.SOUTH;
        else if (direction==BlockFace.EAST) return BlockFace.NORTH;
        else if (direction==BlockFace.SOUTH) return BlockFace.EAST;
        else if (direction==BlockFace.NORTH) return BlockFace.WEST;
        else throw new IllegalArgumentException("Invalid block face: " + direction);
    }

    private static BlockFace getRightFace(BlockFace direction) {
        if (direction==BlockFace.WEST) return BlockFace.NORTH;
        else if (direction==BlockFace.EAST) return BlockFace.SOUTH;
        else if (direction==BlockFace.SOUTH) return BlockFace.WEST;
        else if (direction==BlockFace.NORTH) return BlockFace.EAST;
        else throw new IllegalArgumentException("Invalid block face: " + direction);
    }

    private static BlockFace getOppositeFace(BlockFace direction) {
        if (direction==BlockFace.WEST) return BlockFace.EAST;
        else if (direction==BlockFace.EAST) return BlockFace.WEST;
        else if (direction==BlockFace.SOUTH) return BlockFace.NORTH;
        else if (direction==BlockFace.NORTH) return BlockFace.SOUTH;
        else throw new IllegalArgumentException("Invalid block face: " + direction);
    }


    private static BlockFace findSignDirection(byte signData) {
        if (signData==0x2) return BlockFace.WEST;
        else if (signData==0x3) return BlockFace.EAST;
        else if (signData==0x4) return BlockFace.SOUTH;
        else if (signData==0x5) return BlockFace.NORTH;
        else throw new IllegalArgumentException("Invalid wall sign data value: " + signData);
    }

    private String[] getArgsFromSign(Sign sign) {
        String sargs = sign.getLine(1) + " " + sign.getLine(2) + " " + sign.getLine(3);
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
        if (line.charAt(0)==(char)167)
            line = line.substring(2);

        return line;
    }

    public void checkCircuitDestroyed(Block b, CommandSender s) {
        Circuit destroyed = structureLookupMap.get(b.getLocation());

        if (destroyed!=null && circuits.containsValue(destroyed)) {
            if (s!=null) s.sendMessage(rc.getPrefsManager().getErrorColor() + "You destroyed the " + destroyed.getClass().getSimpleName() + "(" + destroyed.id + ") chip.");
            destroyCircuit(destroyed, s, false);
        }
    }


    public HashMap<Integer, Circuit> getCircuits() {
        return circuits;
    }

    public Circuit getCircuitByStructureBlock(Block structureBlock) {
        return this.structureLookupMap.get(structureBlock.getLocation());
    }

    public Circuit getCircuitByActivationBlock(Block activationBlock) {
        return this.activationLookupMap.get(activationBlock.getLocation());
    }

    void setCircuitMap(HashMap<Integer, Circuit> circuits) {
        this.circuits = circuits;

        for (Circuit c : circuits.values())
            addCircuitLookups(c);

        rc.log(Level.INFO, circuits.size() + " active circuits");
    }

    public List<InputPin> lookupInputBlock(Block inputBlock) {
        return this.inputLookupMap.get(inputBlock.getLocation());
    }

    public Object[] lookupOutputBlock(Block outputBlock) {
        return this.outputLookupMap.get(outputBlock.getLocation());
    }

    private void addCircuitLookups(Circuit c) {
        List<Chunk> usedChunks = new ArrayList<Chunk>();

        for (int i=0; i<c.structure.length; i++)
            structureLookupMap.put(c.structure[i], c);

        for (int i=0; i<c.outputs.length; i++) {
            outputLookupMap.put(c.outputs[i], new Object[]{c, i});

            Chunk outputChunk = c.world.getBlockAt(c.outputs[i].getBlockX(), c.outputs[i].getBlockY(), c.outputs[i].getBlockZ()).getChunk();

            if (!usedChunks.contains(outputChunk))
                usedChunks.add(outputChunk);
        }

        activationLookupMap.put(c.activationBlock, c);

        for (int i=0; i<c.inputs.length; i++) {
            for (Location l : c.inputs[i].getPowerBlocks()) {
                if (!inputLookupMap.containsKey(l))
                    inputLookupMap.put(l, new ArrayList<InputPin>());

                inputLookupMap.get(l).add(c.inputs[i]);
            }
        }

        for (Chunk chunk : usedChunks) {
            ChunkLocation cl = ChunkLocation.fromChunk(chunk);
            if (!chunkLookupMap.containsKey(cl))
                chunkLookupMap.put(cl, new ArrayList<Circuit>());
            chunkLookupMap.get(cl).add(c);
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

    }

    public boolean destroyCircuit(Circuit destroyed, CommandSender destroyer, boolean destroyBlocks) {
        if (destroyBlocks) {
            boolean enableDestroyCommand = (Boolean)rc.getPrefsManager().getPrefs().get(PrefsManager.Prefs.enableDestroyCommand.name());
            if (!enableDestroyCommand) {
                destroyer.sendMessage(rc.getPrefsManager().getErrorColor()+"/rcdestroy is disabled. You can enable it using /rcprefs enableDestroyCommand true");
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
                s.sendMessage(rc.getPrefsManager().getDebugColor() + "A " + destroyed.getCircuitClass() + " circuit you were debugging was " +
                        rc.getPrefsManager().getErrorColor() + "deactivated " + rc.getPrefsManager().getDebugColor() + "by " + dName +
                        rc.getPrefsManager().getDebugColor() + " (@" + destroyed.activationBlock.getX() + "," + destroyed.activationBlock.getY() + "," + destroyed.activationBlock.getZ() + ").");
            }
        }

        return true;
    }

    public void resetCircuit(Circuit c, CommandSender reseter) {
        Block activationBlock = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
        List<CommandSender> debuggers = c.getDebuggers();
        int id = c.id;

        rc.getCircuitManager().destroyCircuit(c, reseter, false);
        Block a = c.world.getBlockAt(c.activationBlock.getBlockX(), c.activationBlock.getBlockY(), c.activationBlock.getBlockZ());
        rc.getCircuitManager().checkForCircuit(a, reseter,
                rc.getPrefsManager().getInputBlockType(), rc.getPrefsManager().getOutputBlockType(), rc.getPrefsManager().getInterfaceBlockType());
        Circuit newCircuit = rc.getCircuitManager().getCircuitByActivationBlock(activationBlock);

        if (newCircuit!=null) {
            newCircuit.id = id;
            for (CommandSender d : debuggers) newCircuit.addDebugger(d);
            reseter.sendMessage(rc.getPrefsManager().getInfoColor() + "The " + ChatColor.YELLOW + newCircuit.getCircuitClass() + " (" + + newCircuit.id + ")" + rc.getPrefsManager().getInfoColor() + " circuit is reactivated.");
        }

    }

    public void checkDebuggerQuit(Player player) {
        for (Circuit c : circuits.values()) {
            if (c.getDebuggers().contains(player)) {
                c.getDebuggers().remove(player);
            }

        }
    }

    public void checkUpdateOutputLevers(Chunk chunk) {
        List<Circuit> circuitsInChunk = chunkLookupMap.get(ChunkLocation.fromChunk(chunk));
        if (circuitsInChunk!=null) {
            for (Circuit c : circuitsInChunk) {
                c.updateOutputLevers();
            }
        }
    }

    private boolean isTypeAllowed(Material material) {
        return material!=rc.getPrefsManager().getInputBlockType().getItemType() &&
                material!=rc.getPrefsManager().getOutputBlockType().getItemType() &&
                material!=rc.getPrefsManager().getInterfaceBlockType().getItemType() &&
                material.isBlock() && material!=Material.GRAVEL && material!=Material.SAND;
    }

    public int generateId() {
        int i = 0;

        if (circuits!=null)
            while(circuits.containsKey(i)) i++;

        return i;
    }

    class ScanParameters {
        Material chipMaterial;
        MaterialData inputBlockType;
        MaterialData outputBlockType;
        MaterialData interfaceBlockType;
        Block origin;
        BlockFace direction;
        List<Block> inputs;
        List<Block> outputs;
        List<Block> interfaces;
        List<Block> structure;
    }
}
