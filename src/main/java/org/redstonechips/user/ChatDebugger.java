package org.redstonechips.user;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.util.BooleanArrays;


/**
 * A circuit debugger that uses chat messages.
 * 
 * @author Tal Eisenberg
 */
class ChatDebugger extends Debugger {

    public ChatDebugger(UserSession session) {
        super(session);
    }

    @Override
    public void chipDestroyed(Chip c, CommandSender destroyer) {
        ChatColor ecolor = RCPrefs.getErrorColor();
        ChatColor dcolor = RCPrefs.getDebugColor();
        String dname;
        if (destroyer==null)
            dname = "unknown cause";
        else
            dname = destroyer.getName();
        
        debug(c, "The chip was " +  ecolor + "deactivated " + dcolor + "by " + dname + " (@" + c.activationBlock.getX() + "," 
                + c.activationBlock.getY() + "," + c.activationBlock.getZ() + ").");
    }

    @Override
    public void chipDisabled(Chip c) {
        debug(c, RCPrefs.getErrorColor() + "Chip is disabled.");
    }

    @Override
    public void chipEnabled(Chip c) {
        debug(c, "Chip is Enabled.");
    }   

    @Override
    public void circuitMessage(Chip c, String msg) {
        debug(c, msg);
    }
    
    @Override
    public void inputChanged(Chip c, int idx, boolean state) {
        List<Flag> cflags = flags.get(c);
        if (cflags!=null && cflags.contains(Flag.IO)) {
            long inputInt = BooleanArrays.toUnsignedInt(c.circuit.inputs, 0, c.circuit.inputlen);
            String inputBin = BooleanArrays.toPrettyString(c.circuit.inputs, 0, c.circuit.inputlen);
            String i = ChatColor.WHITE + inputBin + " (0x" + Long.toHexString(inputInt) + ")";

            debug(c, "Input " + idx + " is " + (state?"on":"off")+ ": " + i + ".");
        }

    }   

    @Override
    public void outputChanged(Chip c, int idx, boolean state) {
        List<Flag> cflags = flags.get(c);
        if (cflags!=null && cflags.contains(Flag.IO)) {

            long outputInt = BooleanArrays.toUnsignedInt(c.circuit.outputs, 0, c.circuit.outputlen);
            String outputBin = BooleanArrays.toPrettyString(c.circuit.outputs, 0, c.circuit.outputlen);
            String o = ChatColor.YELLOW + outputBin + " (0x" +
                    Long.toHexString(outputInt) + ")";

            debug(c, "Output " + idx + " is " + (state?"on":"off") + ": " + o + ".");            
        }
    }
    
    private void debug(Chip c, String msg) {
        Player p = session.getPlayer();
        if (p!=null)
            p.sendMessage(RCPrefs.getDebugColor() + c.toString() + ": " + msg);        
    }
    
}
