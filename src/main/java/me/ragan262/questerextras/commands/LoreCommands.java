package me.ragan262.questerextras.commands;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.annotations.CommandLabels;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.items.Items;
import me.ragan262.questerextras.items.Qitem;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class LoreCommands {

	Items items = null;
	
	public LoreCommands(QrExtras plugin) {
		items = plugin.items;
	}

	
	@CommandLabels({"add", "a"})
	@Command(
			desc = "adds lore line",
			min = 1,
			max = 2,
			usage = "<item ID> [text]")
	public void add(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		String text = "";
		if(context.length() > 1) {
			text = context.getString(1);
		}
		item.addLore(text);
		sender.sendMessage(ChatColor.GREEN + "Lore added.");
	}
	
	@CommandLabels({"remove", "r"})
	@Command(
			desc = "removes lore line",
			min = 2,
			max = 2,
			usage = "<item ID> <line|all>")
	public void remove(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		boolean status = true;
		if(context.getString(1).equalsIgnoreCase("ALL")) {
			item.removeLore();
		}
		else {
			status = item.removeLoreLine(context.getInt(1));
		}
		if(status) {
			sender.sendMessage(ChatColor.GREEN + "Lore removed.");
		}
		else {
			sender.sendMessage(ChatColor.RED + "Line does not exist.");
		}
	}

	@CommandLabels({"set", "s"})
	@Command(
			desc = "sets lore line",
			min = 2,
			max = 3,
			usage = "<item ID> <line> [text]")
	public void set(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		String text = "";
		if(context.length() > 2) {
			text = context.getString(2);
		}
		boolean status = true;
		status = item.setLoreLine(context.getInt(1), text);
		if(status) {
			sender.sendMessage(ChatColor.GREEN + "Lore set.");
		}
		else {
			sender.sendMessage(ChatColor.RED + "Line does not exist.");
		}
	}
}
