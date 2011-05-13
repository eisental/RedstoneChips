
package org.tal.redstonechips.command;

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
