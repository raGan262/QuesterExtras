package me.ragan262.questerextras.items;

import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class Qbook extends Qitem {
	
	protected String title = "";
	protected String author = "";
	protected List<String> pages = new ArrayList<String>();
	
	@Override
	public String getType() {
		return "BOOK";
	}
	
	public boolean isBook() {
		return (material == Material.WRITTEN_BOOK) || (material == Material.BOOK_AND_QUILL);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void clearPages() {
		this.pages.clear();
	}
	
	public boolean swapPages(int page1, int page2) {
		try {
			String temp = this.pages.get(page1);
			this.pages.set(page1, this.pages.get(page2));
			this.pages.set(page2, temp);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public boolean movePage(int from, int to) {
		if(from < 0 || from >= pages.size()
				|| to < 0 || to >= pages.size()) {
			return false;
		}
		Util.moveListUnit(this.pages, from, to);
		return true;
	}
	
	public boolean setPage(int page, String pageText) {
		try {
			this.pages.set(page, pageText);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public boolean addToPage(int page, String text) {
		try {
			String old = this.pages.get(page);
			this.pages.set(page, old + " " + text);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public void addPage(String pageText) {
		this.pages.add(pageText);
	}
	
	public boolean removePage(int page) {
		try {
			this.pages.remove(page);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public List<String> getPages() {
		return pages;
	}
	
	@Override
	public String getInfo(String indent) {
		StringBuilder sb = new StringBuilder(super.getInfo(indent));
		if(!title.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Title: ").append(ChatColor.RESET).append(title);
		}
		if(!author.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Author: ").append(ChatColor.RESET).append(author);
		}
		if(!pages.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Pages: ");
			for(String s : pages) {
				sb.append('\n').append(indent).append("  ").append(ChatColor.RESET).append(s);
			}
		}
		return sb.toString();
	}
	
	@Override
	Qitem getNewObject() {
		return new Qbook();
	}
	
	@Override
	void copyValues(Qitem item) {
		super.copyValues(item);
		if(item instanceof Qbook) {
			Qbook book = (Qbook) item;
			book.setAuthor(author);
			book.setTitle(title);
			for(String p : pages) {
				book.addPage(p);
			}
		}
	}
	
	@Override
	public ItemStack getItemStack() {
		ItemStack is = super.getItemStack();
		if(isBook()) {
			BookMeta bm = (BookMeta) is.getItemMeta();
			if(!title.isEmpty()) {
				bm.setTitle(title);
			}
			if(!author.isEmpty()) {
				bm.setAuthor(author);
			}
			if(!pages.isEmpty()) {
				bm.setPages(pages);
			}
			is.setItemMeta(bm);
		}
		return is;
	}
	
	@Override
	public void serializeKey(StorageKey key) {
		super.serializeKey(key);
		if(!title.isEmpty()) {
			key.setString("title", title);
		}
		if(!author.isEmpty()) {
			key.setString("author", author);
		}
		if(!pages.isEmpty()) {
			key.setString("pages", Qitem.saveList(pages));
		}
	}
	
	static Qitem loadKey(StorageKey key) {
		Qbook book = new Qbook();
		book.setTitle(key.getString("title", ""));
		book.setAuthor(key.getString("author", ""));
		for(String s : Qitem.loadList(key.getString("pages", ""))) {
			book.addPage(s);
		}
		return book;
	}
}
