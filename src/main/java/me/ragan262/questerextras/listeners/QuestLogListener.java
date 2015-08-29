package me.ragan262.questerextras.listeners;

import me.ragan262.quester.Quester;
import me.ragan262.quester.events.ObjectiveCompleteEvent;
import me.ragan262.quester.events.QuestCancelEvent;
import me.ragan262.quester.events.QuestCompleteEvent;
import me.ragan262.quester.events.QuestStartEvent;
import me.ragan262.quester.lang.LanguageManager;
import me.ragan262.quester.profiles.PlayerProfile;
import me.ragan262.quester.profiles.ProfileManager;
import me.ragan262.quester.quests.Quest;
import me.ragan262.quester.quests.QuestManager;
import me.ragan262.quester.utils.Util;
import me.ragan262.questerextras.items.QuestLog;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Iterator;

public class QuestLogListener implements Listener {

	private final QuestManager qm;
	private final ProfileManager pm;
	private final LanguageManager lm;

	public QuestLogListener() {
		qm = Quester.getInstance().getQuestManager();
		pm = Quester.getInstance().getProfileManager();
		lm = Quester.getInstance().getLanguageManager();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onAction(final InventoryClickEvent event) {
		if(QuestLog.isQuestLog(event.getCurrentItem())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDrag(InventoryDragEvent event) {
		if(QuestLog.isQuestLog(event.getOldCursor()) || QuestLog.isQuestLog(event.getCursor())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDrop(final PlayerDropItemEvent event) {
		if(QuestLog.isQuestLog(event.getItemDrop().getItemStack())) {
			event.getPlayer().getInventory().setItem(QuestLog.getItemSlot(), event.getItemDrop().getItemStack());
			event.getItemDrop().remove();
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onSpawn(final PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		if(!QuestLog.isQuestLog(player.getInventory().getItem(QuestLog.getItemSlot()))) {
			final ItemStack qLog = QuestLog.getQuestLogItem();
			final BookMeta bm = (BookMeta)qLog.getItemMeta();
			QuestLog.updateQuestList(player, bm, qm, pm.getProfile(player));
			qLog.setItemMeta(bm);
			player.getInventory().setItem(QuestLog.getItemSlot(), qLog);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(final PlayerDeathEvent event) {
		if(Util.isPlayer(event.getEntity())) {
			final Iterator<ItemStack> it = event.getDrops().iterator();
			while(it.hasNext()) {
				final ItemStack i = it.next();
				if(QuestLog.isQuestLog(i)) {
					it.remove();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if(!QuestLog.isQuestLog(player.getInventory().getItem(QuestLog.getItemSlot()))) {
			final ItemStack qLog = QuestLog.getQuestLogItem();
			final BookMeta bm = (BookMeta)qLog.getItemMeta();
			QuestLog.updateQuestList(player, bm, qm, pm.getProfile(player));
			qLog.setItemMeta(bm);
			player.getInventory().setItem(QuestLog.getItemSlot(), qLog);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onQuestStart(final QuestStartEvent event) {
		updateLog(event.getPlayer(), event.getQuest(), -1, false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onObjectiveComplete(final ObjectiveCompleteEvent event) {
		updateLog(event.getPlayer(), event.getQuest(), event.getObjectiveID(), false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuestComplete(final QuestCompleteEvent event) {
		updateLog(event.getPlayer(), event.getQuest(), -3, true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onQuestCancel(final QuestCancelEvent event) {
		updateLog(event.getPlayer(), event.getQuest(), -2, true);
	}

	private void updateLog(final Player player, final Quest quest, final int occassion,
			final boolean updateList) {
		PlayerProfile prof = pm.getProfile(player);
		QuestLog.updateQuestLog(player, quest, prof, lm.getLang(prof.getLanguage()),
				occassion, qm, updateList);
		player.sendMessage(ChatColor.GRAY + "Quest Log Updated.");
	}
}
