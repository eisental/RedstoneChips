
package org.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPermissions;
import org.redstonechips.RCPrefs;
import org.redstonechips.wireless.BroadcastChannel;

/**
 *
 * @author Tal Eisenberg
 */
public class RCprotect extends RCCommand {

    private enum ProtectSubCommand {
        PROTECT, UNPROTECT, ADD, REMOVE
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {        
        if (args.length == 0) {
            RChelp.printCommandHelp(sender, command.getName(), rc);            

        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("listchannels")) {
                listChannels(sender);
            } else 
                channelInfo(sender, args[0]);
        } else if (args.length >= 2) {
            try {
                switch (ProtectSubCommand.valueOf(args[1].toUpperCase())) {
                    case PROTECT:
                        protect(sender, args);
                        break;
                    case UNPROTECT:
                        unprotect(sender, args);
                        break;
                    case ADD:
                        add(sender, args);
                        break;
                    case REMOVE:
                        remove(sender, args);
                        break;
                }                
            } catch (IllegalArgumentException ie) {
                if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Unknown command: " + args[1]);
            }                                
        }
    }
    
    private void channelInfo(CommandSender sender, String channelName) {
        if (!rc.channelManager().getBroadcastChannels().containsKey(channelName)) {
            if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + channelName + " not found.");
            return;
        }
        
        ChatColor extraColor = ChatColor.YELLOW;
        BroadcastChannel curChannel = rc.channelManager().getChannelByName(channelName, false);
        if (curChannel.isProtected()) {
            if (RCPermissions.enforceChannel(sender, curChannel, true)) return;

            String owners = "";
            String users = "";
            for (String owner : curChannel.owners) {
                owners += owner + ", ";
            }

            for (String user : curChannel.users) {
                users += user + ", ";
            }

            if (!owners.isEmpty())
                if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "admins: " + extraColor + owners.substring(0, owners.length()-2));
            if (!users.isEmpty())
                if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "users: " + extraColor + users.substring(0, users.length()-2));
        } else {
            if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + channelName + " is not protected.");
        }
    }
    
    private void protect(CommandSender sender, String[] args) {
        BroadcastChannel curChannel = rc.channelManager().getChannelByName(args[0], true);
        if (!curChannel.isProtected()) {
            if (!(sender instanceof Player) && args.length < 3) {
                if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Usernames must be specified if run from console.");
                return;
            }

            if (args.length > 2) {
                if (!addUsers(args, curChannel)) {
                    if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Unable to parse user list.");
                    return;
                }
            }

            if (sender instanceof Player) {
                if (!curChannel.owners.contains(((Player)sender).getName().toLowerCase())) curChannel.owners.add(((Player)sender).getName().toLowerCase());
            }

            if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "Channel " + args[0] + " has been protected.");
        } else {
            if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " is already protected.");
        }                    
    }
    
    private void unprotect(CommandSender sender, String[] args) {    
        if (rc.channelManager().getBroadcastChannels().containsKey(args[0])) {
            BroadcastChannel curChannel = rc.channelManager().getChannelByName(args[0], false);
            if (!RCPermissions.enforceChannel(sender, curChannel, true)) return;
            
            if (curChannel.isProtected()) {
                curChannel.owners.clear();
                curChannel.users.clear();
                if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "Channel " + args[0] + " has been unprotected.");
            } else {
                if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " is not protected.");
            }
        } else {
            if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " not found.");
        }
    }
        
    private void add(CommandSender sender, String[] args) {
        if (rc.channelManager().getBroadcastChannels().containsKey(args[0])) {
            BroadcastChannel curChannel = rc.channelManager().getChannelByName(args[0], false);
            if (!RCPermissions.enforceChannel(sender, curChannel, true)) return;

            if (curChannel.isProtected()) {
                if (args.length > 2) {
                    if (!addUsers(args, curChannel)) {
                        if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Unable to parse user list.");
                        return;
                    }
                    if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "Channel " + args[0] + " has been updated.");
                } else {
                    if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "No usernames passed.");
                }
            } else {
                if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " is not protected.");
            }
        } else {
            if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " not found.");
        }
    }
    
    private boolean addUsers(String[] args, BroadcastChannel curChannel) {
        String[] userList;
        for (int i = 2; i < 4; i++) {
            if (i == args.length) return true;
            userList = args[i].toLowerCase().split("[:,]");
            switch (userList[0]) {
                case "users":
                    for (int j = 1; j < userList.length; j++) {
                        if (!curChannel.users.contains(userList[j])) curChannel.users.add(userList[j]);
                    }   break;
                case "admins":
                    for (int j = 1; j < userList.length; j++) {
                        if (!curChannel.owners.contains(userList[j])) curChannel.owners.add(userList[j]);
                    }   break;
                default:
                    return false;
            }
        }
        return true;
    }
    
    private void remove(CommandSender sender, String[] args) {
        if (rc.channelManager().getBroadcastChannels().containsKey(args[0])) {
            BroadcastChannel curChannel = rc.channelManager().getChannelByName(args[0], true);
            if (!RCPermissions.enforceChannel(sender, curChannel, true)) return;
                    
            if (curChannel.isProtected()) {
                if (args.length > 2) {
                    if (!removeUsers(args, curChannel)) {
                        if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Unable to parse user list.");
                    } else if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "Channel " + args[0] + " has been updated.");
                } else {
                    if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "No usernames passed.");
                }
            } else {
                if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " is not protected.");
            }
        } else {
            if (sender != null) sender.sendMessage(RCPrefs.getErrorColor() + "Channel " + args[0] + " not found.");
        }        
    }
    
    private boolean removeUsers(String[] args, BroadcastChannel curChannel) {
        String[] userList;
        for (int i = 2; i < 4; i++) {
            if (i == args.length) return true;
            userList = args[i].toLowerCase().split("[:,]");
            switch (userList[0]) {
                case "users":
                    for (int j = 1; j < userList.length; j++) {
                        curChannel.users.remove(userList[j]);
                    }   break;
                case "admins":
                    for (int j = 1; j < userList.length; j++) {
                        curChannel.owners.remove(userList[j]);
                    }   break;
                default:
                    return false;
            }
        }
        return true;
    }
    
   private void listChannels(CommandSender sender) {
        String protectedChannels = "";
        for (BroadcastChannel curChannel : rc.channelManager().getBroadcastChannels().values()) {
            if (curChannel.isProtected()) {
                protectedChannels += curChannel.name + ", ";
            }
        }

        if (!protectedChannels.isEmpty()) {
            if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "Protected Channels: " + protectedChannels.substring(0, protectedChannels.length()-2));
        } else {
            if (sender != null) sender.sendMessage(RCPrefs.getInfoColor() + "There are no protected channels.");
        }
    }    
}
