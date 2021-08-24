package com.loohp.interactivechat.bungeemessaging;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.ProxyCustomDataRecievedEvent;
import com.loohp.interactivechat.api.events.RemotePlayerAddedEvent;
import com.loohp.interactivechat.api.events.RemotePlayerRemovedEvent;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.objectholders.BuiltInPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerEquipment;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.utils.DataTypeIO;

public class BungeeMessageListener implements PluginMessageListener {

    InteractiveChat plugin;
    private Map<Integer, byte[]> incomming = new HashMap<>();

    public BungeeMessageListener(InteractiveChat instance) {
        plugin = instance;
    }

	@SuppressWarnings("deprecation")
	@Override
    public void onPluginMessageReceived(String channel, Player pluginMessagingPlayer, byte[] bytes) {
   
        if (!channel.equals("interchat:main")) {
            return;
        }
   
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
   
        int packetNumber = in.readInt();
        int packetId = in.readShort();
        boolean isEnding = in.readBoolean();
        byte[] data = new byte[bytes.length - 7];
        in.readFully(data);
        
        byte[] chain = incomming.remove(packetNumber);
    	if (chain != null) {
    		ByteBuffer buff = ByteBuffer.allocate(chain.length + data.length);
    		buff.put(chain);
    		buff.put(data);
    		data = buff.array();
    	}
        
        if (!isEnding) {
        	incomming.put(packetNumber, data);
        	return;
        }
        
        try {
        	ByteArrayDataInput input = ByteStreams.newDataInput(data);
        	
	        switch (packetId) {
	        case 0x00:
	        	int playerAmount = input.readInt();
	        	Set<UUID> localUUID = Bukkit.getOnlinePlayers().stream().map(each -> each.getUniqueId()).collect(Collectors.toSet());
	        	Set<UUID> current = new HashSet<>(InteractiveChat.remotePlayers.keySet());
	        	Set<UUID> newSet = new HashSet<>();
	        	for (int i = 0; i < playerAmount; i++) {
	        		String server = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        		UUID uuid = DataTypeIO.readUUID(input);
	        		String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        		if (InteractiveChat.remotePlayers.containsKey(uuid)) {
	        			ICPlayer player = InteractiveChat.remotePlayers.get(uuid);
	        			if (!player.getRemoteServer().equals(server)) {
	        				player.setRemoteServer(server);
	        			}
	        		}
	        		if (!localUUID.contains(uuid) && !InteractiveChat.remotePlayers.containsKey(uuid)) {
	        			ICPlayer newPlayer = new ICPlayer(server, name, uuid, true, 0, 0, new ICPlayerEquipment(), Bukkit.createInventory(null, 45), Bukkit.createInventory(null, 36));
	        			InteractiveChat.remotePlayers.put(uuid, newPlayer);
	        			Bukkit.getPluginManager().callEvent(new RemotePlayerAddedEvent(newPlayer));
	        		}
	        		newSet.add(uuid);
	        	}
	        	current.removeAll(newSet);
	        	for (UUID uuid : current) {
	        		ICPlayer removedPlayer = InteractiveChat.remotePlayers.remove(uuid);
	        		if (removedPlayer != null) {
	        			Bukkit.getPluginManager().callEvent(new RemotePlayerRemovedEvent(removedPlayer));
	        		}
	        	}
	        	for (UUID uuid : localUUID) {
	        		ICPlayer removedPlayer = InteractiveChat.remotePlayers.remove(uuid);
	        		if (removedPlayer != null) {
	        			Bukkit.getPluginManager().callEvent(new RemotePlayerRemovedEvent(removedPlayer));
	        		}
	        	}
	        	break;
	        case 0x01:
	        	int delay = input.readInt();
	        	short itemStackScheme = input.readShort();
	        	short inventoryScheme = input.readShort();
	        	InteractiveChat.remoteDelay = delay;
	        	BungeeMessageSender.itemStackScheme = itemStackScheme;
	        	BungeeMessageSender.inventoryScheme = inventoryScheme;
	        	break;
	        case 0x02:
	        	UUID sender = DataTypeIO.readUUID(input);
	        	UUID receiver = DataTypeIO.readUUID(input);
	        	InteractiveChat.mentionPair.put(receiver, new MentionPair(sender, receiver));
	        	break;
	        case 0x03:
	        	UUID uuid = DataTypeIO.readUUID(input);
	        	ICPlayer player = InteractiveChat.remotePlayers.get(uuid);
	        	if (player == null) {
	        		break;
	        	}
	        	boolean rightHanded = input.readBoolean();
	        	player.setRemoteRightHanded(rightHanded);
	        	int selectedSlot = input.readByte();
	        	player.setRemoteSelectedSlot(selectedSlot);
	        	int level = input.readInt();
	        	player.setRemoteExperienceLevel(level);
	        	int size = input.readByte();
	        	ItemStack[] equipment = new ItemStack[size];
	        	for (int i = 0; i < equipment.length; i++) {
	        		equipment[i] = DataTypeIO.readItemStack(input, StandardCharsets.UTF_8);
	        	}
	        	player.getEquipment().setHelmet(equipment[0]);
	        	player.getEquipment().setChestplate(equipment[1]);
	        	player.getEquipment().setLeggings(equipment[2]);
	        	player.getEquipment().setBoots(equipment[3]);
	        	if (InteractiveChat.version.isOld()) {
	        		player.getEquipment().setItemInHand(equipment[4]);
	        	} else {
	        		player.getEquipment().setItemInMainHand(equipment[4]);
	        		player.getEquipment().setItemInOffHand(equipment[5]);
	        	}
	        	break;
	        case 0x04:
	        	UUID uuid1 = DataTypeIO.readUUID(input);
	        	ICPlayer player1 = InteractiveChat.remotePlayers.get(uuid1);
	        	if (player1 == null) {
	        		break;
	        	}
	        	boolean rightHanded1 = input.readBoolean();
	        	player1.setRemoteRightHanded(rightHanded1);
	        	int selectedSlot1 = input.readByte();
	        	player1.setRemoteSelectedSlot(selectedSlot1);
	        	int level1 = input.readInt();
	        	player1.setRemoteExperienceLevel(level1);
	        	int type = input.readByte();
	        	if (type == 0) {
	        		player1.setRemoteInventory(DataTypeIO.readInventory(input, StandardCharsets.UTF_8));
	        	} else {
	        		player1.setRemoteEnderChest(DataTypeIO.readInventory(input, StandardCharsets.UTF_8));
	        	}
	        	break;
	        case 0x05:
	        	UUID uuid2 = DataTypeIO.readUUID(input);
	        	ICPlayer player2 = InteractiveChat.remotePlayers.get(uuid2);
	        	if (player2 == null) {
	        		break;
	        	}
	        	int size1 = input.readInt();
	        	for (int i = 0; i < size1; i++) {
	        		String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        	String text = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        	player2.getRemotePlaceholdersMapping().put(placeholder, text);
	        	}
	        	break;
	        case 0x06:
	        	String message = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	UUID uuid3 = DataTypeIO.readUUID(input);
	        	ICPlayer player3 = InteractiveChat.remotePlayers.get(uuid3);
	        	if (player3 == null) {
	        		break;
	        	}
	        	InteractiveChat.messages.put(message, uuid3);
	    		Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> InteractiveChat.messages.remove(message), 60);
	        	break;
	        case 0x07:
	        	int cooldownType = input.readByte();
	        	switch (cooldownType) {
	        	case 0:
	        		UUID uuid4 = DataTypeIO.readUUID(input);
	        		long time = input.readLong();
	        		InteractiveChat.placeholderCooldownManager.setPlayerUniversalLastTimestampRaw(uuid4, time);
	        		break;
	        	case 1:
	        		uuid4 = DataTypeIO.readUUID(input);
	        		String keyword = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        		time = input.readLong();
	        		Optional<ICPlaceholder> optPlaceholder = InteractiveChat.placeholderList.stream().filter(each -> each.getKeyword().equals(keyword)).findFirst();
	        		if (optPlaceholder.isPresent()) {
	        			InteractiveChat.placeholderCooldownManager.setPlayerPlaceholderLastTimestampRaw(uuid4, optPlaceholder.get(), time);
	        		}
	        		break;
	        	}
	        	break;
	        case 0x08:
	        	UUID messageId = DataTypeIO.readUUID(input);
	        	UUID uuid5 = DataTypeIO.readUUID(input);
	        	Player bukkitplayer1 = Bukkit.getPlayer(uuid5);
	        	if (bukkitplayer1 == null) {
	        		break;
	        	}
	        	String component = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
	        		try {
	        			String processed = ProcessExternalMessage.processAndRespond(bukkitplayer1, component);
						BungeeMessageSender.respondProcessedMessage(System.currentTimeMillis(), processed, messageId);
					} catch (Exception e) {
						e.printStackTrace();
					}
	        	});
	        	break;
	        case 0x09:
	        	String server = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	int size2 = input.readInt();
	        	List<ICPlaceholder> list = new ArrayList<>(size2);
	        	for (int i = 0; i < size2; i++) {
	        		boolean isBulitIn = input.readBoolean();
	        		if (isBulitIn) {
	        			String keyword = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			boolean casesensitive = input.readBoolean();
	        			String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			long cooldown = input.readLong();
	        			list.add(new BuiltInPlaceholder(keyword, casesensitive, description, permission, cooldown));
	        		} else {
	        			int customNo = input.readInt();
	        			ParsePlayer parseplayer = ParsePlayer.fromOrder(input.readByte());	
	        			String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			List<String> aliases = new ArrayList<>();
	        			int aliasSize = input.readInt();
	        			for (int u = 0; u < aliasSize; u++) {
	        				aliases.add(DataTypeIO.readString(input, StandardCharsets.UTF_8));
	        			}
	        			boolean parseKeyword = input.readBoolean();
	        			boolean casesensitive = input.readBoolean();
	        			long cooldown = input.readLong();
	        			boolean hoverEnabled = input.readBoolean();
	        			String hoverText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			boolean clickEnabled = input.readBoolean();
	        			String clickAction = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			String clickValue = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			boolean replaceEnabled = input.readBoolean();
	        			String replaceText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        			String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);

	        			list.add(new CustomPlaceholder(customNo, parseplayer, placeholder, aliases, parseKeyword, casesensitive, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), description));
	        		}
	        	}
	        	InteractiveChat.remotePlaceholderList.put(server, list);
	        	break;
	        case 0x0A:
	        	BungeeMessageSender.resetAndForwardPlaceholderList(System.currentTimeMillis(), InteractiveChat.placeholderList);
	        	break;
	        case 0x0B:
	        	int id = input.readInt();
	        	UUID playerUUID = DataTypeIO.readUUID(input);
	        	String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	Player player5 = Bukkit.getPlayer(playerUUID);
	        	BungeeMessageSender.permissionCheckResponse(System.currentTimeMillis(), id, player5 == null ? false : player5.hasPermission(permission));
	        	break;
	        case 0x0C:
	        	BungeeMessageSender.resetAndForwardAliasMapping(System.currentTimeMillis(), InteractiveChat.aliasesMapping);
	        	break;
	        case 0x0D:
	        	UUID playerUUID1 = DataTypeIO.readUUID(input);
	        	PlayerData pd = InteractiveChat.playerDataManager.getPlayerData(playerUUID1);
	        	if (pd != null) {
	        		pd.reload();
	        	}
	        	break;
	        case 0x0E:
	        	SharedType sharedType = SharedType.fromValue(input.readByte());
	        	String sha1 = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	Inventory inventory = DataTypeIO.readInventory(input, StandardCharsets.UTF_8);
	        	InteractiveChatAPI.addInventoryToItemShareList(sharedType, sha1, inventory);
	        	break;
	        case 0x0F:
	        	int requestType = input.readByte();
	        	UUID playerUUID2 = DataTypeIO.readUUID(input);
	        	Player player6 = Bukkit.getPlayer(playerUUID2);
	        	if (player6 != null) {
	        		ICPlayer player7 = new ICPlayer(player6);
	        		switch (requestType) {
		        	case 0:
		        		BungeeMessageSender.forwardInventory(System.currentTimeMillis(), player7.getUniqueId(), player7.isRightHanded(), player7.getSelectedSlot(), player7.getExperienceLevel(), null, player7.getInventory());
		        		break;
		        	case 1:
		        		BungeeMessageSender.forwardEnderchest(System.currentTimeMillis(), player7.getUniqueId(), player7.isRightHanded(), player7.getSelectedSlot(), player7.getExperienceLevel(), null, player7.getEnderChest());
		        		break;
		        	}
	        	}
	        	break;
	        case 0xFF:
	        	String customChannel = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	int dataLength = input.readInt();
	        	byte[] customData = new byte[dataLength];
	        	input.readFully(customData);
	        	ProxyCustomDataRecievedEvent dataEvent = new ProxyCustomDataRecievedEvent(customChannel, customData);
	        	Bukkit.getPluginManager().callEvent(dataEvent);
	        	break;
	        }
	        //for (Player player : Bukkit.getOnlinePlayers()) {
	        //	player.sendMessage(packetId + "");
	        //}
        } catch (Exception e) {
			e.printStackTrace();
		}  
    }
}
