package me.ragan262.questerextras.objectives;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.QLocation;
import me.ragan262.quester.utils.SerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

@QElement("XARROW")
public class XarrowObjective extends Objective {
	
	private final Material material;
	private final byte data;
	private final int amount;
	private final QLocation location;
	private final int range;
	
	public XarrowObjective(final int amt, final Material mat, final int dat, final QLocation loc,
			final int rng) {
		amount = amt;
		material = mat;
		data = (byte)dat;
		location = loc;
		range = rng;
	}
	
	@Override
	public int getTargetAmount() {
		return amount;
	}
	
	@Override
	protected String info() {
		final String dataStr = data < 0 ? "" : ":" + data;
		return String.format("%s[%d%s]; AMT: %d; LOC: %s; RNG: %d", material.name(),
				material.getId(), dataStr, amount, SerUtils.displayLocation(location), range);
	}
	
	@Override
	protected String show(final int progress) {
		final String datStr = data < 0 ? " " : " (data " + data + ") ";
		return "Hit " + material.name().toLowerCase().replace('_', ' ') + datStr
				+ "with an arrow - " + (amount - progress) + "x.";
	}
	
	@Override
	protected void save(final StorageKey key) {
		key.setString("block", SerUtils.serializeItem(material, data));
		if(amount > 1) {
			key.setInt("amount", amount);
		}
		if(location != null) {
			key.setString("location", SerUtils.serializeLocString(location));
			if(range > 0) {
				key.setInt("range", range);
			}
		}
	}
	
	public boolean checkBlock(final Block block) {
		if(block.getType() != material) {
			return false;
		}
		return !(data >= 0 && data != block.getData());
	}
	
	public boolean checkLocation(final Location loc) {
		if(location != null && loc != null) {
			return location.getLocation().distanceSquared(loc) <= range * range;
		}
		return true;
	}
	
	// Custom methods
	
	@Command(min = 1, max = 4, usage = "{<block>} [amount] {[location]} [range]")
	public static Objective fromCommand(final QuesterCommandContext context)
			throws CommandException {
		final int[] itm = SerUtils.parseItem(context.getString(0));
		final Material mat = Material.getMaterial(itm[0]);
		final byte dat = (byte)itm[1];
		if(mat.getId() > 255) {
			throw new CommandException(context.getSenderLang().get("ERROR_CMD_BLOCK_UNKNOWN"));
		}
		int amt = 1;
		if(context.length() > 1) {
			amt = Integer.parseInt(context.getString(1));
		}
		if(amt < 1 || dat < -1) {
			throw new CommandException(context.getSenderLang().get("ERROR_CMD_ITEM_NUMBERS"));
		}
		int rng = 0;
		QLocation loc = null;
		if(context.length() > 2) {
			loc = SerUtils.getLoc(context.getPlayer(), context.getString(2));
			if(context.length() > 3) {
				rng = context.getInt(3);
				if(rng < 0) {
					throw new CommandException(context.getSenderLang().get(
							"ERROR_CMD_RANGE_INVALID"));
				}
			}
		}
		return new XarrowObjective(amt, mat, dat, loc, rng);
	}
	
	protected static Objective load(final StorageKey key) {
		QLocation loc = null;
		int rng = 0;
		Material mat;
		int dat, amt = 1;
		try {
			final int[] itm = SerUtils.parseItem(key.getString("block", ""));
			mat = Material.getMaterial(itm[0]);
			dat = itm[1];
		}
		catch(final IllegalArgumentException e) {
			return null;
		}
		amt = key.getInt("amount", 1);
		if(amt < 1) {
			amt = 1;
		}
		loc = SerUtils.deserializeLocString(key.getString("location", ""));
		rng = key.getInt("range", 0);
		if(rng < 0) {
			rng = 0;
		}
		return new XarrowObjective(amt, mat, dat, loc, rng);
	}
}
