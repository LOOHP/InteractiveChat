/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.proxy.velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("UnstableApiUsage")
public class PluginMessageSendingVelocity {

    private static final Object lastServerPingLock = new Object();
    private static long lastServerPing = 0;

    private static ProxyServer getServer() {
        return InteractiveChatVelocity.plugin.getServer();
    }

    public static void sendPlayerListData() throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        Collection<Player> players = getServer().getAllPlayers();

        List<PlayerListPlayerData> dataList = new ArrayList<>();
        for (Player player : players) {
            if (player.getCurrentServer().isPresent()) {
                String server = player.getCurrentServer().get().getServer().getServerInfo().getName();
                BackendInteractiveChatData info = InteractiveChatVelocity.serverInteractiveChatInfo.get(server);
                if (info != null && info.hasInteractiveChat()) {
                    dataList.add(new PlayerListPlayerData(server, player.getUniqueId(), player.getUsername()));
                }
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

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            for (RegisteredServer server : getServer().getAllServers()) {
                if (!server.getPlayersConnected().isEmpty()) {
                    server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
                    InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void sendDelayAndScheme() {
        sendDelayAndScheme(false);
    }

    public static void sendDelayAndScheme(boolean ignoreCooldown) {
        if (!ignoreCooldown) {
            synchronized (lastServerPingLock) {
                long now = System.currentTimeMillis();
                if (now - lastServerPing < 2000) {
                    return;
                }
                lastServerPing = now;
            }
        }

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
                    return Math.max(ping, 0);
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

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

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

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            for (RegisteredServer server : getServer().getAllServers()) {
                if (!server.getPlayersConnected().isEmpty()) {
                    server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
                    InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void sendPlayerUniversalCooldown(RegisteredServer server, UUID player, long time) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        output.writeByte(0);
        DataTypeIO.writeUUID(output, player);
        output.writeLong(time);

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x07;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
            InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
        }
    }

    public static void sendPlayerPlaceholderCooldown(RegisteredServer server, UUID player, ICPlaceholder placeholder, long time) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        output.writeByte(0);
        DataTypeIO.writeUUID(output, player);
        DataTypeIO.writeUUID(output, placeholder.getInternalId());
        output.writeLong(time);

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x07;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
            InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
        }
    }

    public static void requestMessageProcess(Player player, RegisteredServer server, String component, UUID messageId, boolean preview) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, messageId);
        DataTypeIO.writeUUID(output, player.getUniqueId());
        DataTypeIO.writeString(output, component, StandardCharsets.UTF_8);
        output.writeBoolean(preview);

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x08;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
            InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
        }
    }

    public static void forwardPlaceholderList(List<ICPlaceholder> serverPlaceholderList, RegisteredServer serverFrom) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeString(output, serverFrom.getServerInfo().getName(), StandardCharsets.UTF_8);
        output.writeInt(serverPlaceholderList.size());
        for (ICPlaceholder placeholder : serverPlaceholderList) {
            boolean isBuiltIn = placeholder.isBuildIn();
            output.writeBoolean(isBuiltIn);
            if (isBuiltIn) {
                DataTypeIO.writeString(output, placeholder.getKeyword().pattern(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(output, placeholder.getName(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(output, placeholder.getDescription(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(output, placeholder.getPermission(), StandardCharsets.UTF_8);
                output.writeLong(placeholder.getCooldown());
            } else {
                CustomPlaceholder customPlaceholder = (CustomPlaceholder) placeholder;
                DataTypeIO.writeString(output, customPlaceholder.getKey(), StandardCharsets.UTF_8);
                output.writeByte(customPlaceholder.getParsePlayer().getOrder());
                DataTypeIO.writeString(output, customPlaceholder.getKeyword().pattern(), StandardCharsets.UTF_8);
                output.writeBoolean(customPlaceholder.getParseKeyword());
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

                DataTypeIO.writeString(output, placeholder.getName(), StandardCharsets.UTF_8);
                DataTypeIO.writeString(output, placeholder.getDescription(), StandardCharsets.UTF_8);
            }
        }

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x09;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

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

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

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

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            if (player.getCurrentServer().isPresent()) {
                player.getCurrentServer().get().sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
                InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
            }
        }
    }

    public static void reloadPlayerData(UUID uuid, RegisteredServer serverFrom) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, uuid);

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x0D;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            for (RegisteredServer server : getServer().getAllServers()) {
                if (!server.getServerInfo().getName().equals(serverFrom.getServerInfo().getName())) {
                    server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
                    InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void respondPlayerListRequest(UUID requestId, RegisteredServer server) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, requestId);
        output.writeByte(0);
        Collection<Player> players = getServer().getAllPlayers();
        output.writeInt(players.size());
        for (Player player : players) {
            DataTypeIO.writeUUID(output, player.getUniqueId());
            DataTypeIO.writeString(output, player.getUsername(), StandardCharsets.UTF_8);
            output.writeInt((int) player.getPing());
        }

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x10;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
            InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
        }
    }

    public static void forwardSignedChatEventChange(UUID sender, String originalMessage, String modifiedMessage, long time) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, sender);
        DataTypeIO.writeString(output, originalMessage, StandardCharsets.UTF_8);
        DataTypeIO.writeString(output, modifiedMessage, StandardCharsets.UTF_8);
        output.writeLong(time);

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x13;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            for (RegisteredServer server : getServer().getAllServers()) {
                server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
                InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
            }
        }
    }

    public static void executeBackendCommand(UUID player, String command, RegisteredServer server) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, player);
        DataTypeIO.writeString(output, command, StandardCharsets.UTF_8);

        int packetNumber = InteractiveChatVelocity.random.nextInt();
        int packetId = 0x15;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber); //random packet number
            out.writeInt(i); //packet chunk index
            out.writeInt(dataArray.length); //packet total chunks
            out.writeShort(packetId); //packet id

            out.write(chunk);

            server.sendPluginMessage(ICChannelIdentifier.INSTANCE, out.toByteArray());
            InteractiveChatVelocity.pluginMessagesCounter.incrementAndGet();
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

}
