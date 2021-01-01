package com.loohp.interactivechat.Hooks.VentureChat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import mineverse.Aust1n46.chat.ChatMessage;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.versions.VersionHandler;

//This class listens for chat packets and intercepts them before they are sent to the Player.
//The packets are modified to include advanced json formating and the message remover button if the 
//player has permission to remove messages.
public class PacketListener extends PacketAdapter {
	
	public PacketListener() {
		super(MineverseChat.getInstance(), ListenerPriority.HIGHEST, new PacketType[] { PacketType.Play.Server.CHAT });
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if(event.isCancelled() || event.getPacketType() != PacketType.Play.Server.CHAT) {
			return;
		}
		
		MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(event.getPlayer());
		if(mcp == null) {
			return;
		}
		
		PacketContainer packet = event.getPacket();
		WrappedChatComponent chat = packet.getChatComponents().read(0);
		if(chat == null) {
			return;
		}
		
		if(MineverseChat.posField == null) {
			return;
		}
		
		try {
			if(VersionHandler.is1_7_2() || VersionHandler.is1_7_10() || VersionHandler.is1_7_9()) {
				if(!(((boolean) MineverseChat.posField.get(packet.getHandle())))) {
					return;
				}
			}
			else if(VersionHandler.is1_8() || VersionHandler.is1_9() || VersionHandler.is1_10() || VersionHandler.is1_11()){
				if(((Byte) MineverseChat.posField.get(packet.getHandle())).intValue() > 1) {
					return;
				}
			}
			else {
				if(((Object) MineverseChat.posField.get(packet.getHandle())) == MineverseChat.chatMessageType.getEnumConstants()[2]) {
					return;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		String message = Format.toPlainText(chat.getHandle(), chat.getHandleType());
		if(message == null) {
			return;
		}
		int hash = message.hashCode();
		mcp.addMessage(new ChatMessage(chat, message, hash));
	}
}