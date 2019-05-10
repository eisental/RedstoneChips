package org.redstonechips.chip.scan;

import org.bukkit.block.Block;

/**
 *
 * @author Tal Eisenberg
 */
public class SingleBlockChipScanner extends IOChipScanner {

    @Override
    public void scan(ChipParameters params) throws ChipScanException {
        Block b = params.origin;
        
        if (b.getType()==params.inputBlockType) {
            addInput(params, b);
        } else if (b.getType()==params.outputBlockType) {
            addOutput(params, b);
        } else if (b.getType()==params.interfaceBlockType) {
            addInterface(params, b);
        } else throw new ChipScanException("Origin block is not an IO block.");
    }
    
}
