
package org.redstonechips.command;

import java.io.IOException;
import java.util.ConcurrentModificationException;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redstonechips.RCPersistence;
import org.redstonechips.chip.Chip;
import org.redstonechips.memory.Memory;
import org.redstonechips.memory.Ram;

/**
 *
 * @author Tal Eisenberg
 */
public class RCload extends RCCommand {    
    @Override
    public boolean isOpRequired() { return true; }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
      if (args.length==0) {
    	for (World world : rc.getServer().getWorlds())
            RCPersistence.loadChipsOf(world);
        
        if (sender instanceof Player)
            info(sender, "Done loading " + rc.chipManager().getAllChips().size() + " chip(s). Note: Errors and warnings are only printed to the server console.");
        else info(sender, "Done loading " + rc.chipManager().getAllChips().size() + " chip(s).");
      }
      else {
    	  for (int i=0; i<args.length; i++) {
    		  try {
    			  if (Memory.isValidId(args[i])) {
    				  if (Memory.checkFile(args[i])) {
    					  Memory.reGetMemory(args[i], Ram.class);
    					
    					 try {
    					  for (Chip c : rc.chipManager().getAllChips().values()) {
    				        	for (int j=0; j<c.args.length; j++) {
    				        		if (c.args[i].equals(args[i])) {
    				        			rc.chipManager().resetChip(c, sender);
    				        			info(sender, "Memory File: " + args[i] + ".mem for " + c.toString() + " loaded.");
    				        		}
    				        	}
    				        }
    				      }
    				  
    				      catch (ConcurrentModificationException cm) {}
    				  }
    				  else info(sender, "Memory File: " + args[i] + ".mem not found.");
    			  }
    			  else info(sender, "Invalid memory id: " + args[i]);
    		  }
    		  catch (IOException io) {
    			  info(sender, "Memory File " + args[i] + ".mem not found.");
    		  }
    	  }
      }
    }
}
