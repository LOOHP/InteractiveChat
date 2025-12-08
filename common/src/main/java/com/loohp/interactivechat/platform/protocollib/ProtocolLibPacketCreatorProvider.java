package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.PlatformPacketCreatorProvider;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayServerCustomChatCompletionPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayServerSystemChatPacket;
import com.loohp.interactivechat.platform.protocollib.packets.ProtocolLibPlayServerTabCompletePacket;
import com.loohp.interactivechat.platform.protocollib.utils.WrappedChatComponentUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

public class ProtocolLibPacketCreatorProvider implements PlatformPacketCreatorProvider<PacketContainer> {

    private final ProtocolLibPlatform platform;

    public ProtocolLibPacketCreatorProvider(ProtocolLibPlatform platform) {
        this.platform = platform;
    }

    @Override
    public ProtocolLibPlayServerTabCompletePacket createPlayServerTabCompletePacket(int id, Object suggestions) {
        return new ProtocolLibPlayServerTabCompletePacket(NMS.getInstance().createCommandSuggestionPacket(id, suggestions));
    }

    @Override
    public ProtocolLibPlayServerCustomChatCompletionPacket createPlayServerCustomChatCompletionPacket(CustomTabCompletionAction action, List<String> list) {
        return new ProtocolLibPlayServerCustomChatCompletionPacket(NMS.getInstance().createCustomTabCompletionPacket(action, list));
    }

    @Override
    public ProtocolLibPlayServerSystemChatPacket createPlayServerSystemChatPacket(UUID uuid, Component component) {
        String json = InteractiveChatComponentSerializer.gson().serialize(component);
        PacketContainer packet;
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
            packet = platform.getProtocolManager().createPacket(PacketType.Play.Server.SYSTEM_CHAT);

            if (packet.getStrings().size() > 0) {
                packet.getStrings().write(0, json);
            } else {
                packet.getChatComponents().write(0, WrappedChatComponentUtils.fromJson(json));
            }

            if (packet.getBooleans().size() > 0) {
                packet.getBooleans().write(0, false);
            } else {
                packet.getIntegers().write(0, 1);
            }
        } else {
            packet = platform.getProtocolManager().createPacket(PacketType.Play.Server.CHAT);

            if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
                packet.getChatTypes().write(0, EnumWrappers.ChatType.SYSTEM);
            } else {
                packet.getBytes().write(0, (byte) 1);
            }

            packet.getChatComponents().write(0, WrappedChatComponentUtils.fromJson(json));
            if (packet.getUUIDs().size() > 0) {
                packet.getUUIDs().write(0, uuid);
            }
        }
        return new ProtocolLibPlayServerSystemChatPacket(packet);
    }
}
