package me.ragan262.questerextras.items;

import me.ragan262.quester.storage.StorageKey;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class Qarmor extends Qitem {
	
	private int red = -1;
	private int green = -1;
	private int blue = -1;
	
	@Override
	public String getType() {
		return "LARMOR";
	}
	
	public boolean isArmor() {
		return material == Material.LEATHER_BOOTS
				|| material == Material.LEATHER_CHESTPLATE
				|| material == Material.LEATHER_HELMET
				|| material == Material.LEATHER_LEGGINGS;
	}
	
	public boolean hasColor() {
		return (red >= 0);
	}
	
	public void resetColor() {
		red = blue = green = -1;
	}
	
	public void setColor(Color color) {
		if(color != null) {
			red = color.getRed();
			blue = color.getBlue();
			green = color.getGreen();
		}
	}
	
	@Override
	public String getInfo(String indent) {
		StringBuilder sb = new StringBuilder(super.getInfo(indent));
		if(hasColor()) {
			String color = "" + ChatColor.RED + red + ' ' + ChatColor.GREEN + green + ' ' + ChatColor.BLUE + blue;
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Color: ").append(color);
		}
		return sb.toString();
	}
	
	@Override
	Qitem getNewObject() {
		return new Qarmor();
	}
	
	@Override
	void copyValues(Qitem item) {
		super.copyValues(item);
		if(item instanceof Qarmor) {
			Qarmor armor = (Qarmor)item;
			if(hasColor()) {
				armor.setColor(Color.fromRGB(red, green, blue));
			}
		}
	}
	
	@Override
	public ItemStack getItemStack() {
		ItemStack is = super.getItemStack();
		if(isArmor() && hasColor()) {
			LeatherArmorMeta pm = (LeatherArmorMeta)is.getItemMeta();
			pm.setColor(Color.fromRGB(red, green, blue));
			is.setItemMeta(pm);
		}
		return is;
	}

	@Override
	public void serializeKey(StorageKey key) {
		super.serializeKey(key);
		if(hasColor()) {
			key.setString("color", saveColor());
		}
	}
	
	private String saveColor() {
		return "" + red + ':' + green + ':' + blue;
	}
	
	static Qitem loadKey(StorageKey key) {
		Qarmor armor = new Qarmor();
		armor.setColor(loadColor(key.getString("color")));
		return armor;
	}
	
	private static Color loadColor(String colorString) {
		Color col = null;
		try {
			String[] arr = colorString.split(":");
			col = Color.fromRGB(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
		}
		catch(Exception ignore) {
		}
		return col;
	}
}
