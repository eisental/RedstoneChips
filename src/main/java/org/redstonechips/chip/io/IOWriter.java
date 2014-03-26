
package org.redstonechips.chip.io;

import org.redstonechips.circuit.Circuit;

/**
 *
 * @author taleisenberg
 */
public interface IOWriter {
    public void writeOut(Circuit circuit, boolean state, int index);
}
