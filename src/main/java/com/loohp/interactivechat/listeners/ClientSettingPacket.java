package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.PlayerUtils.ColorSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientSettingPacket implements Listener {

    private static final Map<Player, Boolean> colorSettingsMap = new HashMap<>();

    public static ColorSettings getSettings(Player player) {
        Boolean settings = colorSettingsMap.get(player);
        return settings != null ? (settings ? ColorSettings.ON : ColorSettings.OFF) : ColorSettings.WAITING;
    }

    public static void clientSettingsListener() {
        Bukkit.getPluginManager().registerEvents(new ClientSettingPacket(), InteractiveChat.plugin);
        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Client.SETTINGS)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                //do nothing
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.SETTINGS)) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                boolean colorSettings = packet.getBooleans().read(0);
                ColorSettings originalColorSettings = getSettings(player);

                if ((originalColorSettings.equals(ColorSettings.WAITING) || originalColorSettings.equals(ColorSettings.ON)) && !colorSettings) {
                    Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ColorsDisabled"))), 5);
                }

                if (originalColorSettings.equals(ColorSettings.OFF) && colorSettings) {
                    Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', ConfigManager.getConfig().getString("Messages.ColorsReEnabled"))), 5);
                }

                colorSettingsMap.put(player, colorSettings);
            }
        });
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        colorSettingsMap.remove(event.getPlayer());
    }

}
