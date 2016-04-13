package me.ragan262.questerextras.qevents;

import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.elements.Qevent;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.QLocation;
import me.ragan262.quester.utils.SerUtils;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.items.Qitem;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@QElement("XFILL")
public final class XfillQevent extends Qevent {
	
	private final Qitem item;
	private final QLocation location;
	
	public XfillQevent(final Qitem item, final QLocation loc) {
		this.item = item;
		location = loc;
	}
	
	@Override
	public String info() {
		String target;
		if(location == null) {
			target = "INVALID";
		}
		else {
			target = "BLOCK (" + SerUtils.displayLocation(location) + ")";
		}
		return target + "\n      " + item.getInfo("      ") + "\n  ";
	}
	
	@Override
	protected void run(final Player player, final Quester plugin) {
		final InventoryHolder ih = XfillQevent.getInventoryHolder(location.getLocation());
		if(ih == null) {
			QrExtras.log.warning("XFILL: Invalid container location.");
			return;
		}
		giveItem(item, ih.getInventory(), item.getAmount(), null);
	}
	
	@Override
	protected void save(final StorageKey key) {
		key.removeKey("item");
		item.serializeKey(key.getSubKey("item"));
		if(location != null) {
			key.setString("container", SerUtils.serializeLocString(location));
		}
	}
	
	@Command(min = 2, max = 3, usage = "<item ID in list> <container location> [amount]")
	public static Qevent fromCommand(final QuesterCommandContext context)
			throws QuesterException, CommandException {
		final Qitem item = QrExtras.plugin.items.getItem(context.getInt(0)).getCopy();
		QLocation loc = SerUtils.getLoc(context.getPlayer(), context.getString(1),
				context.getSenderLang());
		if(XfillQevent.getInventoryHolder(loc.getLocation()) == null) {
			throw new CommandException("Invalid container location.");
		}
		if(context.length() > 2) {
			if(context.getInt(2) > 0) {
				item.setAmount(context.getInt(2));
			}
		}

		return new XfillQevent(item, loc);
	}
	
	protected static Qevent load(final StorageKey key) {
		Qitem item = null;
		QLocation loc = null;
		if(key.getString("container", "") != "") {
			loc = SerUtils.deserializeLocString(key.getString("container", ""));
			if(getInventoryHolder(loc.getLocation()) == null) {
				QrExtras.log.warning("XFILL: Invalid container location.");
			}
		}
		if(key.getSubKey("item").hasSubKeys()) {
			item = Qitem.deserializeKey(key.getSubKey("item"));
		}
		else {
			return null;
		}
		
		return new XfillQevent(item, loc);
	}
	
	public static void giveItem(final Qitem item, final Inventory inv, final int amount,
			final Location dropAt) {
		final ItemStack is = item.getItemStack();
		final int maxSize = item.getMaterial().getMaxStackSize();
		int toGive = amount;
		int numSpaces = 0;
		int given = 0;
		final ItemStack[] contents = inv.getContents();
		for(final ItemStack i : contents) {
			if(i == null) {
				numSpaces += maxSize;
			}
			else if(i.isSimilar(is)) {
				numSpaces += maxSize - i.getAmount();
			}
		}
		given = Math.min(toGive, numSpaces);
		toGive -= given;
		numSpaces = (int)Math.ceil((double)given / (double)maxSize);
		int round;
		for(int k = 0; k < numSpaces; k++) {
			round = Math.min(maxSize, given);
			is.setAmount(round);
			inv.addItem(is);
			given -= round;
		}
		
		if(toGive > 0) {
			if(dropAt != null) {
				numSpaces = (int)Math.ceil((double)toGive / (double)maxSize);
				for(int k = 0; k < numSpaces; k++) {
					given = Math.min(toGive, maxSize);
					is.setAmount(given);
					dropAt.getWorld().dropItem(dropAt, is);
					toGive -= given;
				}
			}
			else {
				QrExtras.log.info("XFILL: Container is out of space. " + toGive
						+ " items not given. (" + item.getMaterial().name() + ":"
						+ item.getDamage() + ")");
			}
		}
	}

	private static InventoryHolder getInventoryHolder(Location loc) {
		if(loc == null) {
			return null;
		}
		final BlockState bs = loc.getBlock().getState();
		if(bs instanceof InventoryHolder) {
			return (InventoryHolder)bs;
		}
		return null;
	}
}
