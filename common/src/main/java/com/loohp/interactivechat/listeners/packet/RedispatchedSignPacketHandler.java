package com.loohp.interactivechat.listeners.packet;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

/**
 * Reducing code duplication... one class at a time.
 * (where possible, of course!)
 */
public class RedispatchedSignPacketHandler {

    public static void redispatchCommand(Player player, String command) {
        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
            PlayerUtils.dispatchCommandAsPlayer(player, command);
            if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                ModernChatSigningUtils.detectRateSpam(player, command);
            }
        });
    }

    /**
     * Must check if ModernChatSigningUtils.isChatMessageIllegal is false!
     * @param player Player to dispatch the message as.
     * @param message Message to "re-dispatch"
     */
    public static void redispatchChatMessage(Player player, String message) {
        if (player.isConversing()) {
            Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.acceptConversationInput(message));
            if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> ModernChatSigningUtils.detectRateSpam(player, message));
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
                try {
                    Object decorated = ModernChatSigningUtils.getChatDecorator(player, LegacyComponentSerializer.legacySection().deserialize(message)).get();
                    PlayerUtils.chatAsPlayer(player, message, decorated);
                    if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                        ModernChatSigningUtils.detectRateSpam(player, message);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
