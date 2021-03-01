package com.loohp.interactivechat.Utils;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class TitleUtils {
	
	public static void sendTitle(Player player, String title, String subtitle, String actionbar, int fadeIn, int stay, int fadeOut) {
		PacketContainer packet1 = null;
		if (!title.equals("")) {
			try {
				packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
				packet1.getTitleActions().write(0, TitleAction.TITLE);
				packet1.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(new TextComponent(title))));
			} catch (Exception e) {
				packet1 = null;
			}
		}
		
		PacketContainer packet2 = null;
		if (!subtitle.equals("")) {
			try {
				packet2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
				packet2.getTitleActions().write(0, TitleAction.SUBTITLE);
				packet2.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(new TextComponent(subtitle))));
			} catch (Exception e) {
				packet2 = null;
			}
		}
		
		PacketContainer packet3 = null;
		if (!actionbar.equals("")) {
			try {
				packet3 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
				packet3.getTitleActions().write(0, TitleAction.ACTIONBAR);
				packet3.getChatComponents().write(0, WrappedChatComponent.fromJson(ComponentSerializer.toString(new TextComponent(actionbar))));
			} catch (Exception e) {
				packet3 = null;
			}
		}
		
		PacketContainer packet4 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.TITLE);
		packet4.getTitleActions().write(0, TitleAction.TIMES);
		packet4.getIntegers().write(0, fadeIn);
		packet4.getIntegers().write(1, stay);
		packet4.getIntegers().write(2, fadeOut);
		
		try {
			if (packet1 != null) {
				InteractiveChat.protocolManager.sendServerPacket(player, packet1);	
			}
			if (packet2 != null) {
				InteractiveChat.protocolManager.sendServerPacket(player, packet2);	
			}
			if (packet3 != null) {
				InteractiveChat.protocolManager.sendServerPacket(player, packet3);	
			}
			InteractiveChat.protocolManager.sendServerPacket(player, packet4);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
