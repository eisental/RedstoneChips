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

    public void checkForCircuit(Block signBlock, CommandSender sender) {
        if (signBlock.getType()==Material.WALL_SIGN) {
            BlockState state = signBlock.getState();
            if (!(state instanceof Sign)) return;

            Sign sign = (Sign)signBlock.getState();

            // first check if its already registered
            Circuit check = this.getCircuitByActivationBlock(signBlock);
            if (check!=null) {
                sender.sendMessage(rc.getPrefsManager().getInfoColor() + "Circuit is already activated (" + check.id + ").");
                return;
            }

            // then check if the sign text points to a known circuit type
            if (!rc.getCircuitLoader().getCircuitClasses().containsKey(sign.getLine(0).trim())) return;

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
                    scanBranch(chipMaterial, firstChipBlock, direction, inputs, outputs, interfaceBlocks, structure);
                } catch (IllegalArgumentException ie) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + ie.getMessage());
                    return;
                } catch (StackOverflowError se) {
                    sender.sendMessage(rc.getPrefsManager().getErrorColor() + "If you're trying to build a redstone chip, your chip is way too big.");
                    return;
                }

                if (outputs.size()>0 || inputs.size()>0 || interfaceBlocks.size()>0) {
                    String[] args = getArgsFromSign(sign);

                    try {
                        Circuit c = rc.getCircuitLoader().getCircuitInstance(sign.getLine(0).trim());
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
                            return;
                        } else {
                            sender.sendMessage(rc.getPrefsManager().getErrorColor() + c.getClass().getSimpleName() + " was not activated.");
                            return;
                        }
                    } catch (IllegalArgumentException ex) {
                        // unknown circuit name. shouldn't happen at this stage.
                    } catch (InstantiationException ex) {
                        ex.printStackTrace();
                        rc.log(Level.WARNING, ex.toString());
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                        rc.log(Level.WARNING, ex.toString());
                    }
                }
            }
        }
    }

    public void destroyCircuits() {
        for (Circuit c : circuits.values()) c.circuitDestroyed();
    }

    private void scanBranch(Material chipMaterial, Block origin, BlockFace direction, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        // look in every horizontal direction for inputs, outputs or interface blocks.
        checkAttachedIO(origin, direction, inputs, outputs, interfaces, structure);

        // look in every direction, inculding up and down for more chip blocks.
        checkAttachedChipBlock(chipMaterial, origin, direction, inputs, outputs, interfaces, structure);
    }

    private void checkAttachedChipBlock(Material chipMaterial, Block origin, BlockFace direction, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        // look for chip blocks to the right
        checkForChipBlockOnSideFace(chipMaterial, origin, getRightFace(direction), inputs, outputs, interfaces, structure);

        // look for chip blocks to the left
        checkForChipBlockOnSideFace(chipMaterial, origin, getLeftFace(direction), inputs, outputs, interfaces, structure);
        
        // look for chip blocks in original direction
        checkForChipBlockOnSideFace(chipMaterial, origin, direction, inputs, outputs, interfaces, structure);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        checkForChipBlockOnSideFace(chipMaterial, origin, getOppositeFace(direction), inputs, outputs, interfaces, structure);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getFace(BlockFace.UP);
        if (!structure.contains(up) && up.getType()==chipMaterial) {
            structure.add(up);
            scanBranch(chipMaterial, up, direction, inputs, outputs, interfaces, structure);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getFace(BlockFace.DOWN);
        if (!structure.contains(down) && down.getType()==chipMaterial) {
            structure.add(down);
            scanBranch(chipMaterial, down, direction, inputs, outputs, interfaces, structure);
        }
    }

    private void checkAttachedIO(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        checkForIO(origin, getRightFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, getLeftFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, face, inputs, outputs, interfaces, structure);
        checkForIO(origin, getOppositeFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, BlockFace.UP, inputs, outputs, interfaces, structure);
        checkForIO(origin, BlockFace.DOWN, inputs, outputs, interfaces, structure);
    }

    private void checkForIO(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b)) {
            if (b.getType()==rc.getPrefsManager().getInputBlockType().getItemType()
                    && (b.getData()==rc.getPrefsManager().getInputBlockType().getData() || rc.getPrefsManager().getInputBlockType().getData()==-1)) {
                structure.add(b);
                inputs.add(b);
            } else if (b.getType()==rc.getPrefsManager().getOutputBlockType().getItemType()
                    && (b.getData()==rc.getPrefsManager().getOutputBlockType().getData() || rc.getPrefsManager().getOutputBlockType().getData()==-1)) {
                structure.add(b);
                Block o = findLeverAround(b);
                structure.add(o);
                outputs.add(o);
            } else if (b.getType()==rc.getPrefsManager().getInterfaceBlockType().getItemType()
                    && (b.getData()==rc.getPrefsManager().getInterfaceBlockType().getData() || rc.getPrefsManager().getInterfaceBlockType().getData()==-1)) {
                structure.add(b);
                interfaces.add(b);
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

    private void checkForChipBlockOnSideFace(Material chipMaterial, Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b)) {
            if (b.getType()==chipMaterial) {
                structure.add(b);
                scanBranch(chipMaterial, b, face, inputs, outputs, interfaces, structure);
            }
        }
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

    public void checkCircuitDestroyed(Block b, CommandSender s) {
        Circuit destroyed = structureLookupMap.get(b.getLocation());

        if (destroyed!=null && circuits.containsValue(destroyed)) {
            if (s!=null) s.sendMessage(rc.getPrefsManager().getErrorColor() + "You destroyed the " + destroyed.getClass().getSimpleName() + "(" + destroyed.id + ") chip.");
            destroyCircuit(destroyed, s);
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

    public void destroyCircuit(Circuit destroyed, CommandSender destroyer) {
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
}
