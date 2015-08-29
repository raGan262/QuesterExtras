package me.ragan262.questerextras.qevents;

import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.elements.Qevent;
import me.ragan262.quester.storage.StorageKey;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@QElement("XLOG")
public final class XlogQevent extends Qevent {

	private final String message;
	private final String rawmessage;
	
	public XlogQevent(String msg) {
		this.rawmessage = msg;
		this.message = ChatColor.translateAlternateColorCodes('&', rawmessage).replaceAll("\\\\n", "\n");
	}
	
	@Override
	public String info() {
		return message;
	}

	@Override
	protected void run(Player player, Quester plugin) {
		// does nothing, it is only retrieved and read by quest log
	}
	
	public String getMessage(String playerName) {
		return message.replace("%p", playerName);
	}

	@Command(
			min = 1,
			max = 1,
			usage = "<text>")
	public static Qevent fromCommand(QuesterCommandContext context) {
		return new XlogQevent(context.getString(0));
	}

	@Override
	protected void save(StorageKey key) {
		key.setString("text", rawmessage);
	}
	
	protected static Qevent load(StorageKey key) {
		String msg;
		
		msg = key.getString("text");
		if(msg == null) {
			return null;
		}
		
		return new XlogQevent(msg);
	}
}
