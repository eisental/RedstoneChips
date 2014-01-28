package org.redstonechips.paging;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Tal Eisenberg
 */
public class Pager {
    private static final int CHAT_WINDOW_WIDTH = 320;
    private static final int CHAT_STRING_LENGTH = 119;
    private static final char COLOR_CHAR = '\u00A7';
    
    // until a better solution comes by.
    private static final int CHAR_WIDTH = 6;
    private static final int SPACE_WIDTH = 4;
    private static final String TWO_PIXEL_CHARS = "!,.|:'i;";
    private static final String THREE_PIXEL_CHARS = "l'";
    private static final String FOUR_PIXEL_CHARS = "It[]";
    private static final String FIVE_PIXEL_CHARS = "f<>(){}";
    
    public static Map<CommandSender, PageInfo> playerPages = new HashMap<>();

    /**
     * Maximum number of lines that can fit on the minecraft screen.
     */
    public final static int MaxLines = 16;
    
    public static void beginPaging(CommandSender s, String title, String text, ChatColor infoColor, ChatColor errorColor) {
        beginPaging(s, title, text, infoColor, errorColor, MaxLines);
    }

    public static void beginPaging(CommandSender s, String title, String text, ChatColor infoColor, ChatColor errorColor, int maxLines) {
        String lines[];
        if (s instanceof Player)
            lines = wrapText(text);
        else lines = text.split("\n");
        
        beginPaging(s, title, new ArrayLineSource(lines), infoColor, errorColor, maxLines);
    }
    
    public static void beginPaging(CommandSender s, String title, String[] lines, ChatColor infoColor, ChatColor errorColor) {
        beginPaging(s, title, lines, infoColor, errorColor, MaxLines);
    }

    public static void beginPaging(CommandSender s, String title, String[] lines, ChatColor infoColor, ChatColor errorColor, int maxLines) {
        beginPaging(s, title, new ArrayLineSource(lines), infoColor, errorColor, maxLines);
    }
    
    public static void beginPaging(CommandSender s, String title, LineSource src, ChatColor infoColor, ChatColor errorColor) {
        beginPaging(s, title, src, infoColor, errorColor, MaxLines);
    }
        
    public static void beginPaging(CommandSender s, String title, LineSource src, ChatColor infoColor, ChatColor errorColor, int linesPerPage) {
        int pageCount = (int)(Math.ceil(src.getLineCount()/(float)linesPerPage));
        PageInfo pi = new PageInfo(s, title, pageCount, src, linesPerPage, infoColor, errorColor);
        playerPages.put(s, pi);
        page(pi);
    }
    
    public static void page(PageInfo pi) {
        if (pi.page<1 || pi.page>pi.pageCount) {
            pi.sender.sendMessage(pi.errorColor + "Invalid page number: " + pi.page + 
                    ". Expecting 1-" + pi.pageCount);
        } else {
            CommandSender s = pi.sender;
            ChatColor infoColor = pi.infoColor;
            
            s.sendMessage(infoColor + pi.title + ": " + (pi.pageCount>1?"( page " + pi.page + " / " + pi.pageCount  + " )":""));
            s.sendMessage(infoColor + "---------------------------------------------------");
            for (int i=(pi.page-1)*pi.linesPerPage; i<Math.min(pi.src.getLineCount(), pi.page*pi.linesPerPage); i++) {
                s.sendMessage(pi.src.getLine(i));
            }
            s.sendMessage(infoColor + "---------------------------------------------------");
            if (pi.pageCount>1) s.sendMessage("Use " + ChatColor.YELLOW + (s instanceof Player?"/":"") + "rcp [page#|next|prev|last]" + ChatColor.WHITE + " to see other pages.");
        }        
    }
    
    public static void nextPage(CommandSender s) {
        PageInfo pageInfo = findPageInfo(s);
        if (pageInfo!=null) {
            pageInfo.nextPage();        
            page(pageInfo);
        }
    }
    
    public static void previousPage(CommandSender s) {
        PageInfo pageInfo = findPageInfo(s);
        if (pageInfo!=null) {
            pageInfo.prevPage();        
            page(pageInfo);
        }
    }
    
    public static void gotoPage(CommandSender s, int page) {
        PageInfo pageInfo = findPageInfo(s);
        if (pageInfo!=null) {
            pageInfo.gotoPage(page);        
            page(pageInfo);
        }
    }
    
    public static void lastPage(CommandSender s) {
        PageInfo pageInfo = findPageInfo(s);
        if (pageInfo!=null) {
            pageInfo.lastPage();        
            page(pageInfo);
        }
    }
    
    public static boolean hasPageInfo(CommandSender s) {
        return playerPages.containsKey(s);
    }
    
    private static PageInfo findPageInfo(CommandSender s) {
        if (playerPages.containsKey(s))
            return playerPages.get(s);
        else {
            s.sendMessage(ChatColor.RED + "There are no pages to view.");
            return null;
        }
        
    }
    
    private static String[] wrapText(String text) {
        final StringBuilder out = new StringBuilder();

        int lineWidth = 0;
        int lineLength = 0;
        char colorChar = 'f';
        
        // Go over the message char by char.
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch=='\n') {
                lineLength = 0;
                lineWidth = 0;
            }
            
            // Get the color
            if (ch == COLOR_CHAR && i < text.length() - 1) {
                // We might need a linebreak ... so ugly ;(
                if (lineLength + 2 > CHAT_STRING_LENGTH) {
                    out.append('\n');
                    lineLength = 0;
                    if (colorChar != 'f' && colorChar != 'F') {
                        out.append(COLOR_CHAR).append(colorChar);
                        lineLength += 2;
                    }
                }
                colorChar = text.charAt(++i);
                out.append(COLOR_CHAR).append(colorChar);
                lineLength += 2;
                continue;
            }
            
            // See if we need a linebreak
            if (lineLength + 1 > CHAT_STRING_LENGTH || lineWidth + CHAR_WIDTH >= CHAT_WINDOW_WIDTH) {
                out.append('\n');
                lineLength = 0;
                lineWidth = getCharWidth(ch);

                // Re-apply the last color if it isn't the default
                if (colorChar != 'f' && colorChar != 'F') {
                    out.append(COLOR_CHAR).append(colorChar);
                    lineLength += 2;
                }                
            } else {
                lineWidth += getCharWidth(ch);
            }
            out.append(ch);
            lineLength++;
        }

        // Return it split
        return out.toString().split("\n");
    }
    
    private static int getCharWidth(char ch) {
        if (ch==' ') return SPACE_WIDTH;
        else if (TWO_PIXEL_CHARS.indexOf(ch)!=-1) return 2;
        else if (THREE_PIXEL_CHARS.indexOf(ch)!=-1) return 3;
        else if (FOUR_PIXEL_CHARS.indexOf(ch)!=-1) return 4;
        else if (FIVE_PIXEL_CHARS.indexOf(ch)!=-1) return 5;
        else return CHAR_WIDTH;
        
    }
}
