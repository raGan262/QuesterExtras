package me.ragan262.questerextras.commands;

import me.ragan262.quester.commandmanager.CommandHelp;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.annotations.CommandLabels;
import me.ragan262.quester.commandmanager.annotations.NestedCommand;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.utils.Util;
import me.ragan262.questerextras.QrExtras;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class Commands {
	
	private final QrExtras plugin;
	
	public Commands(final QrExtras plugin) {
		this.plugin = plugin;
	}
	
	@CommandLabels({ "help" })
	@Command(desc = "displays help", usage = "[arg1] [arg2]...", permission = "questerextras.admin")
	public void help(final QuesterCommandContext context, final CommandSender sender) {
		final Map<String, List<CommandHelp>> cmds =
				plugin.getCommands().getHelp(context.getArgs(), sender, context.hasFlag('d'));
		sender.sendMessage(Util.line(ChatColor.BLUE, "QuesterExtras help", ChatColor.GOLD));
		for(final String s : cmds.keySet()) {
			for(final CommandHelp ch : cmds.get(s)) {
				sender.sendMessage(ch.getFormattedHelp());
			}
		}
	}
	
	@CommandLabels({ "item" })
	@Command(desc = "item manipulation", permission = "questerextras.admin")
	@NestedCommand(ItemCommands.class)
	public void item(final QuesterCommandContext context, final CommandSender sender) throws QuesterException {
	}
	
	@CommandLabels({ "mob" })
	@Command(desc = "mob manipulation", permission = "questerextras.admin")
	@NestedCommand(MobCommands.class)
	public void mob(final QuesterCommandContext context, final CommandSender sender) throws QuesterException {
	}
	
	@CommandLabels({ "msg" })
	@Command(
			desc = "broadcasts a message in radius",
			usage = "<radius/player> <message> (-p)",
			min = 2,
			max = 2,
			permission = "questerextras.admin")
	public void msg(final QuesterCommandContext context, final CommandSender sender) throws CommandException {
		if(context.hasFlag('p')) {
			final Player target = Bukkit.getPlayer(context.getString(0));
			if(target != null) {
				final String msg =
						ChatColor.translateAlternateColorCodes('&', context.getString(1));
				target.sendMessage(msg);
				sender.sendMessage(ChatColor.GOLD + "To " + target.getName() + ": "
						+ ChatColor.RESET + msg);
			}
			else {
				throw new CommandException("Player '" + context.getString(0) + "' not found.");
			}
		}
		else {
			Location loc = null;
			if(sender instanceof Player) {
				loc = ((Player) sender).getLocation();
			}
			else if(sender instanceof BlockCommandSender) {
				loc = ((BlockCommandSender) sender).getBlock().getLocation();
			}
			if(loc != null) {
				final String msg =
						ChatColor.translateAlternateColorCodes('&', context.getString(1));
				int radius = context.getInt(0);
				radius = radius * radius;
				for(final Player p : plugin.getServer().getOnlinePlayers()) {
					if(p.getLocation().distanceSquared(loc) <= radius) {
						p.sendMessage(msg);
					}
				}
			}
			else {
				throw new CommandException("This command requires location context.");
			}
		}
	}
	
	@CommandLabels({ "save" })
	@Command(desc = "saves questerextras data", max = 0, permission = "questerextras.admin")
	public void save(final QuesterCommandContext context, final CommandSender sender) throws QuesterException {
		plugin.items.save(plugin.config.getKey("items"));
		plugin.mobs.save(plugin.config.getKey("mobs"));
		plugin.config.save();
		sender.sendMessage(ChatColor.GRAY + "Liquester data saved.");
	}
}
