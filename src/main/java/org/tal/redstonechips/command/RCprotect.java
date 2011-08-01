
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.channel.BroadcastChannel;

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
		if (sender instanceof Player) {
			if (!CommandUtils.checkPermission(rc, (Player)sender, command.getName())) return true;
		}
		
		if (args.length == 0) {
			Map commands = (Map)rc.getDescription().getCommands();
			Map commandMap = (Map)commands.get("rcprotect");
			List<String> lines = new ArrayList<String>();

			lines.add(ChatColor.YELLOW+commandMap.get("description").toString());
			if (commandMap.containsKey("usage") && commandMap.get("usage")!=null) {
				String[] usage = commandMap.get("usage").toString().split("\\n");
				sender.sendMessage("");

				for (String line : usage)
					lines.add(line.toString());
			}

			CommandUtils.pageMaker(sender, "/rcprotect", "rcprotect", lines.toArray(new String[lines.size()]), rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
		}
		
		if (args.length == 1) {
			if (args[0].toLowerCase().equals("listchannels")) {
				String protectedChannels = "";
				for (BroadcastChannel curChannel : rc.broadcastChannels.values()) {
					if (curChannel.isProtected()) {
						protectedChannels += curChannel.name + ", ";
					}
				}
				if (!protectedChannels.isEmpty()) {
					if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Protected Channels: " + protectedChannels.substring(0, protectedChannels.length()-2));
				} else {
					if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no protected channels.");
				}
			} else if (rc.broadcastChannels.containsKey(args[0])) {
				BroadcastChannel curChannel = rc.broadcastChannels.get(args[0]);
				if (curChannel.isProtected()) {
					if (!checkChanPermissions(sender, args[0], false)) {
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
                    curChannel = rc.getChannelByName(args[0]);
					if (!curChannel.isProtected()) {
						if (!(sender instanceof Player) && args.length < 3) {
							if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Usernames must be specified if run from console.");
							return true;
						}
						
						if (args.length > 2) {
							if (!addUsers(args, curChannel)) {
								if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Unable to parse uuser list.");
								return true;
							}
						}
						
						if (sender instanceof Player) {
							curChannel.owners.add(((Player)sender).getName().toLowerCase());
						}
						
						if (sender != null) sender.sendMessage(rc.getPrefs().getInfoColor() + "Channel " + args[0] + " has been protected.");
					} else {
						if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "Channel " + args[0] + " is already protected.");
					}
                    break;
				case UNPROTECT:
                    if (rc.broadcastChannels.containsKey(args[0])) {
						if (!checkChanPermissions(sender, args[0], true)) {
							if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to modify channel " + args[0] + ".");
							return true;
						}
						curChannel = rc.broadcastChannels.get(args[0]);
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
					if (rc.broadcastChannels.containsKey(args[0])) {
						if (!checkChanPermissions(sender, args[0], true)) {
							if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to modify channel " + args[0] + ".");
							return true;
						}
						curChannel = rc.getChannelByName(args[0]);
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
					if (rc.broadcastChannels.containsKey(args[0])) {
						if (!checkChanPermissions(sender, args[0], true)) {
							if (sender != null) sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to modify channel " + args[0] + ".");
							return true;
						}
						curChannel = rc.getChannelByName(args[0]);
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
	
	private boolean checkChanPermissions(CommandSender sender, String name, boolean admin) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		if (!(rc.broadcastChannels.containsKey(name))) {
			return true;
		}
		
		if (!(rc.broadcastChannels.get(name).isProtected())) {
			return true;
		}
		
		String playerName = ((Player)sender).getName();
		if (((Player)sender).hasPermission("redstonechips.channel.admin") || rc.broadcastChannels.get(name).owners.contains(playerName.toLowerCase()) || (admin?false:rc.broadcastChannels.get(name).users.contains(playerName.toLowerCase()))) {
			return true;
		}
		
		return false;
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
