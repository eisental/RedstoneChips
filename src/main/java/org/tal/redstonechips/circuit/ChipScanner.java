package org.tal.redstonechips.circuit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.tal.redstonechips.circuit.io.IOBlock;
import org.tal.redstonechips.util.Locations;

/**
 * A class for scanning chip block structures.
 * 
 * @author Tal Eisenberg
 */
public interface ChipScanner {    
    public static class ChipScanException extends RuntimeException {
        public ChipScanException(String string) {
            super(string);
        }
    }
    
    public ScanParameters scan(Block signBlock) throws ChipScanException;
}
