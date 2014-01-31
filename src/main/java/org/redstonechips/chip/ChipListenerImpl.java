package org.redstonechips.chip;

import org.bukkit.command.CommandSender;

/**
 * A default implementation of {@link ChipListener}. Does nothing. Useful for
 * overriding only a subset of the notification hooks.
 * 
 * @author Tal Eisenberg
 */
public class ChipListenerImpl implements ChipListener {

    @Override
    public void inputChanged(Chip c, int idx, boolean state) {}

    @Override
    public void outputChanged(Chip c, int idx, boolean state) {}

    @Override
    public void chipDisabled(Chip c) {}

    @Override
    public void chipEnabled(Chip c) {}

    @Override
    public void chipShutdown(Chip c) {}

    @Override
    public void chipDestroyed(Chip c, CommandSender destroyer) {}

    @Override
    public void circuitMessage(Chip c, String msg) {}

}
