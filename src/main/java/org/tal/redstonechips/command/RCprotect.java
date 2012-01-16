
package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.page.Pager;
import org.tal.redstonechips.wireless.BroadcastChannel;

/**
 *
 * @author Tal Eisenberg
 */
public class RCprotect extends RCCommand {
    private enum ProtCommand {
        PROTECT, UNPROTECT, ADD, REMOVE
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ChatColor extraColor = ChatColor.YELLOW;
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        
        if (args.length == 0) {
            Pager.beginPaging(sender, "rcprotect", RChelp.getCommandHelp("rcprotect", rc), rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
            return true;
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("listchannels")) {
                String protectedChannels = "";
                for (BroadcastChannel curChannel : rc.getChannelManager().getBroadcastChannels().values()) {
                    if (curChannel.isProtected()) {
                        protectedChannels += curChannel.name + ", ";
                    }
                }
                
                if (!protectedChannels.isEmpty()) {
                    if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Protected Channels: " + protectedChannels.substring(0, protectedChannels.length()-2));
                } else {
                    if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no protected channels.");
                }
                
            } else if (rc.getChannelManager().getBroadcastChannels().containsKey(args[0])) {
                BroadcastChannel curChannel = rc.getChannelManager().getChannelByName(args[0], false);
                if (curChannel.isProtected()) {
                    if (!curChannel.checkChanPermissions(sender, false)) {
                        if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to list channel " + args[0] + ".");
                        return true;
                    }
                    
                    String owners = "";
                    String users = "";
                    for (String owner : curChannel.owners) {
                        owners += owner + ", ";
                    }
                    
                    for (String user : curChannel.users) {
                        users += user + ", ";
                    }
                    
                    if (!owners.isEmpty())
                        if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "admins: " + extraColor + owners.substring(0, owners.length()-2));
                    if (!users.isEmpty())
                        if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "users: " + extraColor + users.substring(0, users.length()-2));
                } else {
                    if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " is not protected.");
                }
            } else {
                if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " not found.");
            }
        }
        
        if (args.length >= 2) {
            ProtCommand protCommand;
            BroadcastChannel curChannel;
            
            try {
                protCommand = ProtCommand.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException ie) {
                if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Unknown command: " + args[1]);
                return true;
            }
        
            switch (protCommand) {
                case PROTECT:
                    curChannel = rc.getChannelManager().getChannelByName(args[0], true);
                    if (!curChannel.isProtected()) {
                        if (!(sender instanceof Player) && args.length < 3) {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Usernames must be specified if run from console.");
                            return true;
                        }
                        
                        if (args.length > 2) {
                            if (!addUsers(args, curChannel)) {
                                if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Unable to parse user list.");
                                return true;
                            }
                        }
                        
                        if (sender instanceof Player) {
                            if (!curChannel.owners.contains(((Player)sender).getName().toLowerCase())) curChannel.owners.add(((Player)sender).getName().toLowerCase());
                        }
                        
                        if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Channel " + args[0] + " has been protected.");
                    } else {
                        if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " is already protected.");
                    }
                    break;
                case UNPROTECT:
                    if (rc.getChannelManager().getBroadcastChannels().containsKey(args[0])) {
                        curChannel = rc.getChannelManager().getChannelByName(args[0], false);
                        if (!curChannel.checkChanPermissions(sender, true)) {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to modify channel " + args[0] + ".");
                            return true;
                        }
                        if (curChannel.isProtected()) {
                            curChannel.owners.clear();
                            curChannel.users.clear();
                            if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Channel " + args[0] + " has been unprotected.");
                        } else {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " is not protected.");
                        }
                    } else {
                        if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " not found.");
                    }
                    break;
                case ADD:
                    if (rc.getChannelManager().getBroadcastChannels().containsKey(args[0])) {
                        curChannel = rc.getChannelManager().getChannelByName(args[0], false);
                        if (!curChannel.checkChanPermissions(sender, true)) {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to modify channel " + args[0] + ".");
                            return true;
                        }
                        if (curChannel.isProtected()) {
                            if (args.length > 2) {
                                if (!addUsers(args, curChannel)) {
                                    if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Unable to parse user list.");
                                    return true;
                                }
                                if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Channel " + args[0] + " has been updated.");
                            } else {
                                if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "No usernames passed.");
                            }
                        } else {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " is not protected.");
                        }
                    } else {
                        if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " not found.");
                    }
                    break;
                case REMOVE:
                    if (rc.getChannelManager().getBroadcastChannels().containsKey(args[0])) {
                        curChannel = rc.getChannelManager().getChannelByName(args[0], true);
                        if (!curChannel.checkChanPermissions(sender, true)) {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to modify channel " + args[0] + ".");
                            return true;
                        }
                        if (curChannel.isProtected()) {
                            if (args.length > 2) {
                                if (!removeUsers(args, curChannel)) {
                                    if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Unable to parse user list.");
                                    return true;
                                }
                                if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Channel " + args[0] + " has been updated.");
                            } else {
                                if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "No usernames passed.");
                            }
                        } else {
                            if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " is not protected.");
                        }
                    } else {
                        if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " not found.");
                    }
                    break;
            }
        }
        
        return true;
    }
    
    private boolean addUsers(String[] args, BroadcastChannel curChannel) {
        String[] userList = null;
        for (int i = 2; i < 4; i++) {
            if (i == args.length) return true;
            userList = args[i].toLowerCase().split("[:,]");
            if (userList[0].equals("users")) {
                for (int j = 1; j < userList.length; j++) {
                    if (!curChannel.users.contains(userList[j])) curChannel.users.add(userList[j]); 
                }
            } else if (userList[0].equals("admins")) {
                for (int j = 1; j < userList.length; j++) {
                    if (!curChannel.owners.contains(userList[j])) curChannel.owners.add(userList[j]); 
                }
            } else {
                return false;
            }
        }
        return true;
    }
    
    private boolean removeUsers(String[] args, BroadcastChannel curChannel) {
        String[] userList = null;
        for (int i = 2; i < 4; i++) {
            if (i == args.length) return true;
            userList = args[i].toLowerCase().split("[:,]");
            if (userList[0].equals("users")) {
                for (int j = 1; j < userList.length; j++) {
                    curChannel.users.remove(userList[j]); 
                }
            } else if (userList[0].equals("admins")) {
                for (int j = 1; j < userList.length; j++) {
                    curChannel.owners.remove(userList[j]); 
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
