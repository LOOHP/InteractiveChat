package com.loohp.interactivechat.Bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.Bungee.Metrics.Charts;
import com.loohp.interactivechat.Bungee.Metrics.Metrics;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.CustomArrayUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.packet.Chat;

public class InteractiveChatBungee extends Plugin implements Listener {
	
	public static net.md_5.bungee.config.Configuration configuration = null;
	public static ConfigurationProvider config = null;

	public static Plugin plugin;
	public static Metrics metrics;
	private static Random random = new Random();
	public static AtomicLong pluginMessagesCounter = new AtomicLong(0);
	
	private Map<Integer, Byte[]> incomming = new HashMap<>();
	
	private Map<UUID, List<String>> forwardedMessages = new ConcurrentHashMap<>(); 
	private Map<Integer, UUID> requestedMessages = new ConcurrentHashMap<>(); 
	
	public static List<String> parseCommands = new ArrayList<>();

	@Override
	public void onEnable() {
		plugin = this;
		
		config = ConfigurationProvider.getProvider(YamlConfiguration.class);
		if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "bungeeconfig.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("bungeeconfig.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        loadConfig();

		getProxy().registerChannel("interchat:main");
		getProxy().getPluginManager().registerListener(this, this);

		getProxy().getPluginManager().registerCommand(this, new Commands());

		getLogger().info(ChatColor.GREEN + "[InteractiveChat] Registered Plugin Messaging Channels!");

		metrics = new Metrics(plugin, 8839);
		Charts.setup(metrics);

		run();

		getLogger().info(ChatColor.GREEN + "[InteractiveChat] InteractiveChatBungee has been enabled!");
	}

	@Override
	public void onDisable() {
		getLogger().info(ChatColor.RED + "[InteractiveChat] InteractiveChatBungee has been disabled!");
	}
	
	public static void loadConfig() {
		try {
			configuration = config.load(new File(plugin.getDataFolder(), "bungeeconfig.yml"));
			parseCommands = configuration.getStringList("Settings.CommandsToParse");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void run() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					sendPlayerListData();
					sendDelay();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0, 10000);
	}

	@EventHandler
	public void onReceive(PluginMessageEvent event) {
		if (!event.getTag().equals("interchat:main")) {
			return;
		}

		SocketAddress senderServer = event.getSender().getSocketAddress();
		
		byte[] packet = Arrays.copyOf(event.getData(), event.getData().length);
		ByteArrayDataInput in = ByteStreams.newDataInput(packet);
		int packetNumber = in.readInt();
		int packetId = in.readShort();
		
		if (packetId == 0x08 || packetId == 0x09) {
			boolean isEnding = in.readBoolean();
	        byte[] data = new byte[packet.length - 7];
	        in.readFully(data);
	        
	        Byte[] chain = incomming.remove(packetNumber);
	    	if (chain != null) {
	    		ByteBuffer buff = ByteBuffer.allocate(chain.length + data.length);
	    		buff.put(ArrayUtils.toPrimitive(chain));
	    		buff.put(data);
	    		data = buff.array();
	    	}
	        
	        if (!isEnding) {
	        	incomming.put(packetNumber, ArrayUtils.toObject(data));
	        	return;
	        }
	        
	        try {
	        	ByteArrayDataInput input = ByteStreams.newDataInput(CompressionUtils.decompress(data));	        	
		        switch (packetId) {
		        case 0x08:
		        	int requestId = input.readInt();		   
		        	String component = DataTypeIO.readString(input, StandardCharsets.UTF_8);
		        	UUID uuid = requestedMessages.get(requestId);
		        	if (uuid != null) {
		        		Chat chatPacket = new Chat(component + "<QUxSRUFEWVBST0NFU1NFRA==>");
		        		UserConnection userConnection = (UserConnection) getProxy().getPlayer(uuid);
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
		        		
		        		channelWrapper.write(chatPacket);
		        	}
		        	break;
		        case 0x09:
		        	loadConfig();
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
		UUID uuid = ((ProxiedPlayer) event.getSender()).getUniqueId();
		String message = event.getMessage();
		
		if (message.startsWith("/")) {
			for (String parsecommand : InteractiveChatBungee.parseCommands) {
				//getProxy().getConsole().sendMessage(new TextComponent(parsecommand));
				if (message.matches(parsecommand)) {
					String command = message.trim();
					String uuidmatch = "<" + UUID.randomUUID().toString() + ">";
					command += uuidmatch;
					event.setMessage(command);
					try {
						sendCommandMatch(uuid, "", uuidmatch);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		} else {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					List<String> messages = forwardedMessages.get(uuid);
					if (!messages.remove(message)) {
						try {
							sendMessagePair(uuid, message);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}, 100);
		}
	}
	
	private void sendCommandMatch(UUID uuid, String placeholder, String uuidmatch) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeUUID(output, uuid);
    	DataTypeIO.writeString(output, placeholder, StandardCharsets.UTF_8);
    	DataTypeIO.writeString(output, uuidmatch, StandardCharsets.UTF_8);

		int packetNumber = random.nextInt();
		int packetId = 0x07;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (ServerInfo server : getProxy().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				pluginMessagesCounter.incrementAndGet();
			}
		}
	}
	
	private void sendMessagePair(UUID uuid, String message) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeString(output, message, StandardCharsets.UTF_8);
    	DataTypeIO.writeUUID(output, uuid);

		int packetNumber = random.nextInt();
		int packetId = 0x06;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (ServerInfo server : getProxy().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				pluginMessagesCounter.incrementAndGet();
			}
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

		pipeline.addBefore(PipelineUtils.BOSS_HANDLER, "packet_interceptor", new ChannelDuplexHandler() {
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
				if (obj instanceof Chat) {
					Chat packet = (Chat) obj;
					forwardedMessages.get(player.getUniqueId()).add(packet.getMessage());
				}
				super.write(channelHandlerContext, obj, channelPromise); // send it to client
			}
		});
	}

	@EventHandler
	public void onPlayerConnected(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		forwardedMessages.put(player.getUniqueId(), new ArrayList<>());
		
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

		pipeline.addBefore(PipelineUtils.BOSS_HANDLER, "packet_interceptor", new ChannelDuplexHandler() {
			@Override
			public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
				if (obj instanceof Chat) {
					Chat packet = (Chat) obj;
					if (packet.getMessage().contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
						packet.setMessage(packet.getMessage().replace("<QUxSRUFEWVBST0NFU1NFRA==>", ""));
					} else {
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								try {
									requestMessageProcess(player, packet.getMessage());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, 150);
						return;
					}
				}
				super.write(channelHandlerContext, obj, channelPromise); // send it to client
			}
		});
	}
	
	private void requestMessageProcess(ProxiedPlayer player, String component) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		int requestId = random.nextInt();
		
		output.writeInt(requestId);
		DataTypeIO.writeUUID(output, player.getUniqueId());
		DataTypeIO.writeString(output, component, StandardCharsets.UTF_8);
		
		ServerInfo server = player.getServer().getInfo();

		int packetNumber = random.nextInt();
		int packetId = 0x08;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			server.sendData("interchat:main", out.toByteArray());
			pluginMessagesCounter.incrementAndGet();
		}
		
		requestedMessages.put(requestId, player.getUniqueId());
	}

	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
					event.getPlayer().sendMessage(new TextComponent(ChatColor.GOLD + "InteractiveChat (Bungeecord) " + plugin.getDescription().getVersion() + " is running!"));
				}
			}
		}, 200);
	}

	@EventHandler
	public void onJoin(PostLoginEvent event) {
		try {
			sendPlayerListData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent event) {
		forwardedMessages.remove(event.getPlayer().getUniqueId());
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					sendPlayerListData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	private void sendPlayerListData() throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
		output.writeInt(players.size());
		for (ProxiedPlayer player : players) {
			DataTypeIO.writeUUID(output, player.getUniqueId());
			DataTypeIO.writeString(output, player.getDisplayName(), StandardCharsets.UTF_8);
		}

		int packetNumber = random.nextInt();
		int packetId = 0x00;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (ServerInfo server : getProxy().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				pluginMessagesCounter.incrementAndGet();
			}
		}
	}

	private void sendDelay() throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		List<CompletableFuture<Integer>> futures = new LinkedList<>();

		for (ServerInfo server : getProxy().getServers().values()) {
			futures.add(getPing(server));
		}
		int highestPing = futures.stream().mapToInt(each -> {
			try {
				return each.get();
			} catch (InterruptedException | ExecutionException e) {
				return 0;
			}
		}).max().orElse(0);

		output.writeInt(highestPing * 2 + 200);

		int packetNumber = random.nextInt();
		int packetId = 0x01;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (ServerInfo server : getProxy().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				pluginMessagesCounter.incrementAndGet();
			}
		}
	}

	private CompletableFuture<Integer> getPing(ServerInfo server) {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		long start = System.currentTimeMillis();
		Callback<ServerPing> callback = new Callback<ServerPing>() {
			@Override
			public void done(ServerPing result, Throwable error) {
				if (error == null) {
					future.complete((int) (System.currentTimeMillis() - start));
				} else {
					future.complete(0);
				}
			}
		};
		server.ping(callback);
		return future;
	}
}