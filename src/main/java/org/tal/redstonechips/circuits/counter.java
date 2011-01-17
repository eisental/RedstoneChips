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
public class counter extends Circuit {
    private static final int inputPin = 0;
    private static final int resetPin = 1;

    int count = 0;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==inputPin) {
            if (newLevel) { // high from low
                count++;
                this.sendInt(0, outputs.length, count);
            }
        } else if (inIdx==resetPin) {
            if (newLevel) { // high from low
                count = 0;
                this.sendInt(0, outputs.length, count);
            }
        }
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (args.length>0) {
            try {
                count = Integer.decode(args[0]);
            } catch (NumberFormatException ne) {
                if (player!=null)
                    player.sendMessage("Invalid count init argument: " + args[0]);
                return false;
            }
        }

        return true;
    }
}
