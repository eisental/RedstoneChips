package org.redstonechips;



import org.bukkit.entity.Player;

/**
 * Represents a receiver of input data from /rctype command.
 * 
 * @author Tal Eisenberg
 */
public interface RCTypeReceiver {
    /**
     * Called when a player executes /rctype command while pointing at the typing block.
     * 
     * @param words /rctype space delimited command arguments.
     * @param player The player using /rctype
     */
    public void type(String[] words, Player player);
}