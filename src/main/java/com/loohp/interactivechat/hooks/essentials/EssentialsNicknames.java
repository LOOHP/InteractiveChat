package com.loohp.interactivechat.hooks.essentials;

import com.earth2me.essentials.Essentials;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import net.ess3.api.events.NickChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EssentialsNicknames implements Listener {

    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static final Map<UUID, List<String>> ESSENTIALS_NICK = new ConcurrentHashMap<>();
    private static Essentials essen;
    private static String prefix;

    public static void _init_() {
        essen = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        prefix = essen.getConfig().getString("nickname-prefix");

        InteractiveChatAPI.registerNicknameProvider(essen, uuid -> {
            if (InteractiveChat.useEssentialsNicknames) {
                List<String> names = ESSENTIALS_NICK.get(uuid);
                return names;
            } else {
                return EMPTY_LIST;
            }
        });

        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                loadNicks(player);
            }
        }, 100);
    }

    public static void loadNicks(Player player) {
        if (essen.getUser(player.getUniqueId()).getNickname() != null && !essen.getUser(player.getUniqueId()).getNickname().equals("")) {
            String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
            List<String> names = new ArrayList<>();
            names.add(prefix + essentialsNick);
            ESSENTIALS_NICK.put(player.getUniqueId(), names);
        }
    }

    @EventHandler
    public void onEssentialsReload(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/essentials reload")) {
            if (event.getPlayer().hasPermission("essentials.essentials")) {
                Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
                    prefix = essen.getConfig().getString("nickname-prefix");
                }, 40);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEssentialsNickChange(NickChangeEvent event) {
        try {
            List<String> names = new ArrayList<>();
            names.add(prefix + event.getValue());
            ESSENTIALS_NICK.put(event.getAffected().getBase().getUniqueId(), names);
        } catch (Exception ignore) {
        }
    }

    @EventHandler
    public void onEssentialsJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
            loadNicks(player);
        }, 100);
    }

    @EventHandler
    public void onEssentialsLeave(PlayerQuitEvent event) {
        ESSENTIALS_NICK.remove(event.getPlayer().getUniqueId());
    }

}
