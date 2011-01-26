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
                Circuit c = RCPersistence.stringToCircuit(circuitString, rc);
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
            props.setProperty(""+circuits.indexOf(c), RCPersistence.toFileString(c, rc));
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
        Block b = signBlock;

        if (b.getType()==Material.WALL_SIGN) {

            // first check if its already registered
            for (Circuit c : circuits) {
                if (c.activationBlock.equals(b)) {
                    player.sendMessage(rc.getPrefsManager().getInfoColor() + "Circuit is already activated.");
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


        while(curBlock.getType()==rc.getPrefsManager().getChipBlockType()) {
            if (curPlus1.getType()==rc.getPrefsManager().getInputBlockType() ||
                    curPlus1.getType()==rc.getPrefsManager().getOutputBlockType()) {
                structure.add(curPlus1);
                Block jack = curPlus1.getRelative((xAxis?0:1), 0, (xAxis?1:0));
                if (curPlus1.getType()==rc.getPrefsManager().getInputBlockType()) {
                    inputs.add(jack);
                    //player.sendMessage("Found input at " + jack.toString());
                } else {
                    outputs.add(jack);
                    structure.add(jack);
                    //player.sendMessage("Found output at " + jack.toString());
                }
            }

            if (curMinus1.getType()==rc.getPrefsManager().getInputBlockType() || curMinus1.getType()==rc.getPrefsManager().getOutputBlockType()) {
                structure.add(curMinus1);
                Block jack = curMinus1.getRelative((xAxis?0:-1), 0, (xAxis?-1:0));
                if (curMinus1.getType()==rc.getPrefsManager().getInputBlockType()) {
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
                player.sendMessage(rc.getPrefsManager().getErrorColor() + "Missing lever block attached to one or more outputs.");
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
                Circuit c = rc.getCircuitLoader().getCircuitInstance(sign.getLine(0).trim());
                c.activationBlock = blockClicked;
                c.outputBlock = outputBlock;
                c.inputs = inputs.toArray(new Block[inputs.size()]);
                c.outputs = outputs.toArray(new Block[outputs.size()]);
                c.structure = structure.toArray(new Block[structure.size()]);
                c.lastLineBlock = lastLineBlock;
                c.args = args;

                if (c.initCircuit(player, args, rc)) {
                    circuits.add(c);
                    addInputLookup(c);
                    addOutputLookup(c);
                    addStructureLookup(c);
                    saveCircuits();
                    player.sendMessage(rc.getPrefsManager().getInfoColor() + "Activated " + c.getClass().getSimpleName() + " with " + inputs.size() + " inputs and " + outputs.size() + " outputs.");
                    return true;
                } else {
                    player.sendMessage(rc.getPrefsManager().getErrorColor() + c.getClass().getSimpleName() + " was not activated.");
                    return false;
                }
            } catch (IllegalArgumentException ex) {
                // unknown circuit name
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                rc.log(Level.WARNING, ex.toString());
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                rc.log(Level.WARNING, ex.toString());
            }
        }

        // if we reached this point the circuit wasn't created.
        return false;
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
