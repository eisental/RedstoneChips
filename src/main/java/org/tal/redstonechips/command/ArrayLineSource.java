package org.tal.redstonechips.command;

/**
 *
 * @author Tal Eisenberg
 */
public class ArrayLineSource implements LineSource {
    String[] lines;
    
    public ArrayLineSource(String[] lines) {
        this.lines = lines;
    }

    @Override
    public String getLine(int idx) {
        return lines[idx];
    }

    @Override
    public float getLineCount() {
        return lines.length;
    }
}
