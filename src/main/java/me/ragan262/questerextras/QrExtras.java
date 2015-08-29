package me.ragan262.questerextras;

import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.CommandManager;
import me.ragan262.quester.commandmanager.QuesterCommandExceptionHandler;
import me.ragan262.quester.commandmanager.QuesterContextFactory;
import me.ragan262.quester.commandmanager.context.ContextFactory;
import me.ragan262.quester.elements.Element;
import me.ragan262.quester.elements.ElementManager;
import me.ragan262.quester.exceptions.ElementException;
import me.ragan262.quester.storage.ConfigStorage;
import me.ragan262.questerextras.commands.Commands;
import me.ragan262.questerextras.items.Items;
import me.ragan262.questerextras.items.QuestLog;
import me.ragan262.questerextras.listeners.*;
import me.ragan262.questerextras.mobs.Mobs;
import me.ragan262.questerextras.objectives.XarrowObjective;
import me.ragan262.questerextras.objectives.XmobKillObjective;
import me.ragan262.questerextras.objectives.XmythicKillObjective;
import me.ragan262.questerextras.qevents.XdespawnQevent;
import me.ragan262.questerextras.qevents.XfillQevent;
import me.ragan262.questerextras.qevents.XlogQevent;
import me.ragan262.questerextras.qevents.XspawnQevent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class QrExtras extends JavaPlugin {
	
	public static Logger log = null;
	public static QrExtras plugin = null;
	public ConfigStorage config = null;
	public Items items = null;
	public Mobs mobs = null;
	private CommandManager commands = null;

	private boolean disable = false;

	@Override
	public void onLoad() {
		//register elements
		@SuppressWarnings("unchecked")
		Class<? extends Element>[] elements = new Class[]{
				XarrowObjective.class,
				XmobKillObjective.class,
				XmythicKillObjective.class,
				XdespawnQevent.class,
				XfillQevent.class,
				XspawnQevent.class,
				XlogQevent.class
		};
		ElementManager em = ElementManager.getInstance();
		if(em == null) {
			getLogger().severe("Element manager instance not available.");
			disable = true;
			return;
		}
		for(Class<? extends Element> c : elements) {
			try {
				em.register(c);
			}
			catch (ElementException e) {
				getLogger().info("Failed to register element: " + e.getMessage());
			}
		}
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		log = getLogger();
		if(!checkQuester()) {
			disable = true;
		}
		if(disable) {
			getPluginLoader().disablePlugin(this);
			return;
		}
		config = new ConfigStorage(new File(getDataFolder(), "config.yml"), getLogger(), null);
		config.load();
		Quester q = Quester.getInstance();
		ContextFactory cf = new QuesterContextFactory(q.getLanguageManager(), q.getProfileManager());
		commands = new CommandManager(cf, log, "/qe", this);
		commands.setExceptionHandler(new QuesterCommandExceptionHandler(this.getLogger()));
		plugin = this;
		items = new Items();
		items.load(config.getKey("items"));
		mobs = new Mobs();
		mobs.load(config.getKey("mobs"));
		
		
		commands.register(Commands.class);
		getServer().getPluginManager().registerEvents(new PluginListener(this), this);
		getServer().getPluginManager().registerEvents(new ObjectiveListener(), this);
		if(getConfig().getBoolean("donator-item", false)) {
			getServer().getPluginManager().registerEvents(new DonatorItemListener(), this);
			getLogger().info("Donator items enabled.");
		}
		if(getConfig().getBoolean("quest-log.enabled", false)) {
			getServer().getPluginManager().registerEvents(new QuestLogListener(), this);
			int slot = getConfig().getInt("quest-log.slot", 8);
			if(slot < 0 || slot > 8) {
				slot = 8;
			}
			QuestLog.setItemSlot(slot);
			getLogger().info("Quest log enabled in slot " + slot + ".");
		}
		if(getServer().getPluginManager().getPlugin("MythicMobs") != null) {
			getServer().getPluginManager().registerEvents(new MythicListener(), this);
			getLogger().info("MythicMobs found and hooked.");
		}
		// reset disable
		disable = false;
	}
	
	@Override
	public void onDisable() {
		items.save(config.getKey("items"));
		mobs.save(config.getKey("mobs"));
		config.save();
		log = null;
		plugin = null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!command.getName().equalsIgnoreCase("qe")) {
			return false;
		}
		commands.handleCommand(args, sender);
		return true;
	}
	
	public CommandManager getCommands() {
		return commands;
	}

	private boolean checkQuester() {
		try {
			Class.forName("me.ragan262.quester.Quester");
		}
		catch (final Exception e) {
			getLogger().severe("Quester not found. Disabling.");
			return false;
		}
		try {
			Class.forName("me.ragan262.quester.holder.QuesterTrait");
			getLogger().severe("Quester in use is too old, update to newer version. Disabling.");
		}
		catch (final Exception e) {
			return true;
		}
		return false;
	}
}
