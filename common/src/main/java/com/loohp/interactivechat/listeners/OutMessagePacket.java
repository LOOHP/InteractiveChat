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

package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.api.events.PreChatPacketSendEvent;
import com.loohp.interactivechat.api.events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;
import com.loohp.interactivechat.hooks.triton.TritonHook;
import com.loohp.interactivechat.hooks.venturechat.VentureChatInjection;
import com.loohp.interactivechat.modules.CommandsDisplay;
import com.loohp.interactivechat.modules.CustomPlaceholderDisplay;
import com.loohp.interactivechat.modules.EnderchestDisplay;
import com.loohp.interactivechat.modules.HoverableItemDisplay;
import com.loohp.interactivechat.modules.InventoryDisplay;
import com.loohp.interactivechat.modules.ItemDisplay;
import com.loohp.interactivechat.modules.MentionDisplay;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.modules.ProcessAccurateSender;
import com.loohp.interactivechat.modules.ProcessCommands;
import com.loohp.interactivechat.modules.SenderFinder;
import com.loohp.interactivechat.objectholders.AsyncChatSendingExecutor;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.ProcessSenderResult;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.ComponentFont;
import com.loohp.interactivechat.utils.ComponentModernizing;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.JsonUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OutMessagePacket implements Listener {

    public static final UUID UUID_NIL = new UUID(0, 0);

    private static final Map<PacketType, PacketHandler> PACKET_HANDLERS = new HashMap<>();
    private static final Executor SCHEDULING_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("InteractiveChat Async ChatMessage Process Scheduling Thread").build());
    private static final AsyncChatSendingExecutor SERVICE;

    static {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
            PACKET_HANDLERS.put(PacketType.Play.Server.DISGUISED_CHAT, new PacketHandler(event -> {
                return InteractiveChat.chatListener;
            }, packet -> {
                ChatComponentType type = ChatComponentType.IChatBaseComponent;
                int field = 0;
                Component component = type.convertFrom(packet.getModifier().read(field));
                return new PacketAccessorResult(component, type, field, false);
            }, (packet, component, type, field, sender) -> {
                boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
                String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
                boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
                packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
            }));
        }

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
            PACKET_HANDLERS.put(PacketType.Play.Server.CHAT, new PacketHandler(event -> {
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                    return InteractiveChat.chatListener;
                }
                int position;
                if (event.getPacket().getIntegers().size() > 0) {
                    position = event.getPacket().getIntegers().read(0);
                } else {
                    Object chatType = event.getPacket().getModifier().read(1);
                    position = ModernChatSigningUtils.getChatMessageType(chatType);
                }
                if (position == 2) {
                    return InteractiveChat.titleListener;
                } else {
                    return InteractiveChat.chatListener;
                }
            }, event -> {
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                    UUID uuid = event.getPacket().getUUIDs().read(0);
                    if (uuid != null) {
                        return ICPlayerFactory.getICPlayer(uuid);
                    }
                }
                return null;
            }, packet -> {
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                    ChatComponentType type = ChatComponentType.IChatBaseComponent;
                    int field = 4;
                    Component component;
                    Object unsignedContent = packet.getModifier().read(field);
                    if (unsignedContent == null) {
                        component = PlainTextComponentSerializer.plainText().deserialize(ModernChatSigningUtils.getSignedMessageBodyAContent(packet.getModifier().read(3)));
                    } else {
                        component = type.convertFrom(unsignedContent);
                    }
                    return new PacketAccessorResult(component, type, field, false);
                } else {
                    if (packet.getModifier().getField(0).getType().getName().equalsIgnoreCase("net.minecraft.network.chat.PlayerChatMessage")) {
                        Object playerChatMessage = packet.getModifier().read(0);
                        Optional<?> unsignedContent = ModernChatSigningUtils.getUnsignedContent(playerChatMessage);
                        ChatComponentType type = ChatComponentType.IChatBaseComponent;
                        Component component;
                        if (unsignedContent.isPresent()) {
                            component = type.convertFrom(unsignedContent.get());
                        } else {
                            Object signedContent = ModernChatSigningUtils.getSignedContent(playerChatMessage);
                            if (signedContent instanceof String) {
                                component = PlainTextComponentSerializer.plainText().deserialize((String) signedContent);
                            } else {
                                component = type.convertFrom(signedContent);
                            }
                        }
                        return new PacketAccessorResult(component, type, Integer.MIN_VALUE, false);
                    } else {
                        int positionOfSignedContent = 0;
                        if (!packet.getModifier().getField(0).getName().equals("a")) {
                            for (int i = 0; i < packet.getModifier().size(); i++) {
                                if (packet.getModifier().getField(i).getName().equals("a")) {
                                    positionOfSignedContent = i;
                                    break;
                                }
                            }
                            if (positionOfSignedContent == 0) {
                                throw new RuntimeException("Unable to find index of field \"a\"");
                            }
                            for (ChatComponentType t : ChatComponentType.byPriority()) {
                                for (int i = 0; i < positionOfSignedContent; i++) {
                                    if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                                        try {
                                            Component component = t.convertFrom(packet.getModifier().read(i));
                                            return new PacketAccessorResult(component, t, i, true);
                                        } catch (Throwable e) {
                                            System.err.println(t.toString(packet.getModifier().read(i)));
                                            e.printStackTrace();
                                            return null;
                                        }
                                    }
                                }
                            }
                        }
                        int field = positionOfSignedContent + 1;
                        Optional<?> unsignedContent = (Optional<?>) packet.getModifier().read(field);
                        ChatComponentType type = ChatComponentType.IChatBaseComponent;
                        Component component;
                        if (unsignedContent.isPresent()) {
                            component = type.convertFrom(unsignedContent.get());
                        } else {
                            component = type.convertFrom(packet.getModifier().read(positionOfSignedContent));
                        }
                        return new PacketAccessorResult(component, type, field, false);
                    }
                }
            }, (packet, component, type, field, sender) -> {
                boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
                String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
                boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                    if (sender != null) {
                        packet.getUUIDs().write(0, sender);
                    }
                    packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                } else {
                    if (field == Integer.MIN_VALUE) {
                        Object chatMessage = ModernChatSigningUtils.withUnsignedContent(packet.getModifier().read(0), type.convertTo(component, legacyRGB));
                        packet.getModifier().write(0, chatMessage);
                    } else {
                        if (packet.getModifier().getField(field).getType().equals(Optional.class)) {
                            packet.getModifier().write(field, Optional.of(type.convertTo(component, legacyRGB)));
                        } else {
                            packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                        }
                        if (sender == null) {
                            sender = UUID_NIL;
                        }
                        if (packet.getUUIDs().size() > 0) {
                            packet.getUUIDs().write(0, sender);
                        }
                    }
                }
                return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
            }));

            if (InteractiveChat.version.isOlderOrEqualTo(MCVersion.V1_19)) {
                //noinspection deprecation
                PACKET_HANDLERS.put(PacketType.Play.Server.CHAT_PREVIEW, new PacketHandler(event -> {
                    return InteractiveChat.chatListener;
                }, event -> {
                    return ICPlayerFactory.getICPlayer(event.getPlayer());
                }, packet -> {
                    Component component = null;
                    ChatComponentType type = null;
                    int field = -1;
                    search:
                    for (ChatComponentType t : ChatComponentType.byPriority()) {
                        for (int i = 1; i < packet.getModifier().size(); i++) {
                            if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                                try {
                                    component = t.convertFrom(packet.getModifier().read(i));
                                } catch (Throwable e) {
                                    System.err.println(t.toString(packet.getModifier().read(i)));
                                    e.printStackTrace();
                                    break search;
                                }
                                field = i;
                                type = t;
                                break search;
                            }
                        }
                    }
                    return new PacketAccessorResult(component, type, field, true);
                }, (packet, component, type, field, sender) -> {
                    if (InteractiveChat.chatPreviewRemoveClickAndHover) {
                        component = ComponentStyling.stripEvents(component);
                    }
                    boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
                    String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
                    boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
                    packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                    if (sender == null) {
                        sender = UUID_NIL;
                    }
                    return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
                }));
            }
        }

        int chatFieldsSize;

        PacketContainer chatPacket = InteractiveChat.protocolManager.createPacket(InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19) ? PacketType.Play.Server.SYSTEM_CHAT : PacketType.Play.Server.CHAT);
        List<String> matches = ChatComponentType.byPriority().stream().map(each -> each.getMatchingRegex()).collect(Collectors.toList());
        for (chatFieldsSize = 1; chatFieldsSize < chatPacket.getModifier().size(); chatFieldsSize++) {
            String clazz = chatPacket.getModifier().getField(chatFieldsSize).getType().getName();
            if (matches.stream().noneMatch(each -> clazz.matches(each))) {
                chatFieldsSize--;
                break;
            }
        }

        int finalChatFieldsSize = chatFieldsSize;
        PACKET_HANDLERS.put(InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19) ? PacketType.Play.Server.SYSTEM_CHAT : PacketType.Play.Server.CHAT, new PacketHandler(event -> {
            if (event.getPacketType().equals(PacketType.Play.Server.CHAT)) {
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_12)) {
                    ChatType type = event.getPacket().getChatTypes().read(0);
                    if (type == null || type.equals(ChatType.GAME_INFO)) {
                        return InteractiveChat.titleListener;
                    }
                } else {
                    byte type = event.getPacket().getBytes().read(0);
                    if (type == (byte) 2) {
                        return InteractiveChat.titleListener;
                    }
                }
                return InteractiveChat.chatListener;
            } else {
                int position;
                if (event.getPacket().getBooleans().size() > 0) {
                    position = event.getPacket().getBooleans().read(0) ? 2 : 0;
                } else {
                    position = event.getPacket().getIntegers().read(0);
                }
                if (position == 2) {
                    return InteractiveChat.titleListener;
                } else {
                    return InteractiveChat.chatListener;
                }
            }
        }, packet -> {
            Component component = null;
            ChatComponentType type = null;
            int field = -1;
            search:
            for (ChatComponentType t : ChatComponentType.byPriority()) {
                for (int i = 0; i < packet.getModifier().size(); i++) {
                    Object obj = packet.getModifier().read(i);
                    if (!CustomArrayUtils.allNull(obj) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                        try {
                            component = t.convertFrom(obj);
                        } catch (Throwable e) {
                            System.err.println(t.toString(obj));
                            e.printStackTrace();
                            break search;
                        }
                        field = i;
                        type = t;
                        break search;
                    }
                }
            }
            return new PacketAccessorResult(component, type, field, false);
        }, (packet, component, type, field, sender) -> {
            boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
            String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
            boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
            if (type.canHandle(component)) {
                try {
                    packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                } catch (Throwable e) {
                    try {
                        if (packet.getChatComponents().size() > 0) {
                            WrappedChatComponent wcc = WrappedChatComponent.fromJson(json);
                            for (int i = 0; i < finalChatFieldsSize; i++) {
                                packet.getModifier().write(i, null);
                            }
                            packet.getChatComponents().write(0, wcc);
                        } else if (packet.getStrings().size() > 0) {
                            for (int i = 0; i < finalChatFieldsSize; i++) {
                                packet.getModifier().write(i, null);
                            }
                            packet.getStrings().write(0, json);
                        }
                    } catch (Throwable ignore) {
                    }
                }
            }
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                if (sender != null) {
                    if (packet.getUUIDs().size() > 0) {
                        packet.getUUIDs().write(0, sender);
                    }
                }
            } else {
                if (sender == null) {
                    sender = UUID_NIL;
                }
                if (packet.getUUIDs().size() > 0) {
                    packet.getUUIDs().write(0, sender);
                }
            }
            return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
        }));

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
            PacketHandler modernTitleHandler = new PacketHandler(event -> {
                return InteractiveChat.titleListener;
            }, packet -> {
                Component component = null;
                ChatComponentType type = null;
                int field = -1;
                search:
                for (ChatComponentType t : ChatComponentType.byPriority()) {
                    for (int i = 0; i < packet.getModifier().size(); i++) {
                        if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                            try {
                                component = t.convertFrom(packet.getModifier().read(i));
                            } catch (Throwable e) {
                                System.err.println(t.toString(packet.getModifier().read(i)));
                                e.printStackTrace();
                                break search;
                            }
                            field = i;
                            type = t;
                            break search;
                        }
                    }
                }
                return new PacketAccessorResult(component, type, field, false);
            }, (packet, component, type, field, sender) -> {
                boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
                String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
                boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
                packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                if (sender == null) {
                    sender = UUID_NIL;
                }
                return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
            });
            PACKET_HANDLERS.put(PacketType.Play.Server.SET_TITLE_TEXT, modernTitleHandler);
            PACKET_HANDLERS.put(PacketType.Play.Server.SET_SUBTITLE_TEXT, modernTitleHandler);
            PACKET_HANDLERS.put(PacketType.Play.Server.SET_ACTION_BAR_TEXT, modernTitleHandler);
        } else {
            //noinspection deprecation
            PACKET_HANDLERS.put(PacketType.Play.Server.TITLE, new PacketHandler(event -> {
                TitleAction type = event.getPacket().getTitleActions().read(0);
                if (type == null || type.equals(TitleAction.RESET) || type.equals(TitleAction.CLEAR) || type.equals(TitleAction.TIMES)) {
                    return false;
                }
                return InteractiveChat.titleListener;
            }, packet -> {
                Component component = null;
                ChatComponentType type = null;
                int field = -1;
                search:
                for (ChatComponentType t : ChatComponentType.byPriority()) {
                    for (int i = 0; i < packet.getModifier().size(); i++) {
                        if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                            try {
                                component = t.convertFrom(packet.getModifier().read(i));
                            } catch (Throwable e) {
                                System.err.println(t.toString(packet.getModifier().read(i)));
                                e.printStackTrace();
                                break search;
                            }
                            field = i;
                            type = t;
                            break search;
                        }
                    }
                }
                return new PacketAccessorResult(component, type, field, false);
            }, (packet, component, type, field, sender) -> {
                boolean legacyRGB = InteractiveChat.version.isLegacyRGB();
                String json = legacyRGB ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
                boolean longerThanMaxLength = InteractiveChat.sendOriginalIfTooLong && json.length() > InteractiveChat.packetStringMaxLength;
                packet.getModifier().write(field, type.convertTo(component, legacyRGB));
                if (sender == null) {
                    sender = UUID_NIL;
                }
                return new PacketWriterResult(longerThanMaxLength, json.length(), sender);
            }));
        }

        SERVICE = new AsyncChatSendingExecutor(() -> (long) (InteractiveChat.bungeecordMode ? InteractiveChat.remoteDelay : 0) + 2000, 5000);
    }

    public static AsyncChatSendingExecutor getAsyncChatSendingExecutor() {
        return SERVICE;
    }

    public static void messageListeners() {
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PACKET_HANDLERS.keySet())) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    if (event.isPlayerTemporary() || !event.isFiltered() || event.isCancelled()) {
                        return;
                    }
                    PacketHandler packetHandler = PACKET_HANDLERS.get(event.getPacketType());
                    if (!packetHandler.getPreFilter().test(event)) {
                        return;
                    }
                    if (InteractiveChat.ventureChatHook) {
                        VentureChatInjection.firePacketListener(event);
                    }
                    InteractiveChat.messagesCounter.getAndIncrement();

                    PacketContainer packet = event.getPacket();
                    boolean readOnly = event.isReadOnly();
                    event.setReadOnly(false);
                    event.setCancelled(true);
                    event.setReadOnly(readOnly);
                    Player receiver = event.getPlayer();
                    UUID messageUUID = UUID.randomUUID();
                    ICPlayer determinedSender = packetHandler.getDeterminedSenderFunction().apply(event);
                    SCHEDULING_SERVICE.execute(() -> {
                        SERVICE.execute(() -> {
                            processPacket(receiver, determinedSender, packet, messageUUID, event.isFiltered(), packetHandler);
                        }, receiver, messageUUID);
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void processPacket(Player receiver, ICPlayer determinedSender, PacketContainer packet, UUID messageUUID, boolean isFiltered, PacketHandler packetHandler) {
        PacketContainer originalPacket = packet.shallowClone();
        try {
            if (packetHandler.getAccessor() == null) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            PacketAccessorResult packetAccessorResult = packetHandler.getAccessor().apply(packet);
            Component component = packetAccessorResult.getComponent();
            ChatComponentType type = packetAccessorResult.getType();
            int field = packetAccessorResult.getField();

            if ((field < 0 && field != Integer.MIN_VALUE) || type == null || component == null) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            component = ComponentModernizing.modernize(component);
            String legacyText = LegacyComponentSerializer.legacySection().serializeOr(component, "");
            try {
                if (legacyText.equals("") || InteractiveChat.messageToIgnore.stream().anyMatch(each -> legacyText.matches(each))) {
                    SERVICE.send(packet, receiver, messageUUID);
                    return;
                }
            } catch (Exception e) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            if (InteractiveChat.version.isOld() && JsonUtils.containsKey(InteractiveChatComponentSerializer.gson().serialize(component), "translate")) {
                SERVICE.send(packet, receiver, messageUUID);
                return;
            }

            Optional<ICPlayer> sender = Optional.ofNullable(determinedSender);
            String rawMessageKey = PlainTextComponentSerializer.plainText().serializeOr(component, "");
            InteractiveChat.keyTime.putIfAbsent(rawMessageKey, System.currentTimeMillis());
            Long timeKey = InteractiveChat.keyTime.get(rawMessageKey);
            long unix = timeKey == null ? System.currentTimeMillis() : timeKey;
            ProcessSenderResult commandSender = ProcessCommands.process(component);
            if (!sender.isPresent()) {
                if (commandSender.getSender() != null) {
                    ICPlayer icplayer = ICPlayerFactory.getICPlayer(commandSender.getSender());
                    if (icplayer != null) {
                        sender = Optional.of(icplayer);
                    }
                }
            }

            ProcessSenderResult chatSender = null;
            if (!sender.isPresent()) {
                if (InteractiveChat.useAccurateSenderFinder) {
                    chatSender = ProcessAccurateSender.process(component);
                    if (chatSender.getSender() != null) {
                        ICPlayer icplayer = ICPlayerFactory.getICPlayer(chatSender.getSender());
                        if (icplayer != null) {
                            sender = Optional.of(icplayer);
                        }
                    }
                }
            }

            if (!sender.isPresent() && !InteractiveChat.useAccurateSenderFinder) {
                sender = SenderFinder.getSender(component, rawMessageKey);
            }

            if (sender.isPresent() && !sender.get().isLocal()) {
                if (isFiltered) {
                    InteractiveChat.plugin.getScheduler().runLaterAsync((task) -> {
                        SERVICE.execute(() -> {
                            processPacket(receiver, determinedSender, packet, messageUUID, false, packetHandler);
                        }, receiver, messageUUID);
                    }, (int) Math.ceil((double) InteractiveChat.remoteDelay / 50) + InteractiveChat.extraProxiedPacketProcessingDelay);
                    return;
                }
            }

            component = commandSender.getComponent();
            if (chatSender != null) {
                component = chatSender.getComponent();
            }
            if (sender.isPresent()) {
                InteractiveChat.keyPlayer.put(rawMessageKey, sender.get());
            }

            component = ComponentReplacing.replace(component, Registry.ID_PATTERN.pattern(), Registry.ID_PATTERN_REPLACEMENT);

            UUID preEventSenderUUID = sender.isPresent() ? sender.get().getUniqueId() : null;
            PrePacketComponentProcessEvent preEvent = new PrePacketComponentProcessEvent(!Bukkit.isPrimaryThread(), receiver, component, preEventSenderUUID);
            Bukkit.getPluginManager().callEvent(preEvent);
            if (preEvent.getSender() != null) {
                Player newsender = Bukkit.getPlayer(preEvent.getSender());
                if (newsender != null) {
                    sender = Optional.of(ICPlayerFactory.getICPlayer(newsender));
                }
            }
            component = preEvent.getComponent();

            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
                if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
                    component = ComponentFont.parseFont(component);
                }
            }

            if (InteractiveChat.translateHoverableItems && InteractiveChat.itemGUI) {
                component = HoverableItemDisplay.process(component, receiver);
            }

            if (InteractiveChat.usePlayerName) {
                component = PlayernameDisplay.process(component, sender, receiver, unix);
            }

            if (InteractiveChat.allowMention && sender.isPresent()) {
                PlayerData data = InteractiveChat.playerDataManager.getPlayerData(receiver);
                if (data == null || !data.isMentionDisabled()) {
                    component = MentionDisplay.process(component, receiver, sender.get(), unix, true);
                }
            }
            component = ComponentReplacing.replace(component, Registry.MENTION_TAG_CONVERTER.getReversePattern().pattern(), true, (result, components) -> {
                return LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHighlightOthers)).replaceText(TextReplacementConfig.builder().matchLiteral("{MentionedPlayer}").replacement(PlainTextComponentSerializer.plainText().deserialize(result.group(2))).build());
            });

            component = CustomPlaceholderDisplay.process(component, sender, receiver, InteractiveChat.placeholderList.values(), unix);

            if (InteractiveChat.useInventory) {
                component = InventoryDisplay.process(component, sender, receiver, packetAccessorResult.isPreview(), unix);
            }

            if (InteractiveChat.useEnder) {
                component = EnderchestDisplay.process(component, sender, receiver, packetAccessorResult.isPreview(), unix);
            }

            if (InteractiveChat.clickableCommands) {
                component = CommandsDisplay.process(component);
            }

            if (InteractiveChat.useItem) {
                component = ItemDisplay.process(component, sender, receiver, packetAccessorResult.isPreview(), unix);
            }

            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags) {
                if (!sender.isPresent() || (sender.isPresent() && PlayerUtils.hasPermission(sender.get().getUniqueId(), "interactivechat.customfont.translate", true, 250))) {
                    component = ComponentFont.parseFont(component);
                }
            }

            if (!PlayerUtils.canChatColor(receiver)) {
                component = ComponentStyling.stripColor(component);
            }

            if (InteractiveChat.tritonHook) {
                component = TritonHook.parseLanguageChat(sender.map(p -> p.getUniqueId()).orElse(null), component);
            }

            PostPacketComponentProcessEvent postEvent = new PostPacketComponentProcessEvent(true, receiver, component, preEventSenderUUID);
            Bukkit.getPluginManager().callEvent(postEvent);
            component = postEvent.getComponent();

            PacketWriterResult packetWriterResult = packetHandler.getWriter().apply(packet, component, type, field, sender.map(each -> each.getUniqueId()).orElse(null));
            boolean longerThanMaxLength = packetWriterResult.isTooLong();
            UUID postEventSenderUUID = packetWriterResult.getSender();
            int jsonLength = packetWriterResult.getJsonLength();

            PreChatPacketSendEvent sendEvent = new PreChatPacketSendEvent(true, receiver, packet, component, postEventSenderUUID, originalPacket, InteractiveChat.sendOriginalIfTooLong, longerThanMaxLength);
            Bukkit.getPluginManager().callEvent(sendEvent);

            InteractiveChat.plugin.getScheduler().runLater((task) -> {
                InteractiveChat.keyTime.remove(rawMessageKey);
                InteractiveChat.keyPlayer.remove(rawMessageKey);
            }, 10);

            if (sendEvent.isCancelled()) {
                if (sendEvent.sendOriginalIfCancelled()) {
                    if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Cancelled a chat packet bounded to " + receiver.getName() + " that is " + jsonLength + " characters long (Longer than maximum allowed in a chat packet), sending original unmodified message instead! [THIS IS NOT A BUG]");
                    }
                    PacketContainer originalPacketModified = sendEvent.getOriginal();
                    SERVICE.send(originalPacketModified, receiver, messageUUID);
                    return;
                }
                if (longerThanMaxLength && InteractiveChat.cancelledMessage) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Cancelled a chat packet bounded to " + receiver.getName() + " that is " + jsonLength + " characters long (Longer than maximum allowed in a chat packet) [THIS IS NOT A BUG]");
                }
                SERVICE.discard(receiver.getUniqueId(), messageUUID);
                return;
            }

            SERVICE.send(packet, receiver, messageUUID);
        } catch (Exception e) {
            e.printStackTrace();
            SERVICE.send(originalPacket, receiver, messageUUID);
        }
    }

    public static class PacketHandler {

        private static final Function<PacketEvent, ICPlayer> UNDETERMINED_SENDER = event -> null;

        private Predicate<PacketEvent> preFilter;
        private Function<PacketEvent, ICPlayer> determinedSenderFunction;
        private PacketAccessor accessor;
        private PacketWriter writer;

        public PacketHandler(Predicate<PacketEvent> preFilter, Function<PacketEvent, ICPlayer> determinedSenderFunction, PacketAccessor accessor, PacketWriter writer) {
            this.preFilter = preFilter;
            this.determinedSenderFunction = determinedSenderFunction;
            this.accessor = accessor;
            this.writer = writer;
        }

        public PacketHandler(Predicate<PacketEvent> preFilter, PacketAccessor accessor, PacketWriter writer) {
            this(preFilter, UNDETERMINED_SENDER, accessor, writer);
        }

        public Predicate<PacketEvent> getPreFilter() {
            return preFilter;
        }

        public Function<PacketEvent, ICPlayer> getDeterminedSenderFunction() {
            return determinedSenderFunction;
        }

        public PacketAccessor getAccessor() {
            return accessor;
        }

        public PacketWriter getWriter() {
            return writer;
        }

    }

    public static class PacketAccessorResult {

        private Component component;
        private ChatComponentType type;
        private int field;
        private boolean preview;

        public PacketAccessorResult(Component component, ChatComponentType type, int field, boolean preview) {
            this.component = component;
            this.type = type;
            this.field = field;
            this.preview = preview;
        }

        public Component getComponent() {
            return component;
        }

        public ChatComponentType getType() {
            return type;
        }

        public int getField() {
            return field;
        }

        public boolean isPreview() {
            return preview;
        }

    }

    public interface PacketAccessor extends Function<PacketContainer, PacketAccessorResult> {

        @Override
        PacketAccessorResult apply(PacketContainer packet);

    }

    public static class PacketWriterResult {

        private boolean tooLong;
        private int jsonLength;
        private UUID sender;

        public PacketWriterResult(boolean tooLong, int jsonLength, UUID sender) {
            this.tooLong = tooLong;
            this.jsonLength = jsonLength;
            this.sender = sender;
        }

        public boolean isTooLong() {
            return tooLong;
        }

        public int getJsonLength() {
            return jsonLength;
        }

        public UUID getSender() {
            return sender;
        }

    }

    public interface PacketWriter {

        PacketWriterResult apply(PacketContainer packet, Component component, ChatComponentType type, int field, UUID sender);

    }

}
