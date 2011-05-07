
package org.tal.redstonechips.circuit;

import org.bukkit.entity.Player;

/**
 * Class for receiving input data from /rctype command.
 * 
 * @author Tal Eisenberg
 */
public interface rcTypeReceiver {
    public void type(String[] words, Player player);
}
