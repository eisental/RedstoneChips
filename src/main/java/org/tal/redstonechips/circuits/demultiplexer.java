package org.tal.redstonechips.circuits;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class demultiplexer extends Circuit {
    private int selectSize, bitCount, outcount, selection = -1;
    private BitSet select;
    private BitSet inputBitSet;


    @Override
    public boolean init(Player player, String[] args) {
        if (args.length==0) {
            player.sendMessage("Syntax for multiplexer is 'multiplexer <no. of output sets>.");
            return false;
        }

        try {
            outcount = Integer.decode(args[0]);
            selectSize = (int)Math.ceil(Math.log(outcount)/Math.log(2));
            bitCount = outputs.length/outcount;
            int expectedInputs = bitCount + selectSize;

            if (inputs.length!=expectedInputs) {
                player.sendMessage("Wrong number of inputs. expecting " + expectedInputs + " inputs (including "+ selectSize + " select pins)");
                return false;
            }

            select = new BitSet(selectSize);
            inputBitSet = new BitSet(bitCount);
            
            return true;
        } catch (NumberFormatException ne) {
            player.sendMessage("Not a number: " + args[0]);
            return false;
        }
    }

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx<selectSize) {
            select.set(inIdx, newLevel);
            selection = Circuit.bitSetToUnsignedInt(select, 0, selectSize);
        } else {
            inputBitSet.set(inIdx - selectSize, newLevel);
        }

        if (selection>0 && selection<outcount) {
            this.sendBitSet(selection*bitCount, bitCount, inputBitSet);
        }

        this.sendBitSet(outputBits);
    }

    @Override
    protected void loadState(Map<String, String> state) {
 
    }

    @Override
    protected Map<String, String> saveState() {
        return Circuit.storeBitSet(new HashMap<String,String>(), "inputBits", inputBits, inputs.length);
    }

}
