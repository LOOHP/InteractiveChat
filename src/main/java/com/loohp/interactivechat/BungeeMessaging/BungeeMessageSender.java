package com.loohp.interactivechat.BungeeMessaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.ValuePairs;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.CustomArrayUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

public class BungeeMessageSender {
	
	private static Random random = new Random();
	protected static short itemStackScheme = 0;
	protected static short inventoryScheme = 0;
	
	public static int getItemStackScheme() {
		return itemStackScheme;
	}
	
	public static int getInventoryScheme() {
		return inventoryScheme;
	}
	
	public static boolean forwardData(int packetId, byte[] data) {
		Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
		if (player == null) {
			return false;
		}
		
		int packetNumber = random.nextInt();
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
    		DataTypeIO.writeItemStack(out, itemStackScheme, itemStack, StandardCharsets.UTF_8);
    	}
    	return forwardData(0x03, out.toByteArray());
    }
    
    public static boolean forwardInventory(UUID player, String title, Inventory inventory) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	out.writeByte(0);
    	DataTypeIO.writeInventory(out, inventoryScheme, title, inventory, StandardCharsets.UTF_8);
    	return forwardData(0x04, out.toByteArray());
    }
    
    public static boolean forwardEnderchest(UUID player, String title, Inventory enderchest) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, player);
    	out.writeByte(1);
    	DataTypeIO.writeInventory(out, inventoryScheme, title, enderchest, StandardCharsets.UTF_8);
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
    
    public static boolean respondProcessedMessage(String component, UUID messageId) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, messageId);
    	DataTypeIO.writeString(out, component, StandardCharsets.UTF_8);
    	return forwardData(0x08, out.toByteArray());
    }
    
    public static boolean reloadBungeeConfig() throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	return forwardData(0x09, out.toByteArray());
    }
    
    public static boolean resetAndForwardAliasMapping(Map<String, String> mapping) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	out.writeInt(mapping.size());
    	for (Entry<String, String> entry : mapping.entrySet()) {
    		DataTypeIO.writeString(out, entry.getKey(), StandardCharsets.UTF_8);
    		DataTypeIO.writeString(out, entry.getValue(), StandardCharsets.UTF_8);
    	}
    	return forwardData(0x10, out.toByteArray());
    }
    
    @SuppressWarnings("deprecation")
	public static boolean resetAndForwardPlaceholderList(List<ICPlaceholder> placeholderList) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	out.writeInt(placeholderList.size());
    	for (ICPlaceholder placeholder : placeholderList) {
    		boolean isBuiltIn = placeholder.isBuildIn();
    		out.writeBoolean(isBuiltIn);
    		if (isBuiltIn) {
    			DataTypeIO.writeString(out, placeholder.getKeyword(), StandardCharsets.UTF_8);
    			out.writeBoolean(placeholder.isCaseSensitive());
    			DataTypeIO.writeString(out, placeholder.getDescription(), StandardCharsets.UTF_8);
    			DataTypeIO.writeString(out, placeholder.getPermission(), StandardCharsets.UTF_8);
    		} else {
    			CustomPlaceholder customPlaceholder = placeholder.getCustomPlaceholder().get();
    			out.writeInt(customPlaceholder.getPosition());
    			out.writeByte(customPlaceholder.getParsePlayer().getOrder());
    			DataTypeIO.writeString(out, customPlaceholder.getKeyword(), StandardCharsets.UTF_8);
    			out.writeInt(customPlaceholder.getAliases().size());
    			for (String each : customPlaceholder.getAliases()) {
    				DataTypeIO.writeString(out, each, StandardCharsets.UTF_8);
    			}
    			out.writeBoolean(customPlaceholder.getParseKeyword());
    			out.writeBoolean(customPlaceholder.isCaseSensitive());
    			out.writeLong(customPlaceholder.getCooldown());
    			
    			CustomPlaceholderHoverEvent hover = customPlaceholder.getHover();
    			out.writeBoolean(hover.isEnabled());
    			DataTypeIO.writeString(out, hover.getText(), StandardCharsets.UTF_8);
    			
    			CustomPlaceholderClickEvent click = customPlaceholder.getClick();
    			out.writeBoolean(click.isEnabled());
    			DataTypeIO.writeString(out, click.getAction() == null ? "" : click.getAction().name(), StandardCharsets.UTF_8);
    			DataTypeIO.writeString(out, click.getValue(), StandardCharsets.UTF_8);
    			
    			CustomPlaceholderReplaceText replace = customPlaceholder.getReplace();
    			out.writeBoolean(replace.isEnabled());
    			DataTypeIO.writeString(out, replace.getReplaceText(), StandardCharsets.UTF_8);
    			
    			DataTypeIO.writeString(out, placeholder.getDescription(), StandardCharsets.UTF_8);
    		}
    	}
    	return forwardData(0x11, out.toByteArray());
    }
    
    public static boolean forwardPlayerDataUpdate(UUID uuid, FileConfiguration config) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	DataTypeIO.writeUUID(out, uuid);
    	DataTypeIO.writeString(out, config.saveToString(), StandardCharsets.UTF_8);
    	return forwardData(0x12, out.toByteArray());
    }
    
    public static boolean permissionCheckResponse(int id, boolean value) throws IOException {
    	ByteArrayDataOutput out = ByteStreams.newDataOutput();
    	out.writeInt(id);
    	out.writeBoolean(value);
    	return forwardData(0x13, out.toByteArray());
    }
}
