
package org.tal.redstonechips.util;

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
                long l = Long.decode(val);
                return BitSet7.valueOf(new long[] {l});
            } catch (NumberFormatException ne) {
                if (val.length()==1) return BitSetUtils.intToBitSet((int)val.charAt(0), 32);
                else if (val.startsWith("b")) {
                    String bits = val.substring(1);
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
