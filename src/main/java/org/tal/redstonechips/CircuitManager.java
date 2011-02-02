/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips;

import org.tal.redstonechips.circuit.Circuit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.tal.redstonechips.circuit.InputPin;

/**
 *
 * @author Tal Eisenberg
 */
public class CircuitManager {
    private RedstoneChips rc;
    
    private List<Circuit> circuits;
    private Map<Block, List<InputPin>> inputLookupMap = new HashMap<Block, List<InputPin>>();
    private Map<Block, Object[]> outputLookupMap = new HashMap<Block, Object[]>();
    private Map<Block, Circuit> structureLookupMap = new HashMap<Block, Circuit>();
    private Map<Block, Circuit> activationLookupMap = new HashMap<Block, Circuit>();

    public CircuitManager(RedstoneChips plugin) {
        rc = plugin;
    }

    public void redstoneChange(BlockRedstoneEvent e) {

        boolean newVal = (e.getNewCurrent()>0);
        boolean oldVal = (e.getOldCurrent()>0);
        if (newVal==oldVal) return; // not a change

        List<InputPin> inputList = inputLookupMap.get(e.getBlock());
        if (inputList==null) return;

        for (InputPin inputPin : inputList) {
            inputPin.updateValue(e.getBlock(), newVal);

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

            List<Block> inputs = new ArrayList<Block>();
            List<Block> outputs = new ArrayList<Block>();
            List<Block> structure = new ArrayList<Block>();
            List<Block> interfaceBlocks = new ArrayList<Block>();

            structure.add(signBlock);
            
            BlockFace direction = findSignDirection(signBlock.getData());
            Block firstChipBlock = signBlock.getFace(direction);

            if (firstChipBlock.getType()==rc.getPrefsManager().getChipBlockType()) {
                structure.add(firstChipBlock);
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
                        c.activationBlock = signBlock;
                        c.inputs = inputs.toArray(new Block[inputs.size()]);
                        c.outputs = outputs.toArray(new Block[outputs.size()]);
                        c.structure = structure.toArray(new Block[structure.size()]);
                        c.interfaceBlocks = interfaceBlocks.toArray(new Block[interfaceBlocks.size()]);

                        c.args = args;

                        if (c.initCircuit(player, args, rc)) {
                            circuits.add(c);
                            this.addCircuitLookups(c);
                            rc.getCircuitPersistence().saveCircuits(circuits);

                            player.sendMessage(rc.getPrefsManager().getInfoColor() + "Activated " + c.getClass().getSimpleName() + " with " + inputs.size() + " input" + (inputs.size()!=1?"s":"") + ", " + outputs.size() + " output" + (outputs.size()!=1?"s":"")+
                                    rc.getPrefsManager().getInfoColor() + " and " + interfaceBlocks.size() + " interface block(s).");
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

    private void scanBranch(Block origin, BlockFace direction, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        // look in every horizontal direction for inputs, outputs or interface blocks.
        checkAttachedIO(origin, direction, inputs, outputs, interfaces, structure);

        // look in every direction, inculding up and down for more chip blocks.
        checkAttachedChipBlock(origin, direction, inputs, outputs, interfaces, structure);
    }

    private void checkAttachedChipBlock(Block origin, BlockFace direction, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        
        // look for chip blocks in original direction
        checkForChipBlockOnSideFace(origin, direction, inputs, outputs, interfaces, structure);
        
        // look backwards. Structure should already contain this block unless last checked block was below or above.
        checkForChipBlockOnSideFace(origin, getOppositeFace(direction), inputs, outputs, interfaces, structure);

        // look up. If found chip block above, will try to continue in the old direction 1 block up.
        Block up = origin.getFace(BlockFace.UP);
        if (!structure.contains(up) && up.getType()==rc.getPrefsManager().getChipBlockType()) {
            structure.add(up);
            scanBranch(up, direction, inputs, outputs, interfaces, structure);
        }

        // look down. If found chip block below, will try to continue in the old direction 1 block down.
        Block down = origin.getFace(BlockFace.DOWN);
        if (!structure.contains(down) && down.getType()==rc.getPrefsManager().getChipBlockType()) {
            structure.add(down);
            scanBranch(down, direction, inputs, outputs, interfaces, structure);
        }

        // look for chip blocks to the right
        checkForChipBlockOnSideFace(origin, getRightFace(direction), inputs, outputs, interfaces, structure);
        
        // look for chip blocks to the left
        checkForChipBlockOnSideFace(origin, getLeftFace(direction), inputs, outputs, interfaces, structure);
    }

    private void checkAttachedIO(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        checkForIO(origin, getRightFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, getLeftFace(face), inputs, outputs, interfaces, structure);
        checkForIO(origin, face, inputs, outputs, interfaces, structure);
        checkForIO(origin, getOppositeFace(face), inputs, outputs, interfaces, structure);
    }

    private void checkForIO(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b)) {
            if (b.getType()==rc.getPrefsManager().getInputBlockType()) {
                structure.add(b);
                inputs.add(b);
            } else if (b.getType()==rc.getPrefsManager().getOutputBlockType()) {
                structure.add(b);
                Block o = findLeverAround(b);
                structure.add(o);
                outputs.add(o);
            } else if (b.getType()==rc.getPrefsManager().getInterfaceBlockType()) {
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

    private void checkForChipBlockOnSideFace(Block origin, BlockFace face, List<Block> inputs, List<Block> outputs, List<Block> interfaces, List<Block> structure) {
        Block b = origin.getFace(face);
        if (!structure.contains(b)) {
            if (b.getType()==rc.getPrefsManager().getChipBlockType()) {
                structure.add(b);
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
        Circuit destroyed = structureLookupMap.get(b);

        if (destroyed!=null && circuits.contains(destroyed)) {
            destroyCircuit(destroyed, p);
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

    void setCircuitList(List<Circuit> circuits) {
        this.circuits = circuits;

        for (Circuit c : circuits)
            addCircuitLookups(c);

        rc.log(Level.INFO, circuits.size() + " active circuits");
    }

    public List<InputPin> lookupInputBlock(Block inputBlock) {
        return this.inputLookupMap.get(inputBlock);
    }

    public Object[] lookupOutputBlock(Block outputBlock) {
        return this.outputLookupMap.get(outputBlock);
    }

    private void addCircuitLookups(Circuit c) {
        for (int i=0; i<c.structure.length; i++)
            structureLookupMap.put(c.structure[i], c);

        for (int i=0; i<c.outputs.length; i++)
            outputLookupMap.put(c.outputs[i], new Object[]{c, i});

        activationLookupMap.put(c.activationBlock, c);

        for (int i=0; i<c.inputs.length; i++) {
            InputPin ipin = new InputPin(c, c.inputs[i], i);
            for (Block b : ipin.getPowerBlocks()) {
                if (!inputLookupMap.containsKey(b))
                    inputLookupMap.put(b, new ArrayList<InputPin>());

                inputLookupMap.get(b).add(ipin);
            }
        }
    }

    private void removeCircuitLookups(Circuit c) {
        for (Block block : c.structure)
            structureLookupMap.remove(block);

        for (Block input : c.inputs)
            inputLookupMap.remove(input);

        for (Block output : c.outputs)
            outputLookupMap.remove(output);

        activationLookupMap.remove(c.activationBlock);

    }

    public void destroyCircuit(Circuit destroyed, CommandSender destroyer) {
        if (destroyer!=null) destroyer.sendMessage(rc.getPrefsManager().getErrorColor() + "You destroyed the " + destroyed.getClass().getSimpleName() + " chip.");
        destroyed.circuitDestroyed();
        circuits.remove(destroyed);
        removeCircuitLookups(destroyed);

        for (Block output : destroyed.outputs) {
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
}
