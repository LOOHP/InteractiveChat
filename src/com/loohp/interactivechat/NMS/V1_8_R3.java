package com.loohp.interactivechat.NMS;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.loohp.interactivechat.Utils.JsonUtils;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class V1_8_R3 {
	
	public static void sendTitle(Player player, String title, String subtitle, int time) {
		IChatBaseComponent chatTitle = ChatSerializer.a(JsonUtils.toJSON(title));
		IChatBaseComponent chatSubtitle = ChatSerializer.a(JsonUtils.toJSON(subtitle));

		PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle subtitlepacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, chatSubtitle);
		PacketPlayOutTitle length = new PacketPlayOutTitle(10, time, 20);

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlepacket);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitlepacket);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
	}

}
