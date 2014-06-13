
package org.redstonechips;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.wireless.BroadcastChannel;

/**
 * A static class for handling permission checks.
 * 
 * @author taleisenberg
 */
public class RCPermissions {
    private RCPermissions() {}
    
    /**
     * Checks if a player has permission to create or destroy a chip.
     * 
     * @param sender
     * @param classname
     * @param create
     * @return true if player has permission.
     */
    public static boolean checkChipPermission(CommandSender sender, String classname, boolean create) {
        if (!RCPrefs.getUsePermissions()) return true;
        if (!(sender instanceof Player)) return true;
        
        Player player = (Player)sender;
        if (player.isOp()) return true;
        
        if (RCPrefs.getUseDenyPermissions() &&
                (player.hasPermission("redstonechips.circuit." + (create?"create":"destroy") + ".deny") || 
                player.hasPermission("redstonechips.circuit." + (create?"create.":"destroy.")  + classname + ".deny"))) 
            return false;
        else return player.hasPermission("redstonechips.circuit." + (create?"create":"destroy") + ".*") || 
                player.hasPermission("redstonechips.circuit." + (create?"create.":"destroy.") + classname);
    }
    
    /**
     * Checks whether the command sender has a permission to use a command.
     * 
     * @param sender
     * @param commandName command name without the slash (/).
     * @param opRequired used in case a permission plugin was not found. If set to true, permission is only granted if the sender is op.
     * @param report Determines whether an error message is sent to the sender in case it doesn't have permission.
     * @return true if the sender has permission to use the command.
     */
    public static boolean enforceCommand(CommandSender sender, String commandName, boolean opRequired, boolean report) {
        if (!RCPrefs.getUsePermissions()) return (opRequired?sender.isOp():true);
        if (!(sender instanceof Player)) return true;
        if(((Player)sender).hasPermission("redstonechips.command." + commandName) &&
                (!RCPrefs.getUseDenyPermissions() ||
                !((Player)sender).hasPermission("redstonechips.command." + commandName + ".deny"))) {
            return true;
        } else {
            if (report) sender.sendMessage(RCPrefs.getErrorColor() + "You do not have permission to use command " + commandName + ".");
            return false;
        }
    }

    /**
     * Checks whether sender has a permission to use a command remotely, usually by using a chip id as an argument instead of pointing at it.
     * @param sender
     * @param commandName command name without the slash (/).
     * @return true if the sender has a permission to use the command.
     */
    public static boolean enforceRemoteCommand(CommandSender sender, String commandName) {
        if (enforceCommand(sender, commandName + ".id", true, false)) return true; 
        else {
            sender.sendMessage(RCPrefs.getErrorColor() + "You do not have permissions to use " + commandName + " remotely.");
            return false;
        }
    }
    
    public static boolean enforceChannel(CommandSender sender, String channelName, boolean report) {
        if (!(RedstoneChips.inst().channelManager().getBroadcastChannels().containsKey(channelName))) return true;
        
        return enforceChannel(sender, RedstoneChips.inst().channelManager().getChannelByName(channelName, false), report);
    }
    
    public static boolean enforceChannel(CommandSender sender, BroadcastChannel channel, boolean report) {
        if (!(sender instanceof Player)) return true;
        
        if (!channel.isProtected()) return true;
        
        String playerName = ((Player)sender).getName();

        boolean ret = ((Player)sender).hasPermission("redstonechips.channel.admin") || 
                channel.users.contains(playerName.toLowerCase()) ||
                channel.owners.contains(playerName.toLowerCase());
        
        if (report && !ret)
            sender.sendMessage(RCPrefs.getErrorColor()+"You do not have permissions to use or modify channel " + channel.name + ".");
        
        return ret;
    }
}
