/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.modules;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.events.PlayerMentionPlayerEvent;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.Either;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.ComponentUtils;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.SoundUtils;
import com.loohp.interactivechat.utils.bossbar.BossBarUpdater;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MentionDisplay implements Listener {

    private static final ItemStack WRITABLE_BOOK = XMaterial.WRITABLE_BOOK.parseItem();

    public static void setup() {
        Bukkit.getPluginManager().registerEvents(new MentionDisplay(), InteractiveChat.plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        InteractiveChat.lastNonSilentMentionTime.put(event.getPlayer().getUniqueId(), new ConcurrentHashMap<>());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        InteractiveChat.lastNonSilentMentionTime.remove(event.getPlayer().getUniqueId());
    }

    public static Component process(Component component, Player receiver, ICPlayer sender, long unix, boolean async) {
        Optional<MentionPair> optPair;
        synchronized (InteractiveChat.mentionPair) {
            optPair = InteractiveChat.mentionPair.stream().filter(each -> each.getreceiver().equals(receiver.getUniqueId())).findFirst();
        }
        if (optPair.isPresent()) {
            MentionPair pair = optPair.get();
            if (pair.getSender().equals(sender.getUniqueId())) {
                Component title = PlaceholderParser.parse(sender, InteractiveChat.mentionTitle);
                Component subtitle = PlaceholderParser.parse(sender, InteractiveChat.mentionSubtitle);
                Component actionbar = PlaceholderParser.parse(sender, InteractiveChat.mentionActionbar);
                Component toast = PlaceholderParser.parse(sender, InteractiveChat.mentionToast);
                Component bossBarText = PlaceholderParser.parse(sender, InteractiveChat.mentionBossBarText);
                String bossBarColorName = InteractiveChat.mentionBossBarColorName;
                String bossBarOverlayName = InteractiveChat.mentionBossBarOverlayName;

                Optional<BossBar> optBossBar;
                if (ComponentUtils.isEmpty(bossBarText)) {
                    optBossBar = Optional.empty();
                } else {
                    optBossBar = Optional.of(BossBar.bossBar(bossBarText, 1, Color.valueOf(bossBarColorName.toUpperCase()), Overlay.valueOf(bossBarOverlayName.toUpperCase())));
                }

                String settings = InteractiveChat.mentionSound;
                Either<Sound, String> sound;
                float volume = 3.0F;
                float pitch = 1.0F;

                String[] settingsArgs = settings.split(":");
                if (settingsArgs.length >= 3) {
                    settings = String.join("", Arrays.copyOfRange(settingsArgs, 0, settingsArgs.length - 2)).toUpperCase();
                    try {
                        volume = Float.parseFloat(settingsArgs[settingsArgs.length - 2]);
                    } catch (Exception ignore) {
                    }
                    try {
                        pitch = Float.parseFloat(settingsArgs[settingsArgs.length - 1]);
                    } catch (Exception ignore) {
                    }
                } else {
                    settings = settings.toUpperCase();
                }

                Sound bukkitSound = SoundUtils.parseSound(settings);
                if (bukkitSound == null) {
                    settings = settings.toLowerCase();
                    if (!settings.contains(":")) {
                        settings = "minecraft:" + settings;
                    }
                    sound = Either.right(settings);
                } else {
                    sound = Either.left(bukkitSound);
                }

                boolean silent = false;
                Map<UUID, Long> lastMentionMapping = InteractiveChat.lastNonSilentMentionTime.get(receiver.getUniqueId());
                if (lastMentionMapping != null) {
                    Long lastMention = lastMentionMapping.get(sender.getUniqueId());
                    silent = lastMention != null && unix - lastMention < InteractiveChat.mentionCooldown;
                }
                PlayerMentionPlayerEvent mentionEvent = new PlayerMentionPlayerEvent(async, receiver, sender.getUniqueId(), title, subtitle, actionbar, toast, optBossBar, sound, silent, false);
                Bukkit.getPluginManager().callEvent(mentionEvent);
                if (!mentionEvent.isCancelled()) {
                    if (!mentionEvent.isSilent()) {
                        if (lastMentionMapping != null) {
                            lastMentionMapping.put(sender.getUniqueId(), unix);
                        }

                        Component titleComponent = mentionEvent.getTitle();
                        Component subtitleComponent = mentionEvent.getSubtitle();
                        Component actionbarComponent = mentionEvent.getActionbar();

                        int time = InteractiveChat.mentionTitleDuration;
                        NMS.getInstance().sendTitle(receiver, titleComponent, subtitleComponent, actionbarComponent, 10, Math.max(time, 1), 20);
                        if (sound != null) {
                            if (sound.isLeft()) {
                                receiver.playSound(receiver.getLocation(), sound.getLeft(), volume, pitch);
                            } else {
                                String soundLocation = sound.getRight();
                                if (!soundLocation.contains(":")) {
                                    soundLocation = "minecraft:" + soundLocation;
                                }
                                receiver.playSound(receiver.getLocation(), soundLocation.toLowerCase(), volume, pitch);
                            }
                        }
                        if (!ComponentUtils.isEmpty(mentionEvent.getToast()) && InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_12)) {
                            String toastJson = InteractiveChatComponentSerializer.gson().serialize(toast);
                            NMS.getInstance().sendToast(sender, receiver, toastJson, WRITABLE_BOOK.clone());
                        }

                        int bossBarTime = InteractiveChat.mentionBossBarDuration;
                        int bossBarRemoveDelay = InteractiveChat.mentionBossBarRemoveDelay;
                        if (mentionEvent.getBossBar().isPresent() && !InteractiveChat.version.isOld()) {
                            BossBarUpdater updater = BossBarUpdater.update(mentionEvent.getBossBar().get(), receiver);
                            BossBarUpdater.countdownBossBar(updater, Math.max(bossBarTime, 1), Math.max(bossBarRemoveDelay, 0));
                        }
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
        String replacementText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHighlight.replace("{MentionedPlayer}", Registry.MENTION_TAG_CONVERTER.revertTags(placeholder)));
        Component replacement = LegacyComponentSerializer.legacySection().deserialize(replacementText);
        String hoverText = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.mentionHover.replace("{Sender}", sender.getDisplayName()).replace("{Receiver}", receiver.getDisplayName()));
        HoverEvent<Component> hoverEvent = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
        replacement = replacement.hoverEvent(hoverEvent);
        return ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(Registry.MENTION_TAG_CONVERTER.getTagStyle(placeholder)), true, replacement);
    }

}
