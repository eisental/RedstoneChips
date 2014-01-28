package org.redstonechips;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks if the current plugin version matches the version on the RedstoneChips website.
 * 
 * @author Tal Eisenberg
 */
public class UpdateChecker {
    /** URL to the remote currentversion file. */
    public static URL currentversionURL;
    
    static {
        try {
            currentversionURL = new URL("http://eisental.github.com/RedstoneChips/currentversion");
        } catch (MalformedURLException ex) {
            Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks for a new RedstoneChips version.
     * 
     * @param currentVersion The version string of the plugin.
     * @return The new version string or null if there is none.
     * @throws IOException When a network error occurs.
     */
    public static String checkUpdate(String currentVersion) throws IOException {
        String inputLine;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(currentversionURL.openStream()))) {
            inputLine = in.readLine().trim().toLowerCase();
        }
        
        if (inputLine!=null && !inputLine.isEmpty() && !currentVersion.equals(inputLine)) return inputLine;
        else return null;                
    }
    
}
