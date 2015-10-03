package me.ragan262.questerextras.items;

import me.ragan262.quester.storage.StorageKey;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Qpotion extends Qitem {
	
	protected List<PotionEffect> effects = new ArrayList<>();
	
	@Override
	public String getType() {
		return "POTION";
	}
	
	public boolean isPotion() {
		return material == Material.POTION;
	}
	
	private void setEffects(List<PotionEffect> effs) {
		for(PotionEffect e : effs) {
			effects.add(e);
		}
	}
	
	public boolean hasEffects() {
		return !effects.isEmpty();
	}
	
	public void addEffect(PotionEffect effect) {
		removeEffect(effect.getType());
		effects.add(effect);
	}
	
	public void removeEffect(PotionEffectType type) {
		Iterator<PotionEffect> it = effects.iterator();
		while(it.hasNext()) {
			if(it.next().getType() == type) {
				it.remove();
			}
		}
	}
	
	public List<PotionEffect> getEffects() {
		return new ArrayList<>(effects);
	}
	
	@Override
	public String getInfo(String indent) {
		StringBuilder sb = new StringBuilder(super.getInfo(indent));
		if(!effects.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Effects: ");
			for(PotionEffect e : effects) {
				sb.append('\n').append(indent).append("  ").append(ChatColor.RESET).append(e.getType().getName()).append(" - Lvl ")
						.append(e.getAmplifier() + 1).append(" - ").append(e.getDuration() / 20.0).append('s');
			}
		}
		return sb.toString();
	}
	
	@Override
	Qitem getNewObject() {
		return new Qpotion();
	}
	
	@Override
	void copyValues(Qitem item) {
		super.copyValues(item);
		if(item instanceof Qpotion) {
			Qpotion potion = (Qpotion)item;
			for(PotionEffect e : effects) {
				potion.addEffect(new PotionEffect(e.getType(), e.getDuration(), e.getAmplifier()));
			}
		}
	}
	
	@Override
	public ItemStack getItemStack() {
		ItemStack is = super.getItemStack();
		if(isPotion()) {
			PotionMeta pm = (PotionMeta)is.getItemMeta();
			if(!effects.isEmpty()) {
				for(PotionEffect eff : effects) {
					pm.addCustomEffect(eff, true);
				}
			}
			is.setItemMeta(pm);
		}
		return is;
	}

	@Override
	public void serializeKey(StorageKey key) {
		super.serializeKey(key);
		if(!effects.isEmpty()) {
			key.setString("effects", saveEffects(effects));
		}
	}
	
	static Qitem loadKey(StorageKey key) {
		Qpotion potion = new Qpotion();
		potion.setEffects(loadEffects(key.getString("effects", "")));
		return potion;
	}
	
	public static String saveEffects(List<PotionEffect> effs) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(PotionEffect e : effs) {
			if(first) {
				first = false;
			}
			else {
				sb.append(',');
			}
			sb.append(e.getType().getId()).append(':').append(e.getDuration()).append(':').append(e.getAmplifier());
		}
		return sb.toString();
	}
	
	public static List<PotionEffect> loadEffects(String string) {
		List<PotionEffect> efs = new ArrayList<>();
		String[] effs = string.split(",");
		for(String s : effs) {
			try {
				String[] arr = s.split(":");
				PotionEffect e = new PotionEffect(PotionEffectType.getById(Integer.parseInt(arr[0])), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
				efs.add(e);
			}
			catch(Exception ignore) {
			}
		}
		return efs;
	}
}
