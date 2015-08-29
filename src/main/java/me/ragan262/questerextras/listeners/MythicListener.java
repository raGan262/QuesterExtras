package me.ragan262.questerextras.listeners;

import me.ragan262.quester.ActionSource;
import me.ragan262.quester.Quester;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.profiles.PlayerProfile;
import me.ragan262.quester.profiles.ProfileManager;
import me.ragan262.quester.quests.Quest;
import me.ragan262.quester.utils.Util;
import me.ragan262.questerextras.objectives.XmythicKillObjective;
import net.elseland.xikage.MythicMobs.API.Bukkit.Events.MythicMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class MythicListener implements Listener {

	private final ProfileManager pm;

	public MythicListener() {
		pm = Quester.getInstance().getProfileManager();
	}


	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void mythicDeath(MythicMobDeathEvent event) {
		if(event.getKiller() instanceof Player) {
			final Player player = (Player)event.getKiller();
			if(!Util.isPlayer(player)) {
				return;
			}
			final PlayerProfile prof = pm.getProfile(player);
			final Quest quest = prof.getQuest();
			if(quest != null) {
				if(!quest.allowedWorld(player.getWorld().getName().toLowerCase())) {
					return;
				}
				final List<Objective> objs = quest.getObjectives();
				for(int i = 0; i < objs.size(); i++) {
					if(objs.get(i).getType().equalsIgnoreCase("XMYTHKILL")) {
						if(!pm.isObjectiveActive(prof, i)) {
							continue;
						}
						final XmythicKillObjective obj = (XmythicKillObjective)objs.get(i);
						if(obj.checkMob(event.getMobType().getInternalName())) {
							pm.incProgress(player, ActionSource.listenerSource(event), i);
							return;
						}
					}
				}
			}
		}
	}
}
