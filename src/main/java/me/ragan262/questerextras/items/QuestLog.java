package me.ragan262.questerextras.items;

import me.ragan262.quester.QConfiguration;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.elements.Qevent;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.lang.QuesterLang;
import me.ragan262.quester.profiles.PlayerProfile;
import me.ragan262.quester.profiles.QuestProgress;
import me.ragan262.quester.profiles.QuestProgress.ObjectiveStatus;
import me.ragan262.quester.quests.Quest;
import me.ragan262.quester.quests.QuestManager;
import me.ragan262.questerextras.qevents.XlogQevent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestLog {
	
	private static final ItemStack QuestLogItem;
	private static int itemSlot = 8;
	
	static {
		final ItemStack questLog = new ItemStack(Material.WRITTEN_BOOK, 1);
		final BookMeta bm = (BookMeta)questLog.getItemMeta();
		bm.setTitle(ChatColor.BLUE + "Quest Log");
		questLog.setItemMeta(bm);
		QuestLogItem = questLog;
	}
	
	public static int getItemSlot() {
		return itemSlot;
	}
	
	public static void setItemSlot(final int itemSlot) {
		QuestLog.itemSlot = itemSlot;
	}
	
	public static ItemStack getQuestLogItem() {
		return QuestLogItem.clone();
	}
	
	public static boolean isQuestLog(final ItemStack item) {
		if(item != null && item.getType() == Material.WRITTEN_BOOK) {
			return ChatColor.stripColor(((BookMeta)item.getItemMeta()).getTitle()).equals(
					"Quest Log");
		}
		return false;
	}
	
	public static void updateQuestLog(final Player player, final Quest quest,
			final PlayerProfile prof, final QuesterLang lang, final int occassion,
			final QuestManager qm, final boolean updateList) {
		
		final ItemStack result = player.getInventory().getItem(itemSlot);
		
		if(isQuestLog(result)) {
			final BookMeta meta = (BookMeta)result.getItemMeta();
			final List<StringBuilder> pages = new ArrayList<StringBuilder>();
			final List<String> story = new ArrayList<String>();
			pages.add(new StringBuilder());
			/* [0] => current page; [1] => current number of letters on page */
			final int[] pageInfo = {0, 0};
			final Map<Integer, Map<Integer, Qevent>> events = quest.getQeventMap("XLOG");

			String completed = occassion == -3 ? ChatColor.GREEN + " (Completed)" : "";
			/* add quest name */
			addText(pages, pageInfo, ChatColor.GOLD + quest.getName() + completed + "\n" + ChatColor.BLACK);
			
			/* add all onStart events */
			if(events.containsKey(-1)) {
				for(final Qevent e : events.get(-1).values()) {
					story.add(" " + ((XlogQevent)e).getMessage(player.getName()));
				}
			}
			
			/* add onObjective events */
			final List<Objective> objs = quest.getObjectives();
			QuestProgress progress = prof.getProgress();
			if(progress == null) {
				progress = QuestProgress.getEmptyProgress(quest);
			}
			for(int i = 0; i < objs.size(); i++) {
				if(occassion == -3 || progress.getObjectiveStatus(i) == ObjectiveStatus.COMPLETED) {
					if(events.containsKey(i)) {
						for(final Qevent e : events.get(i).values()) {
							story.add(" " + ((XlogQevent)e).getMessage(player.getName()));
						}
					}
				}
				ObjectiveStatus status;
				if(occassion == -1) {
					status =
							objs.get(i).getPrerequisites().isEmpty() ? ObjectiveStatus.ACTIVE
									: ObjectiveStatus.INACTIVE;
				}
				else if(occassion == -3) {
					status = ObjectiveStatus.COMPLETED;
				}
				else {
					status = progress.getObjectiveStatus(i);
				}
				if(!objs.get(i).isHidden()
						&& (status == ObjectiveStatus.ACTIVE || status == ObjectiveStatus.COMPLETED || !QConfiguration.ordOnlyCurrent)) {
					final char tag =
							status == ObjectiveStatus.COMPLETED ? (char)9745 : (char)9744;
					ChatColor color = ChatColor.RED;
					switch(status) {
						case ACTIVE:
							if(occassion != -2) {
								color = ChatColor.BLUE;
							}
							break;
						case COMPLETED:
							color = ChatColor.GREEN;
							break;
						default:
							break;
					}
					addText(pages, pageInfo, ChatColor.BLACK + "\n" + tag + color
							+ objs.get(i).inShow(lang));
				}
			}
			
			/* write intro and objective llog events */
			addText(pages, pageInfo, "\n\n" + ChatColor.BLACK);
			for(final String s : story) {
				addText(pages, pageInfo, s);
			}
			
			/* add all onComplete or onCancel llog events */
			if(occassion == -2 && events.containsKey(-2)) {
				for(final Qevent e : events.get(-2).values()) {
					addText(pages, pageInfo, " " + ((XlogQevent)e).getMessage(player.getName()));
				}
			}
			else if(occassion == -3 && events.containsKey(-3)) {
				for(final Qevent e : events.get(-3).values()) {
					addText(pages, pageInfo, " " + ((XlogQevent)e).getMessage(player.getName()));
				}
			}

			/* removed for now
			if(updateList) {
				meta.setPages("", "");
				updateQuestList(player, meta, qm, prof);
			}
			else {
				meta.setPages(meta.getPage(1), meta.getPage(2));
			}*/

			for(int i = 0; i < pages.size(); i++) {
				meta.addPage(pages.get(i).toString());
			}

			if(pages.size() == 0) {
				meta.setPages(ChatColor.RED + "No ongoing quest.");
			}
			
			result.setItemMeta(meta);
			player.getInventory().setItem(itemSlot, result);
		}
	}
	
	private static void addText(final List<StringBuilder> pages, final int[] pageInfo,
			final String text) {
		final int length = textLength(text);
		if(pageInfo[1] + length > 256) {
			pageInfo[0]++;
			pages.add(new StringBuilder());
			pageInfo[1] = 0;
		}
		pageInfo[1] += length;
		pages.get(pageInfo[0]).append(text);
	}
	
	private static int textLength(final String text) {
		int result = ChatColor.stripColor(text).length();
		final Matcher m = Pattern.compile("\\n").matcher(text);
		while(m.find()) {
			result += 12;
		}
		return result;
	}
	
	public static void updateQuestList(final Player player, final BookMeta meta,
			final QuestManager qm, final PlayerProfile prof) {
		final StringBuilder page1 = new StringBuilder("Quest points: ");
		final StringBuilder page2 = new StringBuilder();
		StringBuilder page = page1;
		page.append(ChatColor.GOLD).append(prof.getPoints()).append('\n');
		page.append(ChatColor.BLACK).append("Available quests:\n");
		int counter = 0;
		for(final Quest q : qm.getQuests()) {
			if(qm.isQuestActive(q)) {
				if(counter == 11) {
					page = page2;
				}
				else if(counter > 23) {
					break;
				}
				
				page.append(ChatColor.BLACK).append("* ");
				try {
					if(qm.areConditionsMet(player, q, null)) {
						page.append(ChatColor.GREEN);
					}
					else {
						page.append(ChatColor.RED);
					}
				}
				catch(final QuesterException ignore) {
				}
				page.append(q.getName()).append('\n');
				counter++;
			}
		}
		if(meta.getPageCount() < 2) {
			meta.setPages("", "");
		}
		meta.setPage(1, page1.toString());
		meta.setPage(2, page2.toString());
	}
}
