package com.loohp.interactivechat.listeners.packet;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.utils.ChatColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Everything within the packet package (but not in sub-packages) contain platform independent code.
 * The sub-packages (packetevents and protocollib) contain the platform dependent code which will reference the methods within these classes.
 */
public class ClientSettingsHandler {

    /**
     * The ProtocolLib and PacketEvents independent packet listener classes call on this.
     * This is a simple method that checks the client settings for colour code configs.
     * It notifies the player about the disabling of colours, and sends a warning message concerning it.
     *
     * @param colorSettings Client side colour settings - whether colours are enabled or not
     * @param originalColorSettings Server side original colour settings. Compared with colorSettings with their original setting when they first joined.
     * @param player Player in question.
     */
    public static void handlePacketReceiving(boolean colorSettings, boolean originalColorSettings, Player player) {
        if (originalColorSettings && !colorSettings) {
            sendMessageLater(player, ConfigManager.getConfig().getString("Messages.ColorsDisabled"));
        } else if (!originalColorSettings && colorSettings) {
            sendMessageLater(player, ConfigManager.getConfig().getString("Messages.ColorsReEnabled"));
        }
    }

    private static void sendMessageLater(Player player, String message) {
        InteractiveChat.plugin.getScheduler().runLater((task) -> {
            player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', message));
        }, 5);
    }
}
