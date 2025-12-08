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

package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.platform.protocollib.utils.WrappedChatComponentUtils;
import com.loohp.interactivechat.utils.ChatComponentType;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.CustomArrayUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.loohp.interactivechat.listeners.packet.MessagePacketHandler.*;

public class ProtocolLibOutMessagePacketHelper {
    protected static final Map<PacketType, MessagePacketHandler<PacketEvent, PacketContainer>> PACKET_HANDLERS = new HashMap<>();

    static {
        initializeMessagePacketHandlers();
    }

    private static void initializeMessagePacketHandlers() {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
            PACKET_HANDLERS.put(PacketType.Play.Server.DISGUISED_CHAT, new MessagePacketHandler<>(event -> {
                return InteractiveChat.chatListener;
            }, (packet, player) -> {
                ChatComponentType type = ChatComponentType.IChatBaseComponent;
                int field = 0;
                Component component = type.convertFrom(packet.getModifier().read(field), player);
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
            PACKET_HANDLERS.put(PacketType.Play.Server.CHAT, new MessagePacketHandler<>(event -> {
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                    return InteractiveChat.chatListener;
                }
                int position;
                if (event.getPacket().getIntegers().size() > 0) {
                    if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_21_5)) {
                        position = event.getPacket().getIntegers().read(1);
                    } else {
                        position = event.getPacket().getIntegers().read(0);
                    }
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
            }, (packet, player) -> {
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19_3)) {
                    ChatComponentType type = ChatComponentType.IChatBaseComponent;
                    int field = InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_21_5) ? 5 : 4;
                    Component component;
                    Object unsignedContent = packet.getModifier().read(field);
                    if (unsignedContent == null) {
                        component = PlainTextComponentSerializer.plainText().deserialize(ModernChatSigningUtils.getSignedMessageBodyAContent(packet.getModifier().read(field - 1)));
                    } else {
                        component = type.convertFrom(unsignedContent, player);
                    }
                    return new PacketAccessorResult(component, type, field, false);
                } else {
                    if (packet.getModifier().getField(0).getType().getName().equalsIgnoreCase("net.minecraft.network.chat.PlayerChatMessage")) {
                        Object playerChatMessage = packet.getModifier().read(0);
                        Optional<?> unsignedContent = ModernChatSigningUtils.getUnsignedContent(playerChatMessage);
                        ChatComponentType type = ChatComponentType.IChatBaseComponent;
                        Component component;
                        if (unsignedContent.isPresent()) {
                            component = type.convertFrom(unsignedContent.get(), player);
                        } else {
                            Object signedContent = ModernChatSigningUtils.getSignedContent(playerChatMessage);
                            if (signedContent instanceof String) {
                                component = PlainTextComponentSerializer.plainText().deserialize((String) signedContent);
                            } else {
                                component = type.convertFrom(signedContent, player);
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
                                            Component component = t.convertFrom(packet.getModifier().read(i), player);
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
                            component = type.convertFrom(unsignedContent.get(), player);
                        } else {
                            component = type.convertFrom(packet.getModifier().read(positionOfSignedContent), player);
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
                PACKET_HANDLERS.put(PacketType.Play.Server.CHAT_PREVIEW, new MessagePacketHandler<>(event -> {
                    return InteractiveChat.chatListener;
                }, event -> {
                    return ICPlayerFactory.getICPlayer(event.getPlayer());
                }, (packet, player) -> {
                    Component component = null;
                    ChatComponentType type = null;
                    int field = -1;
                    search:
                    for (ChatComponentType t : ChatComponentType.byPriority()) {
                        for (int i = 1; i < packet.getModifier().size(); i++) {
                            if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                                try {
                                    component = t.convertFrom(packet.getModifier().read(i), player);
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

        PacketContainer chatPacket = ProtocolLibrary.getProtocolManager().createPacket(InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19) ? PacketType.Play.Server.SYSTEM_CHAT : PacketType.Play.Server.CHAT);
        List<String> matches = ChatComponentType.byPriority().stream().map(each -> each.getMatchingRegex()).collect(Collectors.toList());
        for (chatFieldsSize = 1; chatFieldsSize < chatPacket.getModifier().size(); chatFieldsSize++) {
            String clazz = chatPacket.getModifier().getField(chatFieldsSize).getType().getName();
            if (matches.stream().noneMatch(each -> clazz.matches(each))) {
                chatFieldsSize--;
                break;
            }
        }

        int finalChatFieldsSize = chatFieldsSize;
        PACKET_HANDLERS.put(InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19) ? PacketType.Play.Server.SYSTEM_CHAT : PacketType.Play.Server.CHAT, new MessagePacketHandler<>(event -> {
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
        }, (packet, player) -> {
            Component component = null;
            ChatComponentType type = null;
            int field = -1;
            search:
            for (ChatComponentType t : ChatComponentType.byPriority()) {
                for (int i = 0; i < packet.getModifier().size(); i++) {
                    Object obj = packet.getModifier().read(i);
                    if (!CustomArrayUtils.allNull(obj) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                        try {
                            component = t.convertFrom(obj, player);
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
                            WrappedChatComponent wcc = WrappedChatComponentUtils.fromJson(json);
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
            MessagePacketHandler<PacketEvent, PacketContainer> modernTitleHandler = new MessagePacketHandler<>(event -> {
                return InteractiveChat.titleListener;
            }, (packet, player) -> {
                Component component = null;
                ChatComponentType type = null;
                int field = -1;
                search:
                for (ChatComponentType t : ChatComponentType.byPriority()) {
                    for (int i = 0; i < packet.getModifier().size(); i++) {
                        if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                            try {
                                component = t.convertFrom(packet.getModifier().read(i), player);
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
            PACKET_HANDLERS.put(PacketType.Play.Server.TITLE, new MessagePacketHandler<>(event -> {
                TitleAction type = event.getPacket().getTitleActions().read(0);
                if (type == null || type.equals(TitleAction.RESET) || type.equals(TitleAction.CLEAR) || type.equals(TitleAction.TIMES)) {
                    return false;
                }
                return InteractiveChat.titleListener;
            }, (packet, player) -> {
                Component component = null;
                ChatComponentType type = null;
                int field = -1;
                search:
                for (ChatComponentType t : ChatComponentType.byPriority()) {
                    for (int i = 0; i < packet.getModifier().size(); i++) {
                        if (!CustomArrayUtils.allNull(packet.getModifier().read(i)) && packet.getModifier().getField(i).getType().getName().matches(t.getMatchingRegex())) {
                            try {
                                component = t.convertFrom(packet.getModifier().read(i), player);
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
    }

}