package me.ragan262.questerextras.objectives;

import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.commandmanager.annotations.Command;
import me.ragan262.quester.commandmanager.exceptions.CommandException;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.elements.QElement;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.quester.utils.Util;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

@QElement("XMYTHKILL")
public class XmythicKillObjective extends Objective {
	
	private final List<String> mobs;
	private final String mobString;
	private final int amount;
	
	public XmythicKillObjective(List<String> mobs, int amount) {
		Validate.notNull(mobs);
		this.mobs = mobs;
		this.mobString = Util.implodeIterable(mobs, ",");
		this.amount = amount;
	}
	
	@Override
	public int getTargetAmount() {
		return amount;
	}
	
	@Override
	protected String show(int progress) {
		return "Kill mythic mobs. (%a/%t)";
	}
	
	@Override
	protected String info() {
		return mobString + "; AMT: " + amount;
	}
	
	@Override
	protected void save(StorageKey key) {
		key.setString("mobs", mobString);
		if(amount > 1) {
			key.setInt("amount", amount);
		}
	}
	
	public boolean checkMob(String mob) {
		return mobs.contains(mob);
	}
	
	@Command(min = 1, max = 2, usage = "<mobs> [amount]")
	public static Objective fromCommand(final QuesterCommandContext context)
			throws CommandException {
		List<String> mobs = new ArrayList<String>();
		for(String mob : context.getString(0).split(",")) {
			mobs.add(mob);
		}
		int amount = 1;
		if(context.length() > 1) {
			amount = context.getInt(1);
			if(amount < 1) {
				amount = 1;
			}
		}
		return new XmythicKillObjective(mobs, amount);
	}
	
	protected static Objective load(final StorageKey key) {
		int amt = 1;
		List<String> mobs = new ArrayList<String>();
		if(key.getString("mobs") == null) {
			return null;
		}
		for(String mob : key.getString("mobs").split(",")) {
			mobs.add(mob);
		}
		amt = key.getInt("amount", 1);
		if(amt < 1) {
			amt = 1;
		}
		return new XmythicKillObjective(mobs, amt);
	}
}
