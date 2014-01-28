package org.redstonechips.command.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.Chip;
import org.redstonechips.command.CommandUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class WorldFilter implements ChipFilter {
    public World world;

    @Override
    public void parse(CommandSender sender, String[] string) throws IllegalArgumentException {
        if (string.length==1) {
            String sworld = string[0];
            if (sworld.equalsIgnoreCase("this")) {
                Player p = CommandUtils.enforceIsPlayer(sender, false);
                if (p==null) throw new IllegalArgumentException("Command sender must be a player when using `this`.");
                else world = p.getWorld();
            } else {                
                world = RedstoneChips.inst().getServer().getWorld(string[0]);
            }
            
            if (world==null)
                throw new IllegalArgumentException("Unknown world: " + string[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : string) sb.append(s);
            throw new IllegalArgumentException("Bad world filter: " + sb.toString() + ". Expecting 'world: <world-name>'.");
        }
    }

    @Override
    public Collection<Chip> filter(Collection<Chip> circuits) {
        List<Chip> filtered = new ArrayList<>();

        for (Chip chip : circuits) {
            if (chip.world.getName().equals(world.getName())) filtered.add(chip);
        }

        return filtered;
    }    
}
