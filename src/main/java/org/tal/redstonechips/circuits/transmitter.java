package org.tal.redstonechips.circuits;


import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.tal.redstonechips.RCPlugin;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class transmitter extends Circuit {
    private List<receiver> receivers = new ArrayList<receiver>();
    private String channel;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        for (receiver r : receivers) {
            r.receive(inputBits);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (args.length>0) {
            channel = args[0];

            // register the transmitter
            RCPlugin.transmitters.add(this);

            // find already existing receivers for this channel
            for (receiver r : RCPlugin.receivers) {
                if (r.getChannel()!=null && r.getChannel().equals(channel)) addReceiver(r);
            }

            return true;
        } else {
            player.sendMessage("Channel name is missing.");
            return false;
        }
    }

    public void addReceiver(receiver r) { 
        System.out.println("adding receiver: " + r + " (" + r.getChannel() + ")");
        receivers.add(r);
    }

    public void removeReceiver(receiver r) {
        System.out.println("removing receiver: " + r + " (" + r.getChannel() + ")");
        receivers.remove(r);
    }

    public String getChannel() { return channel; }

    @Override
    public void circuitDestroyed() {
        RCPlugin.transmitters.remove(this);
    }
}
