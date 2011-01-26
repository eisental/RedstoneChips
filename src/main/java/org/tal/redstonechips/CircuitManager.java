/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitManager {
    public final static String circuitsFileName = "redstonechips.dat";

    private RedstoneChips rc;
    
    private List<Circuit> circuits;
    private Map<Block, Object[]> inputLookupMap = new HashMap<Block, Object[]>();
    private Map<Block, Object[]> outputLookupMap = new HashMap<Block, Object[]>();
    private Map<Block, Circuit> structureLookupMap = new HashMap<Block, Circuit>();
    private Map<Block, Circuit> activationLookupMap = new HashMap<Block, Circuit>();

    public CircuitManager(RedstoneChips plugin) {
        rc = plugin;
    }

    public void loadCircuits() {
        Properties props = new Properties();
        File propFile = new File(rc.getDataFolder(), circuitsFileName);
        if (!propFile.exists()) { // create empty file if doesn't already exist
            try {
                props.store(new FileOutputStream(propFile), "");
            } catch (IOException ex) {
                rc.log(Level.SEVERE, ex.getMessage());
            }
        }

        circuits = new ArrayList<Circuit>();

        try {
            props.load(new FileInputStream(propFile));
            for (String id : props.stringPropertyNames()) {
                String circuitString = props.getProperty(id);
                Circuit c = CircuitPersistense.stringToCircuit(circuitString, rc);
                if (c==null) rc.log(Level.WARNING, "Error while loading circuit: " + circuitString);
                else {
                    circuits.add(c);
                    addInputLookup(c);
                    addOutputLookup(c);
                    addStructureLookup(c);
                }
            }
            rc.log(Level.INFO, circuits.size() + " active circuits");
        } catch (Exception ex) {
            rc.log(Level.SEVERE, ex.getMessage());
        }

    }

    public void saveCircuits() {
        Properties props = new Properties();
        File propFile = new File(rc.getDataFolder(), circuitsFileName);

        for (Circuit c : circuits) {
            props.setProperty(""+circuits.indexOf(c), CircuitPersistense.toFileString(c, rc));
        }

        try {
            props.store(new FileOutputStream(propFile), "");
            rc.log(Level.INFO, "Saved circuits state to file.");
        } catch (IOException ex) {
            rc.log(Level.SEVERE, ex.getMessage());
        }
    }

    public void redstoneChange(BlockRedstoneEvent e) {

        boolean newVal = (e.getNewCurrent()>0);
        boolean oldVal = (e.getOldCurrent()>0);
        if (newVal==oldVal) return; // not a change

        Object[] o = inputLookupMap.get(e.getBlock());
        if (o!=null) {
            final Circuit c = (Circuit)o[0];
            final int i = (Integer)o[1];
            if (e.getBlock().getType()==Material.STONE_BUTTON) { // manually reset the button after 1 sec.
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

    public void checkForCircuit(Block signBlock, Player player) {
        if (signBlock.getType()==Material.WALL_SIGN) {
            Sign sign = (Sign)signBlock.getState();

            // first check if its already registered
            Circuit check = this.getCircuitByActivationBlock(signBlock);
            if (check!=null) {
                player.sendMessage(rc.getPrefsManager().getInfoColor() + "Circuit is already activated.");
                return;
            }

            // then check if the sign points to a known circuit type
            if (!rc.getCircuitLoader().getCircuitClasses().containsKey(sign.getLine(0).trim())) return;

            List<Block> inputs = new ArrayList<Block>();
            List<Block> outputs = new ArrayList<Block>();
            List<Block> structure = new ArrayList<Block>();
            List<Block> interactions = new ArrayList<Block>();

            structure.add(signBlock);
            
            BlockFace direction = findSignDirection(signBlock.getData());
            Block firstChipBlock = signBlock.getFace(direction);

            if (firstChipBlock.getType()==rc.getPrefsManager().getChipBlockType()) {
                structure.add(firstChipBlock);
                scanBranch(firstChipBlock, direction, inputs, outputs, interactions, structure);

                if (outputs.size()>0 || inputs.size()>0) {
                    if (!checkForLevers(outputs, player)) return;
                    String[] args = getArgsFromSign(sign);

                    try {
                        Circuit c = rc.getCircuitLoader().getCircuitInstance(sign.getLine(0).trim());
                        c.activationBlock = signBlock;
                        c.inputs = inputs.toArray(new Block[inputs.size()]);
                        c.outputs = outputs.toArray(new Block[outputs.size()]);
                        c.structure = structure.toArray(new Block[structure.size()]);
                        c.interactionBlocks = interactions.toArray(new Block[interactions.size()]);

                        c.args = args;

                        if (c.initCircuit(player, args, rc)) {
                            circuits.add(c);
                            addInputLookup(c);
                            addOutputLookup(c);
                            addStructureLookup(c);
                            saveCircuits();
                            player.sendMessage(rc.getPrefsManager().getInfoColor() + "Activated " + c.getClass().getSimpleName() + " with " + inputs.size() + " inputs and " + outputs.size() + " outputs.");
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

    private void scanBranch(Block origin, BlockFace direction, List<Block> inputs, List<Block> outputs, List<Block> interactions, List<Block> structure) {
        rc.log(Level.INFO, "scanBranch: " + origin + " inputs: " + inputs.size() + " outputs: " + outputs.size() + " structure: " + structure.size());

        // look in every horizontal direction for inputs, outputs or interaction blocks.
        checkAttachedIO(origin, direction, inputs, outputs, interactions, structure);

        // look in every direction, inculding up and down for more chip blocks.
        checkAttachedChipBlock(origin, direction, inputs, outputs, interactions, structure);
    }

    private void checkAttachedChipBlock(Block origin, BlockFace direction, List<Block> inputs, List<Block> outputs, List<Block> interactions, List<Block> structure) {
        
        // look for chip blocks in original direction
        checkForChipBlockOnSideFace(origin, direction, inputs, outputs, interactions, structure);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        checkForChipBlockOnSideFace(origin, getOppositeFace(direction), inputs, outputs, interactions, structure);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getFace(BlockFace.UP);
        if (!structure.contains(up) && up.getType()==rc.getPrefsManager().getChipBlockType()) {
            structure.add(up);
            scanBranch(up, direction, inputs, outputs, interactions, structure);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getFace(BlockFace.DOWN);
        if (!structure.contains(down) && down.getType()==rc.getPrefsManager().getChipBlockType()) {
            structure.add(down);
            scanBranch(down, direction, inputs, outputs, interactions, structure);
        }

        // look for chip blocks to the right
        checkForChipBlockOnSideFace(origin, getRightFace(direction), inputs, outputs, interactions, structure);
        
        // look for chip blocks to the left
        checkForChipBlockOnSideFace(origin, getLeftFace(direction), inputs, outputs, interactions, structure);
    }

    private void checkAttachedIO(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interactions, List<Block> structure) {
        checkForIO(origin, getRightFace(face), inputs, outputs, interactions, structure);
        checkForIO(origin, getLeftFace(face), inputs, outputs, interactions, structure);
        checkForIO(origin, face, inputs, outputs, interactions, structure);
        checkForIO(origin, getOppositeFace(face), inputs, outputs, interactions, structure);
    }

    private void checkForIO(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interactions, List<Block> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b)) {
            if (b.getType()==rc.getPrefsManager().getInputBlockType()) {
                structure.add(b);
                inputs.add(b.getFace(face));
            } else if (b.getType()==rc.getPrefsManager().getOutputBlockType()) {
                structure.add(b);
                Block o = b.getFace(face);
                structure.add(o);
                outputs.add(o);
            } else if (b.getType()==rc.getPrefsManager().getInteractionBlockType()) {
                structure.add(b);
                interactions.add(b.getFace(face));
            }
        }
    }

    private void checkForChipBlockOnSideFace(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interactions, List<Block> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b)) {
            if (b.getType()==rc.getPrefsManager().getChipBlockType()) {
                structure.add(b);
                scanBranch(b, face, inputs, outputs, interactions, structure);
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

    private boolean checkForLevers(List<Block> outputs, Player player) {
        for (Block o : outputs) {
            if (o.getType()!=Material.LEVER) {
                player.sendMessage(rc.getPrefsManager().getErrorColor() + "A lever is missing from one or more outputs.");
                return false;
            }
        } return true;
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
        Circuit destroyed = structureLookupMap.get(b);

        if (destroyed!=null && circuits.contains(destroyed)) {
            if (p!=null) p.sendMessage(rc.getPrefsManager().getErrorColor() + "You destroyed the " + destroyed.getClass().getSimpleName() + " chip.");
            destroyed.circuitDestroyed();
            circuits.remove(destroyed);
            removeInputLookup(destroyed);
            removeOutputLookup(destroyed);
            removeStructureLookup(destroyed);
            saveCircuits();
        }
    }


    public List<Circuit> getCircuits() {
        return circuits;
    }

    public Circuit getCircuitByStructureBlock(Block structureBlock) {
        return this.structureLookupMap.get(structureBlock);
    }

    public Circuit getCircuitByActivationBlock(Block activationBlock) {
        return this.activationLookupMap.get(activationBlock);
    }

    public Object[] lookupInputBlock(Block inputBlock) {
        return this.inputLookupMap.get(inputBlock);
    }

    public Object[] lookupOutputBlock(Block outputBlock) {
        return this.outputLookupMap.get(outputBlock);
    }

    private void addInputLookup(Circuit c) {
        for (int i=0; i<c.inputs.length; i++) {
            Block input = c.inputs[i];
            inputLookupMap.put(input, new Object[]{c, i});
        }
    }

    private void addOutputLookup(Circuit c) {
        for (int i=0; i<c.outputs.length; i++) {
            Block output = c.outputs[i];
            outputLookupMap.put(output, new Object[]{c, i});
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

    private void removeOutputLookup(Circuit c) {
        for (Block output : c.outputs) {
            outputLookupMap.remove(output);
        }
    }
}
