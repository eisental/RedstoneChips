
package org.tal.redstonechips.circuit;

import org.bukkit.entity.Player;

/**
 * Represents a receiver of input data from /rctype command.
 * 
 * @author Tal Eisenberg
 */
public interface rcTypeReceiver {
    public void type(String[] words, Player player);
}
