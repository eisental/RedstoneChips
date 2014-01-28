package org.redstonechips.util;

import java.util.StringTokenizer;
import org.bukkit.block.Sign;

/**
 *
 * @author taleisenberg
 */
public class Signs {

    public static void writeSignArgs(Sign sign, String[] args) {
        String line = ""; 
        int curLine = 1;
        
        for (String a : args) {
            String added = line + " " + a;
            if (added.length()>13 && curLine!=3) {
                sign.setLine(curLine, line);
                line = a;
                curLine++;
            } else line = added;
        }

        sign.setLine(curLine, line);

        if (curLine<3)
            for (int i=curLine+1; i<4; i++) sign.setLine(i, "");
    }
    
    public static void writeChipSign(Sign sign, String type, String[] args) {
        sign.setLine(0, type);
        writeSignArgs(sign, args);
        sign.update();
    }

    public static String[] readArgsFromSign(Sign sign) {
        String sargs = (sign.getLine(1) + " " + sign.getLine(2) + " " + sign.getLine(3)).replaceAll("\\xA7\\d", "");
        StringTokenizer t = new StringTokenizer(sargs);
        String[] args = new String[t.countTokens()];
        int i = 0;
        while (t.hasMoreElements()) {
            args[i] = t.nextToken();
            i++;
        }
        return args;
    }

    public static String readClassFromSign(Sign sign) {
        String line = sign.getLine(0);
        line = line.replaceAll("\\xA7\\d", "");
        return line.trim();
    }
}
