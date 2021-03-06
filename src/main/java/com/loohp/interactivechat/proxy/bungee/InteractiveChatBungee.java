package com.loohp.interactivechat.proxy.bungee;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactivechat.config.Config;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.proxy.bungee.metrics.Charts;
import com.loohp.interactivechat.proxy.bungee.metrics.Metrics;
import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.proxy.objectholders.MessageForwardingHandler;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.CompressionUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MessageUtils;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.packet.Chat;
import us.myles.ViaVersion.api.Via;

public class InteractiveChatBungee extends Plugin implements Listener {
	
	public static final int BSTATS_PLUGIN_ID = 8839;
	public static final String CONFIG_ID = "config";
	
	public static boolean viaVersionHook = false;

	public static InteractiveChatBungee plugin;
	public static Metrics metrics;
	protected static Random random = new Random();
	public static AtomicLong pluginMessagesCounter = new AtomicLong(0);
	private static volatile boolean filtersAdded = false;
	
	private static Map<Integer, byte[]> incomming = new HashMap<>();
	
	protected static Map<UUID, Map<String, Long>> forwardedMessages = new ConcurrentHashMap<>();

	private static Map<Integer, Boolean> permissionChecks = new ConcurrentHashMap<>();
	
	public static List<String> parseCommands = new ArrayList<>();
	
	public static Map<String, Map<String, String>> aliasesMapping = new HashMap<>();
	public static Map<String, List<ICPlaceholder>> placeholderList = new HashMap<>();
	
	public static boolean useAccurateSenderFinder = true;
	
	public static int delay = 200;
	protected static Map<String, BackendInteractiveChatData> serverInteractiveChatInfo = new ConcurrentHashMap<>();
	
	private static MessageForwardingHandler messageForwardingHandler;

	@Override
	public void onEnable() {
		plugin = this;
        
		if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
		}
        Config.loadConfig(CONFIG_ID, new File(getDataFolder(), "bungeeconfig.yml"), getResourceAsStream("config_proxy.yml"), getResourceAsStream("config_proxy.yml"), true);
        loadConfig();

		getProxy().registerChannel("interchat:main");
		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new CommandsBungee());
		
		if (ProxyServer.getInstance().getPluginManager().getPlugin("ViaVersion") != null) {
			viaVersionHook = true;
			ProxyServer.getInstance().getLogger().info(ChatColor.AQUA + "[InteractiveChat] InteractiveChatBungee has hooked into ViaVersion!");
		}

		ProxyServer.getInstance().getLogger().info(ChatColor.GREEN + "[InteractiveChat] Registered Plugin Messaging Channels!");

		metrics = new Metrics(plugin, BSTATS_PLUGIN_ID);
		Charts.setup(metrics);

		run();
		
		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("InteractiveChatProxy ChatMessage Processing Thread #%d").build();
		ExecutorService threadPool = Executors.newCachedThreadPool(factory);
		messageForwardingHandler = new MessageForwardingHandler(threadPool, (info, component) -> {
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(info.getPlayer());
			Server server = player.getServer();
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if (player != null && server != null) {
							PluginMessageSendingBungee.requestMessageProcess(player, server.getInfo(), component, info.getId());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, delay + 50);
		}, (info, component) -> {
			Chat chatPacket = new Chat(component + "<QUxSRUFEWVBST0NFU1NFRA==>", info.getPosition());
    		UserConnection userConnection = (UserConnection) ProxyServer.getInstance().getPlayer(info.getPlayer());
    		ChannelWrapper channelWrapper;
    		Field channelField = null;
    		if (userConnection == null) {
    			return;
    		}
    		try {
    			channelField = userConnection.getClass().getDeclaredField("ch");
    			channelField.setAccessible(true);
    			channelWrapper = (ChannelWrapper) channelField.get(userConnection);
    		} catch (NoSuchFieldException | IllegalAccessException e) {
    			throw new RuntimeException(e);
    		} finally {
    			if (channelField != null) {
    				channelField.setAccessible(false);
    			}
    		}    		
    		channelWrapper.write(chatPacket);
		}, uuid -> {
			return ProxyServer.getInstance().getPlayer(uuid) != null;
		}, uuid -> {
			return hasInteractiveChat(ProxyServer.getInstance().getPlayer(uuid).getServer());
		}, () -> (long) delay + 2000);

		ProxyServer.getInstance().getLogger().info(ChatColor.GREEN + "[InteractiveChat] InteractiveChatBungee has been enabled!");
    	
    	addFilters();
	}

	@Override
	public void onDisable() {
		try {
			messageForwardingHandler.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ProxyServer.getInstance().getLogger().info(ChatColor.RED + "[InteractiveChat] InteractiveChatBungee has been disabled!");
	}
	
	private void addFilters() {
		filtersAdded = true;
		Map<String, Logger> loggers = new LinkedHashMap<>();
		loggers.put("Main", ProxyServer.getInstance().getLogger());
		ProxyServer.getInstance().getPluginManager().getPlugins().stream().forEach(p -> loggers.put(p.getClass().getSimpleName(), p.getLogger()));
		for (Entry<String, Logger> entry : loggers.entrySet()) {
			try {
				Logger logger = entry.getValue();
	    		logger.setFilter(new Filter() {	    		    		
	    			@Override
	    			public boolean isLoggable(LogRecord record) {
	    				String message = record.getMessage();
	    				if (!Registry.ID_PATTERN.matcher(message).find()) {
	    					record.setMessage(message.replaceAll(Registry.ID_PATTERN.pattern(), ""));
	    				}
	    				return true;
	    			}
	        	});
		    } catch (Exception e) {
		    	e.printStackTrace();
		    	ProxyServer.getInstance().getLogger().info(ChatColor.YELLOW + "[InteractiveChat] Unable to add filter to the " + entry.getKey() + " logger, safely skipping...");
		    }
		}
	}
	
	public static Map<String, BackendInteractiveChatData> getBackendInteractiveChatInfo() {
		return Collections.unmodifiableMap(serverInteractiveChatInfo);
	}
	
	public static CompletableFuture<Boolean> hasPermission(CommandSender sender, String permission) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		if (!(sender instanceof ProxiedPlayer)) {
			future.complete(sender.hasPermission(permission));
			return future;
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (player.hasPermission(permission)) {
			future.complete(true);
		} else {
			if (player.getServer() == null) {
				future.complete(false);
			} else {
				new Thread(new Runnable() {
        			@Override
        			public void run() {
        				try {
        					int id = random.nextInt();
							PluginMessageSendingBungee.checkPermission(player, permission, id);
							long start = System.currentTimeMillis() + delay + 500;
							while (System.currentTimeMillis() < start) {
								Boolean value = permissionChecks.remove(id);
								if (value != null) {
									future.complete(value);
									return;
								} else {
									TimeUnit.NANOSECONDS.sleep(500000);
								}
							}
							future.complete(false);
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
        			}
        		}).start();
			}
		}
		return future;
	}
	
	public static void loadConfig() {
		Config config = Config.getConfig(CONFIG_ID);
		config.reload();
		
		parseCommands = config.getConfiguration().getStringList("Settings.CommandsToParse");
		useAccurateSenderFinder = config.getConfiguration().getBoolean("Settings.UseAccurateSenderParser");
	}

	private void run() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					PluginMessageSendingBungee.sendPlayerListData();
					PluginMessageSendingBungee.sendDelayAndScheme();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				long now = System.currentTimeMillis();
				for (Map<String, Long> list : forwardedMessages.values()) {
					Iterator<Long> itr = list.values().iterator();
					while (itr.hasNext()) {
						long time = itr.next();
						if (time - 5000 > now) {
							itr.remove();
						}
					}
				}
			}
		}, 0, 5000);
	}

	@EventHandler
	public void onReceive(PluginMessageEvent event) {
		if (!event.getTag().equals("interchat:main")) {
			return;
		}
		
		event.setCancelled(true);

		SocketAddress senderServer = event.getSender().getSocketAddress();
		
		byte[] packet = Arrays.copyOf(event.getData(), event.getData().length);
		ByteArrayDataInput in = ByteStreams.newDataInput(packet);
		int packetNumber = in.readInt();
		int packetId = in.readShort();
		
		if (packetId >= 0x08) {
			boolean isEnding = in.readBoolean();
	        byte[] data = new byte[packet.length - 7];
	        in.readFully(data);
	        
	        byte[] chain = incomming.remove(packetNumber);
	    	if (chain != null) {
	    		ByteBuffer buff = ByteBuffer.allocate(chain.length + data.length);
	    		buff.put(chain);
	    		buff.put(data);
	    		data = buff.array();
	    	}
	        
	        if (!isEnding) {
	        	incomming.put(packetNumber, data);
	        	return;
	        }
	        
	        try {
	        	ByteArrayDataInput input = ByteStreams.newDataInput(CompressionUtils.decompress(data));	        	
		        switch (packetId) {
		        case 0x08:
		        	UUID messageId = DataTypeIO.readUUID(input);
		        	String component = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        	messageForwardingHandler.recievedProcessedMessage(messageId, component);
		        	break;
		        case 0x09:
		        	loadConfig();
		        	break;
		        case 0x0A:
		        	int size = input.readInt();
		        	Map<String, String> map = new HashMap<>();
		        	for (int i = 0; i < size; i++) {
		        		String key = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        		String value = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        		map.put(key, value);
		        	}
		        	aliasesMapping.put(((Server) event.getSender()).getInfo().getName(), map);
		        	break;
		        case 0x0B:
		        	int id = input.readInt();
		        	boolean permissionValue = input.readBoolean();
		        	permissionChecks.put(id, permissionValue);
		        	break;
		        case 0x0C:
		        	int size1 = input.readInt();
		        	List<ICPlaceholder> list = new ArrayList<>(size1);
		        	for (int i = 0; i < size1; i++) {
		        		boolean isBulitIn = input.readBoolean();
		        		if (isBulitIn) {
		        			String keyword = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			boolean casesensitive = input.readBoolean();
		        			String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			list.add(new ICPlaceholder(keyword, casesensitive, description, permission));
		        		} else {
		        			int customNo = input.readInt();
		        			ParsePlayer parseplayer = ParsePlayer.fromOrder(input.readByte());	
		        			String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			List<String> aliases = new ArrayList<>();
		        			int aliasSize = input.readInt();
		        			for (int u = 0; u < aliasSize; u++) {
		        				aliases.add(DataTypeIO.readString(input, StandardCharsets.UTF_8));
		        			}
		        			boolean parseKeyword = input.readBoolean();
		        			boolean casesensitive = input.readBoolean();
		        			long cooldown = input.readLong();
		        			boolean hoverEnabled = input.readBoolean();
		        			String hoverText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			boolean clickEnabled = input.readBoolean();
		        			String clickAction = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			String clickValue = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			boolean replaceEnabled = input.readBoolean();
		        			String replaceText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        			String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);

		        			list.add(new CustomPlaceholder(customNo, parseplayer, placeholder, aliases, parseKeyword, casesensitive, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), description));
		        		}
		        	}
		        	placeholderList.put(((Server) event.getSender()).getInfo().getName(), list);
		        	PluginMessageSendingBungee.forwardPlaceholderList(list, ((Server) event.getSender()).getInfo());
		        	break;
		        case 0x0D:
		        	UUID uuid2 = DataTypeIO.readUUID(input);
		        	PluginMessageSendingBungee.reloadPlayerData(uuid2, ((Server) event.getSender()).getInfo());
		        	break;
	        	}
	        } catch (IOException | DataFormatException e) {
				e.printStackTrace();
			}
		} else {
			for (ServerInfo server : getProxy().getServers().values()) {
				if (!server.getSocketAddress().equals(senderServer) && server.getPlayers().size() > 0) {
					server.sendData("interchat:main", event.getData());
					pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	@EventHandler
	public void onBungeeChat(ChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		UUID uuid = player.getUniqueId();
		String message = event.getMessage();
		
		Map<String, String> serverAliasesMapping = aliasesMapping.get(player.getServer().getInfo().getName());
		List<ICPlaceholder> serverPlaceholderList = placeholderList.get(player.getServer().getInfo().getName());
		if (serverAliasesMapping != null && serverPlaceholderList != null) {
			if (message.startsWith("/")) {
				if (InteractiveChatBungee.parseCommands.stream().anyMatch(each -> event.getMessage().matches(each))) {
					message = MessageUtils.preprocessMessage(message, serverPlaceholderList, serverAliasesMapping);
				}
			} else {
				message = MessageUtils.preprocessMessage(message, serverPlaceholderList, serverAliasesMapping);
			}
			event.setMessage(message);
		}
		
		String newMessage = event.getMessage();
		boolean hasInteractiveChat = false;
		Server server = player.getServer();
		if (server != null) {
			BackendInteractiveChatData data = serverInteractiveChatInfo.get(server.getInfo().getName());
			if (data != null) {
				hasInteractiveChat = data.hasInteractiveChat();
			}
		}
		
		if (newMessage.startsWith("/")) {
			if (hasInteractiveChat) {
				for (String parsecommand : InteractiveChatBungee.parseCommands) {
					//getProxy().getConsole().sendMessage(new TextComponent(parsecommand));
					if (newMessage.matches(parsecommand)) {
						String command = newMessage.trim();
						String uuidmatch = "<cmd=" + uuid.toString() + ">";
						command += " " + uuidmatch;
						event.setMessage(command);
						break;
					}
				}
			}
		} else {
			if (InteractiveChatBungee.useAccurateSenderFinder && hasInteractiveChat) {
				String uuidmatch = "<chat=" + uuid.toString() + ">";
				message += " " + uuidmatch;
				event.setMessage(message);
			}

			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					Map<String, Long> messages = forwardedMessages.get(uuid);
					if (messages != null && messages.remove(newMessage) != null) {
						try {
							PluginMessageSendingBungee.sendMessagePair(uuid, newMessage);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}, 100);
		}
	}
	
	@EventHandler
	public void onServerConnected(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();
		
		ServerConnection serverConnection = (ServerConnection) event.getServer();
		ChannelWrapper channelWrapper;
		Field channelField = null;

		try {
			channelField = serverConnection.getClass().getDeclaredField("ch");
			channelField.setAccessible(true);
			channelWrapper = (ChannelWrapper) channelField.get(serverConnection);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} finally {
			if (channelField != null) {
				channelField.setAccessible(false);
			}
		}

		ChannelPipeline pipeline = channelWrapper.getHandle().pipeline();

		pipeline.addBefore(PipelineUtils.BOSS_HANDLER, "interactivechat_interceptor", new ChannelDuplexHandler() {
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
				try {
					if (obj instanceof Chat) {
						Chat packet = (Chat) obj;
						UUID uuid = player.getUniqueId();
						String message = packet.getMessage();
						byte position = packet.getPosition();
						if ((position == 0 || position == 1) && uuid != null && message != null) {
							Map<String, Long> list = forwardedMessages.get(uuid);
							if (list != null) {
								list.put(message, System.currentTimeMillis());
							}
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				super.write(channelHandlerContext, obj, channelPromise);
			}
		});
	}

	@EventHandler
	public void onPlayerConnected(PostLoginEvent event) {
		if (!filtersAdded) {
			addFilters();
		}
		
		ProxiedPlayer player = event.getPlayer();

		forwardedMessages.put(player.getUniqueId(), new ConcurrentHashMap<>());
		
		if (player.hasPermission("interactivechat.backendinfo")) {
			String proxyVersion = plugin.getDescription().getVersion();
			for (BackendInteractiveChatData data  : serverInteractiveChatInfo.values()) {
				if (data.isOnline() && data.getProtocolVersion() != Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION) {
					String msg = ChatColor.RED + "[InteractiveChat] Warning: Backend Server " + data.getServer() + " is not running a version of InteractiveChat which has the same plugin messaging protocol version as the proxy!";
					Component text = LegacyComponentSerializer.legacySection().deserialize(msg);
					text = text.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(ChatColor.YELLOW + "Proxy Version: " + proxyVersion + " (" + Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION + ")\n" + ChatColor.RED + data.getServer() + " Version: " + data.getVersion() + " (" + data.getProtocolVersion() + ")")));
					sendMessage(player, text);
					sendMessage(ProxyServer.getInstance().getConsole(), text);
				}
			}
		}
		
		UserConnection userConnection = (UserConnection) player;
		ChannelWrapper channelWrapper;
		Field channelField = null;

		try {
			channelField = userConnection.getClass().getDeclaredField("ch");
			channelField.setAccessible(true);
			channelWrapper = (ChannelWrapper) channelField.get(userConnection);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} finally {
			if (channelField != null) {
				channelField.setAccessible(false);
			}
		}

		ChannelPipeline pipeline = channelWrapper.getHandle().pipeline();

		pipeline.addBefore(PipelineUtils.BOSS_HANDLER, "interactivechat_interceptor", new ChannelDuplexHandler() {
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
				try {
					if (obj instanceof Chat) {
						Chat packet = (Chat) obj;
						String message = packet.getMessage();
						byte position = packet.getPosition();
						if ((position == 0 || position == 1) && message != null) {
							if (message.contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
								packet.setMessage(message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", ""));
								if (Registry.ID_PATTERN.matcher(message).find()) {
									packet.setMessage(message.replaceAll(Registry.ID_PATTERN.pattern(), "").trim());
								}
							} else if (hasInteractiveChat(player.getServer())) {
								messageForwardingHandler.processMessage(player.getUniqueId(), message, position);
								return;
							}
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
				super.write(channelHandlerContext, obj, channelPromise);
			}
		});
	}

	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		ServerInfo to = event.getPlayer().getServer().getInfo();
		if (!placeholderList.containsKey(to.getName())) {
			try {
				PluginMessageSendingBungee.requestPlaceholderList(to);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!aliasesMapping.containsKey(to.getName())) {
			try {
				PluginMessageSendingBungee.requestAliasesMapping(to);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PluginMessageSendingBungee.sendPlayerListData();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PluginMessageSendingBungee.sendDelayAndScheme();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
					sendMessage(event.getPlayer(), LegacyComponentSerializer.legacySection().deserialize(ChatColor.GOLD + "InteractiveChat (Bungeecord) " + plugin.getDescription().getVersion() + " is running!"));
				}
			}
		}, 100);
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		forwardedMessages.remove(event.getPlayer().getUniqueId());
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					PluginMessageSendingBungee.sendPlayerListData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}
	
	private boolean hasInteractiveChat(Server server) {
		if (server == null || server.getInfo() == null) {
			return false;
		}
		BackendInteractiveChatData data = serverInteractiveChatInfo.get(server.getInfo().getName());
		if (data == null) {
			return false;
		}
		return data.hasInteractiveChat();
	}
	
	public static void sendMessage(CommandSender sender, Component component) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if (getVersion(player) < Registry.MINECRAFT_1_16_PROTOCOL_VERSION) {
				sender.sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.legacyGson().serialize(component)));
			} else {
				sender.sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)));
			}
		} else {
			sender.sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)));
		}
	}
	
	public static int getVersion(ProxiedPlayer player) {
        if (viaVersionHook) {
            return Via.getAPI().getPlayerVersion(player.getUniqueId());
        }
        return player.getPendingConnection().getVersion();
    }
	
}
