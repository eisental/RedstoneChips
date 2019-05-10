package org.redstonechips.chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.redstonechips.RedstoneChips;
import org.redstonechips.Serializer;
import org.redstonechips.chip.ChipFactory.MaybeChip;
import org.redstonechips.chip.scan.ChipParameters;
import org.redstonechips.util.BooleanArrays;

/**
 * Responsible for turning a {@link org.redstonechips.chip.Chip Chip} object into
 * a Map for serialization, as well as, turning a Map object back into a {@link org.redstonechips.chip.Chip Chip}.
 * 
 * @author taleisenberg
 */
public class ChipSerializer extends Serializer {

    @Override
    public Map<String, Object> serialize(Object o) {
        Map<String, Object> map = new HashMap<>();
        Chip c = (Chip)o;
        
        map.put(Serializer.Key.CLASS.key, c.getType());
        map.put(Serializer.Key.WORLD.key, c.world.getName());
        map.put(Serializer.Key.SIGN.key, makeBlockList(c.activationBlock));
        if (c.inputPins!=null && c.inputPins.length!=0) 
            map.put(Serializer.Key.INPUTS.key, makeIOBlockList(c.inputPins));
        if (c.outputPins!=null && c.outputPins.length!=0) 
            map.put(Serializer.Key.OUTPUTS.key, makeIOBlockList(c.outputPins));
        if (c.interfaceBlocks!=null && c.interfaceBlocks.length!=0) 
            map.put(Serializer.Key.INTERFACES.key, makeIOBlockList(c.interfaceBlocks));
        map.put(Serializer.Key.STRUCTURE.key, makeBlockListsList(c.structure));
        if (c.args!=null && c.args.length!=0) 
            map.put(Serializer.Key.ARGS.key, c.args);

        Map<String,String> state = c.circuit.getInternalState();
        if (state!=null && !state.isEmpty()) 
            map.put(Serializer.Key.STATE.key, c.circuit.getInternalState());

        map.put(Serializer.Key.ID.key, c.id);
        if (c.name!=null) 
            map.put(Serializer.Key.NAME.key, c.name);
        if (c.isDisabled()) 
            map.put(Serializer.Key.DISABLED.key, c.isDisabled());
        if (c.outputPins!=null && c.outputPins.length!=0) 
            map.put(Serializer.Key.OUTPUT_BITS.key, BooleanArrays.toString(c.circuit.outputs, c.outputPins.length));

        return map;

    }

    @Override
    public MaybeChip deserialize(Map<String, Object> map) {
        if (!containsKeys(map, Key.SIGN, Key.WORLD, Key.STRUCTURE)) return null;
        
        Server s = RedstoneChips.inst().getServer();
        World world = findWorld(s, (String)map.get(Key.WORLD.key));        
        Location signBlock = parseLocation(world, (List<Integer>)map.get(Key.SIGN.key));        
        ChipParameters params = ChipParameters.generateDefaultParams(signBlock.getBlock());
        if (params==null) return null;
        
        params.structure = parseLocationList(world, (List<List<Integer>>)map.get(Key.STRUCTURE.key)); 
        
        compileIOBlocks(params, map);

        MaybeChip mChip = ChipFactory.maybeCreateChip(params, null);
        
        if (mChip!=MaybeChip.AChip) return mChip; // fail
        else {
            Chip c = mChip.getChip();
            if (map.containsKey(Key.NAME.key))        c.name = (String)map.get(Key.NAME.key);
            if (map.containsKey(Key.ID.key))          c.id = (Integer)map.get(Key.ID.key);        
            if (map.containsKey(Key.OUTPUT_BITS.key)) updateOutputPins(c, BooleanArrays.fromString((String)map.get(Key.OUTPUT_BITS.key)));
            if (map.containsKey(Key.DISABLED.key))    c.disabled = (Boolean)map.get(Key.DISABLED.key);

            return mChip;
        }
    }   
    
    public static World findWorld(Server server, String worldName) {
        World w = server.getWorld(worldName);

        if (w!=null) return w;
        else throw new IllegalArgumentException("World " + worldName + " was not found on the server.");
    }
    
    private Location parseLocation(World w, List<Integer> coords) {
        return new Location(w, coords.get(0), coords.get(1), coords.get(2));
    }

    private List<Block> parseLocationList(World w, List<List<Integer>> list) {
        List<Block> blocks = new ArrayList<>();
        if(list!=null)
          for (List<Integer> coords : list)
            blocks.add(parseLocation(w, coords).getBlock());

        return blocks;
    }

    private void compileIOBlocks(ChipParameters params, Map<String, Object> map) {
        World w = params.signBlock.getWorld();
        
        if (map.containsKey(Key.INPUTS.key))
            params.inputs = parseLocationList(w, (List<List<Integer>>)map.get(Key.INPUTS.key));
                
        if (map.containsKey(Key.OUTPUTS.key))
            params.outputs = parseLocationList(w, (List<List<Integer>>)map.get(Key.OUTPUTS.key));
        
        if (map.containsKey(Key.INTERFACES.key))
            params.interfaces = parseLocationList(w, (List<List<Integer>>)map.get(Key.INTERFACES.key));        
    }
    
    private boolean containsKeys(Map<String,Object> map, Key... keys) {
        for (Key k : keys)
            if (!map.containsKey(k.key)) return false;
        
        return true;
    }
    
    private void updateOutputPins(Chip c, boolean[] bits) {
        for (int i=0; i<bits.length; i++) {
            c.outputPins[i].forceState(bits[i]);
        }
    }
}
