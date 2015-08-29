package me.ragan262.questerextras.qevents;

import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.elements.Qevent;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.questerextras.QrExtras;
import me.ragan262.questerextras.items.Qitem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@QElement("XITEM")
public final class XitemQevent extends Qevent {

	private Qitem item = null;
	
	public XitemQevent(Qitem item) {
		this.item = item.getCopy();
	}
	
	@Override
	public String info() {
		return item.getInfo("      ") + "\n  ";
	}

	@Override
	protected void run(Player player, Quester plugin) {
		giveItem(item, player, item.getAmount());
	}

	@Command(
			min = 1,
			max = 2,
			usage = "<item ID in list> [new amount]")
	public static Qevent fromCommand(QuesterCommandContext context) throws QuesterException {
		Qitem item = QrExtras.plugin.items.getItem(context.getInt(0));
		if(context.length() > 1) {
			if(context.getInt(1) > 0) {
				item.setAmount(context.getInt(1));
			}
		}
		
		return new XitemQevent(item);
	}
	
	@Override
	protected void save(StorageKey key) {
		key.removeKey("item");
		item.serializeKey(key.getSubKey("item"));
	}
	
	protected static Qevent load(StorageKey key) {
		Qitem item = null;
		if(key.getSubKey("item").hasSubKeys()) {
			item = Qitem.deserializeKey(key.getSubKey("item"));
		}
		else {
			return null;
		}
		
		return new XitemQevent(item);
	}
	
	public static void giveItem(Qitem item, Player player, int amount) {
		ItemStack is = item.getItemStack();
		int maxSize = item.getMaterial().getMaxStackSize();
        int toGive = amount;
        int numSpaces = 0;
        int given = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack i : contents) {
            if (i == null) {
                numSpaces += maxSize;
            } 
            else if (i.isSimilar(is)) {
                   numSpaces += (maxSize - i.getAmount());
            }
        }
        given = Math.min(toGive, numSpaces);
        toGive -= given;
        numSpaces = (int) Math.ceil((double)given / (double)maxSize);
        int round;
        PlayerInventory inv = player.getInventory();
        for(int k=0; k<numSpaces; k++) {
        	round = Math.min(maxSize, given);
	        is.setAmount(round);
	        inv.addItem(is);
	        given -= round;
        }

        if(toGive > 0) {
            numSpaces = (int) Math.ceil((double)toGive / (double)maxSize);
        	for(int k=0; k<numSpaces; k++) {
        		given = Math.min(toGive, maxSize);
        		is.setAmount(given);
	        	player.getWorld().dropItem(player.getLocation(), is);
	        	toGive -= given;
        	}
        }
	}
}
