package me.ragan262.questerextras.listeners;

import me.ragan262.questerextras.QrExtras;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class PluginListener implements Listener {

	private final QrExtras plugin;

	public PluginListener(QrExtras plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuesterDisable(PluginDisableEvent event) {
		if(event.getPlugin().getName().equals("Quester")) {
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
}
