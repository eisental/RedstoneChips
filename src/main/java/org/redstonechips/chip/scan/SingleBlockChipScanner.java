package org.redstonechips.chip.scan;

import org.bukkit.block.Block;
import org.redstonechips.chip.scan.ChipScanner.ChipScanException;

/**
 *
 * @author Tal Eisenberg
 */
public class SingleBlockChipScanner extends IOChipScanner {

    @Override
    public void scan(ChipParameters params) throws ChipScanException {
        Block b = params.origin;
        
        if (b.getType()==params.inputBlockType.getItemType()
                && (b.getData()==params.inputBlockType.getData() || params.inputBlockType.getData()==-1)) {
            addInput(params, b);
        } else if (b.getType()==params.outputBlockType.getItemType()
                && (b.getData()==params.outputBlockType.getData() || params.outputBlockType.getData()==-1)) {
            addOutput(params, b);
        } else if (b.getType()==params.interfaceBlockType.getItemType()
                && (b.getData()==params.interfaceBlockType.getData() || params.interfaceBlockType.getData()==-1)) {
            addInterface(params, b);
        } else throw new ChipScanException("Origin block is not an IO block.");
    }
    
}
