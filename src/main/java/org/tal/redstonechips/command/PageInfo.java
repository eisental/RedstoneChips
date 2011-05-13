/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.redstonechips.command;

import org.bukkit.ChatColor;

/**
 *
 * @author Tal Eisenberg
 */
public class PageInfo {
    public int page = 1;
    public int pageCount;
    public String[] lines;
    public String title;
    public ChatColor infoColor, errorColor;
    public int linesPerPage;

    PageInfo(String title, int pageCount, String[] lines, int linesPerPage, ChatColor infoColor, ChatColor errorColor) {
        this.pageCount = pageCount;
        this.lines = lines;
        this.linesPerPage = linesPerPage;
        this.title = title;
        this.infoColor = infoColor;
        this.errorColor = errorColor;
    }
}
