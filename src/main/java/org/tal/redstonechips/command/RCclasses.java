
package org.tal.redstonechips.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.CircuitIndex;
import net.eisental.common.page.Pager;

/**
 *
 * @author Tal Eisenberg
 */
public class RCclasses extends RCCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtils.checkPermission(rc, sender, command.getName(), false, true)) return true;
    
        List<CircuitIndex> libs = rc.getCircuitLoader().getCircuitLibraries();
        
        if (libs.isEmpty()) sender.sendMessage(rc.getPrefs().getInfoColor() + "There are no circuit classes installed.");
        else {
            printClassesList(sender, libs);
        }

        return true;
    }

    private void printClassesList(CommandSender sender, List<CircuitIndex> libs) {
        String list = "";        
        List<String> libNames = new ArrayList<String>();
        for (CircuitIndex lib : libs) libNames.add(lib.getName());
        Collections.sort(libNames);
        
        for (String libName : libNames) {
            CircuitIndex lib = findLibrary(libs, libName);
            if (lib==null) continue;
            
            list += "\n";
            ChatColor color = rc.getPrefs().getInfoColor();
            
            List<String> names = new ArrayList<String>();
            for (Class c : lib.getCircuitClasses()) names.add(c.getSimpleName());
            Collections.sort(names);
            
            list += ChatColor.WHITE + lib.getName() + " " + lib.getVersion() + ":\n   ";
            for (String name : names) {
                list += color + name + ", ";
                if (color==rc.getPrefs().getInfoColor()) color = ChatColor.YELLOW;
                else color = rc.getPrefs().getInfoColor();
            }
            list = list.substring(0, list.length()-2) + "\n";
            
        }
        
        
        if (!list.isEmpty()) 
            Pager.beginPaging(sender, "Installed circuit classes", list, rc.getPrefs().getInfoColor(), rc.getPrefs().getErrorColor());
    }
    
    private CircuitIndex findLibrary(List<CircuitIndex> libs, String libName) { 
        for (CircuitIndex lib : libs) 
            if (lib.getName().equals(libName)) return lib;
        
        return null;
    }

}
