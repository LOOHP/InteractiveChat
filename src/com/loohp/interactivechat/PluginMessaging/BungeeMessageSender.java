package com.loohp.interactivechat.PluginMessaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.ValuePairs;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.CustomArrayUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

public class BungeeMessageSender {
	
	private static Random random = new Random();
	
	public static boolean forwardData(int packetId, byte[] data) {
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			return false;
		}
		
		int packetNumber = random.nextInt();
		Player player = Bukkit.getOnlinePlayers().iterator().next();
		try {
			byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);
			
			for (int i = 0; i < dataArray.length; i++) {
				byte[] chunk = dataArray[i];
				
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeInt(packetNumber);
				
		        out.writeShort(packetId);
		        out.writeBoolean(i == (dataArray.length - 1));
		        
		        out.write(chunk);
		        player.sendPluginMessage(InteractiveChat.plugin, "interchat:main", out.toByteArray());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return true;
	}
	
    public static boolean forwardMentionPair(UUID sender, UUID receiver) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, sender);
    	DataTypeIO.writeUUID(out, receiver);
    	return forwardData(0x02, out.toByteArray());
    }
    
    public static boolean forwardEquipment(UUID player, ItemStack... equipment) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	out.writeByte(equipment.length);
    	for (ItemStack itemStack : equipment) {
    		DataTypeIO.writeItemStack(out, itemStack, StandardCharsets.UTF_8);
    	}
    	return forwardData(0x03, out.toByteArray());
    }
    
    public static boolean forwardInventory(UUID player, String title, Inventory inventory) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	out.writeByte(0);
    	DataTypeIO.writeInventory(out, title, inventory, StandardCharsets.UTF_8);
    	return forwardData(0x04, out.toByteArray());
    }
    
    public static boolean forwardEnderchest(UUID player, String title, Inventory enderchest) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	out.writeByte(1);
    	DataTypeIO.writeInventory(out, title, enderchest, StandardCharsets.UTF_8);
    	return forwardData(0x04, out.toByteArray());
    }
    
	public static boolean forwardPlaceholders(UUID player, List<ValuePairs<String, String>> pairs) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	out.writeInt(pairs.size());
    	for (ValuePairs<String, String> pair : pairs) {
    		DataTypeIO.writeString(out, pair.getFirst(), StandardCharsets.UTF_8);
        	DataTypeIO.writeString(out, pair.getSecond(), StandardCharsets.UTF_8);
    	}
    	return forwardData(0x05, out.toByteArray());
    }
    
    public static boolean addMessage(String message, UUID player) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeString(out, message, StandardCharsets.UTF_8);
    	DataTypeIO.writeUUID(out, player);
    	return forwardData(0x06, out.toByteArray());
    }
    
    public static boolean addCommandMatch(UUID player, String placeholder, String uuidmatch) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	DataTypeIO.writeString(out, placeholder, StandardCharsets.UTF_8);
    	DataTypeIO.writeString(out, uuidmatch, StandardCharsets.UTF_8);
    	return forwardData(0x07, out.toByteArray());
    }
    
    public static boolean respondProcessedMessage(int requestId, String component) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	out.writeInt(requestId);
    	DataTypeIO.writeString(out, component, StandardCharsets.UTF_8);
    	return forwardData(0x08, out.toByteArray());
    }
    
    public static boolean reloadBungeeConfig() throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	return forwardData(0x09, out.toByteArray());
    }
}
