package com.loohp.interactivechat.platform.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTabComplete;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.listeners.packet.packetevents.PEOutMessagePacket;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketEventsPlatform implements ProtocolPlatform {
    @Override
    public void initialise() {
        new PEOutMessagePacket().messageListeners();
    }

    @Override
    public void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list) {
        List<WrapperPlayServerTabComplete.CommandMatch> suggestions = new ArrayList<>();
        for (String cmd : list) {
            suggestions.add(new WrapperPlayServerTabComplete.CommandMatch(cmd, null));
        }

        WrapperPlayServerTabComplete packet = new WrapperPlayServerTabComplete(
                null,
                new WrapperPlayServerTabComplete.CommandRange(0, list.size()),
                suggestions
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    @Override
    public void sendUnprocessedChatMessage(CommandSender sender, UUID uuid, Component component) {
        if (sender instanceof Player) {
            PacketWrapper<?> packet;
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
                packet = new WrapperPlayServerSystemChatMessage(false, component);
            } else {
                ChatMessage message;
                if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
                    message = new ChatMessage_v1_16(component, ChatTypes.SYSTEM, uuid);
                } else {
                    message = new ChatMessageLegacy(component, ChatTypes.SYSTEM);
                }

                packet = new WrapperPlayServerChatMessage(message);
            }

            PacketEvents.getAPI().getPlayerManager().sendPacket(sender, packet);
        } else {
            String json = InteractiveChatComponentSerializer.gson().serialize(component);
            sender.spigot().sendMessage(ComponentSerializer.parse(json));
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean hasChatSigning() {
        return InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19);
    }
}
