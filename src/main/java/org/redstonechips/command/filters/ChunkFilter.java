package org.redstonechips.command.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.chip.Chip;
import org.redstonechips.util.ChunkLocation;

/**
 *
 * @author Tal Eisenberg
 */
public class ChunkFilter implements ChipFilter {
    ChunkLocation chunk;

    @Override
    public Collection<Chip> filter(Collection<Chip> chips) {
        List<Chip> filtered = new ArrayList<>();

        for (Chip chip : chips) {
            for (ChunkLocation c : chip.chunks)
                if (c.getX()==chunk.getX() && c.getZ()==chunk.getZ()) filtered.add(chip);
        }

        return filtered;
    }

    @Override
    public void parse(CommandSender sender, String[] string) throws IllegalArgumentException {
        if (string.length==1 && string[0].equalsIgnoreCase("this") && (sender instanceof Player)) {
            chunk = ChunkLocation.fromLocation(((Player)sender).getLocation());
        } else if (string.length==2) {
            try {
                int x = Integer.decode(string[0]);
                int z = Integer.decode(string[1]);
                chunk = new ChunkLocation(x,z,null);
            } catch (NumberFormatException ne) {
                StringBuilder sb = new StringBuilder();
                for (String s : string) sb.append(s);

                throw new IllegalArgumentException("Bad chunk filter: " + sb.toString() + ". Expecting 'chunk: <x>,<z>'.");
            }
        } else  {
            StringBuilder sb = new StringBuilder();
            for (String s : string) sb.append(s);

            throw new IllegalArgumentException("Bad chunk filter: " + sb.toString() + ". Expecting 'chunk: <x>,<z>'.");
        }

    }    
}
