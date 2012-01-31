package org.tal.redstonechips.circuit.scan;

/**
 * A class for scanning chip structures.
 * 
 * @author Tal Eisenberg
 */
public interface ChipScanner {    
    public static class ChipScanException extends RuntimeException {
        public ChipScanException(String string) {
            super(string);
        }
    }
    
    /**
     * Scan a chip structure according to the specified parameters.
     * 
     * @param params An initialized ScanParameters object.
     * @return ScanParameters object containing all discovered information from the scan.
     * @throws org.tal.redstonechips.circuit.scan.ChipScanner.ChipScanException 
     */
    public ScanParameters scan(ScanParameters params) throws ChipScanException;
}
