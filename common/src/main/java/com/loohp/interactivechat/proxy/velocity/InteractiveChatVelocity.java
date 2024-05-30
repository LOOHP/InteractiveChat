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

package com.loohp.interactivechat.proxy.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.loohp.interactivechat.config.Config;
import com.loohp.interactivechat.objectholders.BuiltInPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.LogFilter;
import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.proxy.objectholders.ChatPacketType;
import com.loohp.interactivechat.proxy.objectholders.ForwardedMessageData;
import com.loohp.interactivechat.proxy.objectholders.ProxyHandlePacketTypes;
import com.loohp.interactivechat.proxy.objectholders.ProxyMessageForwardingHandler;
import com.loohp.interactivechat.proxy.objectholders.ProxyPlayerCooldownManager;
import com.loohp.interactivechat.proxy.velocity.metrics.Charts;
import com.loohp.interactivechat.proxy.velocity.metrics.Metrics;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.loohp.interactivechat.utils.NativeAdventureConverter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.network.Connections;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ChatType;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import com.velocitypowered.proxy.protocol.packet.chat.SystemChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.legacy.LegacyChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;
import com.velocitypowered.proxy.protocol.packet.title.GenericTitlePacket.ActionType;
import com.velocitypowered.proxy.protocol.packet.title.LegacyTitlePacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleActionbarPacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleSubtitlePacket;
import com.velocitypowered.proxy.protocol.packet.title.TitleTextPacket;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InteractiveChatVelocity {

    public static final int BSTATS_PLUGIN_ID = 10945;
    public static final String CONFIG_ID = "config";
    private static final boolean filtersAdded = false;
    private static final Map<Integer, byte[][]> incoming = new HashMap<>();
    private static final Map<Integer, Boolean> permissionChecks = new ConcurrentHashMap<>();
    public static InteractiveChatVelocity plugin = null;
    public static AtomicLong pluginMessagesCounter = new AtomicLong(0);
    public static List<String> parseCommands = new ArrayList<>();
    public static Map<String, List<ICPlaceholder>> placeholderList = new HashMap<>();
    public static boolean useAccurateSenderFinder = true;
    public static boolean tagEveryIdentifiableMessage = false;
    public static boolean handleProxyMessage = true;
    public static ProxyHandlePacketTypes proxyHandlePacketTypesType = ProxyHandlePacketTypes.ALL;
    public static PostOrder chatEventPostOrder = PostOrder.LATE;
    public static int delay = 200;
    public static ProxyPlayerCooldownManager playerCooldownManager;
    public static ProxyServer proxyServer;
    protected static Random random = new Random();
    protected static Map<UUID, Set<ForwardedMessageData>> forwardedMessages = new ConcurrentHashMap<>();
    protected static Map<String, BackendInteractiveChatData> serverInteractiveChatInfo = new ConcurrentHashMap<>();
    private static ProxyMessageForwardingHandler messageForwardingHandler;
    private static ThreadPoolExecutor pluginMessageHandlingExecutor;
    private static Field componentHolderProtocolField;

    static {
        try {
            componentHolderProtocolField = ComponentHolder.class.getDeclaredField("version");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static ProtocolVersion getProtocolVersion(ComponentHolder componentHolder) {
        try {
            componentHolderProtocolField.setAccessible(true);
            return (ProtocolVersion) componentHolderProtocolField.get(componentHolder);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, BackendInteractiveChatData> getBackendInteractiveChatInfo() {
        return Collections.unmodifiableMap(serverInteractiveChatInfo);
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
                proxyServer.getScheduler().buildTask(plugin, () -> {
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
                }).schedule();
            }
        }
        return future;
    }

    public static void sendMessage(Object sender, Component component) {
        NativeAdventureConverter.sendNativeAudienceMessage(sender, component, false);
    }

    private final Logger logger;
    private final File dataFolder;
    private final Metrics.Factory metricsFactory;
    private VelocityPluginDescription description;

    @Inject
    public InteractiveChatVelocity(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, @DataDirectory Path dataDirectory) {
        InteractiveChatVelocity.proxyServer = server;
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
        try {
            Config.loadConfig(CONFIG_ID, new File(getDataFolder(), "bungeeconfig.yml"), getClass().getClassLoader().getResourceAsStream("config_proxy.yml"), getClass().getClassLoader().getResourceAsStream("config_proxy.yml"), true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        loadConfig();

        CommandsVelocity.createBrigadierCommand();

        proxyServer.getChannelRegistrar().register(ICChannelIdentifier.INSTANCE);

        getLogger().info(TextColor.GREEN + "[InteractiveChat] Registered Plugin Messaging Channels!");

        Metrics metrics = metricsFactory.make(this, BSTATS_PLUGIN_ID);
        Charts.setup(metrics);

        playerCooldownManager = new ProxyPlayerCooldownManager(placeholderList.values().stream().flatMap(each -> each.stream()).distinct().collect(Collectors.toList()));

        messageForwardingHandler = new ProxyMessageForwardingHandler((info, component) -> {
            Player player = proxyServer.getPlayer(info.getPlayer()).get();
            ServerConnection server = player.getCurrentServer().get();
            proxyServer.getScheduler().buildTask(plugin, () -> {
                try {
                    if (player != null && server != null) {
                        PluginMessageSendingVelocity.requestMessageProcess(player, server.getServer(), component, info.getId(), info.getType().isPreview());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).delay(delay + 50, TimeUnit.MILLISECONDS).schedule();
        }, (info, component) -> {
            Player player = proxyServer.getPlayer(info.getPlayer()).get();
            boolean legacyRGB = player.getProtocolVersion().getProtocol() < Registry.MINECRAFT_1_16_PROTOCOL_VERSION;
            MinecraftPacket packet;
            switch (info.getType()) {
                case LEGACY_CHAT:
                    packet = new LegacyChatPacket(component + "<QUxSRUFEWVBST0NFU1NFRA==>", (byte) info.getPosition(), null);
                    break;
                case SYSTEM_CHAT:
                    SystemChatPacket originalSystemChatPacket = (SystemChatPacket) info.getOriginalPacket();
                    packet = new SystemChatPacket();
                    ChatType chatType = Arrays.stream(ChatType.values()).filter(c -> c.getId() == info.getPosition()).findFirst().orElse(ChatType.CHAT);
                    try {
                        Field[] fields = packet.getClass().getDeclaredFields();
                        fields[0].setAccessible(true);
                        fields[0].set(packet, new ComponentHolder(getProtocolVersion(originalSystemChatPacket.getComponent()), component + "<QUxSRUFEWVBST0NFU1NFRA==>"));
                        fields[1].setAccessible(true);
                        fields[1].set(packet, chatType);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                /*
                case PLAYER_CHAT:
                    packet = new SessionPlayerChat();
                    try {
                        Field[] fields = packet.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            field.set(packet, field.get(info.getOriginalPacket()));
                        }
                        fields[1].set(packet, NativeAdventureConverter.componentToNative(GsonComponentSerializer.gson().deserialize(component).append(Component.text("<QUxSRUFEWVBST0NFU1NFRA==>")), legacyRGB));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                */
                case CHAT_PREVIEW:
                    //IT'S REMOVED!
                    return;
                case LEGACY_TITLE:
                    LegacyTitlePacket originalTitlePacket = (LegacyTitlePacket) info.getOriginalPacket();
                    LegacyTitlePacket legacyTitlePacket = new LegacyTitlePacket();
                    legacyTitlePacket.setComponent(new ComponentHolder(getProtocolVersion(originalTitlePacket.getComponent()), component + "<QUxSRUFEWVBST0NFU1NFRA==>"));
                    legacyTitlePacket.setAction(originalTitlePacket.getAction());
                    packet = legacyTitlePacket;
                    break;
                case TITLE:
                    TitleTextPacket originalTitleTextPacket = (TitleTextPacket) info.getOriginalPacket();
                    TitleTextPacket titleTextPacket = new TitleTextPacket();
                    titleTextPacket.setComponent(new ComponentHolder(getProtocolVersion(originalTitleTextPacket.getComponent()), component + "<QUxSRUFEWVBST0NFU1NFRA==>"));
                    packet = titleTextPacket;
                    break;
                case SUBTITLE:
                    TitleSubtitlePacket originalSubtitleTextPacket = (TitleSubtitlePacket) info.getOriginalPacket();
                    TitleSubtitlePacket titleSubtitlePacket = new TitleSubtitlePacket();
                    titleSubtitlePacket.setComponent(new ComponentHolder(getProtocolVersion(originalSubtitleTextPacket.getComponent()), component + "<QUxSRUFEWVBST0NFU1NFRA==>"));
                    packet = titleSubtitlePacket;
                    break;
                case ACTION_BAR:
                    TitleActionbarPacket originalActionbarPacket = (TitleActionbarPacket) info.getOriginalPacket();
                    TitleActionbarPacket titleActionbarPacket = new TitleActionbarPacket();
                    titleActionbarPacket.setComponent(new ComponentHolder(getProtocolVersion(originalActionbarPacket.getComponent()), component + "<QUxSRUFEWVBST0NFU1NFRA==>"));
                    packet = titleActionbarPacket;
                    break;
                default:
                    throw new IllegalStateException("Unable to send packet of type " + info.getType());
            }
            Optional<Player> optplayer = getServer().getPlayer(info.getPlayer());
            if (optplayer.isPresent()) {
                ConnectedPlayer userConnection = (ConnectedPlayer) optplayer.get();
                userConnection.getConnection().getChannel().write(packet);
            }
        }, uuid -> {
            return proxyServer.getPlayer(uuid).isPresent();
        }, uuid -> {
            Optional<ServerConnection> optCurrentServer = proxyServer.getPlayer(uuid).get().getCurrentServer();
            return optCurrentServer.isPresent() && hasInteractiveChat(optCurrentServer.get().getServer());
        }, () -> (long) delay + 2000);

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("InteractiveChatProxy Async PluginMessage Processing Thread #%d").build();
        pluginMessageHandlingExecutor = new ThreadPoolExecutor(8, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(true), factory);

        getLogger().info(TextColor.GREEN + "[InteractiveChat] InteractiveChat (Velocity) has been enabled!");

        run();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        try {
            messageForwardingHandler.close();
            pluginMessageHandlingExecutor.shutdown();
            ServerPingVelocity.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info(TextColor.RED + "[InteractiveChat] InteractiveChat (Velocity) has been disabled!");
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
        return proxyServer;
    }

    public void loadConfig() {
        Config config = Config.getConfig(CONFIG_ID);
        config.reload();

        parseCommands = config.getConfiguration().getStringList("Settings.CommandsToParse");
        useAccurateSenderFinder = config.getConfiguration().getBoolean("Settings.UseAccurateSenderParser");
        tagEveryIdentifiableMessage = config.getConfiguration().getBoolean("Settings.TagEveryIdentifiableMessage");
        handleProxyMessage = config.getConfiguration().getBoolean("Settings.HandleProxyMessage");
        proxyHandlePacketTypesType = ProxyHandlePacketTypes.fromStringList(config.getConfiguration().getStringList("Settings.HandlePacketType"));
        String chatEventPriorityString = config.getConfiguration().getString("Settings.ChatEventPriority").toUpperCase();
        if (chatEventPriorityString.equals("DEFAULT")) {
            chatEventPriorityString = "LATE";
        }
        chatEventPostOrder = PostOrder.valueOf(chatEventPriorityString);
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

    private void run() {
        proxyServer.getScheduler().buildTask(plugin, () -> {
            try {
                PluginMessageSendingVelocity.sendPlayerListData();
                PluginMessageSendingVelocity.sendDelayAndScheme();
            } catch (IOException e) {
                e.printStackTrace();
            }

            long now = System.currentTimeMillis();
            for (Set<ForwardedMessageData> list : forwardedMessages.values()) {
                Iterator<ForwardedMessageData> itr = list.iterator();
                while (itr.hasNext()) {
                    long time = itr.next().getTimeStamp();
                    if (time - 5000 > now) {
                        itr.remove();
                    }
                }
            }
        }).delay(5000, TimeUnit.MILLISECONDS).schedule();
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
        int packetChunkIndex = in.readInt();
        int packetChunkSize = in.readInt();
        int packetId = in.readShort();

        if (!Registry.PROXY_PASSTHROUGH_RELAY_PACKETS.contains(packetId)) {
            byte[] data = new byte[packet.length - 14];
            in.readFully(data);

            byte[][] chunks = incoming.remove(packetNumber);
            if (chunks == null) {
                chunks = new byte[packetChunkSize][];
            }
            if (chunks.length != packetChunkSize) {
                byte[][] adjusted = new byte[packetChunkSize][];
                System.arraycopy(chunks, 0, adjusted, 0, adjusted.length);
                chunks = adjusted;
            }
            chunks[packetChunkIndex] = data;
            if (CustomArrayUtils.anyNull(chunks)) {
                incoming.put(packetNumber, chunks);
                return;
            }
            data = new byte[Arrays.stream(chunks).mapToInt(a -> a.length).sum()];
            for (int i = 0, pos = 0; i < chunks.length; i++) {
                byte[] chunk = chunks[i];
                System.arraycopy(chunk, 0, data, pos, chunk.length);
                pos += chunk.length;
            }

            byte[] finalData = data;
            pluginMessageHandlingExecutor.submit(() -> {
                try {
                    ByteArrayDataInput input = ByteStreams.newDataInput(finalData);
                    switch (packetId) {
                        case 0x07:
                            int cooldownType = input.readByte();
                            switch (cooldownType) {
                                case 0:
                                    UUID uuid = DataTypeIO.readUUID(input);
                                    long time = input.readLong();
                                    playerCooldownManager.setPlayerUniversalLastTimestamp(uuid, time);
                                    break;
                                case 1:
                                    uuid = DataTypeIO.readUUID(input);
                                    UUID internalId = DataTypeIO.readUUID(input);
                                    time = input.readLong();
                                    playerCooldownManager.setPlayerPlaceholderLastTimestamp(uuid, internalId, time);
                                    break;
                            }
                            for (RegisteredServer eachServer : getServer().getAllServers()) {
                                if (!eachServer.getServerInfo().getName().equals(senderServer) && eachServer.getPlayersConnected().size() > 0) {
                                    eachServer.sendPluginMessage(ICChannelIdentifier.INSTANCE, finalData);
                                    pluginMessagesCounter.incrementAndGet();
                                }
                            }
                            break;
                        case 0x08:
                            UUID messageId = DataTypeIO.readUUID(input);
                            String component = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            messageForwardingHandler.receivedProcessedMessage(messageId, component);
                            break;
                        case 0x09:
                            loadConfig();
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
                                boolean isBuiltIn = input.readBoolean();
                                if (isBuiltIn) {
                                    String keyword = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    long cooldown = input.readLong();
                                    list.add(new BuiltInPlaceholder(Pattern.compile(keyword), name, description, permission, cooldown));
                                } else {
                                    String key = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    ParsePlayer parseplayer = ParsePlayer.fromOrder(input.readByte());
                                    String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    boolean parseKeyword = input.readBoolean();
                                    long cooldown = input.readLong();
                                    boolean hoverEnabled = input.readBoolean();
                                    String hoverText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    boolean clickEnabled = input.readBoolean();
                                    String clickAction = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String clickValue = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    boolean replaceEnabled = input.readBoolean();
                                    String replaceText = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);

                                    list.add(new CustomPlaceholder(key, parseplayer, Pattern.compile(placeholder), parseKeyword, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), name, description));
                                }
                            }
                            placeholderList.put(server.getServerInfo().getName(), list);
                            playerCooldownManager.reloadPlaceholders(placeholderList.values().stream().flatMap(each -> each.stream()).distinct().collect(Collectors.toList()));
                            PluginMessageSendingVelocity.forwardPlaceholderList(list, server);
                            break;
                        case 0x0D:
                            UUID uuid2 = DataTypeIO.readUUID(input);
                            PluginMessageSendingVelocity.reloadPlayerData(uuid2, server);
                            break;
                        case 0x10:
                            UUID requestUUID = DataTypeIO.readUUID(input);
                            int requestType = input.readByte();
                            switch (requestType) {
                                case 0:
                                    PluginMessageSendingVelocity.respondPlayerListRequest(requestUUID, server);
                                    break;
                                default:
                                    break;
                            }
                        case 0x15:
                            UUID playerUUID = DataTypeIO.readUUID(input);
                            String command = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            Optional<Player> optPlayer = proxyServer.getPlayer(playerUUID);
                            if (optPlayer.isPresent()) {
                                if (!proxyServer.getCommandManager().executeImmediatelyAsync(optPlayer.get(), command).get()) {
                                    PluginMessageSendingVelocity.executeBackendCommand(playerUUID, command, server);
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            pluginMessageHandlingExecutor.submit(() -> {
                for (RegisteredServer eachServer : getServer().getAllServers()) {
                    if (!eachServer.getServerInfo().getName().equals(senderServer) && eachServer.getPlayersConnected().size() > 0) {
                        eachServer.sendPluginMessage(ICChannelIdentifier.INSTANCE, event.getData());
                        pluginMessagesCounter.incrementAndGet();
                    }
                }
            });
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onVelocityChatFirst(PlayerChatEvent event) {
        if (chatEventPostOrder.equals(PostOrder.FIRST)) {
            handleVelocityChat(event);
        }
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onVelocityChatEarly(PlayerChatEvent event) {
        if (chatEventPostOrder.equals(PostOrder.EARLY)) {
            handleVelocityChat(event);
        }
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onVelocityChatNormal(PlayerChatEvent event) {
        if (chatEventPostOrder.equals(PostOrder.NORMAL)) {
            handleVelocityChat(event);
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void onVelocityChatLate(PlayerChatEvent event) {
        if (chatEventPostOrder.equals(PostOrder.LATE)) {
            handleVelocityChat(event);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onVelocityChatLast(PlayerChatEvent event) {
        if (chatEventPostOrder.equals(PostOrder.LAST)) {
            handleVelocityChat(event);
        }
    }

    public void handleVelocityChat(PlayerChatEvent event) {
        handleChat(event.getPlayer(), event.getMessage(), event.getResult().isAllowed(), newMessage -> {
            try {
                Field messageField = event.getClass().getDeclaredField("message");
                messageField.setAccessible(true);
                messageField.set(event, newMessage);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }, newMessage -> event.setResult(ChatResult.message(newMessage)));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onCommandExecuteFirst(CommandExecuteEvent event) {
        if (chatEventPostOrder.equals(PostOrder.FIRST)) {
            handleCommandExecute(event);
        }
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onCommandExecuteEarly(CommandExecuteEvent event) {
        if (chatEventPostOrder.equals(PostOrder.EARLY)) {
            handleCommandExecute(event);
        }
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onCommandExecuteNormal(CommandExecuteEvent event) {
        if (chatEventPostOrder.equals(PostOrder.NORMAL)) {
            handleCommandExecute(event);
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void onCommandExecuteLate(CommandExecuteEvent event) {
        if (chatEventPostOrder.equals(PostOrder.LATE)) {
            handleCommandExecute(event);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onCommandExecuteLast(CommandExecuteEvent event) {
        if (chatEventPostOrder.equals(PostOrder.LAST)) {
            handleCommandExecute(event);
        }
    }

    public void handleCommandExecute(CommandExecuteEvent event) {
        CommandSource sender = event.getCommandSource();
        if (sender instanceof Player) {
            handleChat((Player) sender, event.getCommand(), event.getResult().isAllowed(), newMessage -> {
                try {
                    Field messageField = event.getClass().getDeclaredField("command");
                    messageField.setAccessible(true);
                    messageField.set(event, newMessage);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }, newMessage -> event.setResult(CommandExecuteEvent.CommandResult.command(newMessage)));
        }
    }

    private void handleChat(Player player, String eventMessage, boolean isAllowed, Consumer<String> setEventMessage, Consumer<String> setResult) {
        if (!isAllowed) {
            return;
        }

        if (!player.getCurrentServer().isPresent()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        String message = eventMessage;

        String newMessage = Registry.ID_PATTERN.matcher(eventMessage).replaceAll("");
        setEventMessage.accept(newMessage);

        boolean hasInteractiveChat = false;
        BackendInteractiveChatData data = serverInteractiveChatInfo.get(player.getCurrentServer().get().getServerInfo().getName());
        if (data != null) {
            hasInteractiveChat = data.hasInteractiveChat();
        }

        boolean usage = false;
        outer:
        for (List<ICPlaceholder> serverPlaceholders : placeholderList.values()) {
            for (ICPlaceholder icplaceholder : serverPlaceholders) {
                if (icplaceholder.getKeyword().matcher(message).find()) {
                    usage = true;
                    break outer;
                }
            }
        }

        if (newMessage.startsWith("/")) {
            if (usage && hasInteractiveChat) {
                for (String parsecommand : InteractiveChatVelocity.parseCommands) {
                    if (newMessage.matches(parsecommand)) {
                        String command = newMessage.trim();
                        outer:
                        for (List<ICPlaceholder> serverPlaceholders : placeholderList.values()) {
                            for (ICPlaceholder icplaceholder : serverPlaceholders) {
                                Pattern placeholder = icplaceholder.getKeyword();
                                Matcher matcher = placeholder.matcher(command);
                                if (matcher.find()) {
                                    String uuidmatch = "<cmd=" + uuid + ":" + Registry.ID_ESCAPE_PATTERN.matcher(command.substring(matcher.start(), matcher.end())).replaceAll("\\>") + ":>";
                                    command = command.substring(0, matcher.start()) + uuidmatch + command.substring(matcher.end());
                                    if (command.length() > 256) {
                                        command = command.substring(0, 256);
                                    }
                                    newMessage = command;
                                    break outer;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            if (usage && InteractiveChatVelocity.useAccurateSenderFinder && hasInteractiveChat) {
                outer:
                for (List<ICPlaceholder> serverPlaceholders : placeholderList.values()) {
                    for (ICPlaceholder icplaceholder : serverPlaceholders) {
                        Pattern placeholder = icplaceholder.getKeyword();
                        Matcher matcher = placeholder.matcher(message);
                        if (matcher.find()) {
                            String uuidmatch = "<chat=" + uuid + ":" + Registry.ID_ESCAPE_PATTERN.matcher(message.substring(matcher.start(), matcher.end())).replaceAll("\\>") + ":>";
                            message = message.substring(0, matcher.start()) + uuidmatch + message.substring(matcher.end());
                            if (message.length() > 256) {
                                message = message.substring(0, 256);
                            }
                            newMessage = message;
                            break outer;
                        }
                    }
                }
            }
            setEventMessage.accept(message);

            String finalNewMessage = newMessage;
            proxyServer.getScheduler().buildTask(plugin, () -> {
                Set<ForwardedMessageData> messages = forwardedMessages.get(uuid);
                if (messages != null && messages.removeIf(each -> each.getMessage().equals(finalNewMessage))) {
                    try {
                        PluginMessageSendingVelocity.sendMessagePair(uuid, finalNewMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).delay(100, TimeUnit.MILLISECONDS).schedule();
        }

        if (!eventMessage.equals(newMessage)) {
            IdentifiedKey key = player.getIdentifiedKey();
            if (key != null && key.getKeyRevision().compareTo(IdentifiedKey.Revision.LINKED_V2) >= 0) {
                try {
                    PluginMessageSendingVelocity.forwardSignedChatEventChange(uuid, eventMessage, newMessage, System.currentTimeMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                setResult.accept(newMessage);
            }
        }
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer to = player.getCurrentServer().get().getServer();
        UUID uuid = player.getUniqueId();
        if (!placeholderList.containsKey(to.getServerInfo().getName())) {
            try {
                PluginMessageSendingVelocity.requestPlaceholderList(to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PluginMessageSendingVelocity.sendPlayerListData();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        long universalTime = playerCooldownManager.getPlayerUniversalLastTimestamp(uuid);
        if (universalTime >= 0) {
            try {
                PluginMessageSendingVelocity.sendPlayerUniversalCooldown(to, uuid, universalTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<ICPlaceholder> placeholders = placeholderList.get(to.getServerInfo().getName());
        if (placeholders != null) {
            for (ICPlaceholder placeholder : placeholders) {
                long placeholderTime = playerCooldownManager.getPlayerPlaceholderLastTimestamp(uuid, placeholder.getInternalId());
                if (placeholderTime >= 0) {
                    try {
                        PluginMessageSendingVelocity.sendPlayerPlaceholderCooldown(to, uuid, placeholder, placeholderTime);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        proxyServer.getScheduler().buildTask(plugin, () -> {
            PluginMessageSendingVelocity.sendDelayAndScheme();
        }).schedule();
        proxyServer.getScheduler().buildTask(plugin, () -> {
            if (event.getPlayer().getUsername().equals("LOOHP") || event.getPlayer().getUsername().equals("AppLEshakE")) {
                sendMessage(event.getPlayer(), Component.text(TextColor.GOLD + "InteractiveChat (Velocity) " + getDescription().getVersion() + " is running!"));
            }
        }).delay(100, TimeUnit.MILLISECONDS).schedule();

        if (!handleProxyMessage) return;
        VelocityServerConnection serverConnection = ((ConnectedPlayer) event.getPlayer()).getConnectedServer();
        ChannelPipeline pipeline = serverConnection.ensureConnected().getChannel().pipeline();

        pipeline.addBefore(Connections.HANDLER, "interactivechat_interceptor", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
                try {
                    if (obj instanceof LegacyChatPacket) {
                        LegacyChatPacket packet = (LegacyChatPacket) obj;
                        UUID uuid = player.getUniqueId();
                        String message = packet.getMessage();
                        byte position = packet.getType();
                        if ((position != 2 && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.CHAT)) || (position == 2 && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.ACTIONBAR))) {
                            if ((position == 0 || position == 1) && uuid != null && message != null) {
                                Set<ForwardedMessageData> list = forwardedMessages.get(uuid);
                                if (list != null) {
                                    list.add(new ForwardedMessageData(message, ChatPacketType.LEGACY_CHAT, System.currentTimeMillis()));
                                }
                            }
                        }
                    } else if (obj instanceof SessionPlayerChatPacket && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.CHAT)) {
                        SessionPlayerChatPacket packet = (SessionPlayerChatPacket) obj;
                        Set<ForwardedMessageData> list = forwardedMessages.get(player.getUniqueId());
                        if (list != null) {
                            list.add(new ForwardedMessageData(packet.getMessage(), ChatPacketType.CLIENT_CHAT, System.currentTimeMillis()));
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

        forwardedMessages.put(player.getUniqueId(), Collections.newSetFromMap(new ConcurrentHashMap<>()));

        if (player.hasPermission("interactivechat.backendinfo")) {
            String proxyVersion = getDescription().getVersion();
            for (BackendInteractiveChatData data : serverInteractiveChatInfo.values()) {
                if (data.isOnline() && data.getProtocolVersion() != Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION) {
                    String msg = TextColor.RED + "[InteractiveChat] Warning: Backend Server " + data.getServer() + " is not running a version of InteractiveChat which has the same plugin messaging protocol version as the proxy!";
                    HoverEvent<Component> hoverComponent = Component.text(TextColor.YELLOW + "Proxy Version: " + proxyVersion + " (" + Registry.PLUGIN_MESSAGING_PROTOCOL_VERSION + ")\n" + TextColor.RED + data.getServer() + " Version: " + data.getVersion() + " (" + data.getProtocolVersion() + ")").asHoverEvent();
                    TextComponent text = Component.text(msg).hoverEvent(hoverComponent);
                    sendMessage(player, text);
                    sendMessage(getServer().getConsoleCommandSource(), text);
                }
            }
        }

        if (!handleProxyMessage) return;
        ConnectedPlayer userConnection = (ConnectedPlayer) player;
        ChannelPipeline pipeline = userConnection.getConnection().getChannel().pipeline();

        pipeline.addBefore(Connections.HANDLER, "interactivechat_interceptor", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
                try {
                    if (obj instanceof LegacyChatPacket) {
                        LegacyChatPacket packet = (LegacyChatPacket) obj;
                        String message = packet.getMessage();
                        byte position = packet.getType();
                        if ((position != 2 && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.CHAT)) || (position == 2 && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.ACTIONBAR))) {
                            if ((position == 0 || position == 1) && message != null) {
                                if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                    message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                    if (Registry.ID_PATTERN.matcher(message).find()) {
                                        message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                    }
                                    packet.setMessage(message);
                                } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                    messageForwardingHandler.processMessage(player.getUniqueId(), message, position, ChatPacketType.LEGACY_CHAT, packet);
                                    return;
                                }
                            }
                        }
                    } else if (obj instanceof SystemChatPacket && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.SYSTEM_CHAT)) {
                        SystemChatPacket packet = (SystemChatPacket) obj;
                        ComponentHolder holder = packet.getComponent();
                        String message = holder == null ? null : holder.getJson();
                        int position = packet.getType().getId();
                        if (message != null) {
                            if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                if (Registry.ID_PATTERN.matcher(message).find()) {
                                    message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                }
                                Field messageField = packet.getClass().getDeclaredField("component");
                                messageField.setAccessible(true);
                                messageField.set(packet, new ComponentHolder(getProtocolVersion(holder), message));
                            } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                messageForwardingHandler.processMessage(player.getUniqueId(), message, position, ChatPacketType.SYSTEM_CHAT, packet);
                                return;
                            }
                        }
                    /*
                    } else if (obj instanceof ServerPlayerChat) {
                        ServerPlayerChat packet = (ServerPlayerChat) obj;
                        Field unsignedContentField = packet.getClass().getDeclaredField("unsignedComponent");
                        unsignedContentField.setAccessible(true);
                        Object unsignedContent = unsignedContentField.get(packet);
                        String message = unsignedContent == null ? null : NativeAdventureConverter.jsonStringFromNative(unsignedContent);
                        int position = packet.getType();
                        if (message != null) {
                            if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                if (Registry.ID_PATTERN.matcher(message).find()) {
                                    message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                }
                                unsignedContentField.set(packet, new ComponentHolder(getProtocolVersion(holder), message));
                            } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                messageForwardingHandler.processMessage(player.getUniqueId(), message, position, ChatPacketType.PLAYER_CHAT, packet);
                                return;
                            }
                        }
                    */
                    } else if (obj instanceof LegacyTitlePacket && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.TITLE)) {
                        LegacyTitlePacket packet = (LegacyTitlePacket) obj;
                        ComponentHolder holder = packet.getComponent();
                        String message = holder == null ? null : holder.getJson();
                        if (packet.getAction().equals(ActionType.SET_TITLE) || packet.getAction().equals(ActionType.SET_SUBTITLE) || packet.getAction().equals(ActionType.SET_ACTION_BAR)) {
                            if (message != null) {
                                if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                    message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                    if (Registry.ID_PATTERN.matcher(message).find()) {
                                        message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                    }
                                    packet.setComponent(new ComponentHolder(getProtocolVersion(holder), message));
                                } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                    messageForwardingHandler.processMessage(player.getUniqueId(), message, 0, ChatPacketType.LEGACY_TITLE, packet);
                                    return;
                                }
                            }
                        }
                    } else if (obj instanceof TitleTextPacket && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.TITLE)) {
                        TitleTextPacket packet = (TitleTextPacket) obj;
                        ComponentHolder holder = packet.getComponent();
                        String message = holder == null ? null : holder.getJson();
                        if (message != null) {
                            if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                if (Registry.ID_PATTERN.matcher(message).find()) {
                                    message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                }
                                packet.setComponent(new ComponentHolder(getProtocolVersion(holder), message));
                            } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                messageForwardingHandler.processMessage(player.getUniqueId(), message, 0, ChatPacketType.TITLE, packet);
                                return;
                            }
                        }
                    } else if (obj instanceof TitleSubtitlePacket && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.TITLE)) {
                        TitleSubtitlePacket packet = (TitleSubtitlePacket) obj;
                        ComponentHolder holder = packet.getComponent();
                        String message = holder == null ? null : holder.getJson();
                        if (message != null) {
                            if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                if (Registry.ID_PATTERN.matcher(message).find()) {
                                    message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                }
                                packet.setComponent(new ComponentHolder(getProtocolVersion(holder), message));
                            } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                messageForwardingHandler.processMessage(player.getUniqueId(), message, 0, ChatPacketType.SUBTITLE, packet);
                                return;
                            }
                        }
                    } else if (obj instanceof TitleActionbarPacket && proxyHandlePacketTypesType.hasType(ProxyHandlePacketTypes.ProxyPacketType.ACTIONBAR)) {
                        TitleActionbarPacket packet = (TitleActionbarPacket) obj;
                        ComponentHolder holder = packet.getComponent();
                        String message = holder == null ? null : holder.getJson();
                        if (message != null) {
                            if ((message = CustomStringUtils.unescapeUnicode(message)).contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                if (Registry.ID_PATTERN.matcher(message).find()) {
                                    message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                }
                                packet.setComponent(new ComponentHolder(getProtocolVersion(holder), message));
                            } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
                                messageForwardingHandler.processMessage(player.getUniqueId(), message, 0, ChatPacketType.ACTION_BAR, packet);
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
    public void onLeave(DisconnectEvent event) {
        forwardedMessages.remove(event.getPlayer().getUniqueId());
        proxyServer.getScheduler().buildTask(plugin, () -> {
            try {
                PluginMessageSendingVelocity.sendPlayerListData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).delay(1000, TimeUnit.MILLISECONDS).schedule();
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