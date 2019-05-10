
package org.redstonechips.command;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.util.ChunkLocation;

/**
 *
 * @author Tal Eisenberg
 */
public class RCinfo extends RCRemoteChipCommand {
    @Override
    protected void runWithChip(Chip target, CommandSender sender, Command command, String label, String[] args) {
        printCircuitInfo(sender, target);
    }

    public static void printCircuitInfo(CommandSender sender, Chip c) {
        ChatColor infoColor = RCPrefs.getInfoColor();
        ChatColor extraColor = ChatColor.YELLOW;

        String disabled;
        if (c.isDisabled()) disabled = ChatColor.GRAY + " - disabled";
        else disabled = "";

        String loc = c.activationBlock.getBlockX() + ", " + c.activationBlock.getBlockY() + ", " + c.activationBlock.getBlockZ();
        String chunkCoords = "";
        for (ChunkLocation l : c.chunks)
            chunkCoords += (l.isChunkLoaded()?extraColor:ChatColor.WHITE) + "[" + l.getX() + ", " + l.getZ() + " " + (l.isChunkLoaded()?"L":"u") + "]" + infoColor + ", ";

        if (!chunkCoords.isEmpty()) chunkCoords = chunkCoords.substring(0, chunkCoords.length()-2);
        
        info(sender, "");
        info(sender, c.toString() + disabled);
        info(sender, extraColor + "----------------------------------------");

        info(sender, "" + c.inputPins.length + " input(s), " + c.outputPins.length + " output(s) and " + c.interfaceBlocks.length + " interface blocks.");
        info(sender, "location: " + extraColor + loc + infoColor + " @ " + extraColor + c.world.getName());
        info(sender, " chunks: " + chunkCoords);

        long inputInt = BooleanArrays.toUnsignedInt(c.circuit.inputs, 0, c.circuit.inputlen);
        String inputBin = BooleanArrays.toPrettyString(c.circuit.inputs, 0, c.circuit.inputlen);
        long outputInt = BooleanArrays.toUnsignedInt(c.circuit.outputs, 0, c.circuit.outputlen);
        String outputBin = BooleanArrays.toPrettyString(c.circuit.outputs, 0, c.circuit.outputlen);
        
        
        if (c.inputPins.length>0)
            info(sender, "input states: " + extraColor + inputBin + infoColor + " (0x" + Long.toHexString(inputInt) + ")");

        if (c.outputPins.length>0)
            info(sender, "output states: " + extraColor + outputBin + infoColor + " (0x" + Long.toHexString(outputInt) + ")");

        String signargs = "";
        for (String arg : c.args)
            signargs += arg + " ";

        info(sender, "sign args: " + extraColor + signargs);

        Map<String,String> internalState = c.circuit.getInternalState();
        if (!internalState.isEmpty()) {
            info(sender, "internal state:");
            String internal = "   ";
            for (String key : internalState.keySet())
                internal += infoColor + key + ": " + extraColor + internalState.get(key) + infoColor + ", ";
            
            sender.sendMessage(internal.substring(0, internal.length()-2));
        }
    }
}
