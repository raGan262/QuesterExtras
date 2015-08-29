package me.ragan262.questerextras.mobs;

import me.ragan262.quester.exceptions.CustomException;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.questerextras.QrExtras;

import java.util.ArrayList;
import java.util.List;

public class Mobs {
	
	private List<Qmob> mobs = new ArrayList<Qmob>();
	
	public int size() {
		return mobs.size();
	}
	
	public Qmob getMob(int id) throws CustomException {
		try {
			return mobs.get(id);
		}
		catch(Exception e) {
			throw new CustomException("No such mob.");
		}
	}
	
	public String getMobString(int id) {
		try {
			Qmob mob = mobs.get(id);
			return mob.getType() + " " + mob.getName();
		}
		catch(Exception e) {
			return "No such mob.";
		}
	}
	
	public boolean addMob(Qmob mob) {
		if(mob != null) {
			mobs.add(mob);
			return true;
		}
		return false;
	}
	
	public boolean removeMob(int ID) {
		try {
			mobs.remove(ID);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public void save(StorageKey key) {
		key.removeKey("");
		for(int i = 0; i < mobs.size(); i++) {
			mobs.get(i).serializeKey(key.getSubKey(String.valueOf(i)));
		}
	}
	
	public void load(StorageKey key) {
		if(key.hasSubKeys()) {
			int count = 0;
			for(StorageKey subKey : key.getSubKeys()) {
				Qmob mob = Qmob.deserializeKey(subKey);
				if(mob != null) {
					mobs.add(mob);
					count++;
				}
			}
			QrExtras.log.info("Mobs loaded. (" + count + ")");
		}
		else {
			QrExtras.log.info("No mobs found.");
		}
	}
}
