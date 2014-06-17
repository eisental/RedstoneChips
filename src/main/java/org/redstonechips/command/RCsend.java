package org.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPermissions;
import org.redstonechips.RCPrefs;
import org.redstonechips.parsing.Parsing;
import org.redstonechips.wireless.BroadcastChannel;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class RCsend extends RCCommand {

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (args.length<2) {
            error(sender, "Invalid /rcsend command.");
            return;
        }
        
        if (args[0].startsWith("#")) args[0] = args[0].substring(1);
        BroadcastChannel c = rc.channelManager().getChannelByName(args[0], false);
        if (c==null) {
            error(sender, "Wireless channel does not exist: " + args[0]);
            return;
        } 
        
        if (c.isProtected() && !RCPermissions.enforceChannel(sender, c, false)) {
            error(sender, "You do not have permissions to transmit over this channel.");
            return;
        }
                           
        for (int i=1; i<args.length; i++) {
            try {
                String arg = args[i];
                String data;
                int startBit = 0;
                if (arg.indexOf(":")==-1) data = arg;
                else {
                    data = arg.substring(arg.indexOf(":")+1);
                    String sb = arg.substring(0, arg.indexOf(":"));
                    if (Parsing.isInt(sb)) startBit = Integer.parseInt(sb);
                    else {
                        error(sender, "Bad start bit string: " + sb);
                        return;
                    }
                }
                
                boolean[] bits;
                int length;                
                try {
                    int idata = Integer.decode(data);
                    length = BooleanArrays.requiredBitsForUnsigned(idata);
                    bits = BooleanArrays.fromInt(idata, length);
                } catch (NumberFormatException ne) {
                    if (data.length()==1) {
                        bits = BooleanArrays.fromInt((int)data.charAt(0), 8);
                        length = 8;
                    } else if (data.startsWith("b")) {
                        String sbits = data.substring(1);
                        bits = BooleanArrays.fromString(sbits);
                        length = sbits.length();
                    } else {
                        error(sender, "Bad data: `" + data + "`. Expecting either an integer number, an ascii character or a binary number starting with b.");
                        return;
                    }
                }

                ChatColor ic = RCPrefs.getInfoColor();
                sender.sendMessage(ic + "Transmitting " + ChatColor.YELLOW + BooleanArrays.toPrettyString(bits) 
                        + ic + " over channel " + c.name + " bit " + startBit + ".");
                
                if (startBit+length > c.getLength()) {
                    length = c.getLength() - startBit;
                    sender.sendMessage(ChatColor.DARK_PURPLE + "Transmission is truncated to " + length + " bits to fit channel length.");
                }
                
                c.transmit(bits, startBit, length);
            } catch (Exception e) {
                error(sender, e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
}
