package com.loohp.interactivechat.proxy.velocity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.LogFilter;
import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.proxy.velocity.metrics.Charts;
import com.loohp.interactivechat.proxy.velocity.metrics.Metrics;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.CompressionUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.loohp.interactivechat.utils.MessageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.network.Connections;
import com.velocitypowered.proxy.protocol.packet.Chat;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;

public class InteractiveChatVelocity {
	
	public static final int BSTATS_PLUGIN_ID = 10945;
	
	public static InteractiveChatVelocity plugin = null;
	protected static Random random = new Random();
	public static AtomicLong pluginMessagesCounter = new AtomicLong(0);
	private static volatile boolean filtersAdded = false;
	
	private static Map<Integer, byte[]> incomming = new HashMap<>();
	
	protected static Map<UUID, List<String>> forwardedMessages = new ConcurrentHashMap<>(); 
	protected static Map<UUID, UUID> requestedMessages = new ConcurrentHashMap<>(); 
	
	private static Map<UUID, List<UUID>> requestedMessageProcesses = new ConcurrentHashMap<>();
	private static Map<UUID, Byte> messagePositions = new ConcurrentHashMap<>();
	private static Map<Integer, Boolean> permissionChecks = new ConcurrentHashMap<>();
	
	public static List<String> parseCommands = new ArrayList<>();
	
	public static Map<String, Map<String, String>> aliasesMapping = new HashMap<>();
	public static Map<String, List<ICPlaceholder>> placeholderList = new HashMap<>();
	
	public static boolean useAccurateSenderFinder = true;
	
	public static int delay = 200;
	protected static Map<String, BackendInteractiveChatData> serverInteractiveChatInfo = new ConcurrentHashMap<>();
	
	public static File configFile = null;
	public static VelocityFileConfiguration config = null;
	
	private ProxyServer server;
	private VelocityPluginDescription description;
    private Logger logger;
    private File dataFolder;
    private Metrics.Factory metricsFactory;

    @Inject
    public InteractiveChatVelocity(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataFolder = dataDirectory.toFile();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    	plugin = this;
    	
    	try {
			JSONObject json = (JSONObject) new JSONParser().parse(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("velocity-plugin.json"), StandardCharsets.UTF_8));
			description = new VelocityPluginDescription(json);
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}
        
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
		}
        configFile = new File(getDataFolder(), "velocityconfig.yml");

        if (!configFile.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config_proxy.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
			config = new VelocityFileConfiguration(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        CommandsVelocity.createBrigadierCommand();
        
        server.getChannelRegistrar().register(ICChannelIdentifier.INSTANCE);
        
        getLogger().info(TextColor.GREEN + "[InteractiveChat] Registered Plugin Messaging Channels!");
        
        Metrics metrics = metricsFactory.make(this, BSTATS_PLUGIN_ID);
        Charts.setup(metrics);
        
        getLogger().info(TextColor.GREEN + "[InteractiveChat] InteractiveChatVelocity has been enabled!");
        
        run();
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
    	getLogger().info(TextColor.RED + "[InteractiveChat] InteractiveChatBungee has been disabled!");
    }
    
    public File getDataFolder() {
    	return dataFolder;
    }
    
    public VelocityPluginDescription getDescription() {
    	return description;
    }
    
    public Logger getLogger() {
    	return logger;
    }
    
    public ProxyServer getServer() {
    	return server;
    }
    
	@SuppressWarnings("unchecked")
	public void loadConfig() {
    	try {
			config.reloadConfig(configFile);
			parseCommands = (ArrayList<String>) config.get("Settings.CommandsToParse", ArrayList.class);
			useAccurateSenderFinder = config.get("Settings.UseAccurateSenderParser", boolean.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
    
    public static Map<String, BackendInteractiveChatData> getBackendInteractiveChatInfo() {
		return Collections.unmodifiableMap(serverInteractiveChatInfo);
	}
    
    private void addFilters() {
    	try {
    		org.apache.logging.log4j.Logger logger = LogManager.getRootLogger();
	    	LogFilter filter = new LogFilter();
	    	Method method = logger.getClass().getMethod("addFilter", Filter.class);
	    	method.invoke(logger, filter);
	    } catch (Exception e) {
	    	getLogger().info(TextColor.YELLOW + "[InteractiveChat] Unable to add filter to logger, safely skipping...");
	    }
    }
	
	public static CompletableFuture<Boolean> hasPermission(CommandSource sender, String permission) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		if (!(sender instanceof Player)) {
			future.complete(sender.hasPermission(permission));
			return future;
		}
		
		Player player = (Player) sender;
		if (player.hasPermission(permission)) {
			future.complete(true);
		} else {
			if (!player.getCurrentServer().isPresent()) {
				future.complete(false);
			} else {
				new Thread(new Runnable() {
        			@Override
        			public void run() {
        				try {
        					int id = random.nextInt();
							PluginMessageSendingVelocity.checkPermission(player, permission, id);
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

	private void run() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					PluginMessageSendingVelocity.sendPlayerListData();
					PluginMessageSendingVelocity.sendDelayAndScheme();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0, 5000);
	}

	@Subscribe
	public void onReceive(PluginMessageEvent event) {
		if (!event.getIdentifier().getId().equals("interchat:main")) {
			return;
		}
		
		ChannelMessageSource source = event.getSource();
		
		if (!(source instanceof ServerConnection)) {
			return;
		}
		
		event.setResult(ForwardResult.handled());

		RegisteredServer server = ((ServerConnection) source).getServer();
		String senderServer = server.getServerInfo().getName();
		
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
		        	UUID playerUUID = requestedMessages.get(messageId);
		        	List<UUID> messageQueue = requestedMessageProcesses.get(playerUUID);
		        	
		        	//ProxyServer.getInstance().getConsole().sendMessage(new TextComponent(messageId.toString() + " <- " + component));
		        	
		        	if (playerUUID != null && messageQueue != null) {
		        		new Thread(new Runnable() {
		        			@Override
		        			public void run() {
				        		CompletableFuture<Void> future = new CompletableFuture<Void>();
				        		new Thread(new Runnable() {
				        			@Override
				        			public void run() {
				        				while (true) {
				        					if (messageQueue.indexOf(messageId) == 0) {
				        						future.complete(null);
				        						break;
				        					}
				        					if (future.isDone()) {
				        						break;
				        					}
				        					try {
												TimeUnit.MILLISECONDS.sleep(10);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
				        				}
				        			}
				        		}).start();
				        		
				        		try {
									future.get(delay + 2000, TimeUnit.MILLISECONDS);
								} catch (InterruptedException | ExecutionException | TimeoutException e) {}
				        		if (!future.isDone()) {
				        			future.complete(null);
	        					}			     
				        		
				        		Byte position = messagePositions.remove(messageId);
				        		Chat chatPacket = new Chat(component + "<QUxSRUFEWVBST0NFU1NFRA==>", position == null ? 0 : position, null);
				        		Optional<Player> optplayer = getServer().getPlayer(playerUUID);
				        		if (optplayer.isPresent()) {
				        			ConnectedPlayer userConnection = (ConnectedPlayer) optplayer.get();				    
					        		userConnection.getConnection().getChannel().write(chatPacket);
					        		messageQueue.remove(messageId);
				        		}				    
		        			}
		        		}).start();
		        	}
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
		        	aliasesMapping.put(server.getServerInfo().getName(), map);
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
		        	placeholderList.put(server.getServerInfo().getName(), list);
		        	PluginMessageSendingVelocity.forwardPlaceholderList(list, server);
		        	break;
		        case 0x0D:
		        	UUID uuid2 = DataTypeIO.readUUID(input);
		        	PluginMessageSendingVelocity.reloadPlayerData(uuid2, server);
		        	break;
	        	}
	        } catch (IOException | DataFormatException e) {
				e.printStackTrace();
			}
		} else {
			for (RegisteredServer eachServer : getServer().getAllServers()) {
				if (!eachServer.getServerInfo().getName().equals(senderServer) && eachServer.getPlayersConnected().size() > 0) {
					eachServer.sendPluginMessage(ICChannelIdentifier.INSTANCE, event.getData());
					pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	@Subscribe
	public void onBungeeChat(PlayerChatEvent event) {
		if (!event.getResult().isAllowed()) {
			return;
		}
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		String message = event.getMessage();
		
		if (!player.getCurrentServer().isPresent()) {
			return;
		}
		
		Map<String, String> serverAliasesMapping = aliasesMapping.get(player.getCurrentServer().get().getServerInfo().getName());
		List<ICPlaceholder> serverPlaceholderList = placeholderList.get(player.getCurrentServer().get().getServerInfo().getName());
		if (serverAliasesMapping != null && serverPlaceholderList != null) {
			if (message.startsWith("/")) {
				if (InteractiveChatVelocity.parseCommands.stream().anyMatch(each -> event.getMessage().matches(each))) {
					message = MessageUtils.preprocessMessage(message, serverPlaceholderList, serverAliasesMapping);
				}
			} else {
				message = MessageUtils.preprocessMessage(message, serverPlaceholderList, serverAliasesMapping);
			}
			event.setResult(ChatResult.message(message));
		}
		
		String newMessage = event.getMessage();
		
		if (newMessage.startsWith("/")) {
			for (String parsecommand : InteractiveChatVelocity.parseCommands) {
				//getProxy().getConsole().sendMessage(new TextComponent(parsecommand));
				if (newMessage.matches(parsecommand)) {
					String command = newMessage.trim();
					String uuidmatch = "<cmd=" + UUID.randomUUID().toString() + ">";
					command += " " + uuidmatch;
					event.setResult(ChatResult.message(command));
					try {
						PluginMessageSendingVelocity.sendCommandMatch(uuid, "", uuidmatch);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		} else {
			if (InteractiveChatVelocity.useAccurateSenderFinder) {
				String uuidmatch = "<chat=" + UUID.randomUUID().toString() + ">";
				message += " " + uuidmatch;
				event.setResult(ChatResult.message(message));
				try {
					PluginMessageSendingVelocity.sendSenderMatch(uuid, uuidmatch);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					List<String> messages = forwardedMessages.get(uuid);
					if (messages != null && !messages.remove(newMessage)) {
						try {
							PluginMessageSendingVelocity.sendMessagePair(uuid, newMessage);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}, 100);
		}
	}
	
	@Subscribe
	public void onServerConnected(ServerPostConnectEvent event) {
		Player player = event.getPlayer();
		
		VelocityServerConnection serverConnection = ((ConnectedPlayer) event.getPlayer()).getConnectedServer();
		ChannelPipeline pipeline = serverConnection.ensureConnected().getChannel().pipeline();

		pipeline.addBefore(Connections.HANDLER, "packet_interceptor", new ChannelDuplexHandler() {
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
				try {
					if (obj instanceof Chat) {
						Chat packet = (Chat) obj;
						UUID uuid = player.getUniqueId();
						String message = packet.getMessage();
						byte position = packet.getType();
						if ((position == 0 || position == 1) && uuid != null && message != null) {
							List<String> list = forwardedMessages.get(uuid);
							if (list != null) {
								list.add(message);
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

	@Subscribe
	public void onPlayerConnected(PostLoginEvent event) {
		if (!filtersAdded) {
			addFilters();
		}
		
		Player player = event.getPlayer();

		forwardedMessages.put(player.getUniqueId(), new ArrayList<>());
		List<UUID> messageQueue = Collections.synchronizedList(new LinkedList<>());
		requestedMessageProcesses.put(player.getUniqueId(), messageQueue);
		
		if (player.hasPermission("interactivechat.backendinfo")) {
			String proxyVersion = getDescription().getVersion();
			for (BackendInteractiveChatData data  : serverInteractiveChatInfo.values()) {
				if (data.isOnline() && data.getProtocolVersion() != Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION) {
					String msg = TextColor.RED + "[InteractiveChat] Warning: Backend Server " + data.getServer() + " is not running a version of InteractiveChat which has the same plugin messaging protocol version as the proxy!";
					HoverEvent<Component> hoverComponent = Component.text(TextColor.YELLOW + "Proxy Version: " + proxyVersion + " (" + Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION + ")\n" + TextColor.RED + data.getServer() + " Version: " + data.getVersion() + " (" + data.getProtocolVersion() + ")").asHoverEvent();
					TextComponent text = Component.text(msg).hoverEvent(hoverComponent);
					player.sendMessage(text);
					getServer().getConsoleCommandSource().sendMessage(text);
				}
			}
		}
		
		ConnectedPlayer userConnection = (ConnectedPlayer) player;
		ChannelPipeline pipeline = userConnection.getConnection().getChannel().pipeline();

		pipeline.addBefore(Connections.HANDLER, "packet_interceptor", new ChannelDuplexHandler() {
			private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
				try {
					if (obj instanceof Chat) {
						Chat packet = (Chat) obj;
						String message = packet.getMessage();
						byte position = packet.getType();
						if ((position == 0 || position == 1) && message != null) {
							if (message.contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
								packet.setMessage(message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", ""));
								if (message.matches(".*<cmd=" + UUID_REGEX + ">.*") || message.matches(".*<chat=" + UUID_REGEX + ">.*")) {
									packet.setMessage(message.replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", "").trim());
								}
							} else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
								RegisteredServer server = player.getCurrentServer().get().getServer();
								UUID messageId = UUID.randomUUID();
								messageQueue.add(messageId);
								messagePositions.put(messageId, position);
								new Timer().schedule(new TimerTask() {
									@Override
									public void run() {
										try {
											if (player != null && server != null) {
												PluginMessageSendingVelocity.requestMessageProcess(player, server, message, messageId);
											}
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}, delay + 50);
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

	@Subscribe
	public void onSwitch(ServerConnectedEvent event) {
		RegisteredServer to = event.getServer();
		if (!placeholderList.containsKey(to.getServerInfo().getName())) {
			try {
				PluginMessageSendingVelocity.requestPlaceholderList(to);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!aliasesMapping.containsKey(to.getServerInfo().getName())) {
			try {
				PluginMessageSendingVelocity.requestAliasesMapping(to);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PluginMessageSendingVelocity.sendPlayerListData();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PluginMessageSendingVelocity.sendDelayAndScheme();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (event.getPlayer().getUsername().equals("LOOHP") || event.getPlayer().getUsername().equals("AppLEskakE")) {
					event.getPlayer().sendMessage(Component.text(TextColor.GOLD + "InteractiveChat (Velocity) " + getDescription().getVersion() + " is running!"));
				}
			}
		}, 100);
	}

	@Subscribe
	public void onLeave(DisconnectEvent event) {
		forwardedMessages.remove(event.getPlayer().getUniqueId());
		requestedMessageProcesses.remove(event.getPlayer().getUniqueId());
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					PluginMessageSendingVelocity.sendPlayerListData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}
	
	private boolean hasInteractiveChat(RegisteredServer server) {
		if (server == null || server.getServerInfo() == null) {
			return false;
		}
		BackendInteractiveChatData data = serverInteractiveChatInfo.get(server.getServerInfo().getName());
		if (data == null) {
			return false;
		}
		return data.hasInteractiveChat();
	}

}
