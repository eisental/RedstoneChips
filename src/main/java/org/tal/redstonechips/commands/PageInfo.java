/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.commands;

/**
 *
 * @author Tal Eisenberg
 */
public class PageInfo {
    public int page = 1;
    public String lastCommandName;
    public String[] lastArgs;
    public int pageCount;

    PageInfo(String commandName, String[] args, int pageCount) {
        this.lastCommandName = commandName;
        this.lastArgs = args;
        this.pageCount = pageCount;
    }

    boolean isNewCommand(String commandName, String[] args) {
        if (!commandName.equalsIgnoreCase(lastCommandName)) return true;
        if (lastArgs==null && args!=null) return false;
        if (args.length!=lastArgs.length) return true;
        
        for (int i=0; i<args.length; i++) if (!args[i].equalsIgnoreCase(lastArgs[i])) return true;

        return false;
    }
}
