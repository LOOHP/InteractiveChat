package com.loohp.interactivechat.Listeners;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.API.Events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.API.Events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.Modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.Modules.EnderchestDisplay;
import com.loohp.interactivechat.Modules.InventoryDisplay;
import com.loohp.interactivechat.Modules.ItemDisplay;
import com.loohp.interactivechat.Modules.MentionDisplay;
import com.loohp.interactivechat.Modules.PlayernameDisplay;
import com.loohp.interactivechat.Modules.ProcessCommands;
import com.loohp.interactivechat.Modules.SenderFinder;
import com.loohp.interactivechat.ObjectHolders.ProcessCommandsReturn;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPackets {
	
	public static void chatMessageListener() {		
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(InteractiveChat.plugin, ListenerPriority.MONITOR, PacketType.Play.Server.CHAT) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
		        	return;
		        }
		        
		        PacketContainer packet = event.getPacket();
		        Player reciever = event.getPlayer();		    
		        
		        if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12) || InteractiveChat.version.equals(MCVersion.V1_11)) {
			        ChatType type = packet.getChatTypes().read(0);
			        if (type.equals(ChatType.GAME_INFO)) {
			        	return;
			        }
		        }
		        
		        WrappedChatComponent wcc = packet.getChatComponents().read(0);
		        Object field1 = packet.getModifier().read(1);
		        if (wcc == null && field1 == null) {
		        	return;
		        }
		        BaseComponent[] basecomponentarray = (wcc != null) ? ComponentSerializer.parse(wcc.getJson()) : (BaseComponent[]) field1;		    
		        int field = (wcc != null) ? 0 : 1;
		        BaseComponent basecomponent = basecomponentarray[0];
		        try {
		        	if (basecomponent.toLegacyText().equals("")) {
		        		return;
		        	}
		        } catch (Exception e) {
		        	return;
		        }

		        if ((InteractiveChat.version.isOld()) && JsonUtils.containsKey(ComponentSerializer.toString(basecomponent), "translate")) {		       
		        	return;
		        }
		        
		        String rawMessageKey = basecomponent.toPlainText();
		        if (!InteractiveChat.keyTime.containsKey(rawMessageKey)) {
		        	InteractiveChat.keyTime.put(rawMessageKey, System.currentTimeMillis());
		        }
		        long unix = InteractiveChat.keyTime.get(rawMessageKey);
		        if (!InteractiveChat.cooldownbypass.containsKey(unix)) {
		        	InteractiveChat.cooldownbypass.put(unix, new HashSet<String>());
		        }
		        ProcessCommandsReturn commandsender = ProcessCommands.process(basecomponent);
		        Optional<Player> sender = commandsender.getSender() != null ? Optional.of(commandsender.getSender()) : SenderFinder.getSender(basecomponent, rawMessageKey);
		        basecomponent = commandsender.getBaseComponent();
		        if (sender.isPresent()) {
		        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
		        }
		        Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> {
		        	InteractiveChat.keyTime.remove(rawMessageKey);
		        	InteractiveChat.keyPlayer.remove(rawMessageKey);
		        }, 5);
		        
		        UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
				PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(event.isAsync(), reciever, basecomponent, field, preEventSenderUUID);
				Bukkit.getPluginManager().callEvent(preEvent);
				if (preEvent.getSender() != null) {
					Player newsender = Bukkit.getPlayer(preEvent.getSender());
					if (newsender != null) {
						sender = Optional.of(newsender);
					}
				}

		        if (InteractiveChat.usePlayerName) {
		        	basecomponent = PlayernameDisplay.process(basecomponent, rawMessageKey, sender, unix);
		        }

		        if (InteractiveChat.AllowMention && sender.isPresent()) {
		        	basecomponent = MentionDisplay.process(basecomponent, reciever, sender.get(), rawMessageKey, unix, event.isAsync());
		        }

		        if (InteractiveChat.useItem) {
		        	basecomponent = ItemDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
		        }

		        if (InteractiveChat.useInventory) {
		        	basecomponent = InventoryDisplay.process(basecomponent, sender, rawMessageKey, unix);
		        }
		        
		        if (InteractiveChat.useEnder) {
		        	basecomponent = EnderchestDisplay.process(basecomponent, sender, rawMessageKey, unix);
		        }
		        
		        basecomponent = CustomPlaceholderDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
		        
		        basecomponentarray[0] = InteractiveChat.FilterUselessColorCodes ? ChatComponentUtils.cleanUpLegacyText(basecomponent, reciever) : ChatComponentUtils.respectClientColorSettingsWithoutCleanUp(basecomponent, reciever);
		        String json = ComponentSerializer.toString(basecomponentarray);
		        boolean longerThanMaxLength = false;
		        if ((InteractiveChat.version.isLegacy() || InteractiveChat.protocolManager.getProtocolVersion(reciever) < 393) && json.length() > 32767) {
		        	longerThanMaxLength = true;
		        }
		        	
		        if (field == 0) {		   
		        	packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
		        } else {
		        	packet.getModifier().write(1, basecomponentarray);
		        }
		        
		        UUID postEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
		        PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(event.isAsync(), reciever, packet, postEventSenderUUID, longerThanMaxLength);
		        Bukkit.getPluginManager().callEvent(postEvent);
		        if (postEvent.isCancelled()) {
		        	event.setReadOnly(false);
		        	event.setCancelled(true);
		        	event.setReadOnly(true);
		        	if (longerThanMaxLength) {
		        		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Cancelled a chat packet bounded to " + reciever.getName() + " that is " + json.length() + " characters long (Max 32767) [THIS IS NOT A BUG]");
		        	}
		        }		        
		    }
		});	
	}

}
