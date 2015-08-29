package me.ragan262.questerextras.mobs;

import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.SerUtils;
import me.ragan262.questerextras.items.Qitem;
import me.ragan262.questerextras.items.Qpotion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Qmob {
	
	protected String name = "";
	protected int health = 0;
	protected EntityType type = null;
	protected QitemSlot[] slots = {new QitemSlot(), new QitemSlot(), new QitemSlot(), new QitemSlot(), new QitemSlot()};
	protected List<PotionEffect> effects = new ArrayList<PotionEffect>(); 
	

	class QitemSlot {
		Qitem item = null;
		float drop = 0;
	}
	
	public boolean setType(EntityType type) {
		if(type != null && type.isAlive()) {
			this.type = type;
			return true;
		}
		return false;
	}
	
	public EntityType getType() {
		return type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
	
	public boolean setItem(int slot, Qitem item) {
		if(slot < 0 || slot > 4) {
			return false;
		}
		slots[slot].item = item.getCopy();
		return true;
	}
	
	public boolean setChance(int slot, float chance) {
		if(slot < 0 || slot > 4) {
			return false;
		}
		slots[slot].drop = chance;
		return true;
	}
	
	public Qitem getItem(int slot) {
		return slots[slot].item;
	}
	
	public float getChance(int slot) {
		return slots[slot].drop;
	}
	
	public void setEffects(Qpotion potion) {
		effects.clear();
		for(PotionEffect e : potion.getEffects()) {
			effects.add(e);
		}
	}
	
	private void setEffects(List<PotionEffect> effs) {
		for(PotionEffect e : effs) {
			effects.add(e);
		}
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
	
	public String getInfo(String indent) {
		StringBuilder sb = new StringBuilder();
		String t = (type == null) ? "NONE" : type.getName();
		sb.append(ChatColor.BLUE).append("Type: ").append(ChatColor.RESET).append(t);
		if(!name.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Name: ").append(ChatColor.RESET).append(ChatColor.translateAlternateColorCodes('&', name));
		}
		if(health > 0) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Health: ").append(ChatColor.RESET).append(health);
		}
		if(slots[0].item != null) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Hand: ");
			sb.append('\n').append(indent).append("  ").append(ChatColor.BLUE).append("Drop chance: ")
					.append(ChatColor.RESET).append(slots[0].drop * 100).append('%');
			sb.append('\n').append(indent).append("  ").append(slots[0].item.getInfo(indent + "  "));
		}
		if(slots[1].item != null) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Feet: ");
			sb.append('\n').append(indent).append("  ").append(ChatColor.BLUE).append("Drop chance: ")
					.append(ChatColor.RESET).append(slots[1].drop * 100).append('%');
			sb.append('\n').append(indent).append("  ").append(slots[1].item.getInfo(indent + "  "));
		}
		if(slots[2].item != null) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Legs: ");
			sb.append('\n').append(indent).append("  ").append(ChatColor.BLUE).append("Drop chance: ")
					.append(ChatColor.RESET).append(slots[2].drop * 100).append('%');
			sb.append('\n').append(indent).append("  ").append(slots[2].item.getInfo(indent + "  "));
		}
		if(slots[3].item != null) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Chest: ");
			sb.append('\n').append(indent).append("  ").append(ChatColor.BLUE).append("Drop chance: ")
					.append(ChatColor.RESET).append(slots[3].drop * 100).append('%');
			sb.append('\n').append(indent).append("  ").append(slots[3].item.getInfo(indent + "  "));
		}
		if(slots[4].item != null) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Head: ");
			sb.append('\n').append(indent).append("  ").append(ChatColor.BLUE).append("Drop chance: ")
					.append(ChatColor.RESET).append(slots[4].drop * 100).append('%');
			sb.append('\n').append(indent).append("  ").append(slots[4].item.getInfo(indent + "  "));
		}
		if(!effects.isEmpty()) {
			sb.append('\n').append(indent).append(ChatColor.BLUE).append("Effects: ");
			for(PotionEffect e : effects) {
				sb.append('\n').append(indent).append("  ").append(ChatColor.RESET).append(e.getType().getName()).append(" - Lvl ")
						.append(e.getAmplifier()+1).append(" - ").append(e.getDuration()/20.0).append('s');
			}
		}
		return sb.toString();
	}
	
	public LivingEntity spawnAt(Location location) {
		try {
			LivingEntity e = (LivingEntity) location.getWorld().spawnEntity(location, type);
			if(!name.isEmpty()) {
				e.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
				e.setCustomNameVisible(true);
			}
			if(health > 0 && health < e.getMaxHealth()) {
				e.setHealth(health);
			}
			EntityEquipment ee = e.getEquipment();
			Qitem item = slots[0].item;
			float chance = 0F;
			if(item != null) {
				chance = slots[0].drop;
				ee.setItemInHand(item.getItemStack());
				ee.setItemInHandDropChance(chance);
			}
			item = slots[1].item;
			if(item != null) {
				chance = slots[1].drop;
				ee.setBoots(item.getItemStack());
				ee.setBootsDropChance(chance);
			}
			item = slots[2].item;
			if(item != null) {
				chance = slots[2].drop;
				ee.setLeggings(item.getItemStack());
				ee.setLeggingsDropChance(chance);
			}
			item = slots[3].item;
			if(item != null) {
				chance = slots[3].drop;
				ee.setChestplate(item.getItemStack());
				ee.setChestplateDropChance(chance);
			}
			item = slots[4].item;
			if(item != null) {
				chance = slots[4].drop;
				ee.setHelmet(item.getItemStack());
				ee.setHelmetDropChance(chance);
			}
			if(!effects.isEmpty()) {
				for(PotionEffect ef : effects) {
					e.addPotionEffect(ef);
				}
			}
			return e;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public Qmob getCopy() {
		Qmob mob = new Qmob();
		mob.setType(this.type);
		if(!name.isEmpty()) {
			mob.setName(name);
		}
		if(health != 0) {
			mob.setHealth(health);
		}
		if(!effects.isEmpty()) {
			for(PotionEffect e : effects) {
				mob.addEffect(new PotionEffect(e.getType(), e.getDuration(), e.getAmplifier()));
			}
		}
		for(int i=0; i<slots.length; i++) {
			if(slots[i].item != null) {
				mob.setItem(i, slots[i].item.getCopy());
				mob.setChance(i, slots[i].drop);
			}
		}
		
		return mob;
	}
	
	public void serializeKey(StorageKey key) {
		if(type != null) {
			key.setInt("entity", type.getTypeId());
		}
		if(!name.isEmpty()) {
			key.setString("name", name);
		}
		if(health > 0) {
			key.setInt("health", health);
		}
		if(!effects.isEmpty()) {
			key.setString("effects", Qpotion.saveEffects(effects));
		}
		if(slots[0].item != null) {
			slots[0].item.serializeKey(key.getSubKey("hand"));
			if(slots[0].drop > 0F) {
				key.getSubKey("hand").setDouble("chance", slots[0].drop);
			}
		}
		if(slots[1].item != null) {
			slots[1].item.serializeKey(key.getSubKey("feet"));
			if(slots[1].drop > 0F) {
				key.getSubKey("feet").setDouble("chance", slots[1].drop);
			}
		}
		if(slots[2].item != null) {
			slots[2].item.serializeKey(key.getSubKey("legs"));
			if(slots[2].drop > 0F) {
				key.getSubKey("legs").setDouble("chance", slots[2].drop);
			}
		}
		if(slots[3].item != null) {
			slots[3].item.serializeKey(key.getSubKey("chest"));
			if(slots[3].drop > 0F) {
				key.getSubKey("chest").setDouble("chance", slots[3].drop);
			}
		}
		if(slots[4].item != null) {
			slots[4].item.serializeKey(key.getSubKey("head"));
			if(slots[4].drop > 0F) {
				key.getSubKey("head").setDouble("chance", slots[4].drop);
			}
		}
	}
	
	public static Qmob deserializeKey(StorageKey key) {
		Qmob mob = new Qmob();
		try {
			mob.setType(SerUtils.parseEntity(key.getString("entity", null)));
		}
		catch (Exception ignore) {}
		mob.setName(key.getString("name", ""));
		mob.setHealth(key.getInt("health", 0));
		mob.setEffects(Qpotion.loadEffects(key.getString("effects", "")));
		StorageKey subKey = key.getSubKey("hand");
		if(subKey.hasSubKeys()) {
			mob.slots[0].item = Qitem.deserializeKey(subKey);
			mob.slots[0].drop = (float) subKey.getDouble("chance", 0F);
		}
		subKey = key.getSubKey("feet");
		if(subKey.hasSubKeys()) {
			mob.slots[1].item = Qitem.deserializeKey(subKey);
			mob.slots[1].drop = (float) subKey.getDouble("chance", 0F);
		}
		subKey = key.getSubKey("legs");
		if(subKey.hasSubKeys()) {
			mob.slots[2].item = Qitem.deserializeKey(subKey);
			mob.slots[2].drop = (float) subKey.getDouble("chance", 0F);
		}
		subKey = key.getSubKey("chest");
		if(subKey.hasSubKeys()) {
			mob.slots[3].item = Qitem.deserializeKey(subKey);
			mob.slots[3].drop = (float) subKey.getDouble("chance", 0F);
		}
		subKey = key.getSubKey("head");
		if(subKey.hasSubKeys()) {
			mob.slots[4].item = Qitem.deserializeKey(subKey);
			mob.slots[4].drop = (float) subKey.getDouble("chance", 0F);
		}
		return mob;
	}
}
