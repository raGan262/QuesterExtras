package me.ragan262.questerextras.commands;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.annotations.CommandLabels;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.utils.SerUtils;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.items.Items;
import me.ragan262.questerextras.items.Qitem;
import me.ragan262.questerextras.mobs.Mobs;
import me.ragan262.questerextras.mobs.Qmob;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;

public class MobCommands {

	Mobs mobs = null;
	Items items = null;
	
	public MobCommands(QrExtras plugin) {
		mobs = plugin.mobs;
		items = plugin.items;
	}
	
	@CommandLabels({"list"})
	@Command(
			desc = "mob list",
			max = 0)
	public void list(QuesterCommandContext context, CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "Mob list:");
		int size = mobs.size();
		for(int i=0; i<size; i++) {
			sender.sendMessage("[" + i + "] " + mobs.getMobString(i));
		}
	}
	@CommandLabels({"info"})
	@Command(
			desc = "mob info",
			min = 1,
			max = 1,
			usage = "<mob ID>")
	public void info(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		try {
			sender.sendMessage(mobs.getMob(context.getInt(0)).getInfo(""));
		}
		catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
	
	@CommandLabels({"create", "c"})
	@Command(
			desc = "creates mob",
			min = 1,
			usage = "<type> [name]")
	public void add(QuesterCommandContext context, CommandSender sender) {
		Qmob mob = new Qmob();
		try {
			mob.setType(SerUtils.parseEntity(context.getString(0)));
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "Invalid mob");
			return;
		}
		if(context.length() > 1) {
			mob.setName(context.getString(1));
		}
		if(mobs.addMob(mob)) {
			sender.sendMessage(ChatColor.GREEN + "Mob created. (ID " + (mobs.size()-1) + ")");
		}
		else {
			sender.sendMessage(ChatColor.RED + "Failed to create a mob. (this should never happen)");
		}
	}
	
	@CommandLabels({"remove", "r"})
	@Command(
			desc = "removes mob",
			min = 1,
			usage = "<mob ID>")
	public void remove(QuesterCommandContext context, CommandSender sender) {
		mobs.removeMob(context.getInt(0));
	}
	
	@CommandLabels({"spawn"})
	@Command(
			desc = "spawns a mob",
			min = 1,
			usage = "<mob ID>")
	public void get(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		if(context.getPlayer() == null) {
			sender.sendMessage(ChatColor.RED + "This command requires player context.");
			return;
		}
		try {
			if(mobs.getMob(context.getInt(0)).spawnAt(context.getPlayer().getLocation()) == null) {
				sender.sendMessage(ChatColor.RED + "Failed to spawn a mob. (for some reason)");
			}
		}
		catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
	
	@CommandLabels({"set", "s"})
	@Command(
			desc = "sets mob type",
			min = 2,
			max = 2,
			usage = "<mob ID> <type>")
	public void set(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qmob mob = mobs.getMob(context.getInt(0));
		try {
			if(!mob.setType(SerUtils.parseEntity(context.getString(1)))) {
				throw new IllegalArgumentException();
			}
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "Invalid mob.");
		}
		sender.sendMessage(ChatColor.GREEN + "Mob type set.");
	}
	
	@CommandLabels({"name"})
	@Command(
			desc = "changes mob name",
			min = 1,
			max = 2,
			usage = "<mob ID> [name]")
	public void name(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qmob mob = mobs.getMob(context.getInt(0));
		String name = "";
		if(context.length() > 1) {
			name = context.getString(1);
		}
		mob.setName(name);
		if(name.isEmpty()) {
			sender.sendMessage(ChatColor.GREEN + "Name removed.");
		}
		else {
			sender.sendMessage(ChatColor.GREEN + "Name set to '" + name + "'");
		}
	}
	
	@CommandLabels({"health"})
	@Command(
			desc = "changes mob health",
			min = 1,
			max = 2,
			usage = "<mob ID> [health]")
	public void health(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qmob mob = mobs.getMob(context.getInt(0));
		int health = 0;
		if(context.length() > 1) {
			health = context.getInt(1);
		}
		mob.setHealth(health);
		if(health > 0) {
			sender.sendMessage(ChatColor.GREEN + "Health set to '" + health + "'");
		}
		else {
			sender.sendMessage(ChatColor.GREEN + "Health reset to default.");
		}
	}
	
	@CommandLabels({"equip"})
	@Command(
			desc = "changes equipment of a mob",
			min = 2,
			usage = "<mob ID> <<slot>:[item ID]:[chance]>...")
	public void equip(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qmob mob = mobs.getMob(context.getInt(0));
		StringBuilder errors = new StringBuilder();
		boolean success = false;
		Slot slot = null;
		for(int i=1; i<context.length(); i++) {
			try {
				slot = getSlot(context.getString(i));
				if(!mob.setItem(slot.id, slot.item)) {
					throw new IllegalArgumentException();
				}
				mob.setChance(slot.id, slot.chance);
				success = true;
			}
			catch (Exception e) {
				errors.append(", ").append(context.getString(i));
			}
		}
		if(success) {
			sender.sendMessage(ChatColor.GREEN + "Equipment changed.");
		}
		if(!errors.toString().isEmpty()) {
			sender.sendMessage(ChatColor.RED + "Could not resolve following arguments: " + errors.substring(2));
		}
	}
	
	private class Slot {
		int id = -1;
		Qitem item = null;
		float chance = 0F;
	}
	
	private Slot getSlot(String arg) {
		Slot slot = new Slot();
		String[] ss = arg.split(":");
		try {
			if(ss[0].equalsIgnoreCase("hand")) {
				slot.id = 0;
			}
			else if(ss[0].equalsIgnoreCase("feet")) {
				slot.id = 1;
			}
			else if(ss[0].equalsIgnoreCase("legs")) {
				slot.id = 2;
			}
			else if(ss[0].equalsIgnoreCase("chest")) {
				slot.id = 3;
			}
			else if(ss[0].equalsIgnoreCase("head")) {
				slot.id = 4;
			}
			else {
				slot.id = Integer.parseInt(ss[0]);
			}
		}
		catch (Exception ignore) {}
		if(ss.length > 1) {
			try {
				slot.item = items.getItem(Integer.parseInt(ss[1]));
			}
			catch (Exception ignore) {}
			if(ss.length > 2) {
				try {
					int raw = Integer.parseInt(ss[2]);
					if(raw <= 0) {
					}
					else if(raw > 100) {
						slot.chance = 1F;
					}
					else {
						slot.chance = raw / 100F;
					}
				}
				catch (Exception ignore) {}
			}
		}
		return slot;
	}
	
	@CommandLabels({"effect", "eff"})
	@Command(
			desc = "changes potion effects",
			min = 2,
			usage = "<mob ID> {<effect>}...")
	public void effect(QuesterCommandContext context, CommandSender sender) throws QuesterException {
		Qmob mob = mobs.getMob(context.getInt(0));
		StringBuilder errors = new StringBuilder();
		boolean success = false;
		PotionEffect eff = null;
		for(int i=1; i<context.length(); i++) {
			if(context.getString(i).charAt(0) == '!') {
				String s = context.getString(i).substring(1);
				eff = ItemCommands.getEffect(s);
				if(eff == null) {
					errors.append(", ").append(s);
				}
				else {
					success = true;
					mob.removeEffect(eff.getType());
				}
			}
			else {
				eff = ItemCommands.getEffect(context.getString(i));
				if(eff == null) {
					errors.append(", ").append(context.getString(i));
				}
				else {
					success = true;
					mob.addEffect(eff);
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
}
