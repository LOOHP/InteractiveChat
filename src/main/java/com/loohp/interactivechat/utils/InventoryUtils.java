package com.loohp.interactivechat.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactivechat.InteractiveChat;

public class InventoryUtils {
	
	private static final ItemStack ITEMSTACK_AIR = new ItemStack(Material.AIR);
	
	public static int toMultipleOf9(int num) {
		return num % 9 == 0 ? num : (num / 9 + 1) * 9;
	}
	
	public static void restorePlayerInventory(Player player) {
		sendFakePlayerInventory(player, player.getInventory(), true, true);
	}
	
	public static void sendFakePlayerInventory(Player player, Inventory inventory, boolean armor, boolean offhand) {
		PacketContainer packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
		packet1.getIntegers().write(0, 0);
		
		ItemStack[] items = new ItemStack[46];
		Arrays.fill(items, ITEMSTACK_AIR);
		int u = 36;
		for (int i = 0; i < 9; i++) {
			ItemStack item = inventory.getItem(i);
			items[u] = item == null ? ITEMSTACK_AIR : item.clone();
			u++;
		}
		for (int i = 9; i < 36; i++) {
			ItemStack item = inventory.getItem(i);
			items[i] = item == null ? ITEMSTACK_AIR : item.clone();
		}
		if (armor) {
			u = 8;
			for (int i = 36; i < 40; i++) {
				ItemStack item = inventory.getItem(i);
				items[u] = item == null ? ITEMSTACK_AIR : item.clone();
				u--;
			}
		}
		if (offhand && !InteractiveChat.version.isOld()) {
			ItemStack item = inventory.getItem(40);
			items[45] = item == null ? ITEMSTACK_AIR : item.clone();
		}
		
		if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_11)) {
			packet1.getItemListModifier().write(0, Arrays.asList(items));
		} else {
			packet1.getItemArrayModifier().write(0, items);
		}
		
		PacketContainer packet2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
		packet2.getIntegers().write(0, -1);
		packet2.getIntegers().write(1, -1);
		packet2.getItemModifier().write(0, ITEMSTACK_AIR);
		
		try {
			InteractiveChat.protocolManager.sendServerPacket(player, packet1);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
			try {
				InteractiveChat.protocolManager.sendServerPacket(player, packet2);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static String toBase64(Inventory inventory) throws IllegalStateException {
	    try {
	    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
	            
	        // Write the size of the inventory
	        dataOutput.writeInt(inventory.getSize());
	            
	        // Save every element in the list
	        for (int i = 0; i < inventory.getSize(); i++) {
	            dataOutput.writeObject(inventory.getItem(i));
	        }
	            
	        // Serialize that array
	        dataOutput.close();
	        return Base64Coder.encodeLines(outputStream.toByteArray());
	    } catch (Exception e) {
	        throw new IllegalStateException("Unable to save item stacks.", e);
        }
	}
	
	public static Inventory fromBase64(String data, InventoryType type, String title) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
	        Inventory inventory;
	        if (type.equals(InventoryType.CHEST)) {
		        if (title == null || title.equals("")) {
		       	 	inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());
		        } else {
		       	 	inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(), title);
		        }
	        } else {
	        	dataInput.readInt();
	        	if (title == null || title.equals("")) {
		       	 	inventory = Bukkit.getServer().createInventory(null, type);
		        } else {
		       	 	inventory = Bukkit.getServer().createInventory(null, type, title);
		        }
	        }
	   
	        // Read the serialized inventory
	        for (int i = 0; i < inventory.getSize(); i++) {
	            inventory.setItem(i, (ItemStack) dataInput.readObject());
	        }
	            
	        dataInput.close();
	        return inventory;
	    } catch (ClassNotFoundException e) {
	        throw new IOException("Unable to decode class type.", e);
	    }
	}
	    
	public static Inventory fromBase64(String data, String title) throws IOException {
		return fromBase64(data, InventoryType.CHEST, title);
	}
	
}
