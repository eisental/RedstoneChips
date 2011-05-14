/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Tal Eisenberg
 */
public class DebugSettings {
    private List<Circuit> debuggedCircuits;
    private List<Circuit> ioDebuggedCircuits;
    private boolean paused;
    private CommandSender debugger;

    public DebugSettings(CommandSender debugger) {
        debuggedCircuits = new ArrayList<Circuit>();
        ioDebuggedCircuits = new ArrayList<Circuit>();

        this.debugger = debugger;
        paused = false;
    }

    public void setPaused(boolean paused) {
        if (this.paused==paused) return;
        this.paused = paused;

        if (paused) {
            try {
                for (Circuit c : debuggedCircuits) c.removeDebugger(debugger);
                for (Circuit c : ioDebuggedCircuits) c.removeIODebugger(debugger);
            } catch (IllegalArgumentException ie) { }
        } else {
            try {
                for (Circuit c : debuggedCircuits) c.addDebugger(debugger);
                for (Circuit c : ioDebuggedCircuits) c.addIODebugger(debugger);
            } catch (IllegalArgumentException ie) { }
        }
    }
}
