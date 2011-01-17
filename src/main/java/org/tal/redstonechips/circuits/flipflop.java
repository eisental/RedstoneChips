package org.tal.redstonechips.circuits;

import org.bukkit.entity.Player;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class flipflop extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (newLevel) {
            this.sendOutput(inIdx, !outputBits.get(inIdx));
        }

    }

    @Override
    public boolean init(Player player, String[] args) {
        if (outputs.length!=inputs.length) {
            player.sendMessage("flipflop number of outputs must match number of inputs.");
            return false;
        } else return true;
    }

}
