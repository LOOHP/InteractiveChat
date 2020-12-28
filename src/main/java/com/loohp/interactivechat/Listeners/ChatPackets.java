package com.loohp.interactivechat.Listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
import com.loohp.interactivechat.Data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.Modules.CommandsDisplay;
import com.loohp.interactivechat.Modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.Modules.EnderchestDisplay;
import com.loohp.interactivechat.Modules.InventoryDisplay;
import com.loohp.interactivechat.Modules.ItemDisplay;
import com.loohp.interactivechat.Modules.MentionDisplay;
import com.loohp.interactivechat.Modules.PlayernameDisplay;
import com.loohp.interactivechat.Modules.ProcessCommands;
import com.loohp.interactivechat.Modules.SenderFinder;
import com.loohp.interactivechat.ObjectHolders.PlayerWrapper;
import com.loohp.interactivechat.ObjectHolders.ProcessCommandsReturn;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPackets implements Listener {
	
	private static Map<UUID, List<UUID>> messagesOrder = new ConcurrentHashMap<>();
	
	public static void chatMessageListener() {		
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Server.CHAT)) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		    	if (!event.isFiltered()) {
		    		return;
		    	}
		    	event.setReadOnly(false);
		    	event.setCancelled(true);
		    	event.setReadOnly(false);
		    	PacketContainer packet = event.getPacket().deepClone();
		        Player reciever = event.getPlayer();
		        
		        List<UUID> q = messagesOrder.get(reciever.getUniqueId());
		        if (q == null) {
		        	q = Collections.synchronizedList(new LinkedList<>());
		        	messagesOrder.put(reciever.getUniqueId(), q);		  
		        }
		        UUID messageUUID = UUID.randomUUID();
		        q.add(messageUUID);
		        List<UUID> queue = q;
		    	
		    	Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			    	InteractiveChat.messagesCounter.getAndIncrement();
			    	int debug = 0;
			    	try {
			        if (!event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
			        	return;
			        }
			        debug++;
			          
			        debug++;
			        if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
				        ChatType type = packet.getChatTypes().read(0);
				        if (type == null || type.equals(ChatType.GAME_INFO)) {
				        	return;
				        }
			        } else {
			        	byte type = packet.getBytes().read(0);
			        	if (type == (byte) 2) {
			        		return;
			        	}
			        }
			        debug++;
			        WrappedChatComponent wcc = packet.getChatComponents().read(0);
			        Object field1 = packet.getModifier().read(1);
			        if (wcc == null && field1 == null) {
			        	return;
			        }
			        debug++;
			        BaseComponent[] basecomponentarray = null;
			        int field = -1;
			        try {
				        if (wcc != null) {
				        	basecomponentarray = ComponentSerializer.parse(wcc.getJson());
				        	field = 0;
				        } else {
				        	basecomponentarray = (BaseComponent[]) field1;
				        	field = 1;
				        }
			        } catch (Exception e) {
			        	try {
				        	basecomponentarray = (BaseComponent[]) field1;
				        	field = 1;
			        	} catch (Exception skip) {
			        		return;
			        	}
			        }
			        BaseComponent basecomponent;
			        try {
			        	basecomponent = ChatComponentUtils.join(ComponentSerializer.parse(ChatColorUtils.filterIllegalColorCodes(ComponentSerializer.toString(basecomponentarray))));
			        } catch (Exception e) {
			        	return;
			        }
			        debug++;
			        try {
			        	String text = basecomponent.toLegacyText();
			        	if (text.equals("") || InteractiveChat.messageToIgnore.stream().anyMatch(each -> text.matches(each))) {
			        		return;
			        	}
			        } catch (Exception e) {
			        	return;
			        }
			        debug++;
			        if ((InteractiveChat.version.isOld()) && JsonUtils.containsKey(ComponentSerializer.toString(basecomponent), "translate")) {		       
			        	return;
			        }
			        debug++;
			        String rawMessageKey = basecomponent.toPlainText();
			        if (!InteractiveChat.keyTime.containsKey(rawMessageKey)) {
			        	InteractiveChat.keyTime.put(rawMessageKey, System.currentTimeMillis());
			        }
			        debug++;
			        long unix = InteractiveChat.keyTime.get(rawMessageKey);
			        if (!InteractiveChat.cooldownbypass.containsKey(unix)) {
			        	InteractiveChat.cooldownbypass.put(unix, new HashSet<String>());
			        }
			        debug++;
			        ProcessCommandsReturn commandsender = ProcessCommands.process(basecomponent);
			        Optional<PlayerWrapper> sender = Optional.empty();
			        if (commandsender.getSender() != null) {
			        	Player bukkitplayer = Bukkit.getPlayer(commandsender.getSender());
			        	if (bukkitplayer != null) {
			        		sender = Optional.of(new PlayerWrapper(bukkitplayer));
			        	} else {
			        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(commandsender.getSender()));
			        	}
			        }
			        if (!sender.isPresent()) {
			        	sender = SenderFinder.getSender(basecomponent, rawMessageKey);
			        }
			        if (sender.isPresent() && !sender.get().isLocal()) {
			        	if (event.isFiltered()) {
			        		PacketContainer clone = packet.deepClone();
			        		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
								try {
									InteractiveChat.protocolManager.sendServerPacket(reciever, clone, false);
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								}
							}, (int) Math.ceil((double) InteractiveChat.remoteDelay / 50));
			        		event.setReadOnly(false);
			        		event.setCancelled(true);
			        		event.setReadOnly(true);
			        		return;
			        	}
			        }
			        basecomponent = commandsender.getBaseComponent();
			        if (sender.isPresent()) {
			        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
			        }		 
			        debug++;		
			        UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
					PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(true, reciever, basecomponent, field, preEventSenderUUID);
					Bukkit.getPluginManager().callEvent(preEvent);
					if (preEvent.getSender() != null) {
						Player newsender = Bukkit.getPlayer(preEvent.getSender());
						if (newsender != null) {
							sender = Optional.of(new PlayerWrapper(newsender));
						}
					}
					basecomponent = preEvent.getBaseComponent();
					debug++;
			        if (InteractiveChat.usePlayerName) {
			        	basecomponent = PlayernameDisplay.process(basecomponent, rawMessageKey, sender, unix);
			        }
			        debug++;
			        if (InteractiveChat.AllowMention && sender.isPresent()) {
			        	PlayerData data = InteractiveChat.playerDataManager.getPlayerData(reciever);
			        	if (data == null || !data.isMentionDisabled()) {
			        		basecomponent = MentionDisplay.process(basecomponent, reciever, sender.get(), rawMessageKey, unix, true);
			        	}
			        }
			        debug++;
			        if (InteractiveChat.useItem) {
			        	basecomponent = ItemDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
			        }
			        debug++;
			        if (InteractiveChat.useInventory) {
			        	basecomponent = InventoryDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
			        }
			        debug++;
			        if (InteractiveChat.useEnder) {
			        	basecomponent = EnderchestDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
			        }
			        debug++;
			        basecomponent = CustomPlaceholderDisplay.process(basecomponent, sender, reciever, rawMessageKey, InteractiveChat.placeholderList, unix);
			        debug++;
			        if (InteractiveChat.clickableCommands) {
			        	basecomponent = CommandsDisplay.process(basecomponent);
			        }
			        debug++;
			        if (InteractiveChat.version.isPost1_16()) {
				        if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
				        	basecomponent = ChatComponentUtils.translatePluginFontFormatting(basecomponent);
				        }
			        }
			        debug++;		        
			        basecomponent = InteractiveChat.FilterUselessColorCodes ? ChatComponentUtils.cleanUpLegacyText(basecomponent, reciever) : ChatComponentUtils.respectClientColorSettingsWithoutCleanUp(basecomponent, reciever);       
			        String json = ComponentSerializer.toString(basecomponent);
			        boolean longerThanMaxLength = false;
			        if ((InteractiveChat.block30000 && json.length() > 30000) || ((InteractiveChat.version.isLegacy() || InteractiveChat.protocolManager.getProtocolVersion(reciever) < 393) && json.length() > 30000) || (!InteractiveChat.version.isLegacy() && json.length() > 262000)) {
			        	longerThanMaxLength = true;
			        }
			        debug++;
			        //Bukkit.getConsoleSender().sendMessage(ComponentSerializer.toString(basecomponent));
			        if (field == 0) {
			        	packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
			        } else {
			        	packet.getModifier().write(1, new BaseComponent[]{basecomponent});
			        }
			        UUID postEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : new UUID(0, 0);
			        if (packet.getUUIDs().size() > 0) {
			        	packet.getUUIDs().write(0, postEventSenderUUID);
			        }
			        PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(true, reciever, packet, postEventSenderUUID, longerThanMaxLength);
			        Bukkit.getPluginManager().callEvent(postEvent);
			        debug++;	  
			        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
			        	InteractiveChat.keyTime.remove(rawMessageKey);
			        	InteractiveChat.keyPlayer.remove(rawMessageKey);
			        }, 10);
			        debug++;
			        if (postEvent.isCancelled()) {
			        	event.setReadOnly(false);
			        	event.setCancelled(true);
			        	event.setReadOnly(true);
			        	if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
			        		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Cancelled a chat packet bounded to " + reciever.getName() + " that is " + json.length() + " characters long (Longer than maximum allowed in a chat packet) [THIS IS NOT A BUG]");
			        	}
			        }
			        debug++;
			        
			        long timeout = System.currentTimeMillis() + 1000;
			        while (!queue.get(0).equals(messageUUID) && System.currentTimeMillis() < timeout) {
			        	TimeUnit.NANOSECONDS.sleep(100000);
			        }
			        queue.remove(messageUUID);
			        InteractiveChat.protocolManager.sendServerPacket(reciever, packet, false);			   
			    	} catch (Exception e) {
			    		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "IC DEBUG " + event.getPlayer().getName() + " " + debug);
			    		e.printStackTrace();
			    	}
		    	});
		    }
		});	
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		messagesOrder.remove(event.getPlayer().getUniqueId());
	}

}
