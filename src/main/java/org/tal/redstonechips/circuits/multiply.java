package org.tal.redstonechips.circuits;


import java.util.BitSet;
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
public class multiply extends BitSetCircuit {
    int constant = 0;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet set) {
        int mul = constant;
        for (BitSet s : this.inputBitSets) {
            mul = mul * Circuit.bitSetToUnsignedInt(s, 0, bitCount);
        }

        this.sendInt(0, outputs.length, mul);
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (!super.init(player, args)) return false;
        if (args.length>0) {
            try {
                constant = Integer.decode(args[0]);
                return true;
            } catch (NumberFormatException ne) {
                player.sendMessage("Bad argument: " + args[0] + " expected a number.");
                return false;
            }
        } else constant = 1;

        return true;
    }

    @Override
    protected void loadState(Map<String, String> state) {
        String sconst = state.get("constant");
        if (sconst==null) return;

        constant = Integer.decode(sconst);
        super.loadState(state);
    }

    @Override
    protected Map<String, String> saveState() {
        Map<String,String> map = super.saveState();
        map.put("constant", ""+constant);
        return map;
    }
}
