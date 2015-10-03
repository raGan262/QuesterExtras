package me.ragan262.questerextras.commands;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.annotations.CommandLabels;
import me.ragan262.quester.commandmanager.annotations.NestedCommand;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.utils.SerUtils;
import me.ragan262.questerextras.items.*;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.qevents.XfillQevent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map.Entry;

public class ItemCommands {

	Items items = null;
	
	public ItemCommands(QrExtras plugin) {
		items = plugin.items;
	}
	
	@CommandLabels({"list"})
	@Command(
			desc = "item list",
			max = 0)
	public void list(QuesterCommandContext context, CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "Item list:");
		int size = items.size();
		for(int i = 0; i < size; i++) {
			sender.sendMessage("[" + i + "] " + items.getItemString(i));
		}
	}

	@CommandLabels({"info"})
	@Command(
			desc = "item info",
			min = 1,
			max = 1,
			usage = "<item ID>")
	public void info(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		try {
			sender.sendMessage(items.getItem(context.getInt(0)).getInfo(""));
		}
		catch(IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
	
	@CommandLabels({"create", "c"})
	@Command(
			desc = "creates item",
			min = 1,
			usage = "<type> [name] [amount] [lore line]... (-qd)")
	public void add(QuesterCommandContext context, CommandSender sender) {
		Qitem item;
		if(context.getString(0).equalsIgnoreCase("BOOK")) {
			item = new Qbook();
			item.setMaterial(Material.WRITTEN_BOOK);
		}
		else if(context.getString(0).equalsIgnoreCase("POTION")) {
			item = new Qpotion();
			item.setMaterial(Material.POTION);
		}
		else if(context.getString(0).equalsIgnoreCase("SKULL")) {
			item = new Qskull();
			item.setMaterial(Material.SKULL_ITEM);
		}
		else if(context.getString(0).equalsIgnoreCase("LARMOR")) {
			item = new Qarmor();
			item.setMaterial(Material.LEATHER_CHESTPLATE);
		}
		else {
			if(context.getString(0).equalsIgnoreCase("THIS")) {
				if(sender instanceof Player) {
					item = getPlayersItem((Player)sender);
					if(item == null) {
						sender.sendMessage(ChatColor.RED + "No item in hand.");
						return;
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "Item 'this' requires player context.");
					return;
				}
			}
			else {
				item = new Qitem();
				try {
					int[] itm = SerUtils.parseItem(context.getString(0));
					item.setMaterial(Material.getMaterial(itm[0]));
					if(itm[1] >= 0) {
						item.setDamage((short)itm[1]);
					}
				}
				catch(IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + "Invalid item.");
				}
			}
		}
		if(context.length() > 1) {
			item.setName(context.getString(1));
			if(context.length() > 2) {
				int amt = context.getInt(2, 1);
				if(amt > 1) {
					item.setAmount(amt);
				}
				if(context.length() > 3) {
					item.removeLore();
					for(int i = 3; i < context.length(); i++) {
						item.addLore(context.getString(i));
					}
				}
			}
		}
		if(context.hasFlag('q')) {
			item.setQuestItem(true);
		}
		else if(context.hasFlag('d')) {
			item.setDonatorItem(true);
		}
		if(items.addItem(item)) {
			sender.sendMessage(ChatColor.GREEN + "Item created. (ID " + (items.size() - 1) + ")");
		}
		else {
			sender.sendMessage(ChatColor.RED + "Failed to create an item. (this should never happen)");
		}
	}
	
	private Qitem getPlayersItem(Player player) {
		ItemStack is = player.getItemInHand();
		Qitem item;
		if(is == null) {
			return null;
		}
		ItemMeta im = is.getItemMeta();
		if(im instanceof BookMeta) {
			BookMeta bm = (BookMeta)im;
			Qbook book = new Qbook();
			item = book;
			if(bm.hasAuthor()) {
				book.setAuthor(bm.getAuthor());
			}
			if(bm.hasTitle()) {
				book.setTitle(bm.getTitle());
			}
			if(bm.hasPages()) {
				for(String s : bm.getPages()) {
					book.addPage(s);
				}
			}
		}
		else if(im instanceof PotionMeta) {
			PotionMeta pm = (PotionMeta)im;
			Qpotion potion = new Qpotion();
			item = potion;
			if(pm.hasCustomEffects()) {
				for(PotionEffect e : pm.getCustomEffects()) {
					potion.addEffect(e);
				}
			}
		}
		else if(im instanceof SkullMeta) {
			SkullMeta sm = (SkullMeta)im;
			Qskull skull = new Qskull();
			item = skull;
			if(sm.hasOwner()) {
				skull.setOwner(sm.getOwner());
			}
		}
		else if(im instanceof LeatherArmorMeta) {
			LeatherArmorMeta am = (LeatherArmorMeta)im;
			Qarmor armor = new Qarmor();
			item = armor;
			armor.setColor(am.getColor());
		}
		else {
			item = new Qitem();
		}
		item.setMaterial(is.getType());
		item.setDamage(is.getDurability());
		item.setAmount(is.getAmount());
		if(im != null) {
			if(im.hasDisplayName()) {
				item.setName(im.getDisplayName());
			}
			if(im.hasLore()) {
				boolean first = true;
				for(String s : im.getLore()) {
					if(first && ChatColor.stripColor(s).equalsIgnoreCase("Quest item")) {
						item.setQuestItem(true);
					}
					else {
						item.addLore(s);
					}
					first = false;
				}
			}
			if(im.hasEnchants()) {
				for(Entry<Enchantment, Integer> e : im.getEnchants().entrySet()) {
					item.addEnachnt(e.getKey(), e.getValue());
				}
			}
		}
		return item;
	}
	
	@CommandLabels({"remove", "r"})
	@Command(
			desc = "removes item",
			min = 1,
			usage = "<item ID>")
	public void remove(QuesterCommandContext context, CommandSender sender) {
		items.removeItem(context.getInt(0));
	}
	
	@CommandLabels({"get"})
	@Command(
			desc = "gives item",
			min = 1,
			max = 1,
			usage = "<item ID>")
	public void get(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		if(context.getPlayer() == null) {
			sender.sendMessage(ChatColor.RED + "This command requires player context.");
			return;
		}
		try {
			context.getPlayer().getInventory().addItem(items.getItem(context.getInt(0)).getItemStack());
		}
		catch(IllegalArgumentException e) {
			if(e instanceof NumberFormatException) {
				throw e;
			}
			else {
				sender.sendMessage(ChatColor.RED + e.getMessage());
			}
		}
	}
	
	@CommandLabels({"give"})
	@Command(
			desc = "gives item to another player",
			min = 2,
			max = 3,
			usage = "<player> <item ID> [amount]")
	public void give(QuesterCommandContext context, CommandSender sender)
			throws CommandException, QuesterException {
		try {
			Player to = Bukkit.getServer().getPlayer(context.getString(0));
			if(to == null) {
				throw new CommandException("Invalid player.");
			}
			Qitem item = items.getItem(context.getInt(1));
			int amt = item.getAmount();
			if(context.length() > 2) {
				amt = context.getInt(2);
			}
			XfillQevent.giveItem(item, to.getInventory(), amt, to.getLocation());
		}
		catch(IllegalArgumentException e) {
			if(e instanceof NumberFormatException) {
				throw e;
			}
			else {
				sender.sendMessage(ChatColor.RED + e.getMessage());
			}
		}
	}
	
	@CommandLabels({"set", "s"})
	@Command(
			desc = "sets item material",
			min = 1,
			max = 2,
			usage = "<item ID> <item>|(-qd)")
	public void material(QuesterCommandContext context, CommandSender sender)
			throws QuesterException, CommandException {
		Qitem item = items.getItem(context.getInt(0));
		boolean test = true;
		if(context.length() > 1) {
			try {
				int[] itm = SerUtils.parseItem(context.getString(1));
				item.setMaterial(Material.getMaterial(itm[0]));
				if(itm[1] >= 0) {
					item.setDamage((short)itm[1]);
				}
			}
			catch(IllegalArgumentException e) {
				sender.sendMessage(ChatColor.RED + "Invalid material.");
			}
			sender.sendMessage(ChatColor.GREEN + "Material set.");
			test = false;
		}
		if(context.hasFlag('q')) {
			item.setQuestItem(!item.isQuestItem());
			test = false;
		}
		else if(context.hasFlag('d')) {
			item.setDonatorItem(!item.isQuestItem());
			test = false;
		}
		if(test) {
			throw new CommandException("You must specify item flag or new material, or both.");
		}
	}
	
	@CommandLabels({"lore"})
	@Command(desc = "lore manipulation")
	@NestedCommand(LoreCommands.class)
	public void lore(QuesterCommandContext context, CommandSender sender) {
	}
	
	@CommandLabels({"name"})
	@Command(
			desc = "changes item name",
			min = 1,
			max = 2,
			usage = "<item ID> [name]")
	public void name(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		String name = "";
		if(context.length() > 1) {
			name = context.getString(1);
		}
		item.setName(name);
		if(name.isEmpty()) {
			sender.sendMessage(ChatColor.GREEN + "Name removed.");
		}
		else {
			sender.sendMessage(ChatColor.GREEN + "Name set to '" + name + "'");
		}
	}
	
	@CommandLabels({"author"})
	@Command(
			desc = "changes book author",
			min = 1,
			max = 2,
			usage = "<item ID> [author]")
	public void author(QuesterCommandContext context, CommandSender sender)
			throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			String author = "";
			if(context.length() > 1) {
				author = context.getString(1);
			}
			((Qbook)item).setAuthor(author);
			if(author.isEmpty()) {
				sender.sendMessage(ChatColor.GREEN + "Author removed.");
			}
			else {
				sender.sendMessage(ChatColor.GREEN + "Author set to '" + author + "'");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}

	@CommandLabels({"page"})
	@Command(desc = "page manipulation")
	@NestedCommand(PageCommands.class)
	public void page(QuesterCommandContext context, CommandSender sender) {
	}
	
	@CommandLabels({"title"})
	@Command(
			desc = "changes book title",
			min = 1,
			max = 2,
			usage = "<item ID> [title]")
	public void title(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qbook) {
			String title = "";
			if(context.length() > 1) {
				title = context.getString(1);
			}
			((Qbook)item).setTitle(title);
			if(title.isEmpty()) {
				sender.sendMessage(ChatColor.GREEN + "Title removed.");
			}
			else {
				sender.sendMessage(ChatColor.GREEN + "Title set to '" + title + "'");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a book.");
		}
	}
	
	@CommandLabels({"ench"})
	@Command(
			desc = "changes item enchantments",
			min = 2,
			usage = "<item ID> <enchant>...")
	public void ench(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		StringBuilder errors = new StringBuilder();
		boolean success = false;
		Ench en = null;
		for(int i = 1; i < context.length(); i++) {
			if(context.getString(i).charAt(0) == '!') {
				String s = context.getString(i).substring(1);
				en = getEnchant(s);
				if(en.ench == null) {
					errors.append(", ").append(s);
				}
				else {
					success = true;
					item.removeEnchant(en.ench);
				}
			}
			else {
				en = getEnchant(context.getString(i));
				if(en.ench == null) {
					errors.append(", ").append(context.getString(i));
				}
				else {
					success = true;
					item.addEnachnt(en.ench, en.level);
				}
			}
		}
		if(success) {
			sender.sendMessage(ChatColor.GREEN + "Enchantments changed.");
		}
		if(!errors.toString().isEmpty()) {
			sender.sendMessage(ChatColor.RED + "Could not resolve following enchantments: " + errors.substring(2));
		}
	}
	
	private Ench getEnchant(String s) {
		Ench en = new Ench();
		String[] ss = s.split(":");
		if(ss.length > 1) {
			try {
				en.level = Integer.parseInt(ss[1]);
				if(en.level < 1) {
					en.level = 1;
				}
			}
			catch(Exception e) {
			}
		}
		en.ench = Enchantment.getByName(ss[0].toUpperCase());
		if(en.ench == null) {
			try {
				en.ench = Enchantment.getById(Integer.parseInt(ss[0]));
			}
			catch(Exception e) {
			}
		}
		return en;
	}
	
	@CommandLabels({"effect", "eff"})
	@Command(
			desc = "changes potion effects",
			min = 2,
			usage = "<item ID> {<effect>}...")
	public void effect(QuesterCommandContext context, CommandSender sender)
			throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qpotion) {
			StringBuilder errors = new StringBuilder();
			boolean success = false;
			PotionEffect eff = null;
			for(int i = 1; i < context.length(); i++) {
				if(context.getString(i).charAt(0) == '!') {
					String s = context.getString(i).substring(1);
					eff = getEffect(s);
					if(eff == null) {
						errors.append(", ").append(s);
					}
					else {
						success = true;
						((Qpotion)item).removeEffect(eff.getType());
					}
				}
				else {
					eff = getEffect(context.getString(i));
					if(eff == null) {
						errors.append(", ").append(context.getString(i));
					}
					else {
						success = true;
						((Qpotion)item).addEffect(eff);
					}
				}
			}
			if(success) {
				sender.sendMessage(ChatColor.GREEN + "Effects changed.");
			}
			if(!errors.toString().isEmpty()) {
				sender.sendMessage(ChatColor.RED + "Could not resolve following effects: " + errors.substring(2));
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a potion.");
		}
	}
	
	@CommandLabels({"owner"})
	@Command(
			desc = "changes skull owner",
			min = 1,
			max = 2,
			usage = "<item ID> [owner]")
	public void owner(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qskull) {
			String owner = "";
			if(context.length() > 1) {
				owner = context.getString(1);
			}
			((Qskull)item).setOwner(owner);
			if(owner.isEmpty()) {
				sender.sendMessage(ChatColor.GREEN + "Owner removed.");
			}
			else {
				sender.sendMessage(ChatColor.GREEN + "Owner set to '" + owner + "'");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a skull.");
		}
	}
	
	@CommandLabels({"color"})
	@Command(
			desc = "changes armor color",
			min = 1,
			max = 2,
			usage = "<item ID> [color]")
	public void color(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qitem item = items.getItem(context.getInt(0));
		if(item instanceof Qarmor) {
			Color color = null;
			if(context.length() > 1) {
				color = getColor(context.getString(1));
				((Qarmor)item).setColor(color);
				if(color == null) {
					sender.sendMessage(ChatColor.RED + "Could not resolve color " + context.getString(1) + ".");
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "Color set.");
				}
			}
			else {
				((Qarmor)item).resetColor();
				sender.sendMessage(ChatColor.GREEN + "Color reset.");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "Item is not a leather armor.");
		}
	}
	
	private Color getColor(String arg) {
		Color color = null;
		try {
			String[] s = arg.split(":");
			color = Color.fromRGB(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		}
		catch(Exception ignore) {
		}
		return color;
	}
	
	public static PotionEffect getEffect(String s) {
		PotionEffectType eff = null;
		int power = 0;
		int duration = 0;
		String[] ss = s.split(":");
		if(ss.length > 1) {
			try {
				duration = (int)Math.round(Double.parseDouble(ss[1]) * 20);
				if(duration < 0) {
					duration = 0;
				}
			}
			catch(Exception e) {
			}
			if(ss.length > 2) {
				try {
					power = Integer.parseInt(ss[2]);
					if(power < 0) {
						power = 0;
					}
				}
				catch(Exception e) {
				}
			}
		}
		eff = PotionEffectType.getByName(ss[0].toUpperCase());
		if(eff == null) {
			try {
				eff = PotionEffectType.getById(Integer.parseInt(ss[0]));
			}
			catch(Exception e) {
			}
		}
		if(eff == null) {
			return null;
		}
		return new PotionEffect(eff, duration, power);
	}
	
	class Ench {
		Enchantment ench = null;
		int level = 1;
	}
}
