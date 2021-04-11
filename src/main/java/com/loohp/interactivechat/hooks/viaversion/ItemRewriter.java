package com.loohp.interactivechat.hooks.viaversion;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ItemNBTUtils;
import com.loohp.interactivechat.utils.MCVersion;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.nbt.BinaryTagIO;

public class ItemRewriter {
	
	private static final Map<Integer, MCVersion> PROTOCOL_TO_VERSION = new HashMap<>();
	private static MCVersion SERVER_PROTOCOL_VERSION;
	
	static {
		for (int i = 0; i < 47; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.UNSUPPORTED);
		}
		for (int i = 47; i < 48; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_8);
		}
		for (int i = 48; i < 111; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_9);
		}
		for (int i = 201; i < 211; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_10);
		}
		for (int i = 301; i < 317; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_11);
		}
		for (int i = 317; i < 341; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_12);
		}
		for (int i = 341; i < 405; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_13);
		}
		for (int i = 441; i < 499; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_14);
		}
		for (int i = 550; i < 579; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_15);
		}
		for (int i = 701; i < 755; i++) {
			PROTOCOL_TO_VERSION.put(i, MCVersion.V1_16);
		}
		
		switch (InteractiveChat.version) {
		case V1_8:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_8;
		case V1_8_3:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_8;
		case V1_8_4:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_8;
		case V1_9:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_9;
		case V1_9_4:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_9;
		case V1_10:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_10;
		case V1_11:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_11;
		case V1_12:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_12;
		case V1_13:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_13;
		case V1_13_1:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_13;
		case V1_14:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_14;
		case V1_15:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_15;
		case V1_16:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_16;
		case V1_16_2:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_16;
		case V1_16_4:
			SERVER_PROTOCOL_VERSION = MCVersion.V1_16;
		case UNSUPPORTED:
			SERVER_PROTOCOL_VERSION = MCVersion.UNSUPPORTED;
		default:
			SERVER_PROTOCOL_VERSION = MCVersion.values()[0];
		}
	}
	
	public static MCVersion getServerProtocolVersion() {
		return SERVER_PROTOCOL_VERSION;
	}
	
	public static Map<Integer, MCVersion> getProtocolToVersionMap() {
		return Collections.unmodifiableMap(PROTOCOL_TO_VERSION);
	}
	
	public static String getConvertedItemStackNbtJson(ItemStack itemstack, Player player) throws IOException {
		return getConvertedItemStackNbtJson(itemstack, Via.getAPI().getPlayerVersion(player.getUniqueId()));
	}
	
	@SuppressWarnings("deprecation")
	public static String getConvertedItemStackNbtJson(ItemStack itemstack, int protocol) throws IOException {
		return getConvertedItemStackNbtJson(itemstack.getType().getId(), (byte) itemstack.getAmount(), itemstack.getDurability(), ItemNBTUtils.getNMSItemStackJson(itemstack), protocol);
	}
	
	public static String getConvertedItemStackNbtJson(int id, byte amount, short data, String nbt, int protocol) throws IOException {
		return getConvertedItemStackNbtJson(id, amount, data, nbt, PROTOCOL_TO_VERSION.get(protocol), SERVER_PROTOCOL_VERSION);
	}
	
	public static String getConvertedItemStackNbtJson(int id, byte amount, short data, String nbt, MCVersion clientVersion, MCVersion serverVersion) throws IOException {
		Item item = new Item(id, amount, data, BinaryTagIO.readString(nbt));
		
		if (serverVersion.isNewerThan(clientVersion)) {
			if (serverVersion.isNewerThan(MCVersion.V1_15) && clientVersion.isOlderOrEqualTo(MCVersion.V1_15)) {
				us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.InventoryPackets.toClient(item);
			}
			if (serverVersion.isNewerThan(MCVersion.V1_14) && clientVersion.isOlderOrEqualTo(MCVersion.V1_14)) {
				us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets.toClient(item);
			}
			if (serverVersion.isNewerThan(MCVersion.V1_13) && clientVersion.isOlderOrEqualTo(MCVersion.V1_14)) {
				us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets.toClient(item);
			}
			if (serverVersion.isNewerThan(MCVersion.V1_12) && clientVersion.isOlderOrEqualTo(MCVersion.V1_13)) {
				us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets.toClient(item);
			}
			return BinaryTagIO.writeString(item.getTag());
		} else if (serverVersion.isOlderThan(clientVersion)) {
			if (serverVersion.isOlderOrEqualTo(MCVersion.V1_12) && clientVersion.isNewerThan(MCVersion.V1_12)) {
				us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.InventoryPackets.toServer(item);
			}
			if (serverVersion.isOlderOrEqualTo(MCVersion.V1_13) && clientVersion.isNewerThan(MCVersion.V1_13)) {
				us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets.toServer(item);
			}
			if (serverVersion.isOlderOrEqualTo(MCVersion.V1_14) && clientVersion.isNewerThan(MCVersion.V1_14)) {
				us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.InventoryPackets.toServer(item);
			}
			if (serverVersion.isOlderOrEqualTo(MCVersion.V1_15) && clientVersion.isNewerThan(MCVersion.V1_15)) {
				us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.InventoryPackets.toServer(item);
			}
			return BinaryTagIO.writeString(item.getTag());
		} else {
			return nbt;
		}
	}

}
