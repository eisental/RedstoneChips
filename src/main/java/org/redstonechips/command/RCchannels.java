
package org.redstonechips.command;

import org.redstonechips.paging.ArrayLineSource;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPermissions;
import org.redstonechips.RCPrefs;
import org.redstonechips.paging.Pager;
import org.redstonechips.wireless.BroadcastChannel;
import org.redstonechips.wireless.Receiver;
import org.redstonechips.wireless.Transmitter;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class RCchannels extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (rc.channelManager().getBroadcastChannels().isEmpty()) {
            info(sender, "There are no active broadcast channels.");
        } else {
            if (args.length>0 && rc.channelManager().getBroadcastChannels().containsKey(args[0])) {
                if (RCPermissions.enforceChannel(sender, args[0], true)) printChannelInfo(sender, args[0]);
            } else {
                printChannelList(sender);
            }
        }
    }

    public static void printChannelList(CommandSender sender) {
        org.redstonechips.RedstoneChips rc = org.redstonechips.RedstoneChips.inst();
        
        List<String> lines = new ArrayList<>();
        for (BroadcastChannel channel : rc.channelManager().getBroadcastChannels().values()) {
            if (RCPermissions.enforceChannel(sender, channel, false)) {
                lines.add(ChatColor.YELLOW + channel.name + ChatColor.WHITE + " - " + channel.getLength() + " bits, " + channel.getTransmitters().size() + " transmitters, " + channel.getReceivers().size() + " receivers." + ChatColor.GREEN + (channel.isProtected()?" Protected":""));
            }
        }

        if (lines.isEmpty()) {
            info(sender, "There are no known active broadcast channels.");                    
        } else {
            String[] outputLines = lines.toArray(new String[lines.size()]);
            sender.sendMessage("");
            Pager.beginPaging(sender, "Active wireless broadcast channels", new ArrayLineSource(outputLines), RCPrefs.getInfoColor(), RCPrefs.getErrorColor(), Pager.MaxLines - 1);
            sender.sendMessage("Use " + ChatColor.YELLOW + "/rcchannels <channel name>" + ChatColor.WHITE + " for more info about it.");
        }        
    }
    
    public static void printChannelInfo(CommandSender sender, String channelName) {
        org.redstonechips.RedstoneChips rc = org.redstonechips.RedstoneChips.inst();
        
        BroadcastChannel channel = rc.channelManager().getChannelByName(channelName, false);
        if (channel==null) {
            error(sender, "Channel " + channelName + " doesn't exist.");
        } else {
            String sTransmitters = "";
            for (Transmitter t : channel.getTransmitters()) {
                String range = "[";
                if (t.getLength()>1)
                    range += "bits " + t.getStartBit() + "-" + (t.getLength()+t.getStartBit()-1) + "]";
                else range += "bit " + t.getStartBit() + "]";

                sTransmitters += t.getCircuit().chip + " " + range + ", ";
            }

            String sReceivers = "";
            for (Receiver r : channel.getReceivers()) {
                String range = "[";
                if (r.getLength()>1)
                    range += "bits " + r.getStartBit() + "-" + (r.getLength()+r.getStartBit()-1) + "]";
                else range += "bit " + r.getStartBit() + "]";
                sReceivers += r.getCircuit().chip + " " + range + ", ";
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

            ChatColor infoColor = RCPrefs.getInfoColor();
            ChatColor extraColor = ChatColor.YELLOW;

            info(sender, "");
            info(sender, extraColor + channel.name + ":");
            info(sender, extraColor + "----------------------");

            info(sender, "last broadcast: " + extraColor + BooleanArrays.toPrettyString(channel.bits, 0, channel.getLength()) + infoColor + " length: " + extraColor + channel.getLength());

            if (!sTransmitters.isEmpty())
                info(sender, "transmitters: " + extraColor + sTransmitters.substring(0, sTransmitters.length()-2));
            if (!sReceivers.isEmpty())
                info(sender, "receivers: " + extraColor + sReceivers.substring(0, sReceivers.length()-2));
            if (!owners.isEmpty())
                info(sender, "admins: " + extraColor + owners.substring(0, owners.length()-2));
            if (!users.isEmpty())
                info(sender, "users: " + extraColor + users.substring(0, users.length()-2));
        }
    }    
}
