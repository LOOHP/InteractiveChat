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
import com.loohp.interactivechat.proxy.bungee.InteractiveChatBungee;
import com.loohp.interactivechat.proxy.objectholders.BackendInteractiveChatData;
import com.loohp.interactivechat.proxy.objectholders.ProxyMessageForwardingHandler;
import com.loohp.interactivechat.proxy.objectholders.ProxyPlayerCooldownManager;
import com.loohp.interactivechat.proxy.velocity.metrics.Charts;
import com.loohp.interactivechat.proxy.velocity.metrics.Metrics;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.DataTypeIO;
import com.loohp.interactivechat.utils.NativeAdventureConverter;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InteractiveChatVelocity {

    public static final int BSTATS_PLUGIN_ID = 10945;
    public static final String CONFIG_ID = "config";
    private static final boolean filtersAdded = false;
    private static final Map<Integer, byte[]> incomming = new HashMap<>();
    private static final Map<Integer, Boolean> permissionChecks = new ConcurrentHashMap<>();
    public static InteractiveChatVelocity plugin = null;
    public static AtomicLong pluginMessagesCounter = new AtomicLong(0);
    public static List<String> parseCommands = new ArrayList<>();
    public static Map<String, List<ICPlaceholder>> placeholderList = new HashMap<>();
    public static boolean useAccurateSenderFinder = true;
    public static boolean tagEveryIdentifiableMessage = false;
    public static int delay = 200;
    public static ProxyPlayerCooldownManager playerCooldownManager;
    public static ProxyServer proxyServer;
    protected static Random random = new Random();
    protected static Map<UUID, Map<String, Long>> forwardedMessages = new ConcurrentHashMap<>();
    protected static Map<String, BackendInteractiveChatData> serverInteractiveChatInfo = new ConcurrentHashMap<>();
    private static ProxyMessageForwardingHandler messageForwardingHandler;
    private static ThreadPoolExecutor pluginMessageHandlingExecutor;

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
                        PluginMessageSendingVelocity.requestMessageProcess(player, server.getServer(), component, info.getId());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).delay(delay + 50, TimeUnit.MILLISECONDS).schedule();
        }, (info, component) -> {
            Chat chatPacket = new Chat(component + "<QUxSRUFEWVBST0NFU1NFRA==>", info.getPosition(), null);
            Optional<Player> optplayer = getServer().getPlayer(info.getPlayer());
            if (optplayer.isPresent()) {
                ConnectedPlayer userConnection = (ConnectedPlayer) optplayer.get();
                userConnection.getConnection().getChannel().write(chatPacket);
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
            for (Map<String, Long> list : forwardedMessages.values()) {
                Iterator<Long> itr = list.values().iterator();
                while (itr.hasNext()) {
                    long time = itr.next();
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
        int packetId = in.readShort();

        if (!Registry.PROXY_PASSTHROUGH_RELAY_PACKETS.contains(packetId)) {
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
                                boolean isBulitIn = input.readBoolean();
                                if (isBulitIn) {
                                    String keyword = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String name = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String description = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    String permission = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                                    long cooldown = input.readLong();
                                    list.add(new BuiltInPlaceholder(Pattern.compile(keyword), name, description, permission, cooldown));
                                } else {
                                    int customNo = input.readInt();
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

                                    list.add(new CustomPlaceholder(customNo, parseplayer, Pattern.compile(placeholder), parseKeyword, cooldown, new CustomPlaceholderHoverEvent(hoverEnabled, hoverText), new CustomPlaceholderClickEvent(clickEnabled, clickEnabled ? ClickEventAction.valueOf(clickAction) : null, clickValue), new CustomPlaceholderReplaceText(replaceEnabled, replaceText), name, description));
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
                    }
                } catch (IOException e) {
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

    @Subscribe(order = PostOrder.LATE)
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

        String newMessage = event.getMessage();

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
                                    event.setResult(ChatResult.message(command));
                                    break outer;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            if (usage && InteractiveChatBungee.useAccurateSenderFinder && hasInteractiveChat) {
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
                            event.setResult(ChatResult.message(message));
                            break outer;
                        }
                    }
                }
            }

            proxyServer.getScheduler().buildTask(plugin, () -> {
                Map<String, Long> messages = forwardedMessages.get(uuid);
                if (messages != null && messages.remove(newMessage) != null) {
                    try {
                        PluginMessageSendingVelocity.sendMessagePair(uuid, newMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).delay(100, TimeUnit.MILLISECONDS).schedule();
        }
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        Player player = event.getPlayer();

        VelocityServerConnection serverConnection = ((ConnectedPlayer) event.getPlayer()).getConnectedServer();
        ChannelPipeline pipeline = serverConnection.ensureConnected().getChannel().pipeline();

        pipeline.addBefore(Connections.HANDLER, "interactivechat_interceptor", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
                try {
                    if (obj instanceof Chat) {
                        Chat packet = (Chat) obj;
                        UUID uuid = player.getUniqueId();
                        String message = packet.getMessage();
                        byte position = packet.getType();
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

    @Subscribe
    public void onPlayerConnected(PostLoginEvent event) {
        if (!filtersAdded) {
            addFilters();
        }

        Player player = event.getPlayer();

        forwardedMessages.put(player.getUniqueId(), new ConcurrentHashMap<>());

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

        ConnectedPlayer userConnection = (ConnectedPlayer) player;
        ChannelPipeline pipeline = userConnection.getConnection().getChannel().pipeline();

        pipeline.addBefore(Connections.HANDLER, "interactivechat_interceptor", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object obj, ChannelPromise channelPromise) throws Exception {
                try {
                    if (obj instanceof Chat) {
                        Chat packet = (Chat) obj;
                        String message = packet.getMessage();
                        byte position = packet.getType();
                        if ((position == 0 || position == 1) && message != null) {
                            if (message.contains("<QUxSRUFEWVBST0NFU1NFRA==>")) {
                                message = message.replace("<QUxSRUFEWVBST0NFU1NFRA==>", "");
                                if (Registry.ID_PATTERN.matcher(message).find()) {
                                    message = Registry.ID_PATTERN.matcher(message).replaceAll("").trim();
                                }
                                packet.setMessage(message);
                            } else if (player.getCurrentServer().isPresent() && hasInteractiveChat(player.getCurrentServer().get().getServer())) {
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

    @Subscribe
    public void onSwitch(ServerConnectedEvent event) {
        RegisteredServer to = event.getServer();
        Player player = event.getPlayer();
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
            try {
                PluginMessageSendingVelocity.sendDelayAndScheme();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).schedule();
        proxyServer.getScheduler().buildTask(plugin, () -> {
            if (event.getPlayer().getUsername().equals("LOOHP") || event.getPlayer().getUsername().equals("AppLEskakE")) {
                sendMessage(event.getPlayer(), Component.text(TextColor.GOLD + "InteractiveChat (Velocity) " + getDescription().getVersion() + " is running!"));
            }
        }).delay(100, TimeUnit.MILLISECONDS).schedule();
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