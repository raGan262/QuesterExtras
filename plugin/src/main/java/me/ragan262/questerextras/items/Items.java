package me.ragan262.questerextras.items;

import me.ragan262.quester.exceptions.CustomException;
import me.ragan262.quester.storage.StorageKey;
import me.ragan262.questerextras.QrExtras;

import java.util.ArrayList;
import java.util.List;

public class Items {
	
	private List<Qitem> items = new ArrayList<>();
	
	public int size() {
		return items.size();
	}
	
	public Qitem getItem(int id) throws CustomException {
		try {
			return items.get(id);
		}
		catch(Exception e) {
			throw new CustomException("No such item.");
		}
	}
	
	public String getItemString(int id) {
		try {
			Qitem item = items.get(id);
			return item.getType() + " " + item.getName();
		}
		catch(Exception e) {
			return "No such item.";
		}
	}
	
	public boolean addItem(Qitem item) {
		if(item != null) {
			items.add(item);
			return true;
		}
		return false;
	}
	
	public boolean removeItem(int ID) {
		try {
			items.remove(ID);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public void save(StorageKey key) {
		key.removeKey("");
		for(int i = 0; i < items.size(); i++) {
			items.get(i).serializeKey(key.getSubKey(String.valueOf(i)));
		}
	}
	
	public void load(StorageKey key) {
		if(key.hasSubKeys()) {
			int count = 0;
			for(StorageKey subKey : key.getSubKeys()) {
				Qitem item = Qitem.deserializeKey(subKey);
				if(item != null) {
					items.add(item);
					count++;
				}
			}
			QrExtras.log.info("Items loaded. (" + count + ")");
		}
		else {
			QrExtras.log.info("No items found.");
		}
	}
}
