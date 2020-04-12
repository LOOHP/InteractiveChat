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
import com.loohp.interactivechat.Utils.SerializeUtils;

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
   
        int field = in.readInt();
        String hash = in.readUTF();
   
        //field
    	//0 > keyplayer
    	//1 > item
    	//2 > inv
    	//3 > ender
    	//4 > mention
    	//5 > keytime
        try {
			switch (field) {
			case 0:
				InteractiveChat.messageKeyUUID.putAll((HashMap<String, UUID>) SerializeUtils.deserialize(hash));
				break;
			case 1:
				HashMap<String, String> map = (HashMap<String, String>) SerializeUtils.deserialize(hash);
				HashMap<Long, Inventory> newMap = new HashMap<Long, Inventory>();
				for (Entry<String, String> entry : map.entrySet()) {
					String title = entry.getValue().substring(0, entry.getValue().indexOf(";"));
					Inventory inv = InventoryUtils.fromBase64(entry.getValue().substring(entry.getValue().indexOf(";") + 1), title);
					newMap.put(Long.parseLong(entry.getKey()), inv);
				}
				InteractiveChat.itemDisplay.putAll(newMap);
				break;
			case 2:
				HashMap<String, String> map1 = (HashMap<String, String>) SerializeUtils.deserialize(hash);
				HashMap<Long, Inventory> newMap1 = new HashMap<Long, Inventory>();
				for (Entry<String, String> entry : map1.entrySet()) {
					String title = entry.getValue().substring(0, entry.getValue().indexOf(";"));
					Inventory inv = InventoryUtils.fromBase64(entry.getValue().substring(entry.getValue().indexOf(";") + 1), title);
					newMap1.put(Long.parseLong(entry.getKey()), inv);
				}
				InteractiveChat.inventoryDisplay.putAll(newMap1);
				break;
			case 3:
				HashMap<String, String> map2 = (HashMap<String, String>) SerializeUtils.deserialize(hash);
				HashMap<Long, Inventory> newMap2 = new HashMap<Long, Inventory>();
				for (Entry<String, String> entry : map2.entrySet()) {
					String title = entry.getValue().substring(0, entry.getValue().indexOf(";"));
					Inventory inv = InventoryUtils.fromBase64(entry.getValue().substring(entry.getValue().indexOf(";") + 1), title);
					newMap2.put(Long.parseLong(entry.getKey()), inv);
				}
				InteractiveChat.enderDisplay.putAll(newMap2);
				break;
			case 4:
				HashMap<String, String> map3 = (HashMap<String, String>) SerializeUtils.deserialize(hash);
				HashMap<UUID, UUID> newMap3 = new HashMap<UUID, UUID>();
				for (Entry<String, String> entry : map3.entrySet()) {
					newMap3.put(UUID.fromString(entry.getKey()), UUID.fromString(entry.getValue()));
				}
				InteractiveChat.mentionPair.putAll(newMap3);
				break;
			case 5:
				InteractiveChat.keyTime.putAll((HashMap<String, Long>) SerializeUtils.deserialize(hash));
				break;
			}	
			for (Player each : Bukkit.getOnlinePlayers()) {
				each.sendMessage(field + "");
			}
			Bukkit.getConsoleSender().sendMessage(field + "");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
    }
}
