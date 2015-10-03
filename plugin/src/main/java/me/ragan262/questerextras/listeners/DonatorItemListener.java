package me.ragan262.questerextras.listeners;

import me.ragan262.quester.utils.Util;
import me.ragan262.questerextras.ExtrasUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DonatorItemListener implements Listener {

	// stores donator items in case of death
	private final Map<String, ItemStack[]> items = new HashMap<>();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onAction(final InventoryClickEvent event) {
		if(ExtrasUtils.isDonatorItem(event.getCurrentItem())) {
			if(!event.isShiftClick() || !(event.getInventory().getType().equals(InventoryType.CRAFTING))) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDrag(InventoryDragEvent event) {
		if(ExtrasUtils.isDonatorItem(event.getCursor()) || ExtrasUtils.isDonatorItem(
				event.getOldCursor())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDrop(final PlayerDropItemEvent event) {
		if(ExtrasUtils.isDonatorItem(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(final PlayerDeathEvent event) {
		if(Util.isPlayer(event.getEntity())) {
			final List<ItemStack> itms = new ArrayList<>();
			final Iterator<ItemStack> it = event.getDrops().iterator();
			while(it.hasNext()) {
				final ItemStack i = it.next();
				if(ExtrasUtils.isDonatorItem(i)) {
					itms.add(i);
					it.remove();
				}
			}
			if(!itms.isEmpty()) {
				items.put(event.getEntity().getName(), itms.toArray(new ItemStack[0]));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onSpawn(final PlayerRespawnEvent event) {
		final ItemStack[] itemList = items.get(event.getPlayer().getName());
		final Player player = event.getPlayer();
		if(itemList != null) {
			final Inventory inv = player.getInventory();
			inv.addItem(itemList);
			items.remove(player.getName());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlace(final BlockPlaceEvent event) {
		if(ExtrasUtils.isDonatorItem(event.getItemInHand())) {
			event.setCancelled(true);
		}
	}
}
