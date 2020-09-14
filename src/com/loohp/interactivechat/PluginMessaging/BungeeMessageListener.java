package com.loohp.interactivechat.PluginMessaging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Modules.ProcessBungeeRequestedMessage;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderInfo;
import com.loohp.interactivechat.ObjectHolders.MentionPair;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper.RemoteEquipment;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

public class BungeeMessageListener implements PluginMessageListener {

    InteractiveChat plugin;
    private Map<Integer, Byte[]> incomming = new HashMap<>();

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
        
        Byte[] chain = incomming.remove(packetNumber);
    	if (chain != null) {
    		ByteBuffer buff = ByteBuffer.allocate(chain.length + data.length);
    		buff.put(ArrayUtils.toPrimitive(chain));
    		buff.put(data);
    		data = buff.array();
    	}
        
        if (!isEnding) {
        	incomming.put(packetNumber, ArrayUtils.toObject(data));
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
	        		UUID uuid = DataTypeIO.readUUID(input);
	        		String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        		if (!localUUID.contains(uuid) && !InteractiveChat.remotePlayers.containsKey(uuid)) {
	        			InteractiveChat.remotePlayers.put(uuid, new PlayerWrapper(name, uuid, new RemoteEquipment(), Bukkit.createInventory(null, 45), Bukkit.createInventory(null, 36)));
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
	        	if (size < 6) {
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
	        	int requestId = input.readInt();
	        	UUID uuid5 = DataTypeIO.readUUID(input);
	        	Player bukkitplayer1 = Bukkit.getPlayer(uuid5);
	        	if (bukkitplayer1 == null) {
	        		break;
	        	}
	        	String component = DataTypeIO.readString(input, StandardCharsets.UTF_8);
	        	Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
	        		String processed = ProcessBungeeRequestedMessage.processAndRespond(bukkitplayer1, component);
	        		try {
						BungeeMessageSender.respondProcessedMessage(requestId, processed);
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	});
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
