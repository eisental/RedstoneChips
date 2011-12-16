
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.channel.BroadcastChannel;
import org.tal.redstonechips.channel.ReceivingCircuit;
import org.tal.redstonechips.channel.TransmittingCircuit;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class RCchannels extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        
        if (rc.getChannelManager().getBroadcastChannels().isEmpty()) {
            sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no active broadcast channels.");
        } else {
            if (args.length>0 && rc.getChannelManager().getBroadcastChannels().containsKey(args[0])) {
                if (!(checkChanPermissions(sender, args[0]))) {
                    return true;
                }
                printChannelInfo(sender, args[0]);
            } else {
                List<String> lines = new ArrayList<String>();
                for (BroadcastChannel channel : rc.getChannelManager().getBroadcastChannels().values()) {
                    if (channel.checkChanPermissions(sender, false))
                        lines.add(ChatColor.YELLOW + channel.name + ChatColor.WHITE + " - " + channel.getLength() + " bits, " + channel.getTransmitters().size() + " transmitters, " + channel.getReceivers().size() + " receivers." + ChatColor.GREEN + (channel.isProtected()?" Protected":""));
                }

                if (lines.isEmpty()) {
                    sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no active broadcast channels.");
                    return true;
                }
                String[] outputLines = lines.toArray(new String[lines.size()]);
                sender.sendMessage("");
                CommandUtils.pageMaker(sender, "Active wireless broadcast channels", command.getName(), outputLines, rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor(), CommandUtils.MaxLines - 1);
                sender.sendMessage("Use " + ChatColor.YELLOW + "/rcchannels <channel name>" + ChatColor.WHITE + " for more info about it.");
            }
        }

        return true;
    }

    private void printChannelInfo(CommandSender sender, String channelName) {
        ChatColor infoColor = rc.getPrefs().getInfoColor();
        ChatColor errorColor = rc.getPrefs().getErrorColor();
        ChatColor extraColor = ChatColor.YELLOW;

        BroadcastChannel channel = rc.getChannelManager().getChannelByName(channelName, false);
        if (channel==null) {
            sender.sendMessage(errorColor + "Channel " + channelName + " doesn't exist.");
        } else {
            String sTransmitters = "";
            for (TransmittingCircuit t : channel.getTransmitters()) {
                String range = "[";
                if (t.getChannelLength()>1)
                    range += "bits " + t.getStartBit() + "-" + (t.getChannelLength()+t.getStartBit()-1) + "]";
                else range += "bit " + t.getStartBit() + "]";

                sTransmitters += t.getChipString() + " " + range + ", ";
            }

            String sReceivers = "";
            for (ReceivingCircuit r : channel.getReceivers()) {
                String range = "[";
                if (r.getChannelLength()>1)
                    range += "bits " + r.getStartBit() + "-" + (r.getChannelLength()+r.getStartBit()-1) + "]";
                else range += "bit " + r.getStartBit() + "]";
                sReceivers += r.getChipString() + " " + range + ", ";
            }
            
            String owners = "";
            String users = "";
            if (channel.isProtected()) {
                for (String owner : channel.owners) {
                    owners += owner + ", ";
                }
                
                for (String user : channel.users) {
                    users += user + ", ";
                }
            }

            sender.sendMessage("");
            sender.sendMessage(extraColor + channel.name + ":");
            sender.sendMessage(extraColor + "----------------------");

            sender.sendMessage(infoColor + "last broadcast: " + extraColor + BitSetUtils.bitSetToBinaryString(channel.bits, 0, channel.getLength()) + infoColor + " length: " + extraColor + channel.getLength());

            if (!sTransmitters.isEmpty())
                sender.sendMessage(infoColor + "transmitters: " + extraColor + sTransmitters.substring(0, sTransmitters.length()-2));
            if (!sReceivers.isEmpty())
                sender.sendMessage(infoColor + "receivers: " + extraColor + sReceivers.substring(0, sReceivers.length()-2));
            if (!owners.isEmpty())
                sender.sendMessage(infoColor + "admins: " + extraColor + owners.substring(0, owners.length()-2));
            if (!users.isEmpty())
                sender.sendMessage(infoColor + "users: " + extraColor + users.substring(0, users.length()-2));
        }
    }
    
    private boolean checkChanPermissions(CommandSender sender, String name) {
        if (!(sender instanceof Player)) return true;
        
        if (!(rc.getChannelManager().getBroadcastChannels().containsKey(name))) return true;
        
        if (!(rc.getChannelManager().getBroadcastChannels().get(name).isProtected())) return true;
        
        String playerName = ((Player)sender).getName();
        if (((Player)sender).hasPermission("redstonechips.channel.admin") || 
                rc.getChannelManager().getChannelByName(name, false).users.contains(playerName.toLowerCase()) || 
                rc.getChannelManager().getChannelByName(name, false).owners.contains(playerName.toLowerCase())) {
            return true;
        }
        
        return false;
    }
}
