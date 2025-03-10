package com.loohp.interactivechat.listeners.packet.packetevents;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.RedispatchedSignPacketHandler;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class PERedispatchSignedPacket implements PacketListener {

    private static final List<PacketTypeCommon> packetTypes = getPacketTypes();

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.SERVER_DATA)) {
            handleServerDataPacket(event);
        } else {
            if (shouldIgnoreEvent(event)) return;

            if (event.getPacketType().equals(PacketType.Play.Client.CHAT_MESSAGE)) {
                handleChatPacket(event);
            } else {
                handleChatCommandPacket(event);
            }
        }
    }

    private static boolean shouldIgnoreEvent(PacketSendEvent event) {
        return event.isCancelled() || !InteractiveChat.protocolPlatform.hasChatSigning() || !packetTypes.contains(event.getPacketType());
    }

    private static List<PacketTypeCommon> getPacketTypes() {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
            return Arrays.asList(
                    PacketType.Play.Client.CHAT_COMMAND,
                    PacketType.Play.Client.CHAT_COMMAND_UNSIGNED,
                    PacketType.Play.Client.CHAT_MESSAGE
            );
        } else {
            return Arrays.asList(
                    PacketType.Play.Client.CHAT_COMMAND,
                    PacketType.Play.Client.CHAT_MESSAGE
            );
        }
    }

    private static void handleChatPacket(PacketSendEvent event) {
        if (InteractiveChat.forceUnsignedChatPackets) {
            Player player = event.getPlayer();
            WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);

            String message = PlainTextComponentSerializer.plainText().serialize(packet.getMessage().getChatContent());
            if (message.startsWith("/")) {
                redispatchCommand(event, player, message);
            } else {
                redispatchChatMessage(event, player, message);
            }
        }
    }

    private static void handleChatCommandPacket(PacketSendEvent event) {
        Player player = event.getPlayer();

        if (InteractiveChat.forceUnsignedChatCommandPackets) {
            if (event.getPacketType().equals(PacketType.Play.Server.CHAT_MESSAGE)) {
                WrapperPlayServerChatMessage message = new WrapperPlayServerChatMessage(event);
                if (message.getMessage().getType() != ChatTypes.MSG_COMMAND) return;

                ChatMessage_v1_19 messageV119 = (ChatMessage_v1_19) message.getMessage();
                if (messageV119.getSignature() != null) {
                    String command = "/" + PlainTextComponentSerializer.plainText().serialize(messageV119.getChatContent());
                    redispatchCommand(event, player, command);
                }
            }
        }
    }

    private static void redispatchCommand(PacketSendEvent event, Player player, String command) {
        event.setCancelled(true);

        RedispatchedSignPacketHandler.redispatchCommand(player, command);
    }

    private static void redispatchChatMessage(PacketSendEvent event, Player player, String message) {
        if (!ModernChatSigningUtils.isChatMessageIllegal(message)) {
            event.setCancelled(true);

            RedispatchedSignPacketHandler.redispatchChatMessage(player, message);
        }
    }

    private static void handleServerDataPacket(PacketSendEvent event) {
        if (event.getPacketType().equals(PacketType.Play.Server.SERVER_DATA)) {
            WrapperPlayServerServerData packet = new WrapperPlayServerServerData(event);

            if (InteractiveChat.hideServerUnsignedStatus) packet.setEnforceSecureChat(true);
            event.markForReEncode(true);
        }
    }
}
