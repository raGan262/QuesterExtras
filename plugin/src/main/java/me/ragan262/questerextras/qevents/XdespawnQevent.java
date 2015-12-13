package me.ragan262.questerextras.qevents;

import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.elements.Qevent;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.QLocation;
import me.ragan262.quester.utils.SerUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@QElement("XDESPAWN")
public final class XdespawnQevent extends Qevent {
	
	private final QLocation min;
	private final QLocation max;
	private final EntityType entity;
	
	public XdespawnQevent(final EntityType ent, final QLocation loc1, final QLocation loc2) {
		min =
				new QLocation(loc1.getWorldName(), Math.min(loc1.getX(), loc2.getX()), Math.min(
						loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()), 0, 0);
		max =
				new QLocation(loc1.getWorldName(), Math.max(loc1.getX(), loc2.getX()), Math.max(
						loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()), 0, 0);
		entity = ent;
	}
	
	@Override
	public String info() {
		final String entName = entity == null ? "ALL" : entity.getName();
		return entName + "; MIN: " + SerUtils.displayLocation(min) + "; MAX: "
				+ SerUtils.displayLocation(max);
	}
	
	@Override
	protected void run(final Player player, final Quester plugin) {
		final Chunk ch1 = min.getLocation().getChunk();
		final Chunk ch2 = max.getLocation().getChunk();
		final World world = min.getLocation().getWorld();
		final int minX = Math.min(ch1.getX(), ch2.getX());
		final int maxX = Math.max(ch1.getX(), ch2.getX());
		final int minZ = Math.min(ch1.getZ(), ch2.getZ());
		final int maxZ = Math.max(ch1.getZ(), ch2.getZ());
		for(int i = minX; i <= maxX; i++) {
			for(int j = minZ; j <= maxZ; j++) {
				for(final Entity e : world.getChunkAt(i, j).getEntities()) {
					if(isInside(e)) {
						e.remove();
					}
				}
			}
		}
	}
	
	@Override
	protected void save(final StorageKey key) {
		if(entity != null) {
			key.setInt("entity", entity.getTypeId());
		}
		key.setString("minlocation", SerUtils.serializeLocString(min));
		key.setString("maxlocation", SerUtils.serializeLocString(max));
	}
	
	private boolean isInside(final Entity e) {
		if(e instanceof Player || e.hasMetadata("NPC")) {
			return false;
		}
		if(entity == null || e.getType() == entity) {
			final Location l = e.getLocation();
			return min.getX() <= l.getX() && l.getX() <= max.getX() && min.getY() <= l.getY()
					&& l.getY() <= max.getY() && min.getZ() <= l.getZ() && l.getZ() <= max.getZ();
		}
		return false;
	}
	
	@Command(min = 2, max = 3, usage = "{<location1>} {<location2>} {[entity]}")
	public static Qevent fromCommand(final QuesterCommandContext context) throws CommandException {
		EntityType ent = null;
		QLocation loc1 = null;
		QLocation loc2 = null;
		try {
			loc1 = SerUtils.getLoc(context.getPlayer(), context.getString(0));
			loc2 = SerUtils.getLoc(context.getPlayer(), context.getString(1));
			if(!loc1.getWorldName().equalsIgnoreCase(loc2.getWorldName())) {
				throw new IllegalArgumentException("Worlds are not the same.");
			}
		}
		catch(final IllegalArgumentException e) {
			throw new CommandException(e.getMessage());
		}
		if(context.length() > 2) {
			try {
				ent = SerUtils.parseEntity(context.getString(2));
			}
			catch(final Exception e) {
				throw new CommandException("Could not resolve entity " + context.getString(2)
						+ ".");
			}
		}
		return new XdespawnQevent(ent, loc1, loc2);
	}
	
	protected static Qevent load(final StorageKey key) {
		EntityType ent = null;
		QLocation min = null;
		QLocation max = null;
		try {
			min = SerUtils.deserializeLocString(key.getString("minlocation", ""));
			max = SerUtils.deserializeLocString(key.getString("maxlocation", ""));
			try {
				ent = SerUtils.parseEntity(key.getString("entity", ""));
			}
			catch(final Exception ignore) {
			}
		}
		catch(final Exception e) {
			return null;
		}

		return new XdespawnQevent(ent, min, max);
	}
}
