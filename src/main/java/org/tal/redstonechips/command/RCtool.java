/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class RCtool extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = CommandUtils.checkIsPlayer(rc, sender);
        if (player==null) return true;
        
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;

        ItemStack item = player.getItemInHand();
        Material type = item.getType();
        
        try {
            rc.setChipProbe(player, type);
        } catch (IllegalArgumentException ie) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
            return true;            
        }
        
        sender.sendMessage(rc.getPrefs().getInfoColor() + "Chip probe set to " + ChatColor.RED + type.name().toLowerCase() + ". " 
                + rc.getPrefs().getInfoColor() + "Right-click a chip block to get info about it.");
        return true;
    }

    public void probeChipBlock(Player player, Block block) {
        try {
            RCpin.printPinInfo(block, player, rc);
        } catch (IllegalArgumentException ie) {
            // not probing a pin
            Circuit c = rc.getCircuitManager().getCircuitByStructureBlock(block);
            if (c!=null) {
                if (c.activationBlock.equals(block.getLocation()))
                    RCinfo.printCircuitInfo(player, c, rc);
                else player.performCommand("rcdebug");
            }
                
        }
            
    }
    
}
