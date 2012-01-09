package org.tal.redstonechips.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.wireless.BroadcastChannel;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;
import org.tal.redstonechips.util.ParsingUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class RCtransmit extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
        if (args.length<2) return false;
        
        BroadcastChannel c = rc.getChannelManager().getChannelByName(args[0], false);
        if (c==null) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "Wireless channel does not exist: " + args[0]);
            return true;
        } 
        
        if (c.isProtected() && !c.checkChanPermissions(sender, false)) {
            sender.sendMessage(rc.getPrefs().getErrorColor() + "You do not have permissions to transmit over this channel.");
            return true;
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
                    if (ParsingUtils.isInt(sb)) startBit = Integer.parseInt(sb);
                    else {
                        sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad start bit string: " + sb);
                        return true;
                    }
                }
                
                BitSet7 bits;
                int length;                
                try {
                    int ret = Integer.decode(data);
                    if (ret==0) length = 1;
                    else length = (int)Math.ceil(Math.log(ret)/Math.log(2))+1;
                    bits = BitSetUtils.intToBitSet(ret, length);
                } catch (NumberFormatException ne) {
                    if (data.length()==1) {
                        bits = BitSetUtils.intToBitSet((int)data.charAt(0), 8);
                        length = 8;
                    } else if (data.startsWith("b")) {
                        String sbits = data.substring(1);
                        bits = BitSetUtils.stringToBitSet(sbits);
                        length = sbits.length();
                    } else {
                        sender.sendMessage(rc.getPrefs().getErrorColor() + "Bad data: " + data + ". Expecting either an integer number, an ascii character or a binary number starting with b.");
                        return true;
                    }
                }

                sender.sendMessage(rc.getPrefs().getInfoColor() + "Transmitting " + 
                        ChatColor.YELLOW + BitSetUtils.bitSetToString(bits, length) + rc.getPrefs().getInfoColor() + " over channel " + c.name + " bit " + startBit + ".");
                
                c.transmit(bits, startBit, length);
            } catch (IllegalArgumentException ie) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + ie.getMessage());
                return true;
            } catch (RuntimeException e) {
                sender.sendMessage(rc.getPrefs().getErrorColor() + e.toString());
                return true;
            }
        }
        
        return true;
    }
    
}
