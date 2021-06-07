package com.loohp.interactivechat.listeners;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.api.events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.hooks.venturechat.VentureChatInjection;
import com.loohp.interactivechat.modules.CommandsDisplay;
import com.loohp.interactivechat.modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.modules.EnderchestDisplay;
import com.loohp.interactivechat.modules.HoverableItemDisplay;
import com.loohp.interactivechat.modules.InventoryDisplay;
import com.loohp.interactivechat.modules.ItemDisplay;
import com.loohp.interactivechat.modules.MentionDisplay;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.modules.ProcessAccurateSender;
import com.loohp.interactivechat.modules.ProcessCommands;
import com.loohp.interactivechat.modules.SenderFinder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.OutboundPacket;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.ComponentModernizing;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.PlayerUtils.ColorSettings;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class OutChatPacket implements Listener {
	
	private static Map<UUID, Queue<UUID>> messagesOrder = new ConcurrentHashMap<>();
	private static Queue<OutboundPacket> sendingQueue = new ConcurrentLinkedQueue<>();
	private static int chatFieldsSize;
	
	private static ExecutorService asyncThreadPool;
	private static Map<Future<?>, Long> executingTasks = new ConcurrentHashMap<>();
	
	static {
		PacketContainer packet = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.CHAT);
		List<String> matches = Stream.of(ChatComponentType.byPriority()).map(each -> each.getMatchingRegex()).collect(Collectors.toList());
		
		for (chatFieldsSize = 1; chatFieldsSize < packet.getModifier().size(); chatFieldsSize++) {
			String clazz = packet.getModifier().getField(chatFieldsSize).getType().getName();
			if (!matches.stream().anyMatch(each -> clazz.matches(each))) {
				chatFieldsSize--;
				break;
			}
		}
		
		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("InteractiveChat Async ChatMessage Processing Thread #%d").build();
		asyncThreadPool = Executors.newCachedThreadPool(factory);
	}
	
	private static void execute(Runnable runnable) {
		Future<?> future = asyncThreadPool.submit(runnable);
		executingTasks.put(future, System.currentTimeMillis());
	}
	
	public static void shutdown() {
		asyncThreadPool.shutdown();
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
        sendingQueue.add(new OutboundPacket(reciever, packet));
        queue.remove(messageUUID);
	}
	
	private static void run() {
		Bukkit.getScheduler().runTaskTimer(InteractiveChat.plugin, () -> {
			while (!sendingQueue.isEmpty()) {
				OutboundPacket out = sendingQueue.poll();
				try {
					if (out.getReciever().isOnline()) {
						InteractiveChat.protocolManager.sendServerPacket(out.getReciever(), out.getPacket(), false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1);
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			long time = System.currentTimeMillis();
			Iterator<Entry<Future<?>, Long>> itr = executingTasks.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Future<?>, Long> entry = itr.next();
				Future<?> future = entry.getKey();
				if (future.isDone()) {					
					itr.remove();
				} else if (entry.getValue() + 10000 < time) {
					future.cancel(true);
					itr.remove();
				}
			}
		}, 0, 20);
	}
	
	public static void chatMessageListener() {	
		run();
		InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Server.CHAT)) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		    	if (event.isPlayerTemporary() || !event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
		    		return;
		    	}
		    	
		    	if (InteractiveChat.ventureChatHook) {
		    		VentureChatInjection.firePacketListener(event);
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
		        	q = new ConcurrentLinkedQueue<>();
		        	messagesOrder.put(reciever.getUniqueId(), q);		  
		        }
		        UUID messageUUID = UUID.randomUUID();
		        q.add(messageUUID);
		        Queue<UUID> queue = q;
		        
		        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> queue.remove(messageUUID), (InteractiveChat.bungeecordMode ? (int) Math.ceil((double) InteractiveChat.remoteDelay / 50) : 0) + 60);
		    	
		        execute(() -> {
		    		processPacket(reciever, packet, messageUUID, queue, event.isFiltered());
		    	});
		    }
		});	
	}
	
	private static void processPacket(Player reciever, PacketContainer packet, UUID messageUUID, Queue<UUID> queue, boolean isFiltered) {
		PacketContainer originalPacket = packet.deepClone();
    	try {
    		Component component = null;
    		ChatComponentType type = null;
	        int field = -1;
	        
	        search: for (ChatComponentType t : ChatComponentType.byPriority()) {
	        	for (int i = 0; i < packet.getModifier().size(); i++) {
	        		if (packet.getModifier().read(i) != null && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
	        			try {
	        				component = t.convertFrom(packet.getModifier().read(i));
	        			} catch (Throwable e) {
	        	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	        	return;
	        			}
	        			field = i;
	        			type = t;
	        			break search;
	        		}
	        	}
	        }
	        if (field < 0 || type == null || component == null) {
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }
	        
	        String legacyText = LegacyComponentSerializer.legacySection().serializeOr(component, "");
	        try {
	        	if (legacyText.equals("") || InteractiveChat.messageToIgnore.stream().anyMatch(each -> legacyText.matches(each))) {
	        		orderAndSend(reciever, packet, messageUUID, queue);
	        		return;
	        	}
	        } catch (Exception e) {
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

	        if (InteractiveChat.version.isOld() && JsonUtils.containsKey(Registry.ADVENTURE_GSON_SERIALIZER.serialize(component), "translate")) {
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

	        @SuppressWarnings("unused")
			boolean isCommand = false;
	        @SuppressWarnings("unused")
	        boolean isChat = false;

	        Optional<ICPlayer> sender = Optional.empty();
	        String rawMessageKey = PlainComponentSerializer.plain().serializeOr(component, "");
	        	   
	        InteractiveChat.keyTime.putIfAbsent(rawMessageKey, System.currentTimeMillis());

	        Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
	        long unix = timeKey == null ? System.currentTimeMillis() : timeKey;
	        if (!InteractiveChat.cooldownbypass.containsKey(unix)) {
	        	InteractiveChat.cooldownbypass.put(unix, new HashSet<>());
	        }
	        
	        ProcessSenderResult commandSender = ProcessCommands.process(component);		    
	        if (commandSender.getSender() != null) {
	        	Player bukkitplayer = Bukkit.getPlayer(commandSender.getSender());
	        	if (bukkitplayer != null) {
	        		sender = Optional.of(new ICPlayer(bukkitplayer));
	        		isCommand = true;
	        	} else {
	        		ICPlayer remote = InteractiveChat.remotePlayers.get(commandSender.getSender());
	        		if (remote != null) {
	        			sender = Optional.of(remote);
	        			isCommand = true;
	        		}
	        	}
	        }
	        ProcessSenderResult chatSender = null;
	        if (!sender.isPresent()) {
	        	if (InteractiveChat.useAccurateSenderFinder) {
	        		chatSender = ProcessAccurateSender.process(component);
	        		if (chatSender.getSender() != null) {
	    	        	Player bukkitplayer = Bukkit.getPlayer(chatSender.getSender());
	    	        	if (bukkitplayer != null) {
	    	        		sender = Optional.of(new ICPlayer(bukkitplayer));
	    	        		isChat = true;
	    	        	} else {
	    	        		ICPlayer remote = InteractiveChat.remotePlayers.get(chatSender.getSender());
	    	        		if (remote != null) {
	    	        			sender = Optional.of(remote);
	    	        			isChat = true;
	    	        		}
	    	        	}
	    	        }
	        	}
	        }
	        if (!sender.isPresent()) {
	        	sender = SenderFinder.getSender(component, rawMessageKey);
	        }
	        
	        if (sender.isPresent() && !sender.get().isLocal()) {
	        	if (isFiltered) {
	        		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractiveChat.plugin, () -> {
	        			execute(() -> {
	        				processPacket(reciever, packet, messageUUID, queue, false);
	        			});
					}, (int) Math.ceil((double) InteractiveChat.remoteDelay / 50));
	        		return;
	        	}
	        }
	        component = commandSender.getComponent();
	        if (chatSender != null) {
	        	component = chatSender.getComponent();
	        }
	        if (sender.isPresent()) {
	        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
	        }
	        
	        component = ComponentModernizing.modernize(component);

	        UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
			PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(true, reciever, component, preEventSenderUUID);
			Bukkit.getPluginManager().callEvent(preEvent);
			if (preEvent.getSender() != null) {
				Player newsender = Bukkit.getPlayer(preEvent.getSender());
				if (newsender != null) {
					sender = Optional.of(new ICPlayer(newsender));
				}
			}
			component = preEvent.getComponent();
			
			component = component.replaceText(TextReplacementConfig.builder().match(Registry.ID_PATTERN).replacement("").build());
			
			if (InteractiveChat.translateHoverableItems && InteractiveChat.itemGUI) {
				component = HoverableItemDisplay.process(component, reciever);
			}
			
			if (InteractiveChat.usePlayerName) {
				component = PlayernameDisplay.process(component, sender, reciever, unix);
	        }
			
			if (InteractiveChat.allowMention && sender.isPresent()) {
	        	PlayerData data = InteractiveChat.playerDataManager.getPlayerData(reciever);
	        	if (data == null || !data.isMentionDisabled()) {
	        		component = MentionDisplay.process(component, reciever, sender.get(), unix, true);
	        	}
	        }				    

	        if (InteractiveChat.useItem) {
	        	component = ItemDisplay.process(component, sender, reciever, unix);
	        }

	        if (InteractiveChat.useInventory) {
	        	component = InventoryDisplay.process(component, sender, reciever, unix);
	        }
	        
	        if (InteractiveChat.useEnder) {
	        	component = EnderchestDisplay.process(component, sender, reciever, unix);
	        }

	        component = CustomPlaceholderDisplay.process(component, sender, reciever, InteractiveChat.placeholderList, unix);

	        if (InteractiveChat.clickableCommands) {
	        	component = CommandsDisplay.process(component);
	        }

	        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
		        if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
		        	component = ComponentFont.parseMiniMessageFont(component);
		        }
	        }
	        
	        if (PlayerUtils.getColorSettings(reciever).equals(ColorSettings.OFF)) {
				component = ComponentStyling.stripColor(component);
			}
	        
	        String json = PlayerUtils.isRGBLegacy(reciever) ? Registry.ADVENTURE_GSON_SERIALIZER_LEGACY.serialize(component) : Registry.ADVENTURE_GSON_SERIALIZER.serialize(component);
	        boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > 32767;

	        //Bukkit.getConsoleSender().sendMessage(json.replace(ChatColor.COLOR_CHAR, '$'));
	        
	        try {
	        	packet.getModifier().write(field, type.convertTo(component));
	        } catch (Throwable e) {
	        	for (int i = 0; i < chatFieldsSize; i++) {
	        		packet.getModifier().write(i, null);
	        	}
	        	packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
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
	        orderAndSend(reciever, packet, messageUUID, queue);
    	} catch (Exception e) {
    		e.printStackTrace();
    		try {
				orderAndSend(reciever, originalPacket, messageUUID, queue);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
    	}
	}

}
