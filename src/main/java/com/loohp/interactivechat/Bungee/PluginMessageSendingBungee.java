package com.loohp.interactivechat.Bungee;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.Utils.CompressionUtils;
import com.loohp.interactivechat.Utils.CustomArrayUtils;
import com.loohp.interactivechat.Utils.DataTypeIO;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PluginMessageSendingBungee {
	
	public static void forwardPlayerData(UUID uuid, String playerdata, ServerInfo serverFrom) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeUUID(output, uuid);
    	DataTypeIO.writeString(output, playerdata, StandardCharsets.UTF_8);

		int packetNumber = InteractiveChatBungee.random.nextInt();
		int packetId = 0x12;
		byte[] data = output.toByteArray();

		byte[][] dataArray = CustomArrayUtils.divideArray(CompressionUtils.compress(data), 32700);

		for (int i = 0; i < dataArray.length; i++) {
			byte[] chunk = dataArray[i];

			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeInt(packetNumber);

			out.writeShort(packetId);
			out.writeBoolean(i == (dataArray.length - 1));

			out.write(chunk);

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				if (!server.getSocketAddress().equals(serverFrom.getSocketAddress())) {
					server.sendData("interchat:main", out.toByteArray());
					InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	public static void forwardPlaceholderList(List<ICPlaceholder> serverPlaceholderList, ServerInfo serverFrom) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeString(output, serverFrom.getName(), StandardCharsets.UTF_8);
		output.writeInt(serverPlaceholderList.size());
    	for (ICPlaceholder placeholder : serverPlaceholderList) {
    		boolean isBuiltIn = placeholder.isBuildIn();
    		output.writeBoolean(isBuiltIn);
    		if (isBuiltIn) {
    			DataTypeIO.writeString(output, placeholder.getKeyword(), StandardCharsets.UTF_8);
    			output.writeBoolean(placeholder.isCaseSensitive());
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
    		}
    	}

		int packetNumber = InteractiveChatBungee.random.nextInt();
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

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				if (!server.getSocketAddress().equals(serverFrom.getSocketAddress())) {
					server.sendData("interchat:main", out.toByteArray());
					InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
				}
			}
		}
	}
	
	public static void sendCommandMatch(UUID uuid, String placeholder, String uuidmatch) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeUUID(output, uuid);
    	DataTypeIO.writeString(output, placeholder, StandardCharsets.UTF_8);
    	DataTypeIO.writeString(output, uuidmatch, StandardCharsets.UTF_8);

		int packetNumber = InteractiveChatBungee.random.nextInt();
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

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
			}
		}
	}
	
	public static void sendMessagePair(UUID uuid, String message) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		DataTypeIO.writeString(output, message, StandardCharsets.UTF_8);
    	DataTypeIO.writeUUID(output, uuid);

		int packetNumber = InteractiveChatBungee.random.nextInt();
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

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
			}
		}
	}
	
	public static void requestMessageProcess(ProxiedPlayer player, String component, UUID messageId) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		
		DataTypeIO.writeUUID(output, messageId);
		DataTypeIO.writeUUID(output, player.getUniqueId());
		DataTypeIO.writeString(output, component, StandardCharsets.UTF_8);

		ServerInfo server = player.getServer().getInfo();

		int packetNumber = InteractiveChatBungee.random.nextInt();
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
			InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
		}
		
		InteractiveChatBungee.requestedMessages.put(messageId, player.getUniqueId());
	}
	
	public static void requestPlaceholderList(ServerInfo server) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		int packetNumber = InteractiveChatBungee.random.nextInt();
		int packetId = 0x10;
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
			InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
		}
	}
	
	public static void requestAliasesMapping(ServerInfo server) throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		int packetNumber = InteractiveChatBungee.random.nextInt();
		int packetId = 0x11;
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
			InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
		}
	}
	
	public static void sendPlayerListData() throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
		output.writeInt(players.size());
		for (ProxiedPlayer player : players) {
			if (player.getServer() != null) {
				DataTypeIO.writeString(output, player.getServer().getInfo().getName(), StandardCharsets.UTF_8);
				DataTypeIO.writeUUID(output, player.getUniqueId());
				DataTypeIO.writeString(output, player.getDisplayName(), StandardCharsets.UTF_8);
			}
		}

		int packetNumber = InteractiveChatBungee.random.nextInt();
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

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
			}
		}
	}

	public static void sendDelay() throws IOException {
		ByteArrayDataOutput output = ByteStreams.newDataOutput();

		List<CompletableFuture<ServerPingBungee>> futures = new LinkedList<>();

		for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
			futures.add(ServerPingBungee.getPing(server));
		}
		int highestPing = futures.stream().mapToInt(each -> {
			try {
				ServerPingBungee response = each.get();
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
		
		InteractiveChatBungee.delay = highestPing * 2 + 200;

		output.writeInt(InteractiveChatBungee.delay);

		int packetNumber = InteractiveChatBungee.random.nextInt();
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

			for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
				server.sendData("interchat:main", out.toByteArray());
				InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
			}
		}
	}

}
