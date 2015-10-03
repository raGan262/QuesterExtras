package me.ragan262.questerextras.commands;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.annotations.CommandLabels;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.items.Items;
import me.ragan262.questerextras.items.Qbook;
import me.ragan262.questerextras.items.Qitem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class PageCommands {

	Items items = null;
	
	public PageCommands(QrExtras plugin) {
		items = plugin.items;
	}
	
	@CommandLabels({"add", "a"})
	@Command(
			desc = "adds page",
			min = 1,
			max = 2,
			usage = "<item ID> [text]")
	public void add(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			String text = "";
			if(context.length() > 1) {
				text = context.getString(1);
			}
			((Qbook)item).addPage(text);
			sender.sendMessage(ChatColor.GREEN + "Page added.");
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}
	
	@CommandLabels({"remove", "r"})
	@Command(
			desc = "removes book page",
			min = 2,
			max = 2,
			usage = "<item ID> <page ID|all>")
	public void remove(QuesterCommandContext context, CommandSender sender)
			throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			boolean status = true;
			if(context.getString(1).equalsIgnoreCase("ALL")) {
				((Qbook)item).clearPages();
				sender.sendMessage(ChatColor.GREEN + "All pages removed.");
				return;
			}
			else {
				status = ((Qbook)item).removePage(context.getInt(1));
			}
			if(status) {
				sender.sendMessage(ChatColor.GREEN + "Page removed.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Page does not exist.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}

	@CommandLabels({"set", "s"})
	@Command(
			desc = "sets page text",
			min = 2,
			max = 3,
			usage = "<item ID> <page ID> [text]")
	public void set(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			String text = "";
			if(context.length() > 2) {
				text = context.getString(2);
			}
			boolean status = true;
			status = ((Qbook)item).setPage(context.getInt(1), text);
			if(status) {
				sender.sendMessage(ChatColor.GREEN + "Page set.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Page does not exist.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}

	@CommandLabels({"addto", "at"})
	@Command(
			desc = "adds text to page",
			min = 3,
			max = 3,
			usage = "<item ID> <page ID> <text>")
	public void addto(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			String text = "";
			if(context.length() > 2) {
				text = context.getString(2);
			}
			boolean status = true;
			status = ((Qbook)item).addToPage(context.getInt(1), text);
			if(status) {
				sender.sendMessage(ChatColor.GREEN + "Page content added.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Page does not exist.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}
	
	@CommandLabels({"swap"})
	@Command(
			desc = "swaps pages",
			min = 3,
			max = 3,
			usage = "<item ID> <page ID> <page2 ID>")
	public void swap(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			boolean status = ((Qbook)item).swapPages(context.getInt(1), context.getInt(2));
			if(status) {
				sender.sendMessage(ChatColor.GREEN + "Pages swapped.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Page does not exist.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}
	
	@CommandLabels({"move"})
	@Command(
			desc = "move page",
			min = 3,
			max = 3,
			usage = "<item ID> <from ID> <to ID>")
	public void move(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			boolean status = ((Qbook)item).movePage(context.getInt(1), context.getInt(2));
			if(status) {
				sender.sendMessage(ChatColor.GREEN + "Page moved.");
			}
			else {
				sender.sendMessage(ChatColor.RED + "Page does not exist.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}
}
