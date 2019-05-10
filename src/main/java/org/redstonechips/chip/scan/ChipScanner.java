package org.redstonechips.chip.scan;

/**
 * A class for scanning chip structures.
 * 
 * @author Tal Eisenberg
 */
public interface ChipScanner {    
    @SuppressWarnings("serial")
	public static class ChipScanException extends RuntimeException {
        public ChipScanException(String string) {
            super(string);
        }
    }
    
    /**
     * Scan a chip structure according to the specified parameters.
     * 
     * @param params An initialized ScanParameters object. 
     */
    public void scan(ChipParameters params) throws ChipScanException;
}
