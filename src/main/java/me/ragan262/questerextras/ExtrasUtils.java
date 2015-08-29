package me.ragan262.questerextras;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ExtrasUtils {
	
	private ExtrasUtils() {
		throw new IllegalAccessError();
	}
	
	public static boolean isDonatorItem(ItemStack item) {
		try {
			List<String> lore = item.getItemMeta().getLore();
			return ChatColor.stripColor(lore.get(lore.size() - 1)).equalsIgnoreCase("Donator Item");
		}
		catch(Exception e) {
			return false;
		}
	}
}
