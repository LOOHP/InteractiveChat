package com.loohp.interactivechat.BungeeMessaging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Data.PlayerDataManager;
import com.loohp.interactivechat.Modules.ProcessBungeeRequestedMessage;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderInfo;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.MentionPair;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper.RemoteEquipment;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

import net.md_5.bungee.api.chat.ClickEvent;

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
        	ByteArrayDataInput input = ByteStreams.newDataInput(CompressionUtils.decompress(data));
        	
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
	        			PlayerWrapper player = InteractiveChat.remotePlayers.get(uuid);
	        			if (!player.getRemoteServer().equals(server)) {
	        				player.setRemoteServer(server);
	        			}
	        		}
	        		if (!localUUID.contains(uuid) && !InteractiveChat.remotePlayers.containsKey(uuid)) {
	        			InteractiveChat.remotePlayers.put(uuid, new PlayerWrapper(server, name, uuid, new RemoteEquipment(), Bukkit.createInventory(null, 45), Bukkit.createInventory(null, 36)));
	        		}
	        		newSet.add(uuid);
	        	}
	        	current.removeAll(newSet);
	        	for (UUID uuid : current) {
	        		InteractiveChat.remotePlayers.remove(uuid);
	        	}
	        	break;
	        case 0x01:
	        	int delay = input.readInt();
	        	InteractiveChat.remoteDelay = delay;
	        	break;
	        case 0x02:
	        	UUID sender = DataTypeIO.readUUID(input);
	        	UUID receiver = DataTypeIO.readUUID(input);
	        	InteractiveChat.mentionPair.put(receiver, new MentionPair(sender, receiver, InteractiveChat.mentionPair));
	        	break;
	        case 0x03:
	        	UUID uuid = DataTypeIO.readUUID(input);
	        	PlayerWrapper player = InteractiveChat.remotePlayers.get(uuid);
	        	if (player == null) {
	        		break;
	        	}
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
	        	PlayerWrapper player1 = InteractiveChat.remotePlayers.get(uuid1);
	        	if (player1 == null) {
	        		break;
	        	}
	        	int type = input.readByte();
	        	if (type == 0) {
	        		player1.setRemoteInventory(DataTypeIO.readInventory(input, StandardCharsets.UTF_8));
	        	} else {
	        		player1.setRemoteEnderChest(DataTypeIO.readInventory(input, StandardCharsets.UTF_8));
	        	}
	        	break;
	        case 0x05:
	        	UUID uuid2 = DataTypeIO.readUUID(input);
	        	PlayerWrapper player2 = InteractiveChat.remotePlayers.get(uuid2);
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
	        	PlayerWrapper player3 = InteractiveChat.remotePlayers.get(uuid3);
	        	if (player3 == null) {
	        		break;
	        	}
	        	InteractiveChat.messages.put(message, uuid3);
	    		Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> InteractiveChat.messages.remove(message), 60);
	        	break;
	        case 0x07:
	        	UUID uuid4 = DataTypeIO.readUUID(input);
	        	Player bukkitplayer = Bukkit.getPlayer(uuid4);
	        	PlayerWrapper player4;
	        	if (bukkitplayer != null) {
	        		player4 = new PlayerWrapper(bukkitplayer);
	        	} else {
	        		player4 = InteractiveChat.remotePlayers.get(uuid4);
	        	}
	        	if (player4 == null) {
	        		break;
	        	}
	        	String placeholder1 = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	String uuidmatch = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	InteractiveChat.commandPlaceholderMatch.put(uuidmatch, new CommandPlaceholderInfo(player4, placeholder1, uuidmatch, InteractiveChat.commandPlaceholderMatch));
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
	        		String processed = ProcessBungeeRequestedMessage.processAndRespond(bukkitplayer1, component);
	        		try {
						BungeeMessageSender.respondProcessedMessage(processed, messageId);
					} catch (IOException e) {
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
	        			list.add(new ICPlaceholder(DataTypeIO.readString(input, StandardCharsets.UTF_8), input.readBoolean()));
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

	        			list.add(new CustomPlaceholder(customNo, parseplayer, placeholder, aliases, parseKeyword, casesensitive, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEvent.Action.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText)));
	        		}
	        	}
	        	InteractiveChat.remotePlaceholderList.put(server, list);
	        	break;
	        case 0x10:
	        	BungeeMessageSender.resetAndForwardPlaceholderList(InteractiveChat.placeholderList);
	        	break;
	        case 0x11:
	        	BungeeMessageSender.resetAndForwardAliasMapping(InteractiveChat.aliasesMapping);
	        	break;
	        case 0x12:
	        	UUID playerUUID = DataTypeIO.readUUID(input);
	        	String data1 = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	PlayerDataManager manager = InteractiveChat.playerDataManager;
	        	if (manager.getPlayerData(playerUUID) != null) {
	        		manager.mergeOnline(playerUUID, data1);
	        	} else {
	        		manager.mergeOffline(playerUUID, data1);
	        	}
	        	break;
	        }
	        //for (Player player : Bukkit.getOnlinePlayers()) {
	        //	player.sendMessage(packetId + "");
	        //}
        } catch (IOException | DataFormatException e) {
			e.printStackTrace();
		}  
    }
}
