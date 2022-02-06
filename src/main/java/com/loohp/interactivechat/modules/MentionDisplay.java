package com.loohp.interactivechat.modules;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.events.PlayerMentionPlayerEvent;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.SoundUtils;
import com.loohp.interactivechat.utils.TitleUtils;
import com.loohp.interactivechat.utils.ToastUtils;
import com.loohp.interactivechat.utils.bossbar.BossBarUpdater;
import com.loohp.interactivechat.utils.bossbar.BossBarUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MentionDisplay {

    private static final ItemStack WRITABLE_BOOK = XMaterial.WRITABLE_BOOK.parseItem();

    public static Component process(Component component, Player beenpinged, ICPlayer sender, long unix, boolean async) {
        Optional<MentionPair> optPair;
        synchronized (InteractiveChat.mentionPair) {
            optPair = InteractiveChat.mentionPair.stream().filter(each -> each.getReciever().equals(beenpinged.getUniqueId())).findFirst();
        }
        if (optPair.isPresent()) {
            MentionPair pair = optPair.get();
            if (pair.getSender().equals(sender.getUniqueId())) {
                Player receiver = beenpinged;

                String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.MentionedTitle")));
                String subtitle = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionSubtitle")));
                String actionbar = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.KnownPlayerMentionActionbar")));
                String toast = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.MentionToast")));
                String bossBarText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.MentionBossBar.Text")));
                String bossBarColorName = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.MentionBossBar.Color")));
                String bossBarOverlayName = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(sender, ConfigManager.getConfig().getString("Chat.MentionBossBar.Overlay")));

                Optional<BossBar> optBossBar;
                if (bossBarText.isEmpty()) {
                    optBossBar = Optional.empty();
                } else {
                    optBossBar = Optional.of(BossBar.bossBar(LegacyComponentSerializer.legacySection().deserialize(bossBarText), 1, Color.valueOf(bossBarColorName.toUpperCase()), Overlay.valueOf(bossBarOverlayName.toUpperCase())));
                }

                String settings = ConfigManager.getConfig().getString("Chat.MentionedSound");
                Sound sound = null;
                float volume = 3.0F;
                float pitch = 1.0F;

                String[] settingsArgs = settings.split(":");
                if (settingsArgs.length == 3) {
                    settings = settingsArgs[0];
                    try {
                        volume = Float.parseFloat(settingsArgs[1]);
                    } catch (Exception ignore) {
                    }
                    try {
                        pitch = Float.parseFloat(settingsArgs[2]);
                    } catch (Exception ignore) {
                    }
                } else if (settingsArgs.length > 0) {
                    settings = settingsArgs[0];
                }

                sound = SoundUtils.parseSound(settings);
                if (sound == null) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Invalid Sound: " + settings);
                }

                PlayerMentionPlayerEvent mentionEvent = new PlayerMentionPlayerEvent(async, receiver, sender.getUniqueId(), title, subtitle, actionbar, toast, optBossBar, sound, false);
                Bukkit.getPluginManager().callEvent(mentionEvent);
                if (!mentionEvent.isCancelled()) {
                    title = mentionEvent.getTitle();
                    subtitle = mentionEvent.getSubtitle();
                    actionbar = mentionEvent.getActionbar();

                    int time = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionedTitleDuration") * 20);
                    TitleUtils.sendTitle(receiver, title, subtitle, actionbar, 10, Math.max(time, 1), 20);
                    if (sound != null) {
                        receiver.playSound(receiver.getLocation(), sound, volume, pitch);
                    }
                    if (!mentionEvent.getToast().isEmpty() && InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_12)) {
                        ToastUtils.mention(sender, receiver, toast, WRITABLE_BOOK.clone());
                    }

                    int bossBarTime = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionBossBar.Duration") * 20);
                    int bossBarRemoveDelay = (int) Math.round(ConfigManager.getConfig().getDouble("Chat.MentionBossBar.RemoveDelay") * 20);
                    if (mentionEvent.getBossBar().isPresent() && !InteractiveChat.version.isOld()) {
                        BossBarUpdater updater = BossBarUpdater.update(mentionEvent.getBossBar().get(), receiver);
                        BossBarUtils.countdownBossBar(updater, Math.max(bossBarTime, 1), Math.max(bossBarRemoveDelay, 0));
                    }

                    List<String> names = new ArrayList<>();
                    names.add(ChatColorUtils.stripColor(receiver.getName()));
                    if (InteractiveChat.useBukkitDisplayName && !ChatColorUtils.stripColor(receiver.getName()).equals(ChatColorUtils.stripColor(receiver.getDisplayName()))) {
                        names.add(ChatColorUtils.stripColor(receiver.getDisplayName()));
                    }
                    List<String> list = InteractiveChatAPI.getNicknames(receiver.getUniqueId());
                    for (String name : list) {
                        names.add(ChatColorUtils.stripColor(name));
                    }
                    if (!InteractiveChat.disableHere) {
                        names.add("here");
                    }
                    if (!InteractiveChat.disableEveryone) {
                        names.add("everyone");
                    }

                    for (String name : names) {
                        component = processPlayer(InteractiveChat.mentionPrefix + name, receiver, sender, component, unix);
                    }

                    pair.remove();
                }
            }
        }
        return component;
    }

    public static Component processPlayer(String placeholder, Player receiver, ICPlayer sender, Component component, long unix) {
        String replacementText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHighlight.replace("{MentionedPlayer}", placeholder));
        Component replacement = LegacyComponentSerializer.legacySection().deserialize(replacementText);
        String hoverText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Reciever}", receiver.getDisplayName()).replace("{Receiver}", receiver.getDisplayName()));
        HoverEvent<Component> hoverEvent = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
        replacement = replacement.hoverEvent(hoverEvent);
        return ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(placeholder), true, replacement);
    }

}
