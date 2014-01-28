
package org.redstonechips.util;

import java.util.BitSet;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Converts a BitSet into a YAML !b tag value.
 * The BitSet is converted to a BigInteger that is printed as a decimal integer string.
 * 
 * @author Tal Eisenberg
 */
public class BitSetRepresenter extends Representer {
    public BitSetRepresenter() {
        this.representers.put(BitSet.class, new RepresentBitSet7());
    }

    private class RepresentBitSet7 implements Represent {
        @Override
        public Node representData(Object data) {
            BitSet bits = (BitSet)data;
            String value = BitSetUtils.bitSetToBigInt(bits).toString();
            return representScalar(new Tag("!b"), value);
        }

    }
}
