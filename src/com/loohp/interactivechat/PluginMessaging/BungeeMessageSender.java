package com.loohp.interactivechat.PluginMessaging;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.InventoryUtils;
import com.loohp.interactivechat.Utils.SerializeUtils;

public class BungeeMessageSender {
	
	public static void forwardHashMap(Player player, Object map, int field) {
		forwardHashMap(player, field, map, null);
	}
	
    @SuppressWarnings("unchecked")
    public static void forwardHashMap(Player player, int field, Object map, String title) {
    	//field
    	//0 > keyplayer
    	//1 > item
    	//2 > inv
    	//3 > ender
    	//4 > mention
    	//5 > keytime
    	if (field == 1) {
    		HashMap<String, String> newMap = new HashMap<String, String>();
    		for (Entry<Long, Inventory> entry : ((HashMap<Long, Inventory>) map).entrySet()) {
    			Inventory inv = entry.getValue();
    			String hash = InventoryUtils.toBase64(inv);
    			newMap.put(entry.getKey().toString(), title + ";" + hash);
    		}
    		map = newMap;
    	}
    	if (field == 2) {
    		HashMap<String, String> newMap = new HashMap<String, String>();
    		for (Entry<Long, Inventory> entry : ((HashMap<Long, Inventory>) map).entrySet()) {
    			Inventory inv = entry.getValue();
    			String hash = InventoryUtils.toBase64(inv);
    			newMap.put(entry.getKey().toString(), title + ";" + hash);
    		}
    		map = newMap;
    	}
    	if (field == 3) {
    		HashMap<String, String> newMap = new HashMap<String, String>();
    		for (Entry<Long, Inventory> entry : ((HashMap<Long, Inventory>) map).entrySet()) {
    			Inventory inv = entry.getValue();
    			String hash = InventoryUtils.toBase64(inv);
    			newMap.put(entry.getKey().toString(), title + ";" + hash);
    		}
    		map = newMap;
    	}
    	if (field == 4) {
    		HashMap<String, String> newMap = new HashMap<String, String>();
    		for (Entry<UUID, UUID> entry : ((HashMap<UUID, UUID>) map).entrySet()) {
    			newMap.put(entry.getKey().toString(), entry.getValue().toString());
    		}
    		map = newMap;
    	}
        try {
        	String hash = SerializeUtils.serialize((Serializable) map);
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(field);
            out.writeUTF(hash);
       
            player.sendPluginMessage(InteractiveChat.plugin, "interactivechat:channel", out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
