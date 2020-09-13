package com.loohp.interactivechat.Utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class DataTypeIO {
	
	public static Inventory readInventory(ByteArrayDataInput in, Charset charset) throws IOException {
		boolean hasTitle = in.readBoolean();
		String title = hasTitle ? readString(in, charset) : null;
		String data = readString(in, charset);
		return InventoryUtils.fromBase64(data, title);
	}

	public static void writeInventory(ByteArrayDataOutput out, String title, Inventory inventory, Charset charset) throws IOException {
		if (title == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			writeString(out, title, charset);
		}
	    writeString(out, InventoryUtils.toBase64(inventory), charset);
	}

	public static ItemStack readItemStack(ByteArrayDataInput in, Charset charset) throws IOException {
		String data = readString(in, charset);

		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return config.getItemStack("i", null);
	}

	public static void writeItemStack(ByteArrayDataOutput out, ItemStack itemStack, Charset charset) throws IOException {
		YamlConfiguration config = new YamlConfiguration();
		config.set("i", itemStack);
		writeString(out, config.saveToString(), charset);
	}

	public static UUID readUUID(ByteArrayDataInput in) throws IOException {
		return new UUID(in.readLong(), in.readLong());
	}

	public static void writeUUID(ByteArrayDataOutput out, UUID uuid) throws IOException {
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
	}

	public static String readString(ByteArrayDataInput in, Charset charset) throws IOException {
		int length = in.readInt();

		if (length == -1) {
			throw new IOException("Premature end of stream.");
		}

		byte[] b = new byte[length];
		in.readFully(b);
		return new String(b, charset);
	}

	public static int getStringLength(String string, Charset charset) throws IOException {
		byte[] bytes = string.getBytes(charset);
		return bytes.length;
	}

	public static void writeString(ByteArrayDataOutput out, String string, Charset charset) throws IOException {
		byte[] bytes = string.getBytes(charset);
		out.writeInt(bytes.length);
		out.write(bytes);
	}

}
