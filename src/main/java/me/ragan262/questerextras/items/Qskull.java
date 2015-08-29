package me.ragan262.questerextras.items;

import me.ragan262.quester.storage.StorageKey;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Qskull extends Qitem {

	protected String owner = "";
	
	@Override
	public String getType() {
		return "SKULL";
	}
	
	public boolean isSkull() {
		return material == Material.SKULL_ITEM;
	}
	
	public boolean hasOwner() {
		return !owner.isEmpty();
	}
	
	public void setOwner(String newOwner) {
		if(newOwner == null) {
			owner = "";
		}
		else {
			owner = newOwner;
		}
	}
	
	public String getOwner() {
		return owner;
	}
	
	@Override
	public String getInfo(String indent) {
		StringBuilder sb = new StringBuilder(super.getInfo(indent));
		sb.append('\n').append(indent).append(ChatColor.BLUE).append("Owner: ").append(ChatColor.RESET).append(owner).append('\n');
		return sb.toString();
	}
	
	@Override
	Qitem getNewObject() {
		return new Qskull();
	}
	
	@Override
	void copyValues(Qitem item) {
		super.copyValues(item);
		if(item instanceof Qskull) {
			Qskull skull = (Qskull) item;
			skull.setName(owner);
		}
	}
	
	@Override
	public ItemStack getItemStack() {
		ItemStack is = super.getItemStack();
		if(isSkull()) {
			SkullMeta sm = (SkullMeta) is.getItemMeta();
			if(!owner.isEmpty()) {
				sm.setOwner(owner);
			}
			is.setItemMeta(sm);
		}
		return is;
	}

	@Override
	public void serializeKey(StorageKey key) {
		super.serializeKey(key);
		if(!owner.isEmpty()) {
			key.setString("owner", owner);
		}
	}
	
	static Qitem loadKey(StorageKey key) {
		Qskull skull = new Qskull();
		skull.setOwner(key.getString("owner", ""));
		return skull;
	}
}
