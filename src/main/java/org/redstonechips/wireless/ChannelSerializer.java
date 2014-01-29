
package org.redstonechips.wireless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.redstonechips.RedstoneChips;
import org.redstonechips.Serializer;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author taleisenberg
 */
public class ChannelSerializer extends Serializer {

    @Override
    public Map<String, Object> serialize(Object o) {
        BroadcastChannel chan = (BroadcastChannel)o;
        Map<String, Object> map = new HashMap<>();
        map.put(Key.CHAN_NAME.key, chan.name);
        map.put(Key.CHAN_STATE.key, BooleanArrays.toString(chan.bits));
        map.put(Key.CHAN_OWNERS.key, chan.owners);
        map.put(Key.CHAN_USERS.key, chan.users);
        return map;
    }

    @Override
    public BroadcastChannel deserialize(Map<String, Object> map) {
        BroadcastChannel channel;
        channel = RedstoneChips.inst().channelManager().getChannelByName((String)map.get(Key.CHAN_NAME.key), true);
        if (map.containsKey(Key.CHAN_STATE.key)) 
            channel.bits = BooleanArrays.fromString((String)map.get(Key.CHAN_STATE.key));
        
        channel.owners = (List<String>)map.get(Key.CHAN_OWNERS.key);
        channel.users = (List<String>)map.get(Key.CHAN_USERS.key);
        channel.transmit(channel.bits, 0, channel.getLength());        
        return channel;
    }
}
