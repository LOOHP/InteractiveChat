package com.loohp.interactivechat.PluginMessaging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.InventoryUtils;
import com.loohp.interactivechat.Utils.Utils;

public class BungeeMessageSender {
	
    @SuppressWarnings("unchecked")
	public static void forwardHashMap(Player player, Object map, int field) {
    	//field
    	//0 > keytime
    	//1 > item
    	//2 > inv
    	//3 > ender
    	//4 > mention
    	if (field == 1) {
    		HashMap<Long, String> newMap = new HashMap<Long, String>();
    		for (Entry<Long, Inventory> entry : ((HashMap<Long, Inventory>) map).entrySet()) {
    			Inventory inv = entry.getValue();
    			String hash = InventoryUtils.toBase64(inv);
    			newMap.put(entry.getKey(), hash);
    		}
    		map = newMap;
    	}
    	if (field == 2) {
    		HashMap<Long, String> newMap = new HashMap<Long, String>();
    		for (Entry<Long, Inventory> entry : ((HashMap<Long, Inventory>) map).entrySet()) {
    			Inventory inv = entry.getValue();
    			String hash = InventoryUtils.toBase64(inv);
    			newMap.put(entry.getKey(), hash);
    		}
    		map = newMap;
    	}
    	if (field == 3) {
    		HashMap<Long, String> newMap = new HashMap<Long, String>();
    		for (Entry<Long, Inventory> entry : ((HashMap<Long, Inventory>) map).entrySet()) {
    			Inventory inv = entry.getValue();
    			String hash = InventoryUtils.toBase64(inv);
    			newMap.put(entry.getKey(), hash);
    		}
    		map = newMap;
    	}
        try {
        	String hash = Utils.serialize((Serializable) map);
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(hash);
            out.writeInt(field);
       
            player.sendPluginMessage(InteractiveChat.plugin, "interactivechat:channel", out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
