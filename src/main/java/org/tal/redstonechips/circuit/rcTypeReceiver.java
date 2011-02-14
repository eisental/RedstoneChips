/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import org.bukkit.entity.Player;

/**
 *
 * @author Tal Eisenberg
 */
public interface rcTypeReceiver {
    public void type(String[] words, Player player);
}
