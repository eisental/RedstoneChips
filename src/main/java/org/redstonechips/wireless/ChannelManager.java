package org.redstonechips.wireless;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.redstonechips.RedstoneChips;
import org.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class ChannelManager {
    private final RedstoneChips rc;
    
    private final Map<String, BroadcastChannel> broadcastChannels = new HashMap<>();
    
    public ChannelManager(RedstoneChips rc) {
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
        channel.addWirelessDevice(r);

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
        channel.addWirelessDevice(t);

        return channel;
    }

    /**
     * Removes the transmitter or receiver from the list and removes its channel if it's deserted and unprotected.
     * 
     * @param w
     * @return true if the transmitter was actually removed.
     */
    public boolean removeWireless(Wireless w) {
        BroadcastChannel channel = w.getChannel();
        if (channel==null) return false;
        
        boolean res;
        res = channel.removeWirelessDevice(w);
        
        if (res && channel.isDeserted() && !channel.isProtected())
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
        List<Wireless> list = new ArrayList<>();
        
        for (BroadcastChannel c : broadcastChannels.values()) {
            for (Wireless w : c.getWirelessDevices())
                if (w.getCircuit()==circuit) list.add(w);
        }
        
        return list;
    }
    
    public boolean removeCircuitWireless(Circuit circuit) {
        List<Wireless> list = getCircuitWireless(circuit);
        if (list.isEmpty()) return false;
        else {
            for (Wireless w : list)
                removeWireless(w);
        }
        
        return true;
    }
}
