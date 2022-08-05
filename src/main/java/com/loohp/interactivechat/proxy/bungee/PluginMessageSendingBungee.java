/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

package com.loohp.interactivechat.proxy.bungee;

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
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.xml.crypto.Data;
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
public class PluginMessageSendingBungee {

    public static void sendPlayerListData() throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();

        List<PlayerListPlayerData> dataList = new ArrayList<>();
        for (ProxiedPlayer player : players) {
            if (player.getServer() != null) {
                String server = player.getServer().getInfo().getName();
                BackendInteractiveChatData info = InteractiveChatBungee.serverInteractiveChatInfo.get(server);
                if (info != null && info.hasInteractiveChat()) {
                    dataList.add(new PlayerListPlayerData(server, player.getUniqueId(), player.getName()));
                }
            }
        }

        output.writeInt(dataList.size());
        for (PlayerListPlayerData data : dataList) {
            DataTypeIO.writeString(output, data.getServer(), StandardCharsets.UTF_8);
            DataTypeIO.writeUUID(output, data.getUniqueId());
            DataTypeIO.writeString(output, data.getName(), StandardCharsets.UTF_8);
        }

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x00;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber);

            out.writeShort(packetId);
            out.writeBoolean(i == (dataArray.length - 1));

            out.write(chunk);

            for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                if (!server.getPlayers().isEmpty()) {
                    server.sendData("interchat:main", out.toByteArray());
                    InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void sendDelayAndScheme() throws IOException {
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
                    return Math.max(ping, 0);
                } else {
                    return 0;
                }
            } catch (InterruptedException | ExecutionException e) {
                return 0;
            }
        }).max().orElse(0);

        InteractiveChatBungee.delay = highestPing * 2 + 100;

        output.writeInt(InteractiveChatBungee.delay);

        boolean hasDifferentMCVersions = InteractiveChatBungee.serverInteractiveChatInfo.values().stream().map(each -> each.getExactMinecraftVersion()).distinct().count() > 1;
        if (hasDifferentMCVersions) {
            output.writeShort(1);
            output.writeShort(1);
        } else {
            output.writeShort(0);
            output.writeShort(0);
        }

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x01;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber);

            out.writeShort(packetId);
            out.writeBoolean(i == (dataArray.length - 1));

            out.write(chunk);

            for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                if (!server.getPlayers().isEmpty()) {
                    server.sendData("interchat:main", out.toByteArray());
                    InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
                }
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

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber);

            out.writeShort(packetId);
            out.writeBoolean(i == (dataArray.length - 1));

            out.write(chunk);

            for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                if (!server.getPlayers().isEmpty()) {
                    server.sendData("interchat:main", out.toByteArray());
                    InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void sendPlayerUniversalCooldown(ServerInfo server, UUID player, long time) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        output.writeByte(0);
        DataTypeIO.writeUUID(output, player);
        output.writeLong(time);

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x07;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

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

    public static void sendPlayerPlaceholderCooldown(ServerInfo server, UUID player, ICPlaceholder placeholder, long time) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        output.writeByte(1);
        DataTypeIO.writeUUID(output, player);
        DataTypeIO.writeUUID(output, placeholder.getInternalId());
        output.writeLong(time);

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x07;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

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

    public static void requestMessageProcess(ProxiedPlayer player, ServerInfo server, String component, UUID messageId, boolean preview) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, messageId);
        DataTypeIO.writeUUID(output, player.getUniqueId());
        DataTypeIO.writeString(output, component, StandardCharsets.UTF_8);
        output.writeBoolean(preview);

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x08;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

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

    @SuppressWarnings("deprecation")
    public static void forwardPlaceholderList(List<ICPlaceholder> serverPlaceholderList, ServerInfo serverFrom) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeString(output, serverFrom.getName(), StandardCharsets.UTF_8);
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
                output.writeInt(customPlaceholder.getPosition());
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

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x09;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber);

            out.writeShort(packetId);
            out.writeBoolean(i == (dataArray.length - 1));

            out.write(chunk);

            for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                if (!server.getName().equals(serverFrom.getName())) {
                    server.sendData("interchat:main", out.toByteArray());
                    InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void requestPlaceholderList(ServerInfo server) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x0A;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

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

    public static void checkPermission(ProxiedPlayer player, String permission, int id) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        output.writeInt(id);
        DataTypeIO.writeUUID(output, player.getUniqueId());
        DataTypeIO.writeString(output, permission, StandardCharsets.UTF_8);

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x0B;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber);

            out.writeShort(packetId);
            out.writeBoolean(i == (dataArray.length - 1));

            out.write(chunk);

            if (player.getServer() != null) {
                player.getServer().getInfo().sendData("interchat:main", out.toByteArray());
                InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
            }
        }
    }

    public static void reloadPlayerData(UUID uuid, ServerInfo serverFrom) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, uuid);

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x0D;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

        for (int i = 0; i < dataArray.length; i++) {
            byte[] chunk = dataArray[i];

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(packetNumber);

            out.writeShort(packetId);
            out.writeBoolean(i == (dataArray.length - 1));

            out.write(chunk);

            for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
                if (!server.getName().equals(serverFrom.getName())) {
                    server.sendData("interchat:main", out.toByteArray());
                    InteractiveChatBungee.pluginMessagesCounter.incrementAndGet();
                }
            }
        }
    }

    public static void respondPlayerListRequest(UUID requestId, ServerInfo server) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, requestId);
        output.writeByte(0);
        Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
        output.writeInt(players.size());
        for (ProxiedPlayer player : players) {
            DataTypeIO.writeUUID(output, player.getUniqueId());
            DataTypeIO.writeString(output, player.getName(), StandardCharsets.UTF_8);
            output.writeInt(player.getPing());
        }

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x10;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

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

    public static void forwardSignedChatEventChange(UUID sender, String originalMessage, String modifiedMessage, long time) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();

        DataTypeIO.writeUUID(output, sender);
        DataTypeIO.writeString(output, originalMessage, StandardCharsets.UTF_8);
        DataTypeIO.writeString(output, modifiedMessage, StandardCharsets.UTF_8);
        output.writeLong(time);

        int packetNumber = InteractiveChatBungee.random.nextInt();
        int packetId = 0x13;
        byte[] data = output.toByteArray();

        byte[][] dataArray = CustomArrayUtils.divideArray(data, 32700);

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
