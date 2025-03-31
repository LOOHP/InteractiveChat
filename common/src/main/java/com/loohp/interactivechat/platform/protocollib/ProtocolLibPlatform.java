package com.loohp.interactivechat.platform.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.bungeemessaging.ServerPingListener;
import com.loohp.interactivechat.listeners.packet.listeners.PLibClientSettingPacket;
import com.loohp.interactivechat.listeners.packet.listeners.PLibOutMessagePacket;
import com.loohp.interactivechat.listeners.packet.listeners.PLibOutTabCompletePacket;
import com.loohp.interactivechat.listeners.packet.listeners.PLibRedispatchSignedPacket;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import com.loohp.interactivechat.platform.ProtocolPlatform;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.loohp.interactivechat.InteractiveChat.version;

public class ProtocolLibPlatform implements ProtocolPlatform {

    @Nullable
    public static ProtocolManager protocolManager;

    @Override
    public boolean hasChatSigning() {
        return MinecraftVersion.getCurrentVersion().compareTo(new MinecraftVersion(1, 19, 1)) >= 0;
    }

    @Override
    public int getProtocolVersion(Player player) {
        return protocolManager.getProtocolVersion(player);
    }

    @Override
    public Plugin getRegisteredPlugin() {
        return InteractiveChat.plugin;
    }

    @Override
    public void initialize() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        PLibOutMessagePacket.messageListeners();
        if (version.isNewerOrEqualTo(MCVersion.V1_19)) {
            PLibRedispatchSignedPacket.packetListener();
        }

        if (!version.isLegacy()) {
            PLibOutTabCompletePacket.tabCompleteListener();
        }

        PLibClientSettingPacket.clientSettingsListener();
    }

    @Override
    public void onBungeecordModeEnabled() {
        ServerPingListener.listen();
    }

    @Override
    public void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list) {
        PacketContainer chatCompletionPacket1 = NMS.getInstance().createCustomTabCompletionPacket(action, list);
        protocolManager.sendServerPacket(player, chatCompletionPacket1);
    }

    @Override
    public void sendUnprocessedChatMessage(CommandSender sender, UUID uuid, Component component) {
        String json = InteractiveChatComponentSerializer.gson().serialize(component);
        if (sender instanceof Player) {
            PacketContainer packet;
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_19)) {
                packet = protocolManager.createPacket(PacketType.Play.Server.SYSTEM_CHAT);

                if (packet.getStrings().size() > 0) {
                    packet.getStrings().write(0, json);
                } else {
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
                }

                if (packet.getBooleans().size() > 0) {
                    packet.getBooleans().write(0, false);
                } else {
                    packet.getIntegers().write(0, 1);
                }
            } else {
                packet = protocolManager.createPacket(PacketType.Play.Server.CHAT);

                if (!InteractiveChat.version.isLegacy() || InteractiveChat.version.equals(MCVersion.V1_12)) {
                    packet.getChatTypes().write(0, EnumWrappers.ChatType.SYSTEM);
                } else {
                    packet.getBytes().write(0, (byte) 1);
                }

                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
                if (packet.getUUIDs().size() > 0) {
                    packet.getUUIDs().write(0, uuid);
                }
            }

            protocolManager.sendServerPacket((Player) sender, packet, false);
        } else {
            sender.spigot().sendMessage(ComponentSerializer.parse(json));
        }
    }
}
