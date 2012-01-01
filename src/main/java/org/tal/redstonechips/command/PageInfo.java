
package org.tal.redstonechips.command;

import org.bukkit.ChatColor;

/**
 *
 * @author Tal Eisenberg
 */
public class PageInfo {
    public int page = 1;
    public int pageCount;
    public LineSource src;
    public String title;
    public ChatColor infoColor, errorColor;
    public int linesPerPage;

    PageInfo(String title, int pageCount, LineSource src, int linesPerPage, ChatColor infoColor, ChatColor errorColor) {
        this.pageCount = pageCount;
        this.src = src;
        this.linesPerPage = linesPerPage;
        this.title = title;
        this.infoColor = infoColor;
        this.errorColor = errorColor;
    }
}
