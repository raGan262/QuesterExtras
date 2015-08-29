package me.ragan262.questerextras.items;

import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.SerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Qitem {
	
	protected boolean questItem = false;
	protected boolean donatorItem = false;
	protected int amount = 1;
	protected Material material = Material.AIR;
	protected Short damage = 0;
	protected String name = "";
	protected List<String> lore = new ArrayList<String>();
	protected Map<Integer, Integer> enchants = new HashMap<Integer, Integer>();
	
	public String getType() {
		return "ITEM";
	}
	
	public void setQuestItem(boolean value) {
		questItem = value;
	}
	
	public boolean isQuestItem() {
		return questItem;
	}
	
	public void setDonatorItem(boolean value) {
		donatorItem = value;
	}
	
	public boolean isDonatorItem() {
		return donatorItem;
	}
	
	public void setAmount(int amount) {
		if(amount < 1) {
			amount = 1;
		}
		else {
			this.amount = amount;
		}
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public void setDamage(short damage) {
		this.damage = damage;
	}
	
	public short getDamage() {
		return damage;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		if(name.isEmpty()) {
			return material.name();
		}
		return name;
	}
	
	public boolean removeLoreLine(int id) {
		try {
			this.lore.remove(id);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public void removeLore() {
		this.lore.clear();
	}
	
	public boolean setLoreLine(int line, String loreLine) {
		try {
			this.lore.set(line, loreLine);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public void addLore(String loreLine) {
		this.lore.add(loreLine);
	}
	
	public List<String> getLore() {
		return lore;
	}
	
	private void setEnchants(Map<Integer, Integer> map) {
		if(map != null) {
			enchants = map;
		}
	}
	
	public boolean addEnachnt(Enchantment ench, int level) {
		if(level < 1 || ench == null) {
			return false;
		}
		boolean neww = !enchants.containsKey(ench.getId());
		enchants.put(ench.getId(), level);
		return neww;
	}
	
	public boolean removeEnchant(Enchantment ench) {
		boolean is = enchants.containsKey(ench.getId());
		enchants.remove(ench.getId());
		return is;
	}
	
	public String getInfo(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.BLUE).append("Type: ").append(ChatColor.RESET).append(getType());
		if(questItem) {
			sb.append(" (Quest Item)");
		}
		else if(donatorItem) {
			sb.append(" (Donator Item)");
		}
		sb.append('\n').append(indent).append(ChatColor.BLUE).append("Material: ").append(ChatColor.RESET).append(material.name()).append(':').append(damage);
		sb.append('\n').append(indent).append(ChatColor.BLUE).append("Amount: ").append(ChatColor.RESET).append(amount);
		if(!name.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Name: ").append(ChatColor.RESET).append(name);
		}
		if(!lore.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Lore: ");
			for(String s : lore) {
				sb.append('\n').append(indent).append("  ").append(ChatColor.RESET).append(s);
			}
		}
		if(!enchants.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Enchants: ");
			for(Entry<Integer, Integer> e: enchants.entrySet()) {
				sb.append('\n').append(indent).append("  ").append(ChatColor.RESET)
						.append(Enchantment.getById(e.getKey()).getName()).append(" - ").append(e.getValue());
			}
		}
		return sb.toString();
	}
	
	Qitem getNewObject() {
		return new Qitem();
	}
	
	void copyValues(Qitem item) {
		item.setName(name);
		item.setDamage(damage);
		item.setMaterial(material);
		item.setAmount(amount);
		item.setQuestItem(questItem);
		item.setDonatorItem(donatorItem);
		for(String s : lore) {
			item.addLore(s);
		}
		for(Entry<Integer, Integer> entry : enchants.entrySet()) {
			item.addEnachnt(Enchantment.getById(entry.getKey()), entry.getValue());
		}
	}
	
	public final Qitem getCopy() {
		Qitem item = getNewObject();
		copyValues(item);
		return item;
	}
	
	public ItemStack getItemStack() {
		ItemStack is = new ItemStack(material, amount, damage);
		ItemMeta im = is.getItemMeta();
		if(!name.isEmpty()) {
			im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		}
		if(!lore.isEmpty() || questItem || donatorItem) {
			List<String> list = new ArrayList<String>();
			for(String s : lore) {
				list.add(ChatColor.translateAlternateColorCodes('&', s));
			}
			if(questItem) {
				list.add(ChatColor.BLUE + "Quest Item");
			}
			else if(donatorItem) {
				list.add(ChatColor.GOLD + "Donator Item");
			}
			im.setLore(list);
		}
		if(!enchants.isEmpty()) {
			for(Entry<Integer, Integer> e : enchants.entrySet()) {
				im.addEnchant(Enchantment.getById(e.getKey()), e.getValue(), true);
			}
		}
		is.setItemMeta(im);
		return is;
	}
 	
	public void serializeKey(StorageKey key) {
		key.setString("type", getType());
		if(material != Material.AIR || damage > 0) {
			key.setString("material", SerUtils.serializeItem(material, damage));
		}
		if(questItem) {
			key.setBoolean("questitem", true);
		}
		else if(donatorItem) {
			key.setBoolean("donatoritem", true);
		}
		if(amount > 1) {
			key.setInt("amount", amount);
		}
		if(!name.isEmpty()) {
			key.setString("name", name);
		}
		if(!lore.isEmpty()) {
			key.setString("lore", saveList(lore));
		}
		if(!enchants.isEmpty()) {
			key.setString("enchants", SerUtils.serializeEnchants(enchants));
		}
	}
	
	public static Qitem deserializeKey(StorageKey key) {
		Qitem item = null;
		String type = key.getString("type", "");
		if(type.equalsIgnoreCase("BOOK")) {
			item = Qbook.loadKey(key);
		}
		else if(type.equalsIgnoreCase("POTION")) {
			item = Qpotion.loadKey(key);
		}
		else if(type.equalsIgnoreCase("SKULL")) {
			item = Qskull.loadKey(key);
		}
		else if(type.equalsIgnoreCase("LARMOR")) {
			item = Qarmor.loadKey(key);
		}
		else {
			item = new Qitem();
		}
		try {
			int[] itm = SerUtils.parseItem(key.getString("material", ""));
			item.setMaterial(Material.getMaterial(itm[0]));
			if((short)itm[1] > 0) {
				item.setDamage((short)itm[1]);
			}
		}
		catch (IllegalArgumentException ignore) {}
		item.setQuestItem(key.getBoolean("questitem", false));
		if(!item.isQuestItem()) {
			item.setDonatorItem(key.getBoolean("donatoritem", false));
		}
		item.setAmount(key.getInt("amount", 1));
		item.setName(key.getString("name", ""));
		for(String s : loadList(key.getString("lore", ""))) {
			item.addLore(s);
		}
		try {
			item.setEnchants(SerUtils.parseEnchants(key.getString("enchants", "")));
		}
		catch (IllegalArgumentException ignore) {}
		return item;
	}
	
	static String saveList(List<String> list) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String s : list) {
			if(first) {
				first = false;
			}
			else {
				sb.append("||");
			}
			sb.append(s);
		}
		return sb.toString();
	}
	
	static String[] loadList(String string) {
		if(string.isEmpty()) {
			return new String[0];
		}
		return string.split("\\|\\|");
	}
}