/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.commands;

import org.bukkit.command.CommandExecutor;
import org.tal.redstonechips.RedstoneChips;

/**
 *
 * @author Tal Eisenberg
 */
public abstract class RCCommand implements CommandExecutor {
    protected RedstoneChips rc;

    public void setRCInstance(RedstoneChips rc) { this.rc = rc; }

}
