
package org.tal.redstonechips.bitset;

import java.math.BigInteger;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 *
 * @author Tal Eisenberg
 */
public class BitSet7Constructor extends Constructor {
    public BitSet7Constructor() {
        this.yamlConstructors.put(new Tag("!b"), new ConstructBitSet7());
    }

    private class ConstructBitSet7 extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            String val = (String) constructScalar((ScalarNode)node);
            if (val.isEmpty()) return new BitSet7();

            try {
                BigInteger i = new BigInteger(val);
                return BitSetUtils.bigIntToBitSet(i);
            } catch (NumberFormatException ne) {
                if (val.length()==1) return BitSetUtils.intToBitSet((int)val.charAt(0), 32);
                else if (val.startsWith("0x")) {
                    try {
                        BigInteger i = new BigInteger(val.substring(2), 16);
                        return BitSetUtils.bigIntToBitSet(i);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else if (val.startsWith("b") || val.startsWith("0b")) {
                    int s = (val.startsWith("b")?1:2);
                    String bits = val.substring(s);
                    return BitSetUtils.stringToBitSet(bits);

                } else if (val.startsWith("l")) {
                    String[] split = val.split(",");
                    long[] longs = new long[split.length];
                    for (int i=0; i<longs.length; i++) {
                        try {
                            longs[i] = Long.decode(split[i]);
                        } catch (NumberFormatException ne2) {
                            return null;
                        }
                    }
                    return BitSet7.valueOf(longs);
                    
                } else {
                    return null;
                }
            }

        }
    }
}
