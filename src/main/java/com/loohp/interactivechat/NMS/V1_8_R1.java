package com.loohp.interactivechat.NMS;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;

public class V1_8_R1 {
	
	public static void sendTitle(Player player, String title, String subtitle, int time) {
		IChatBaseComponent chatTitle = ChatSerializer.a(ComponentSerializer.toString(TextComponent.fromLegacyText(title)));
		IChatBaseComponent chatSubtitle = ChatSerializer.a(ComponentSerializer.toString(TextComponent.fromLegacyText(subtitle)));

		PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle subtitlepacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, chatSubtitle);
		PacketPlayOutTitle length = new PacketPlayOutTitle(10, time, 20);

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlepacket);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitlepacket);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
	}

}
