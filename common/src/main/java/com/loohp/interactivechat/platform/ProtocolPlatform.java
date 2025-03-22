package com.loohp.interactivechat.platform;

import com.loohp.interactivechat.objectholders.CustomTabCompletionAction;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public interface ProtocolPlatform {

    Plugin getRegisteredPlugin();

    void initialize();

    void onBungeecordModeEnabled();

    void sendTabCompletionPacket(Player player, CustomTabCompletionAction action, List<String> list);

    void sendUnprocessedChatMessage(CommandSender sender, UUID uuid, Component component);

    boolean hasChatSigning();

    int getProtocolVersion(Player player);

}
