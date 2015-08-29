package me.ragan262.questerextras.objectives;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.exceptions.ObjectiveException;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.SerUtils;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.items.Qitem;
import me.ragan262.questerextras.mobs.Qmob;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

@QElement("XMOBKILL")
public final class XmobKillObjective extends Objective {
	
	private final Qmob mob;
	private final int amount;
	private final Location location;
	private final int range;
	
	private XmobKillObjective(final Qmob mob, final int amt, final Location loc, final int rng) throws QuesterException {
		this.mob = mob;
		if(mob == null) {
			throw new ObjectiveException("Mob can't be null.");
		}
		amount = amt;
		location = loc;
		range = rng;
	}
	
	@Override
	public int getTargetAmount() {
		return amount;
	}
	
	@Override
	protected String show(final int progress) {
		final String mb = mob.getType() == null ? "mob" : mob.getType().getName();
		final String name = mob.getName().isEmpty() ? "" : " " + mob.getName();
		return "Kill " + mb + name + ". " + progress + "/" + amount;
	}
	
	@Override
	protected String info() {
		return amount + "\n  " + mob.getInfo("  ");
	}
	
	@Command(min = 2, max = 4, usage = "<mob ID> <amount> {[location]} [range]")
	public static Objective fromCommand(final QuesterCommandContext context) throws CommandException, QuesterException {
		final Qmob mob = QrExtras.plugin.mobs.getMob(context.getInt(0)).getCopy();
		final int amt = context.getInt(1);
		Location loc = null;
		int rng = 5;
		if(context.length() > 2) {
			loc = SerUtils.getLoc(context.getPlayer(), context.getString(2));
			if(context.length() > 3) {
				rng = context.getInt(3);
				if(rng < 1) {
					throw new CommandException("Invalid range.");
				}
			}
		}
		return new XmobKillObjective(mob, amt, loc, rng);
	}
	
	@Override
	protected void save(final StorageKey key) {
		if(amount != 1) {
			key.setInt("amount", amount);
		}
		mob.serializeKey(key.getSubKey("mob"));
		key.setString("location", SerUtils.serializeLocString(location));
		if(range != 5) {
			key.setInt("range", range);
		}
	}
	
	protected static Objective load(final StorageKey key) {
		int rng = 0, amt = 1;
		Qmob mob = null;
		Location loc = null;
		try {
			loc = SerUtils.deserializeLocString(key.getString("location", ""));
			rng = key.getInt("range", 5);
			if(rng < 1) {
				rng = 5;
			}
			amt = key.getInt("amount", 1);
			if(amt < 1) {
				amt = 1;
			}
			try {
				mob = Qmob.deserializeKey(key.getSubKey("mob"));
			}
			catch (final Exception ignore) {}
			
			return new XmobKillObjective(mob, amt, loc, rng);
		}
		catch (final Exception e) {
			return null;
		}
	}
	
	//Custom methods
	
	public boolean checkMob(final LivingEntity entity) {
		final EntityEquipment ee = entity.getEquipment();
		System.out.println("Objective: "
				+ ChatColor.translateAlternateColorCodes('&', mob.getName()).toLowerCase());
		System.out.println("Mob: " + entity.getCustomName().toLowerCase());
		return (mob.getType() == null || entity.getType() == mob.getType())
				&& (mob.getName().isEmpty() || ChatColor.stripColor(
						entity.getCustomName().toLowerCase()).startsWith(
						ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
								mob.getName()).toLowerCase())))
				&& (mob.getItem(0) == null || compareToItemStack(mob.getItem(0), ee.getItemInHand()))
				&& (mob.getItem(1) == null || compareToItemStack(mob.getItem(1), ee.getBoots()))
				&& (mob.getItem(2) == null || compareToItemStack(mob.getItem(2), ee.getLeggings()))
				&& (mob.getItem(3) == null || compareToItemStack(mob.getItem(3), ee.getChestplate()))
				&& (mob.getItem(4) == null || compareToItemStack(mob.getItem(4), ee.getHelmet()));
	}
	
	public boolean checkLocation(final Location loc) {
		if(location == null) {
			return true;
		}
		if(loc.getWorld().getName().equalsIgnoreCase(location.getWorld().getName())) {
			return loc.distanceSquared(location) < range * range;
		}
		else {
			return false;
		}
	}
	
	private boolean compareToItemStack(final Qitem qitem, final ItemStack itemStack) {
		return qitem.getMaterial() == Material.AIR || qitem.getMaterial() == itemStack.getType();
	}
}
