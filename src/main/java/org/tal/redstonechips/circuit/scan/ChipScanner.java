package org.tal.redstonechips.circuit.scan;

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
    
    public ScanParameters scan(ScanParameters params) throws ChipScanException;
}
