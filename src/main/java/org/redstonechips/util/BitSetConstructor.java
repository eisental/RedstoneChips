
package org.redstonechips.util;

import java.math.BigInteger;
import java.util.BitSet;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * Parses !b tag yaml values into BitSet. The following values will be converted:
 * 
 * a decimal big integer - !b '2456789272879238976' 
 * a hexadecimal big integer with '0x' prefix - !b '0x1FFFFFFFFFFFFFFF' 
 * a binary value with '0b' or 'b' prefix - !b '0b11101010101110101' 
 * a list of longs with 'l' prefix - !b 'l12334,13425,2353452679,342'
 *
 * All values are unlimited in size.
 * 
 * @author Tal Eisenberg
 */
public class BitSetConstructor extends Constructor {
    public BitSetConstructor() {
        this.yamlConstructors.put(new Tag("!b"), new ConstructBitSet());
    }

    private class ConstructBitSet extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            String val = (String) constructScalar((ScalarNode)node);
            if (val.isEmpty()) return new BitSet();

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
                    return BitSet.valueOf(longs);
                    
                } else {
                    return null;
                }
            }

        }
    }
}
