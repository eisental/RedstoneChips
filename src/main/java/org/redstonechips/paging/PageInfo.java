package org.redstonechips.paging;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
    public CommandSender sender;
    
    public PageInfo(CommandSender sender, String title, int pageCount, LineSource src, int linesPerPage, ChatColor infoColor, ChatColor errorColor) {
        this.pageCount = pageCount;
        this.src = src;
        this.linesPerPage = linesPerPage;
        this.title = title;
        this.infoColor = infoColor;
        this.errorColor = errorColor;
        this.sender = sender;
    }

    public void nextPage() {
        if (page<pageCount)
            page = page + 1;
        else {
            page = 1;
        }        

    }

    public void prevPage() {
        if (page>1)
            page = page - 1;
        else page = pageCount;        
    }
    
    public void lastPage() {
        page = pageCount;        
    }

    void gotoPage(int page) {
        this.page = page;
    }
}
