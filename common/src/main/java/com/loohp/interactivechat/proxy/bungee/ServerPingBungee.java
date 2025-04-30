/*
 * This file is part of InteractiveChat4.
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

package com.loohp.interactivechat.proxy.bungee;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.DataStreamIO;
import com.loohp.interactivechat.utils.MCVersion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.chat.ComponentSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerPingBungee {

    public static final String UNKNOWN_VERSION = "unknown";
    public static final int UNKNOWN_PROTOCOL = -1;

    private static final AtomicInteger THREAD_NUMBER_COUNTER = new AtomicInteger(0);
    private static final Map<ServerInfo, CompletableFuture<ServerPingBungee>> ACTIVE_PING = new ConcurrentHashMap<>();

    public static final ExecutorService SERVICE;

    static {
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("InteractiveChatBungee Async Backend Server Ping Thread #%d").build();
        SERVICE = new ThreadPoolExecutor(8, 32, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), factory);
    }

    public static void shutdown() {
        SERVICE.shutdown();
    }

    @SuppressWarnings("deprecation")
    public static CompletableFuture<ServerPingBungee> getPing(ServerInfo server) {
        {
            CompletableFuture<ServerPingBungee> future = ACTIVE_PING.get(server);
            if (future != null) {
                return future;
            }
        }
        CompletableFuture<ServerPingBungee> future = new CompletableFuture<>();
        ACTIVE_PING.put(server, future);
        SERVICE.execute(() -> {
            try (Socket socket = new Socket()) {
                SocketAddress address = server.getSocketAddress();
                if (address instanceof InetSocketAddress) {
                    InetSocketAddress inetAddress = (InetSocketAddress) address;
                    socket.connect(new InetSocketAddress(inetAddress.getHostName(), inetAddress.getPort()), 1500);
                } else {
                    socket.connect(address, 1500);
                }

                if (socket.isConnected()) {
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(buffer);
                    out.writeByte(0x00);
                    DataStreamIO.writeVarInt(out, -1);
                    DataStreamIO.writeString(out, Registry.PLUGIN_MESSAGING_PROTOCOL_IDENTIFIER, StandardCharsets.UTF_8);
                    out.writeShort(25566);
                    DataStreamIO.writeVarInt(out, 1);
                    byte[] handshake = buffer.toByteArray();
                    DataStreamIO.writeVarInt(output, handshake.length);
                    output.write(handshake);

                    buffer = new ByteArrayOutputStream();
                    out = new DataOutputStream(buffer);
                    out.writeByte(0x00);
                    byte[] request = buffer.toByteArray();
                    DataStreamIO.writeVarInt(output, request.length);
                    output.write(request);

                    DataStreamIO.readVarInt(input);
                    int packetId = DataStreamIO.readVarInt(input);

                    if (packetId == -1) {
                        throw new IOException("Premature end of stream.");
                    }

                    if (packetId != 0x00) { //we want a status response
                        throw new IOException("Invalid packetID " + Integer.toHexString(packetId));
                    }
                    int length = DataStreamIO.readVarInt(input); //length of json string

                    if (length == -1) {
                        throw new IOException("Premature end of stream.");
                    }

                    if (length == 0) {
                        throw new IOException("Invalid string length.");
                    }

                    byte[] in = new byte[length];
                    input.readFully(in);
                    String jsonStr = new String(in);

                    boolean present;
                    String version;
                    MCVersion minecraftVersion;
                    String exactMinecraftVersion;
                    int protocol;
                    try {
                        JSONObject json = (JSONObject) new JSONParser().parse(jsonStr);
                        Object descriptionObj = json.get("description");
                        JSONObject data;
                        if (descriptionObj instanceof JSONObject) {
                            JSONObject description = (JSONObject) json.get("description");
                            String descriptionAsStr = ChatColor.stripColor(ComponentSerializer.parse(description.toJSONString())[0].toPlainText());
                            data = (JSONObject) new JSONParser().parse(descriptionAsStr);
                        } else {
                            data = (JSONObject) new JSONParser().parse(CustomStringUtils.unescapeUnicode(descriptionObj.toString()));
                        }
                        present = (boolean) data.get("present");
                        version = (String) data.get("version");
                        minecraftVersion = MCVersion.fromNumber((int) (long) data.get("minecraftVersion"));
                        exactMinecraftVersion = (String) data.get("exactMinecraftVersion");
                        protocol = data.containsKey("protocol") ? (int) (long) data.get("protocol") : 0;
                    } catch (Exception e) {
                        present = false;
                        version = UNKNOWN_VERSION;
                        minecraftVersion = MCVersion.UNSUPPORTED;
                        exactMinecraftVersion = UNKNOWN_VERSION;
                        protocol = UNKNOWN_PROTOCOL;
                    }

                    long start = System.currentTimeMillis();

                    buffer = new ByteArrayOutputStream();
                    out = new DataOutputStream(buffer);
                    out.writeByte(0x01);
                    out.writeLong(start);
                    byte[] ping = buffer.toByteArray();
                    DataStreamIO.writeVarInt(output, ping.length);
                    output.write(ping);

                    DataStreamIO.readVarInt(input);
                    packetId = DataStreamIO.readVarInt(input);

                    int pong = (int) (System.currentTimeMillis() - start);

                    if (packetId == -1) {
                        throw new IOException("Premature end of stream.");
                    }

                    if (packetId != 0x01) { //we want a pong
                        throw new IOException("Invalid packetID " + Integer.toHexString(packetId));
                    }

                    future.complete(new ServerPingBungee(pong, present));

                    BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(server.getName());
                    if (data == null) {
                        InteractiveChatBungee.serverInteractiveChatInfo.put(server.getName(), new BackendInteractiveChatData(server.getName(), true, present, version, minecraftVersion, exactMinecraftVersion, pong, protocol));
                    } else {
                        data.setPing(pong);
                        if (!data.isOnline()) {
                            data.setOnline(true);
                        }
                        if (data.hasInteractiveChat() != present) {
                            data.setInteractiveChat(present);
                        }
                        if (!data.getVersion().equals(version)) {
                            data.setVersion(version);
                        }
                        if (!data.getMinecraftVersion().equals(minecraftVersion)) {
                            data.setMinecraftVersion(minecraftVersion);
                        }
                        if (!data.getExactMinecraftVersion().equals(exactMinecraftVersion)) {
                            data.setExactMinecraftVersion(exactMinecraftVersion);
                        }
                        if (data.getProtocolVersion() != protocol) {
                            data.setProtocolVersion(protocol);
                        }
                    }
                } else {
                    future.complete(new ServerPingBungee(-1, false));

                    BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(server.getName());
                    if (data == null) {
                        InteractiveChatBungee.serverInteractiveChatInfo.put(server.getName(), new BackendInteractiveChatData(server.getName(), false, false, UNKNOWN_VERSION, MCVersion.UNSUPPORTED, UNKNOWN_VERSION, -1, UNKNOWN_PROTOCOL));
                    } else {
                        data.setPing(-1);
                        if (data.isOnline()) {
                            data.setOnline(false);
                        }
                        if (data.hasInteractiveChat()) {
                            data.setInteractiveChat(false);
                        }
                        if (!data.getVersion().equals(UNKNOWN_VERSION)) {
                            data.setVersion(UNKNOWN_VERSION);
                        }
                        if (data.getProtocolVersion() != UNKNOWN_PROTOCOL) {
                            data.setProtocolVersion(UNKNOWN_PROTOCOL);
                        }
                    }
                }
            } catch (IOException e) {
                future.complete(new ServerPingBungee(-1, false));

                BackendInteractiveChatData data = InteractiveChatBungee.serverInteractiveChatInfo.get(server.getName());
                if (data == null) {
                    InteractiveChatBungee.serverInteractiveChatInfo.put(server.getName(), new BackendInteractiveChatData(server.getName(), false, false, UNKNOWN_VERSION, MCVersion.UNSUPPORTED, UNKNOWN_VERSION, -1, UNKNOWN_PROTOCOL));
                } else {
                    data.setPing(-1);
                    if (data.isOnline()) {
                        data.setOnline(false);
                    }
                    if (data.hasInteractiveChat()) {
                        data.setInteractiveChat(false);
                    }
                    if (!data.getVersion().equals(UNKNOWN_VERSION)) {
                        data.setVersion(UNKNOWN_VERSION);
                    }
                    if (data.getProtocolVersion() != UNKNOWN_PROTOCOL) {
                        data.setProtocolVersion(UNKNOWN_PROTOCOL);
                    }
                }
            }
            ACTIVE_PING.remove(server, future);
        });
        return future;
    }

    private final int ping;
    private final boolean hasInteractiveChat;

    private ServerPingBungee(int ping, boolean hasInteractiveChat) {
        this.ping = ping;
        this.hasInteractiveChat = hasInteractiveChat;
    }

    public int getPing() {
        return ping;
    }

    public boolean hasInteractiveChat() {
        return hasInteractiveChat;
    }

}
