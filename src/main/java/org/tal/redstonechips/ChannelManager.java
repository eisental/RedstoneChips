package org.tal.redstonechips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.wireless.BroadcastChannel;
import org.tal.redstonechips.wireless.Receiver;
import org.tal.redstonechips.wireless.Transmitter;
import org.tal.redstonechips.wireless.Wireless;

/**
 *
 * @author Tal Eisenberg
 */
public class ChannelManager {
    private final RedstoneChips rc;
    
    private final Map<String, BroadcastChannel> broadcastChannels = new HashMap<String, BroadcastChannel>();
    
    ChannelManager(RedstoneChips rc) {
        this.rc = rc;
    }
    
    /**
     * Adds the receiver as listener on a channel and returns the BroadcastChannel object that the receiver
     * was added to. If a BroadcastChannel by that name was not found a new one is created.
     * 
     * @param r 
     * @param channelName 
     * @return The channel that the receiver was added to.
     */
    public BroadcastChannel registerReceiver(final Receiver r, String channelName) {
        boolean exists = broadcastChannels.containsKey(channelName);

        final BroadcastChannel channel = getChannelByName(channelName, true);
        channel.addReceiver(r);

        if (exists) {
            rc.getServer().getScheduler().scheduleSyncDelayedTask(rc, new Runnable() {
                @Override
                public void run() {
                    channel.updateReceiver(r);
                }
            });
        } 

        return channel;        
    }
    
    /**
     * Registers the transmitter to a channel and returns the BroadcastChannel object that the transmitter
     * was added to. If a BroadcastChannel by that name was not found a new one is created.
     *
     * @param t
     * @param channelName
     * @return The channel that the receiver was added to.
     */
    public BroadcastChannel registerTransmitter(Transmitter t, String channelName) {
        BroadcastChannel channel = getChannelByName(channelName, true);
        channel.addTransmitter(t);

        return channel;
    }

    /**
     * Removes this transmitter from the list and removes its channel if it's deserted.
     * 
     * @param t
     * @return true if the transmitter was actually removed.
     */
    public boolean removeTransmitter(Transmitter t) {
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
     * @param r
     * @return true if the receiver was actually removed.
     */    
    public boolean removeReceiver(Receiver r) {
        BroadcastChannel channel = r.getChannel();
        if (channel==null) return false;
        
        boolean res = channel.removeReceiver(r);
        if (channel.isDeserted() && !channel.isProtected())
            broadcastChannels.remove(channel.name);

        return res;
    }

    /**
     * Finds a BroadcastChannel with the specified name.
     * 
     * @param name
     * @param create If True creates a new channel when no channel is found.
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

    /**
     * 
     * @return All broadcast channels running on the server.
     */
    public Map<String, BroadcastChannel> getBroadcastChannels() {
        return broadcastChannels;
    }

    /**
     * Finds all Wireless objects (Receivers or Transmitters) that are associated with a circuit.
     * 
     * @param circuit The circuit to match
     * @return A list of Wireless objects that are associated with circuit.
     */
    public List<Wireless> getCircuitWireless(Circuit circuit) {
        List<Wireless> list = new ArrayList<Wireless>();
        
        for (BroadcastChannel c : broadcastChannels.values()) {
            for (Wireless w : c.getReceivers())
                if (w.getCircuit()==circuit) list.add(w);
            
            for (Wireless w : c.getTransmitters())
                if (w.getCircuit()==circuit) list.add(w);
        }
        
        return list;
    }
}
