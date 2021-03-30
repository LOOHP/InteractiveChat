package com.loohp.interactivechat.Listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.translate.UnicodeUnescaper;
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
import com.loohp.interactivechat.Modules.ProcessAccurateSender;
import com.loohp.interactivechat.Modules.ProcessCommands;
import com.loohp.interactivechat.Modules.SenderFinder;
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.ObjectHolders.OutboundPacket;
import com.loohp.interactivechat.ObjectHolders.ProcessSenderResult;
import com.loohp.interactivechat.Utils.ChatColorUtils;
import com.loohp.interactivechat.Utils.ChatComponentUtils;
import com.loohp.interactivechat.Utils.CustomStringUtils;
import com.loohp.interactivechat.Utils.JsonUtils;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.PlayerUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class OutChatPacket implements Listener {
	
	private static Map<UUID, Queue<UUID>> messagesOrder = new ConcurrentHashMap<>();
	private static Queue<OutboundPacket> sendingQueue = new ConcurrentLinkedQueue<>();
	private static AtomicBoolean lock = new AtomicBoolean(false);
	private static int chatFieldsSize;
	private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	
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
	}
	
	public static void chatMessageListener() {	
		run();
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
		        	q = new ConcurrentLinkedQueue<>();
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
	}
	
	private static void processPacket(Player reciever, PacketContainer packet, UUID messageUUID, Queue<UUID> queue, boolean isFiltered) {
		long timeout = System.currentTimeMillis() + (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 1000;
		while (lock.get() && System.currentTimeMillis() < timeout) {
			try {TimeUnit.NANOSECONDS.sleep(1000);} catch (InterruptedException e) {}
		}
		lock.set(true);
		PacketContainer originalPacket = packet.deepClone();
    	try {
    		BaseComponent[] basecomponentarray = null;
    		ChatComponentType type = null;
	        int field = -1;
	        
	        search: for (ChatComponentType t : ChatComponentType.byPriority()) {
	        	for (int i = 0; i < packet.getModifier().size(); i++) {
	        		if (packet.getModifier().read(i) != null && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
	        			basecomponentarray = t.convertFrom(packet.getModifier().read(i));
	        			field = i;
	        			type = t;
	        			break search;
	        		}
	        	}
	        }
	        if (field < 0 || type == null || basecomponentarray == null) {
	        	lock.set(false);
	        	orderAndSend(reciever, packet, messageUUID, queue);
	        	return;
	        }

	        BaseComponent basecomponent;
	        try {
	        	basecomponent = ChatComponentUtils.join(ComponentSerializer.parse(ChatColorUtils.filterIllegalColorCodes(new UnicodeUnescaper().translate(ComponentSerializer.toString(basecomponentarray)))));
	        } catch (Exception e) {
	        	e.printStackTrace();
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

	        ProcessSenderResult commandSender = ProcessCommands.process(basecomponent);
	        Optional<ICPlayer> sender = Optional.empty();
	        if (commandSender.getSender() != null) {
	        	Player bukkitplayer = Bukkit.getPlayer(commandSender.getSender());
	        	if (bukkitplayer != null) {
	        		sender = Optional.of(new ICPlayer(bukkitplayer));
	        	} else {
	        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(commandSender.getSender()));
	        	}
	        }
	        ProcessSenderResult chatSender = null;
	        if (!sender.isPresent()) {
	        	if (InteractiveChat.useAccurateSenderFinder) {
	        		chatSender = ProcessAccurateSender.process(basecomponent);
	        		if (chatSender.getSender() != null) {
	    	        	Player bukkitplayer = Bukkit.getPlayer(chatSender.getSender());
	    	        	if (bukkitplayer != null) {
	    	        		sender = Optional.of(new ICPlayer(bukkitplayer));
	    	        	} else {
	    	        		sender = Optional.ofNullable(InteractiveChat.remotePlayers.get(chatSender.getSender()));
	    	        	}
	    	        }
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
	        basecomponent = commandSender.getBaseComponent();
	        if (chatSender != null) {
	        	basecomponent = chatSender.getBaseComponent();
	        }
	        if (sender.isPresent()) {
	        	InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
	        }

	        UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
			PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(true, reciever, basecomponent, preEventSenderUUID);
			Bukkit.getPluginManager().callEvent(preEvent);
			if (preEvent.getSender() != null) {
				Player newsender = Bukkit.getPlayer(preEvent.getSender());
				if (newsender != null) {
					sender = Optional.of(new ICPlayer(newsender));
				}
			}
			basecomponent = preEvent.getBaseComponent();
			
			List<BaseComponent> basecomponentlist = CustomStringUtils.loadExtras(basecomponent);
			TextComponent product = new TextComponent("");
			for (int i = 0; i < basecomponentlist.size(); i++) {
				BaseComponent each = basecomponentlist.get(i);
				if (each instanceof TextComponent) {
					((TextComponent) each).setText(((TextComponent) each).getText().replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", ""));
				}
				product.addExtra(each);
			}
			basecomponent = product;
			
			if (InteractiveChat.allowMention && sender.isPresent()) {
	        	PlayerData data = InteractiveChat.playerDataManager.getPlayerData(reciever);
	        	if (data == null || !data.isMentionDisabled()) {
	        		basecomponent = MentionDisplay.process(basecomponent, reciever, sender.get(), unix, true);
	        	}
	        }
			
	        if (InteractiveChat.usePlayerName) {
	        	basecomponent = PlayernameDisplay.process(basecomponent, sender, reciever, unix);
	        }

	        if (InteractiveChat.useItem) {
	        	basecomponent = ItemDisplay.process(basecomponent, sender, reciever, unix);
	        }

	        if (InteractiveChat.useInventory) {
	        	basecomponent = InventoryDisplay.process(basecomponent, sender, reciever, unix);
	        }

	        if (InteractiveChat.useEnder) {
	        	basecomponent = EnderchestDisplay.process(basecomponent, sender, reciever, unix);
	        }

	        basecomponent = CustomPlaceholderDisplay.process(basecomponent, sender, reciever, InteractiveChat.placeholderList, unix);

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
	        boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > 32767;

	        //Bukkit.getConsoleSender().sendMessage(json);
	        try {
	        	packet.getModifier().write(field, type.convertTo(basecomponent));
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
	
	public static enum ChatComponentType {
		IChatBaseComponent(".*net\\.minecraft\\.server\\..*\\.IChatBaseComponent.*", object -> {
			return ComponentSerializer.parse(WrappedChatComponent.fromHandle(object).getJson());
		}, component -> {
			return WrappedChatComponent.fromJson(ComponentSerializer.toString(component)).getHandle();
		}),
		BaseComponentArray(".*\\[Lnet\\.md_5\\.bungee\\.api\\.chat\\.BaseComponent.*", object -> {
			return (BaseComponent[]) object;
		}, component -> {
			return component;
		}),
		AdventureComponent(".*net\\.kyori\\.adventure\\.text\\.Component.*", object -> {
			return ComponentSerializer.parse(GsonComponentSerializer.gson().serialize((Component) object));
		}, component -> {
			return GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(component));
		});
		
		private static final ChatComponentType[] BY_PRIORITY = new ChatComponentType[] {AdventureComponent, BaseComponentArray, IChatBaseComponent};
		private String regex;
		private Function<Object, BaseComponent[]> converterFrom;
		private Function<BaseComponent[], Object> converterTo;
		
		ChatComponentType(String regex, Function<Object, BaseComponent[]> converterFrom, Function<BaseComponent[], Object> converterTo) {
			this.regex = regex;
			this.converterFrom = converterFrom;
			this.converterTo = converterTo;
		}
		
		public String getMatchingRegex() {
			return regex;
		}
		
		public BaseComponent[] convertFrom(Object object) {
			return converterFrom.apply(object);
		}
		
		public Object convertTo(BaseComponent... component) {
			return converterTo.apply(component);
		}
		
		public static ChatComponentType[] byPriority() {
			return Arrays.copyOf(BY_PRIORITY, BY_PRIORITY.length);
		}
	}

}
