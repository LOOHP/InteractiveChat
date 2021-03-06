package com.loohp.interactivechat.proxy.velocity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.utils.CompressionUtils;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class PluginMessageSendingVelocity {
	
	private static ProxyServer getServer() {
		return InteractiveChatVelocity.plugin.getServer();
	}
	
	public static void sendPlayerListData() throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		Collection<Player> players = getServer().getAllPlayers();
		
		List<PlayerListPlayerData> dataList = new ArrayList<>();
		for (Player player : players) {
			if (player.getCurrentServer().isPresent()) {
				dataList.add(new PlayerListPlayerData(player.getCurrentServer().get().getServer().getServerInfo().getName(), player.getUniqueId(), player.getUsername()));
			}
		}
		
		output.writeInt(dataList.size());
		for (PlayerListPlayerData data : dataList) {
			DataTypeIO.writeString(output, data.getServer(), StandardCharsets.UTF_8);
			DataTypeIO.writeUUID(output, data.getUniqueId());
			DataTypeIO.writeString(output, data.getName(), StandardCharsets.UTF_8);
		}

		int packetNumber = InteractiveChatVelocity.random.nextInt();
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

			for (RegisteredServer server : getServer().getAllServers()) {
				if (!server.getPlayersConnected().isEmpty()) {
					server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
					InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	private static class PlayerListPlayerData {
		
		private final String server;
		private final UUID uuid;
		private final String name;
		
		public PlayerListPlayerData(String server, UUID uuid, String name) {
			this.server = server;
			this.uuid = uuid;
			this.name = name;
		}
		
		public String getServer() {
			return server;
		}
		public UUID getUniqueId() {
			return uuid;
		}
		public String getName() {
			return name;
		}
	}

	public static void sendDelayAndScheme() throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		List<CompletableFuture<ServerPingVelocity>> futures = new LinkedList<>();

		for (RegisteredServer server : getServer().getAllServers()) {
			futures.add(ServerPingVelocity.getPing(server.getServerInfo()));
		}
		int highestPing = futures.stream().mapToInt(each -> {
			try {
				ServerPingVelocity response = each.get();
				if (response.hasInteractiveChat()) {
					int ping = response.getPing();
					return ping < 0 ? 0 : ping;
				} else {
					return 0;
				}
			} catch (InterruptedException | ExecutionException e) {
				return 0;
			}
		}).max().orElse(0);
		
		InteractiveChatVelocity.delay = highestPing * 2 + 100;

		output.writeInt(InteractiveChatVelocity.delay);
		
		boolean hasDifferentMCVersions = InteractiveChatVelocity.serverInteractiveChatInfo.values().stream().map(each -> each.getExactMinecraftVersion()).distinct().count() > 1;
		if (hasDifferentMCVersions) {
			output.writeShort(1);
			output.writeShort(1);
		} else {
			output.writeShort(0);
			output.writeShort(0);
		}

		int packetNumber = InteractiveChatVelocity.random.nextInt();
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

			for (RegisteredServer server : getServer().getAllServers()) {
				if (!server.getPlayersConnected().isEmpty()) {
					server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
					InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	public static void sendMessagePair(UUID uuid, String message) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeString(output, message, StandardCharsets.UTF_8);
    	DataTypeIO.writeUUID(output, uuid);

		int packetNumber = InteractiveChatVelocity.random.nextInt();
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

			for (RegisteredServer server : getServer().getAllServers()) {
				if (!server.getPlayersConnected().isEmpty()) {
					server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
					InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	public static void requestMessageProcess(Player player, RegisteredServer server, String component, UUID messageId) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		DataTypeIO.writeUUID(output, messageId);
		DataTypeIO.writeUUID(output, player.getUniqueId());
		DataTypeIO.writeString(output, component, StandardCharsets.UTF_8);

		int packetNumber = InteractiveChatVelocity.random.nextInt();
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

			server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
			InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void forwardPlaceholderList(List<ICPlaceholder> serverPlaceholderList, RegisteredServer serverFrom) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeString(output, serverFrom.getServerInfo().getName(), StandardCharsets.UTF_8);
		output.writeInt(serverPlaceholderList.size());
    	for (ICPlaceholder placeholder : serverPlaceholderList) {
    		boolean isBuiltIn = placeholder.isBuildIn();
    		output.writeBoolean(isBuiltIn);
    		if (isBuiltIn) {
    			DataTypeIO.writeString(output, placeholder.getKeyword(), StandardCharsets.UTF_8);
    			output.writeBoolean(placeholder.isCaseSensitive());
    			DataTypeIO.writeString(output, placeholder.getDescription(), StandardCharsets.UTF_8);
    			DataTypeIO.writeString(output, placeholder.getPermission(), StandardCharsets.UTF_8);
    		} else {
    			CustomPlaceholder customPlaceholder = placeholder.getCustomPlaceholder().get();
    			output.writeInt(customPlaceholder.getPosition());
    			output.writeByte(customPlaceholder.getParsePlayer().getOrder());
    			DataTypeIO.writeString(output, customPlaceholder.getKeyword(), StandardCharsets.UTF_8);
    			output.writeInt(customPlaceholder.getAliases().size());
    			for (String each : customPlaceholder.getAliases()) {
    				DataTypeIO.writeString(output, each, StandardCharsets.UTF_8);
    			}
    			output.writeBoolean(customPlaceholder.getParseKeyword());
    			output.writeBoolean(customPlaceholder.isCaseSensitive());
    			output.writeLong(customPlaceholder.getCooldown());
    			
    			CustomPlaceholderHoverEvent hover = customPlaceholder.getHover();
    			output.writeBoolean(hover.isEnabled());
    			DataTypeIO.writeString(output, hover.getText(), StandardCharsets.UTF_8);
    			
    			CustomPlaceholderClickEvent click = customPlaceholder.getClick();
    			output.writeBoolean(click.isEnabled());
    			DataTypeIO.writeString(output, click.getAction() == null ? "" : click.getAction().name(), StandardCharsets.UTF_8);
    			DataTypeIO.writeString(output, click.getValue(), StandardCharsets.UTF_8);
    			
    			CustomPlaceholderReplaceText replace = customPlaceholder.getReplace();
    			output.writeBoolean(replace.isEnabled());
    			DataTypeIO.writeString(output, replace.getReplaceText(), StandardCharsets.UTF_8);
    			
    			DataTypeIO.writeString(output, placeholder.getDescription(), StandardCharsets.UTF_8);
    		}
    	}

		int packetNumber = InteractiveChatVelocity.random.nextInt();
		int packetId = 0x09;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (RegisteredServer server : getServer().getAllServers()) {
				if (!server.getServerInfo().getName().equals(serverFrom.getServerInfo().getName())) {
					server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
					InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	public static void requestPlaceholderList(RegisteredServer server) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		int packetNumber = InteractiveChatVelocity.random.nextInt();
		int packetId = 0x0A;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
			InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
		}
	}
	
	public static void checkPermission(Player player, String permission, int id) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		output.writeInt(id);
		DataTypeIO.writeUUID(output, player.getUniqueId());
    	DataTypeIO.writeString(output, permission, StandardCharsets.UTF_8);

		int packetNumber = InteractiveChatVelocity.random.nextInt();
		int packetId = 0x0B;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			if (player.getCurrentServer().isPresent()) {
				player.getCurrentServer().get().sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
				InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
			}
		}
	}
	
	public static void requestAliasesMapping(RegisteredServer server) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		int packetNumber = InteractiveChatVelocity.random.nextInt();
		int packetId = 0x0C;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
			InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
		}
	}
	
	public static void reloadPlayerData(UUID uuid, RegisteredServer serverFrom) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeUUID(output, uuid);

		int packetNumber = InteractiveChatVelocity.random.nextInt();
		int packetId = 0x0D;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (RegisteredServer server : getServer().getAllServers()) {
				if (!server.getServerInfo().getName().equals(serverFrom.getServerInfo().getName())) {
					server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
					InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}

}
