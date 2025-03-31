package com.loohp.interactivechat.listeners.packet;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.utils.ModernChatSigningUtils;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

/**
 * Reducing code duplication... one class at a time.
 * (where possible, of course!)
 */
public class RedispatchedSignPacketHandler {

    public static void redispatchCommand(Player player, String command) {
        InteractiveChat.plugin.getScheduler().runNextTick(task -> {
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
            InteractiveChat.plugin.getScheduler().runNextTick(task -> player.acceptConversationInput(message));
            if (!InteractiveChat.skipDetectSpamRateWhenDispatchingUnsignedPackets) {
                InteractiveChat.plugin.getScheduler().runAsync(task -> ModernChatSigningUtils.detectRateSpam(player, message));
            }
        } else {
            InteractiveChat.plugin.getScheduler().runAsync(task -> {
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
