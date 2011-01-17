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
public class shiftregister extends Circuit {
    private BitSet register;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==0) { // clock
            Circuit.shiftLeft(register, outputs.length);
            register.set(0, inputBits.get(1));
            sendBitSet(register);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=2) {
            player.sendMessage("Expecting two inputs. ");
            return false;
        }


        register = new BitSet(outputs.length);
        return true;

    }


}
