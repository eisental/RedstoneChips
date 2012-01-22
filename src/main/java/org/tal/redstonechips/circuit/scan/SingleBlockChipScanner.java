/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tal.redstonechips.circuit.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.tal.redstonechips.circuit.scan.ChipScanner.ChipScanException;
import org.tal.redstonechips.circuit.io.IOBlock;

/**
 *
 * @author Tal Eisenberg
 */
public class SingleBlockChipScanner extends IOChipScanner {

    @Override
    public ScanParameters scan(ScanParameters params) throws ChipScanException {
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
        
        return params;
    }
    
}
