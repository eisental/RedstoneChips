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
    
    private List<Circuit> circuits;

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
            inputPin.updateValue(e.getBlock().getLocation(), newVal);

            inputPin.getCircuit().redstoneChange(inputPin.getIndex(), inputPin.getORedValue());

            if (e.getBlock().getType()==Material.STONE_BUTTON) { // manually reset the button after 1 sec.
                final BlockRedstoneEvent buttonEvent = new BlockRedstoneEvent(e.getBlock(), e.getFace(), e.getNewCurrent(), 0);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1000);
                            redstoneChange(buttonEvent);
                        } catch (InterruptedException ex) {}
                    }
                }.start();
            }
        }

    }

    public void checkForCircuit(Block signBlock, Player player) {
        if (signBlock.getType()==Material.WALL_SIGN) {
            Sign sign = (Sign)signBlock.getState();

            // first check if its already registered
            Circuit check = this.getCircuitByActivationBlock(signBlock);
            if (check!=null) {
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "Circuit is already activated.");
                return;
            }

            // then check if the sign text points to a known circuit type
            if (!rc.getCircuitLoader().getCircuitClasses().containsKey(sign.getLine(0).trim())) return;

            List<Location> inputs = new ArrayList<Location>();
            List<Location> outputs = new ArrayList<Location>();
            List<Location> structure = new ArrayList<Location>();
            List<Location> interfaceBlocks = new ArrayList<Location>();

            structure.add(signBlock.getLocation());
            
            BlockFace direction = findSignDirection(signBlock.getData());
            Block firstChipBlock = signBlock.getFace(direction);

            if (firstChipBlock.getType()==rc.getPrefsManager().getChipBlockType()) {
                structure.add(firstChipBlock.getLocation());
                try {
                    scanBranch(firstChipBlock, direction, inputs, outputs, interfaceBlocks, structure);
                } catch (IllegalArgumentException ie) {
                    player.sendMessage(rc.getPrefsManager().getErrorColor() + ie.getMessage());
                    return;
                }

                if (outputs.size()>0 || inputs.size()>0) {
                    String[] args = getArgsFromSign(sign);

                    try {
                        Circuit c = rc.getCircuitLoader().getCircuitInstance(sign.getLine(0).trim());
                        c.world = signBlock.getWorld();
                        c.activationBlock = signBlock.getLocation();
                        c.inputs = inputs.toArray(new Location[inputs.size()]);
                        c.outputs = outputs.toArray(new Location[outputs.size()]);
                        c.structure = structure.toArray(new Location[structure.size()]);
                        c.interfaceBlocks = interfaceBlocks.toArray(new Location[interfaceBlocks.size()]);

                        c.args = args;
                        
                        if (c.initCircuit(player, args, rc)) {
                            this.addCircuitLookups(c, true);
                            circuits.add(c);
                            rc.getCircuitPersistence().saveCircuits(circuits);

                            ChatColor infoColor = rc.getPrefsManager().getInfoColor();
                            ChatColor debugColor = rc.getPrefsManager().getDebugColor();
                            player.sendMessage(infoColor + "Activated " + ChatColor.YELLOW + c.getClass().getSimpleName() + infoColor + " circuit:");
                            player.sendMessage(debugColor + "> " + ChatColor.WHITE + inputs.size() + debugColor + " input"
                                    + (inputs.size()!=1?"s":"") + ", " + ChatColor.YELLOW + outputs.size() + debugColor + " output"
                                    + (outputs.size()!=1?"s":"") + " and " + ChatColor.BLUE + interfaceBlocks.size() + debugColor
                                    + " interface block" + (interfaceBlocks.size()!=1?"s":"") + ".");
                            return;
                        } else {
                            player.sendMessage(rc.getPrefsManager().getErrorColor() + c.getClass().getSimpleName() + " was not activated.");
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
        for (Circuit c : circuits) c.circuitDestroyed();
    }

    private void scanBranch(Block origin, BlockFace direction, List<Location> inputs, List<Location> outputs, List<Location> interfaces, List<Location> structure) {
        // look in every horizontal direction for inputs, outputs or interface blocks.
        checkAttachedIO(origin, direction, inputs, outputs, interfaces, structure);

        // look in every direction, inculding up and down for more chip blocks.
        checkAttachedChipBlock(origin, direction, inputs, outputs, interfaces, structure);
    }

    private void checkAttachedChipBlock(Block origin, BlockFace direction, List<Location> inputs, List<Location> outputs, List<Location> interfaces, List<Location> structure) {
        
        // look for chip blocks in original direction
        checkForChipBlockOnSideFace(origin, direction, inputs, outputs, interfaces, structure);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        checkForChipBlockOnSideFace(origin, getOppositeFace(direction), inputs, outputs, interfaces, structure);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getFace(BlockFace.UP);
        if (!structure.contains(up.getLocation()) && up.getType()==rc.getPrefsManager().getChipBlockType()) {
            structure.add(up.getLocation());
            scanBranch(up, direction, inputs, outputs, interfaces, structure);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getFace(BlockFace.DOWN);
        if (!structure.contains(down.getLocation()) && down.getType()==rc.getPrefsManager().getChipBlockType()) {
            structure.add(down.getLocation());
            scanBranch(down, direction, inputs, outputs, interfaces, structure);
        }

        // look for chip blocks to the right
        checkForChipBlockOnSideFace(origin, getRightFace(direction), inputs, outputs, interfaces, structure);
        
        // look for chip blocks to the left
        checkForChipBlockOnSideFace(origin, getLeftFace(direction), inputs, outputs, interfaces, structure);
    }

    private void checkAttachedIO(Block origin, BlockFace face, List<Location> inputs, List<Location> outputs, List<Location> interfaces, List<Location> structure) {
        checkForIO(origin, getRightFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, getLeftFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, face, inputs, outputs, interfaces, structure);
        checkForIO(origin, getOppositeFace(face), inputs, outputs, interfaces, structure);
    }

    private void checkForIO(Block origin, BlockFace face, List<Location> inputs, List<Location> outputs, List<Location> interfaces, List<Location> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b.getLocation())) {
            if (b.getType()==rc.getPrefsManager().getInputBlockType()) {
                structure.add(b.getLocation());
                inputs.add(b.getLocation());
            } else if (b.getType()==rc.getPrefsManager().getOutputBlockType()) {
                structure.add(b.getLocation());
                Block o = findLeverAround(b);
                structure.add(o.getLocation());
                outputs.add(o.getLocation());
            } else if (b.getType()==rc.getPrefsManager().getInterfaceBlockType()) {
                structure.add(b.getLocation());
                interfaces.add(b.getLocation());
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

    private void checkForChipBlockOnSideFace(Block origin, BlockFace face, List<Location> inputs, List<Location> outputs, List<Location> interfaces, List<Location> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b.getLocation())) {
            if (b.getType()==rc.getPrefsManager().getChipBlockType()) {
                structure.add(b.getLocation());
                scanBranch(b, face, inputs, outputs, interfaces, structure);
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

    public void checkCircuitDestroyed(Block b, Player p) {
        Circuit destroyed = structureLookupMap.get(b.getLocation());

        if (destroyed!=null && circuits.contains(destroyed)) {
            destroyCircuit(destroyed, p);
        }
    }


    public List<Circuit> getCircuits() {
        return circuits;
    }

    public Circuit getCircuitByStructureBlock(Block structureBlock) {
        return this.structureLookupMap.get(structureBlock.getLocation());
    }

    public Circuit getCircuitByActivationBlock(Block activationBlock) {
        return this.activationLookupMap.get(activationBlock.getLocation());
    }

    void setCircuitList(List<Circuit> circuits) {
        this.circuits = circuits;

        for (Circuit c : circuits)
            addCircuitLookups(c, false);

        rc.log(Level.INFO, circuits.size() + " active circuits");
    }

    public List<InputPin> lookupInputBlock(Block inputBlock) {
        return this.inputLookupMap.get(inputBlock.getLocation());
    }

    public Object[] lookupOutputBlock(Block outputBlock) {
        return this.outputLookupMap.get(outputBlock.getLocation());
    }

    private void addCircuitLookups(Circuit c, boolean newCircuit) {
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
            InputPin ipin = new InputPin(c, c.inputs[i], i);
            for (Location l : ipin.getPowerBlocks()) {
                if (!inputLookupMap.containsKey(l))
                    inputLookupMap.put(l, new ArrayList<InputPin>());

                inputLookupMap.get(l).add(ipin);
            }

            if (newCircuit) c.inputChange(ipin.getIndex(), ipin.getORedValue());
        }

        for (Chunk chunk : usedChunks) {
            ChunkLocation cl = ChunkLocation.fromChunk(chunk);
            if (!chunkLookupMap.containsKey(cl))
                chunkLookupMap.put(cl, new ArrayList<Circuit>());
            chunkLookupMap.get(cl).add(c);
        }

        if (newCircuit) {
            // in case the levers were already on before activation.
            c.updateOutputLevers();
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
        if (destroyer!=null) destroyer.sendMessage(rc.getPrefsManager().getErrorColor() + "You destroyed the " + destroyed.getClass().getSimpleName() + " chip.");
        destroyed.circuitDestroyed();
        circuits.remove(destroyed);
        removeCircuitLookups(destroyed);

        for (Location l : destroyed.outputs) {
            Block output = destroyed.world.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());

            if (output.getType()==Material.LEVER) {
                // turn lever off
                output.setData((byte)(output.getData()&0x7));
            }
        }

        String dName;
        if (destroyer!=null && destroyer instanceof Player) dName = ((Player)destroyer).getDisplayName();
        else dName = "unknown command sender";
        for (Player p : destroyed.getDebuggers()) {
            if (!p.equals(destroyer)) {
                p.sendMessage(rc.getPrefsManager().getDebugColor() + "A " + destroyed.getCircuitClass() + " circuit you were debugging was " + 
                        rc.getPrefsManager().getErrorColor() + "destroyed by " + dName +
                        rc.getPrefsManager().getDebugColor() + " (@" + destroyed.activationBlock.getX() + "," + destroyed.activationBlock.getY() + "," + destroyed.activationBlock.getZ() + ").");
            }
        }
        rc.getCircuitPersistence().saveCircuits(circuits);

    }

    public void checkDebuggerQuit(Player player) {
        for (Circuit c : circuits) {
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
}
