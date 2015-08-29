package me.ragan262.questerextras.listeners;

import me.ragan262.quester.ActionSource;
import me.ragan262.quester.Quester;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.profiles.PlayerProfile;
import me.ragan262.quester.profiles.ProfileManager;
import me.ragan262.quester.quests.Quest;
import me.ragan262.quester.utils.Util;
import me.ragan262.questerextras.objectives.XarrowObjective;
import me.ragan262.questerextras.objectives.XmobKillObjective;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.List;

public class ObjectiveListener implements Listener {

	private final ProfileManager pm;
	
	public ObjectiveListener() {
		pm = Quester.getInstance().getProfileManager();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHit(final ProjectileHitEvent event) {
		final Projectile proj = event.getEntity();
		
		if(proj instanceof Arrow) {
			final Arrow arrow = (Arrow) proj;
			if(!(arrow.getShooter() instanceof Player)) {
				return;
			}
			final Player player = (Player) arrow.getShooter();
			final PlayerProfile prof = pm.getProfile(player);
			final Quest quest = prof.getQuest();
			if(quest != null) {
				if(!quest.allowedWorld(player.getWorld().getName())) {
					return;
				}
				final List<Objective> objs = quest.getObjectives();
				for(int i = 0; i < objs.size(); i++) {
					if(objs.get(i).getType().equalsIgnoreCase("XARROW")) {
						if(!pm.isObjectiveActive(prof, i)) {
							continue;
						}
						final XarrowObjective obj = (XarrowObjective) objs.get(i);
						
						final Vector arrowloc = arrow.getLocation().toVector();
						//Vector arrowdir = arrow.getLocation().getDirection();
						final Vector playerdir = player.getLocation().getDirection();
						final BlockIterator bi =
								new BlockIterator(arrow.getWorld(), arrowloc, playerdir, 0, 2);
						
						Block block = null;
						while(bi.hasNext()) {
							block = bi.next();
							if(block.getType() != Material.AIR) {
								break;
							}
						}
						if(block == null) {
							return;
						}
						if(obj.checkLocation(block.getLocation()) && obj.checkBlock(block)) {
							pm.incProgress(player, ActionSource.listenerSource(event), i);
							return;
						}
					}
				}
			}
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMobDeath(final EntityDeathEvent event) {
		if(event.getEntity().getKiller() != null) {
			final Player player = event.getEntity().getKiller();
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
					if(objs.get(i).getType().equalsIgnoreCase("XMOBKILL")) {
						if(!pm.isObjectiveActive(prof, i)) {
							continue;
						}
						final LivingEntity ent = event.getEntity();
						final XmobKillObjective obj = (XmobKillObjective) objs.get(i);
						if(obj.checkMob(ent) && obj.checkLocation(ent.getLocation())) {
							pm.incProgress(player, ActionSource.listenerSource(event), i);
							return;
						}
					}
				}
			}
		}
	}
}
