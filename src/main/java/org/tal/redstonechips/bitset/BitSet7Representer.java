
package org.tal.redstonechips.bitset;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 *
 * @author Tal Eisenberg
 */
public class BitSet7Representer extends Representer {
    public BitSet7Representer() {
        this.representers.put(BitSet7.class, new RepresentBitSet7());
    }

    private class RepresentBitSet7 implements Represent {
        @Override
        public Node representData(Object data) {
            BitSet7 bits = (BitSet7)data;
            long[] longs = bits.toLongArray();

            String value;
            if (longs.length==0) value = "0";
            else if (longs.length==1) value = Long.toString(longs[0]);
            else {
                value = "l";
                for (long l : longs) value += l + ",";
                if (!value.isEmpty() && !value.trim().isEmpty()) value = value.substring(0, value.length()-1);
            }

            return representScalar(new Tag("!b"), value);
        }

    }
}
