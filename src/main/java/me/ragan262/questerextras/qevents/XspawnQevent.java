package me.ragan262.questerextras.qevents;

import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.elements.Qevent;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.SerUtils;
import me.ragan262.quester.utils.Util;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.mobs.Qmob;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@QElement("XSPAWN")
public final class XspawnQevent extends Qevent {
	
	private final Location location;
	private final Qmob mob;
	private final int range;
	private final int amount;
	
	public XspawnQevent(final Location loc, final int rng, final Qmob mob, final int amt) {
		location = loc;
		range = rng;
		this.mob = mob.getCopy();
		amount = amt;
	}
	
	@Override
	public String info() {
		String locStr = "PLAYER";
		if(location != null) {
			locStr = SerUtils.displayLocation(location);
		}
		final String name = mob.getName().isEmpty() ? "" : mob.getName() + ";";
		return name + " AMT: " + amount + "; LOC: " + locStr + "; RNG: " + range;
	}
	
	@Override
	protected void run(final Player player, final Quester plugin) {
		Location temp;
		if(location == null) {
			temp = player.getLocation();
		}
		else {
			temp = location;
		}
		for(int i = 0; i < amount; i++) {
			mob.spawnAt(Util.move(temp, range));
		}
	}
	
	@Command(min = 3, max = 4, usage = "<mob ID> <amount> {<location>} [range]")
	public static Qevent fromCommand(final QuesterCommandContext context) throws CommandException, QuesterException {
		final Qmob mob = QrExtras.plugin.mobs.getMob(context.getInt(0));
		final int amt = context.getInt(1);
		final Location loc = SerUtils.getLoc(context.getPlayer(), context.getString(2));
		int rng = 0;
		if(amt < 1) {
			throw new CommandException(context.getSenderLang().get("ERROR_CMD_AMOUNT_POSITIVE"));
		}
		if(context.length() > 3) {
			rng = context.getInt(3);
			if(rng < 0) {
				throw new CommandException(context.getSenderLang().get("ERROR_CMD_RANGE_INVALID"));
			}
		}
		return new XspawnQevent(loc, rng, mob, amt);
	}
	
	@Override
	protected void save(final StorageKey key) {
		if(amount != 1) {
			key.setInt("amount", amount);
		}
		mob.serializeKey(key.getSubKey("mob"));
		key.setString("location", SerUtils.serializeLocString(location));
		if(range != 0) {
			key.setInt("range", range);
		}
	}
	
	protected static Qevent load(final StorageKey key) {
		int rng = 0, amt = 1;
		Qmob mob = null;
		Location loc = null;
		try {
			loc = SerUtils.deserializeLocString(key.getString("location", ""));
			rng = key.getInt("range", 0);
			if(rng < 0) {
				rng = 0;
			}
			amt = key.getInt("amount", 1);
			if(amt < 1) {
				amt = 1;
			}
			try {
				mob = Qmob.deserializeKey(key.getSubKey("mob"));
			}
			catch (final Exception ignore) {}
			if(mob == null) {
				return null;
			}
		}
		catch (final Exception e) {
			return null;
		}
		
		return new XspawnQevent(loc, rng, mob, amt);
	}
}
