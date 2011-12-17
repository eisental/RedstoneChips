/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips;

import java.util.HashMap;
import java.util.Map;
import org.tal.redstonechips.channel.BroadcastChannel;
import org.tal.redstonechips.channel.ReceivingCircuit;
import org.tal.redstonechips.channel.TransmittingCircuit;

/**
 *
 * @author Tal Eisenberg
 */
public class ChannelManager {
    private RedstoneChips rc;
    
    private Map<String, BroadcastChannel> broadcastChannels = new HashMap<String, BroadcastChannel>();
    
    ChannelManager(RedstoneChips rc) {
        this.rc = rc;
    }
    
    /**
     * Adds the receiving circuit to listen on a channel and returns the BroadcastChannel object that the receiver
     * was added to. If a BroadcastChannel by that name was not found a new one is created.
     * 
     * @param r The receiving circuit.
     * @param channelName Name of the receiver's channel.
     * @return The channel that the receiver was added to.
     */
    public BroadcastChannel registerReceiver(final ReceivingCircuit r, String channelName) {
        final BroadcastChannel channel = getChannelByName(channelName, true);
        channel.addReceiver(r);

        rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, new Runnable() {
            @Override
            public void run() {
                channel.updateReceiver(r);
            }
        });

        return channel;
    }

    /**
     * Adds the transmitter circuit to a channel and returns the BroadcastChannel object that the transmitter
     * was added to. If a BroadcastChannel by that name was not found a new one is created.
     *
     * @param r The receiving circuit.
     * @param channelName Name of the receiver's channel.
     * @return The channel that the receiver was added to.
     */
    public BroadcastChannel registerTransmitter(TransmittingCircuit t, String channelName) {
        BroadcastChannel channel = getChannelByName(channelName, true);
        channel.addTransmitter(t);

        return channel;
    }

    /**
     * Removes this transmitter from the list and removes its channel if it's deserted.
     * 
     * @param t a transmitting circuit.
     * @return true if the transmitter was actually removed.
     */
    public boolean removeTransmitter(TransmittingCircuit t) {
        BroadcastChannel channel = t.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeTransmitter(t);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }

    /**
     * Removes this receiver from the list and removes its channel if it's deserted.
     * 
     * @param r a receiving circuit.
     * @return true if the receiver was actually removed.
     */    
    public boolean removeReceiver(ReceivingCircuit r) {
        BroadcastChannel channel = r.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeReceiver(r);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }

    /**
     * If the named channel doesn't exist and create is true, a new channel is created.
     * 
     * @param Channel name
     * @param boolean create
     * @return a BroadcastChannel instance representing the named channel. 
     */
    public BroadcastChannel getChannelByName(String name, boolean create) {
        BroadcastChannel channel = null;
        if (broadcastChannels.containsKey(name))
            channel = broadcastChannels.get(name);
        else if (create) {
            channel = new BroadcastChannel(name);
            broadcastChannels.put(name, channel);
        }

        return channel;
    }

    public Map<String, BroadcastChannel> getBroadcastChannels() {
        return broadcastChannels;
    }
    
}
