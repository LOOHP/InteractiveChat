package com.loohp.interactivechat.Listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.ObjectHolders.ProcessCommandsResult;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPackets implements Listener {
	
	private static Map<UUID, Queue<UUID>> messagesOrder = new ConcurrentHashMap<>();
	private static AtomicBoolean lock = new AtomicBoolean(false);
	
	public static void chatMessageListener() {		
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Server.CHAT)) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		    	if (!event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
		    		return;
		    	}
		    	
		    	InteractiveChat.messagesCounter.getAndIncrement();
		    	
		    	PacketContainer packetOriginal = event.getPacket();
		    	
		    	if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
			        ChatType type = packetOriginal.getChatTypes().read(0);
			        if (type == null || type.equals(ChatType.GAME_INFO)) {
			        	return;
			        }
		        } else {
		        	byte type = packetOriginal.getBytes().read(0);
		        	if (type == (byte) 2) {
		        		return;
		        	}
		        }
		    	
		    	event.setReadOnly(false);
		    	event.setCancelled(true);
		    	event.setReadOnly(false);
		    	
		        Player reciever = event.getPlayer();
		        PacketContainer packet = packetOriginal.deepClone();
		        
		        Queue<UUID> q = messagesOrder.get(reciever.getUniqueId());
		        if (q == null) {
		        	q = new ConcurrentLinkedQueue<UUID>();
		        	messagesOrder.put(reciever.getUniqueId(), q);		  
		        }
		        UUID messageUUID = UUID.randomUUID();
		        q.add(messageUUID);
		        Queue<UUID> queue = q;
		        
		        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> queue.remove(messageUUID), (InteractiveChat.bungeecordMode ? (int) Math.ceil((double) InteractiveChat.remoteDelay / 50) : 0) + 60);
		    	
		    	Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
		    		processPacket(reciever, packet, messageUUID, queue, event.isFiltered());
		    	});
		    }
		});	
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		messagesOrder.remove(event.getPlayer().getUniqueId());
	}
	
	private static void orderAndSend(Player reciever, PacketContainer packet, UUID messageUUID, Queue<UUID> queue) throws InterruptedException {
		long timeout = System.currentTimeMillis() + (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 1000;
        while (queue.peek() != null && !queue.peek().equals(messageUUID) && System.currentTimeMillis() < timeout) {
        	TimeUnit.NANOSECONDS.sleep(10000);
        }
        queue.remove(messageUUID);
        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
			try {
				InteractiveChat.protocolManager.sendServerPacket(reciever, packet, false);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}
	
	private static void processPacket(Player reciever, PacketContainer packet, UUID messageUUID, Queue<UUID> queue, boolean isFiltered) {
		long timeout = System.currentTimeMillis() + (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 1000;
		while (lock.get() && System.currentTimeMillis() < timeout) {
			try {TimeUnit.NANOSECONDS.sleep(10000);} catch (InterruptedException e) {}
		}
		lock.set(true);
		PacketContainer originalPacket = packet.deepClone();		    		
    	try {
	        WrappedChatComponent wcc = packet.getChatComponents().read(0);
	        Object field1 = packet.getModifier().read(1);
	        if (wcc == null && field1 == null) {
	        	lock.set(false);
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

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
	        		lock.set(false);
	        		orderAndSend(reciever, packet, messageUUID, queue);
	        		return;
	        	}
	        }
	        BaseComponent basecomponent;
	        try {
	        	basecomponent = ChatComponentUtils.join(ComponentSerializer.parse(ChatColorUtils.filterIllegalColorCodes(ComponentSerializer.toString(basecomponentarray))));
	        } catch (Exception e) {
	        	lock.set(false);
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

	        try {
	        	String text = basecomponent.toLegacyText();
	        	if (text.equals("") || InteractiveChat.messageToIgnore.stream().anyMatch(each -> text.matches(each))) {
	        		lock.set(false);
	        		orderAndSend(reciever, packet, messageUUID, queue);
	        		return;
	        	}
	        } catch (Exception e) {
	        	lock.set(false);
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

	        if (InteractiveChat.version.isOld() && JsonUtils.containsKey(ComponentSerializer.toString(basecomponent), "translate")) {
	        	lock.set(false);
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

	        String rawMessageKey = basecomponent.toPlainText();
	        if (!InteractiveChat.keyTime.containsKey(rawMessageKey)) {
	        	InteractiveChat.keyTime.put(rawMessageKey, System.currentTimeMillis());
	        }

	        Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
	        long unix = timeKey == null ? System.currentTimeMillis() : timeKey;
	        if (!InteractiveChat.cooldownbypass.containsKey(unix)) {
	        	InteractiveChat.cooldownbypass.put(unix, new HashSet<>());
	        }

	        ProcessCommandsResult commandsender = ProcessCommands.process(basecomponent);
	        Optional<ICPlayer> sender = Optional.empty();
	        if (commandsender.getSender() != null) {
	        	Player bukkitplayer = Bukkit.getPlayer(commandsender.getSender());
	        	if (bukkitplayer != null) {
	        		sender = Optional.of(new ICPlayer(bukkitplayer));
	        	} else {
	        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(commandsender.getSender()));
	        	}
	        }
	        if (!sender.isPresent()) {
	        	sender = SenderFinder.getSender(basecomponent, rawMessageKey);
	        }
	        if (sender.isPresent() && !sender.get().isLocal()) {
	        	if (isFiltered) {
	        		lock.set(false);
	        		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> {
	        			processPacket(reciever, packet, messageUUID, queue, false);
					}, (int) Math.ceil((double) InteractiveChat.remoteDelay / 50));
	        		return;
	        	}
	        }
	        basecomponent = commandsender.getBaseComponent();
	        if (sender.isPresent()) {
	        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
	        }		 

	        UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
			PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(true, reciever, basecomponent, field, preEventSenderUUID);
			Bukkit.getPluginManager().callEvent(preEvent);
			if (preEvent.getSender() != null) {
				Player newsender = Bukkit.getPlayer(preEvent.getSender());
				if (newsender != null) {
					sender = Optional.of(new ICPlayer(newsender));
				}
			}
			basecomponent = preEvent.getBaseComponent();
			
	        if (InteractiveChat.usePlayerName) {
	        	basecomponent = PlayernameDisplay.process(basecomponent, rawMessageKey, sender, unix);
	        }

	        if (InteractiveChat.AllowMention && sender.isPresent()) {
	        	PlayerData data = InteractiveChat.playerDataManager.getPlayerData(reciever);
	        	if (data == null || !data.isMentionDisabled()) {
	        		basecomponent = MentionDisplay.process(basecomponent, reciever, sender.get(), rawMessageKey, unix, true);
	        	}
	        }

	        if (InteractiveChat.useItem) {
	        	basecomponent = ItemDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
	        }

	        if (InteractiveChat.useInventory) {
	        	basecomponent = InventoryDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
	        }

	        if (InteractiveChat.useEnder) {
	        	basecomponent = EnderchestDisplay.process(basecomponent, sender, reciever, rawMessageKey, unix);
	        }

	        basecomponent = CustomPlaceholderDisplay.process(basecomponent, sender, reciever, rawMessageKey, InteractiveChat.placeholderList, unix);

	        if (InteractiveChat.clickableCommands) {
	        	basecomponent = CommandsDisplay.process(basecomponent);
	        }

	        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
		        if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
		        	basecomponent = ChatComponentUtils.translatePluginFontFormatting(basecomponent);
		        }
	        }
        
	        basecomponent = InteractiveChat.filterUselessColorCodes ? ChatComponentUtils.cleanUpLegacyText(basecomponent, reciever) : ChatComponentUtils.respectClientColorSettingsWithoutCleanUp(basecomponent, reciever);       
	        String json = ComponentSerializer.toString(basecomponent);
	        boolean longerThanMaxLength = false;
	        if (InteractiveChat.sendOriginalIfTooLong && json.length() > 32767) {
	        	longerThanMaxLength = true;
	        }

	        //Bukkit.getConsoleSender().sendMessage(json);
	        if (field == 0) {
	        	packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
	        } else {
	        	packet.getModifier().write(1, new BaseComponent[]{basecomponent});
	        }
	        UUID postEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : new UUID(0, 0);
	        if (packet.getUUIDs().size() > 0) {
	        	packet.getUUIDs().write(0, postEventSenderUUID);
	        }
	        PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(true, reciever, packet, postEventSenderUUID, originalPacket, InteractiveChat.sendOriginalIfTooLong, longerThanMaxLength);
	        Bukkit.getPluginManager().callEvent(postEvent);

	        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
	        	InteractiveChat.keyTime.remove(rawMessageKey);
	        	InteractiveChat.keyPlayer.remove(rawMessageKey);
	        }, 10);

	        if (postEvent.isCancelled()) {
        		if (postEvent.sendOriginalIfCancelled()) {
        			PacketContainer originalPacketModified = postEvent.getOriginal();
        			lock.set(false);
		        	orderAndSend(reciever, originalPacketModified, messageUUID, queue);
		        	return;
        		} else {
        			if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
        				Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Cancelled a chat packet bounded to " + reciever.getName() + " that is " + json.length() + " characters long (Longer than maximum allowed in a chat packet) [THIS IS NOT A BUG]");
        			}
        		}
	        	queue.remove(messageUUID);
	        	return;
	        }
	        lock.set(false);
	        orderAndSend(reciever, packet, messageUUID, queue);
    	} catch (Exception e) {
    		e.printStackTrace();
    		try {
    			lock.set(false);
				orderAndSend(reciever, originalPacket, messageUUID, queue);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				lock.set(false);
			}
    	}
	}

}
