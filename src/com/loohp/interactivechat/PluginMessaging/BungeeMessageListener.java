package com.loohp.interactivechat.PluginMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.InventoryUtils;
import com.loohp.interactivechat.Utils.Utils;

public class BungeeMessageListener implements PluginMessageListener {

    InteractiveChat plugin;

    public BungeeMessageListener(InteractiveChat instance) {
        plugin = instance;
    }

    @SuppressWarnings("unchecked")
	@Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
   
        if (!channel.equals("interactivechat:channel")) {
            return;
        }
   
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
   
        String hash = in.readUTF();
        int field = in.readInt();
   
        //field
    	//0 > keytime
    	//1 > item
    	//2 > inv
    	//3 > ender
    	//4 > mention
        try {
			switch (field) {
			case 0:
				InteractiveChat.messageKeyUUID.putAll((HashMap<String, UUID>) Utils.deserialize(hash));
				break;
			case 1:
				HashMap<Long, String> map = (HashMap<Long, String>) Utils.deserialize(hash);
				HashMap<Long, Inventory> newMap = new HashMap<Long, Inventory>();
				for (Entry<Long, String> entry : map.entrySet()) {
					Inventory inv = InventoryUtils.fromBase64(entry.getValue(), "");
					newMap.put(entry.getKey(), inv);
				}
				InteractiveChat.itemDisplay.putAll(newMap);
				break;
			case 2:
				HashMap<Long, String> map1 = (HashMap<Long, String>) Utils.deserialize(hash);
				HashMap<Long, Inventory> newMap1 = new HashMap<Long, Inventory>();
				for (Entry<Long, String> entry : map1.entrySet()) {
					Inventory inv = InventoryUtils.fromBase64(entry.getValue(), "");
					newMap1.put(entry.getKey(), inv);
				}
				InteractiveChat.inventoryDisplay.putAll(newMap1);
				break;
			case 3:
				HashMap<Long, String> map2 = (HashMap<Long, String>) Utils.deserialize(hash);
				HashMap<Long, Inventory> newMap2 = new HashMap<Long, Inventory>();
				for (Entry<Long, String> entry : map2.entrySet()) {
					Inventory inv = InventoryUtils.fromBase64(entry.getValue(), "");
					newMap2.put(entry.getKey(), inv);
				}
				InteractiveChat.enderDisplay.putAll(newMap2);
				break;
			case 4:
				InteractiveChat.mentionPair.putAll((HashMap<UUID, UUID>) Utils.deserialize(hash));
				break;
			}	
			Bukkit.getConsoleSender().sendMessage(field + "");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
    }
}
