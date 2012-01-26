/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips.user;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.bitset.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
class ChatDebugger extends Debugger {

    public ChatDebugger(UserSession session) {
        super(session);
    }

    @Override
    public void circuitDestroyed(Circuit c, CommandSender destroyer) {
        ChatColor ecolor = session.getPlugin().getPrefs().getErrorColor();
        ChatColor dcolor = session.getPlugin().getPrefs().getDebugColor();
        String dname;
        if (destroyer==null)
            dname = "unknown cause";
        else
            dname = destroyer.getName();
        
        debug(c, "The chip was " +  ecolor + "deactivated " + dcolor + "by " + dname + " (@" + c.activationBlock.getX() + "," 
                + c.activationBlock.getY() + "," + c.activationBlock.getZ() + ").");
    }

    @Override
    public void circuitDisabled(Circuit c) {
        debug(c, session.getPlugin().getPrefs().getErrorColor() + "Chip is disabled.");
    }

    @Override
    public void circuitEnabled(Circuit c) {
        debug(c, "Chip is Enabled.");
    }   

    @Override
    public void circuitMessage(Circuit c, String msg) {
        debug(c, msg);
    }
    
    @Override
    public void inputChanged(Circuit c, int idx, boolean state) {
        List<Flag> cflags = flags.get(c);
        if (cflags!=null && cflags.contains(Flag.IO)) {
            int inputInt = BitSetUtils.bitSetToUnsignedInt(c.getInputBits(), 0, c.inputs.length);
            String i = ChatColor.WHITE + BitSetUtils.bitSetToBinaryString(c.getInputBits(), 0, c.inputs.length) + " (0x" +
                    Integer.toHexString(inputInt) + ")";

            debug(c, "Input " + idx + " is " + (state?"on":"off")+ ": " + i + ".");
        }

    }   

    @Override
    public void outputChanged(Circuit c, int idx, boolean state) {
        List<Flag> cflags = flags.get(c);
        if (cflags!=null && cflags.contains(Flag.IO)) {

            int outputInt = BitSetUtils.bitSetToUnsignedInt(c.getOutputBits(), 0, c.outputs.length);
            String o = ChatColor.YELLOW + BitSetUtils.bitSetToBinaryString(c.getOutputBits(), 0, c.outputs.length) + " (0x" +
                    Integer.toHexString(outputInt) + ")";

            debug(c, "Output " + idx + " is " + (state?"on":"off") + ": " + o + ".");            
        }
    }
    
    private void debug(Circuit c, String msg) {
        Player p = session.getPlayer();
        if (p!=null)
            p.sendMessage(session.getPlugin().getPrefs().getDebugColor() + c.getChipString() + ": " + msg);        
    }
    
}
