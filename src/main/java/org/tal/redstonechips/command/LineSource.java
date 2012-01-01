package org.tal.redstonechips.command;

/**
 * Provides pageMaker with content lines.
 * 
 * @author Tal Eisenberg
 */
public interface LineSource {

    /**
     * Requests one line from the line source.
     * 
     * @param idx Line number.
     * @return a line.
     */
    public String getLine(int idx);

    /**
     * 
     * @return Total number of available lines.
     */
    public float getLineCount();
}
