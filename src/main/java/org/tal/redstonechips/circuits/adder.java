package org.tal.redstonechips.circuits;


import java.util.BitSet;
import org.bukkit.entity.Player;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class adder extends BitSetCircuit {
    BitSet constant;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet set) {
        BitSet output = (BitSet)constant.clone();
        for (BitSet s : this.inputBitSets) {
            output = addBitSets(output, s, bitCount);
        }

        this.sendBitSet(output);
    }

    public static BitSet addBitSets(BitSet aSet, BitSet bSet, int length) {
        BitSet s = (BitSet)aSet.clone();
        s.xor(bSet);
        BitSet c = (BitSet)aSet.clone();
        c.and(bSet);

        while (!c.isEmpty()) {
            shiftLeft(c, length);
            BitSet oldS = (BitSet)s.clone();
            s.xor(c);
            c.and(oldS);
        }

        s.clear(length, s.size());

        return s;
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (!super.init(player, args)) return false;
        if (args.length>0) {
            try {
                int c = Integer.decode(args[0]);
                constant = Circuit.intToBitSet(c, bitCount); // TODO: support two's complement
                return true;
            } catch (NumberFormatException ne) {
                player.sendMessage("Bad argument: " + args[0] + " expected a number.");
                return false;
            }
        }

        return true;
    }
}
