package com.loohp.interactivechat.listeners.packet;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class OutTabCompletePacketHandler {

    public static final AtomicReference<Map<String, UUID>> playerNames = new AtomicReference<>(Collections.emptyMap());

    public static void init() {
        schedulePlayerNamesUpdate();
    }

    private static void schedulePlayerNamesUpdate() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            if (InteractiveChat.useTooltipOnTab) {
                Map<String, UUID> names = new HashMap<>();
                for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                    addPlayerNames(names, player);
                }
                Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> playerNames.set(names));
            }
        }, 0, 100);
    }

    private static void addPlayerNames(Map<String, UUID> playernames, ICPlayer player) {
        playernames.put(ChatColorUtils.stripColor(player.getName()), player.getUniqueId());
        if (!player.getName().equals(player.getDisplayName())) {
            playernames.put(ChatColorUtils.stripColor(player.getDisplayName()), player.getUniqueId());
        }
        List<String> names = InteractiveChatAPI.getNicknames(player.getUniqueId());
        for (String name : names) {
            playernames.put(ChatColorUtils.stripColor(name), player.getUniqueId());
        }
    }

    public static ICPlayer findICPlayer(String text) {
        for (Map.Entry<String, UUID> entry : playerNames.get().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(text)) {
                return ICPlayerFactory.getICPlayer(entry.getValue());
            }
        }
        return null;
    }

    public static Component createComponent(ICPlayer icplayer, Player tabCompleter) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(icplayer, InteractiveChat.tabTooltip)));
        if (!PlayerUtils.canChatColor(tabCompleter)) {
            component = ComponentStyling.stripColor(component);
        }
        return component;
    }
}
