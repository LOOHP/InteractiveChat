/*
 * This file is part of InteractiveChat.
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
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent.InventoryPlaceholderType;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CompassUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.SkinUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryDisplay {

    public static final List<Integer> LAYOUTS = Stream.of(0, 1).collect(Collectors.toList());

    public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, boolean preview, long unix) throws Exception {
        String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
        if (InteractiveChat.invPlaceholder.getKeyword().matcher(plain).find()) {
            String regex = InteractiveChat.invPlaceholder.getKeyword().pattern();
            if (optplayer.isPresent()) {
                ICPlayer player = optplayer.get();
                if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.inventory", true, 5)) {

                    String replaceText = InteractiveChat.invReplaceText;
                    String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invTitle));
                    String sha1 = HashUtils.createSha1(player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), title, player.getInventory());

                    if (!preview && !InteractiveChat.inventoryDisplay.containsKey(sha1)) {
                        layout0(player, sha1, title, reciever, component, unix);
                        layout1(player, sha1, title, reciever, component, unix);
                    }

                    String componentText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replaceText));

                    List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.Inventory.HoverMessage");
                    String hoverText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, String.join("\n", hoverList)));

                    String command = "/interactivechat viewinv " + sha1;

                    Component invComponent = LegacyComponentSerializer.legacySection().deserialize(componentText);
                    invComponent = invComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText)));
                    invComponent = invComponent.clickEvent(ClickEvent.runCommand(command));
                    component = ComponentReplacing.replace(component, regex, true, invComponent);
                }
            } else {
                Component message;
                if (InteractiveChat.playerNotFoundReplaceEnable) {
                    message = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholder}", InteractiveChat.invName));
                } else {
                    message = Component.text(InteractiveChat.invName);
                }
                if (InteractiveChat.playerNotFoundHoverEnable) {
                    message = message.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundHoverText.replace("{Placeholder}", InteractiveChat.invName))));
                }
                if (InteractiveChat.playerNotFoundClickEnable) {
                    String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholder}", InteractiveChat.invName));
                    message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), clickValue));
                }
                component = ComponentReplacing.replace(component, regex, true, message);
            }
        }
        return component;
    }

    public static String getLevelTranslation(int level) {
        if (level == 1) {
            return "container.enchant.level.one";
        } else {
            return "container.enchant.level.many";
        }
    }

    public static void layout0(ICPlayer player, String sha1, String title, Player reciever, Component component, long unix) throws Exception {
        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 54, title);
        int f1 = 0;
        int f2 = 0;
        int u = 45;
        for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
            ItemStack item = player.getInventory().getItem(j);
            if (item != null && !item.getType().equals(Material.AIR)) {
                if ((j >= 9 && j < 18) || j >= 36) {
                    if (item.getType().equals(InteractiveChat.invFrame1.getType())) {
                        f1++;
                    } else if (item.getType().equals(InteractiveChat.invFrame2.getType())) {
                        f2++;
                    }
                }
                if (j < 36) {
                    inv.setItem(u, item.clone());
                }
            }
            if (u >= 53) {
                u = 18;
            } else {
                u++;
            }
        }
        ItemStack frame = f1 > f2 ? InteractiveChat.invFrame2.clone() : InteractiveChat.invFrame1.clone();
        if (frame.getItemMeta() != null) {
            ItemMeta frameMeta = frame.getItemMeta();
            frameMeta.setDisplayName(ChatColor.YELLOW + "");
            frame.setItemMeta(frameMeta);
        }
        for (int j = 0; j < 18; j++) {
            inv.setItem(j, frame);
        }

        int level = player.getExperienceLevel();
        ItemStack exp = XMaterial.EXPERIENCE_BOTTLE.parseItem();
        if (InteractiveChat.version.isNewerThan(MCVersion.V1_15)) {
            TranslatableComponent expText = Component.translatable(getLevelTranslation(level)).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE);
            if (level != 1) {
                expText = expText.arguments(Component.text(level));
            }
            NMS.getInstance().setItemStackDisplayName(exp, expText);
        } else {
            ItemMeta expMeta = exp.getItemMeta();
            expMeta.setDisplayName(ChatColor.YELLOW + LanguageUtils.getTranslation(getLevelTranslation(level), InteractiveChat.language).getResult().replaceFirst("%s|%d", level + ""));
            exp.setItemMeta(expMeta);
        }
        inv.setItem(1, exp);

        inv.setItem(3, player.getInventory().getItem(39));
        inv.setItem(4, player.getInventory().getItem(38));
        inv.setItem(5, player.getInventory().getItem(37));
        inv.setItem(6, player.getInventory().getItem(36));

        ItemStack offhand = player.getInventory().getSize() > 40 ? player.getInventory().getItem(40) : null;
        if (!InteractiveChat.version.isOld() || (offhand != null && offhand.getType().equals(Material.AIR))) {
            inv.setItem(8, offhand);
        }

        if (InteractiveChat.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, component, unix, inv, InventoryPlaceholderType.INVENTORY);
        Bukkit.getPluginManager().callEvent(event);
        inv = event.getInventory();

        Inventory finalRef = inv;
        InteractiveChat.plugin.getScheduler().runAsync((task) -> {
            ItemStack skull = SkinUtils.getSkull(player.getUniqueId());
            ItemMeta meta = skull.getItemMeta();
            String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invSkullName));
            meta.setDisplayName(name);
            skull.setItemMeta(meta);
            finalRef.setItem(0, skull);
        });

        InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY, sha1, inv);

        if (InteractiveChat.bungeecordMode) {
            if (player.isLocal()) {
                try {
                    Inventory toForward = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45, title);
                    for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
                        ItemStack item = player.getInventory().getItem(j);
                        if (item != null && !item.getType().equals(Material.AIR)) {
                            toForward.setItem(j, item.clone());
                        }
                    }
                    BungeeMessageSender.forwardInventory(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, toForward);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void layout1(ICPlayer player, String sha1, String title, Player reciever, Component component, long unix) throws Exception {
        boolean rightHanded = player.isRightHanded();
        int selectedSlot = player.getSelectedSlot();
        int level = player.getExperienceLevel();

        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 54, title);
        int f1 = 0;
        int f2 = 0;
        for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
            if (j == selectedSlot || j >= 36) {
                ItemStack item = player.getInventory().getItem(j);
                if (item != null && !item.getType().equals(Material.AIR)) {
                    if (item.getType().equals(InteractiveChat.invFrame1.getType())) {
                        f1++;
                    } else if (item.getType().equals(InteractiveChat.invFrame2.getType())) {
                        f2++;
                    }
                }
            }
        }
        ItemStack frame = f1 > f2 ? InteractiveChat.invFrame2.clone() : InteractiveChat.invFrame1.clone();
        if (frame.getItemMeta() != null) {
            ItemMeta frameMeta = frame.getItemMeta();
            frameMeta.setDisplayName(ChatColor.YELLOW + "");
            frame.setItemMeta(frameMeta);
        }
        for (int j = 0; j < 54; j++) {
            inv.setItem(j, frame);
        }
        inv.setItem(12, player.getInventory().getItem(39));
        inv.setItem(21, player.getInventory().getItem(38));
        inv.setItem(30, player.getInventory().getItem(37));
        inv.setItem(39, player.getInventory().getItem(36));

        ItemStack offhand = player.getInventory().getSize() > 40 ? player.getInventory().getItem(40) : null;
        if (InteractiveChat.version.isOld() && (offhand == null || offhand.getType().equals(Material.AIR))) {
            inv.setItem(24, player.getInventory().getItem(selectedSlot));
        } else {
            inv.setItem(23, rightHanded ? offhand : player.getInventory().getItem(selectedSlot));
            inv.setItem(25, rightHanded ? player.getInventory().getItem(selectedSlot) : offhand);
        }

        ItemStack exp = XMaterial.EXPERIENCE_BOTTLE.parseItem();
        if (InteractiveChat.version.isNewerThan(MCVersion.V1_15)) {
            TranslatableComponent expText = Component.translatable(getLevelTranslation(level)).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, State.FALSE);
            if (level != 1) {
                expText = expText.arguments(Component.text(level));
            }
            NMS.getInstance().setItemStackDisplayName(exp, expText);
        } else {
            ItemMeta expMeta = exp.getItemMeta();
            expMeta.setDisplayName(ChatColor.YELLOW + LanguageUtils.getTranslation(getLevelTranslation(level), InteractiveChat.language).getResult().replaceFirst("%s|%d", level + ""));
            exp.setItemMeta(expMeta);
        }
        inv.setItem(37, exp);

        Inventory inv2 = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45, title);
        for (int j = 0; j < Math.min(player.getInventory().getSize(), 45); j++) {
            ItemStack item = player.getInventory().getItem(j);
            if (item != null && !item.getType().equals(Material.AIR)) {
                inv2.setItem(j, item.clone());
            }
        }

        if (InteractiveChat.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, reciever, component, unix, inv, InventoryPlaceholderType.INVENTORY1_UPPER);
        Bukkit.getPluginManager().callEvent(event);
        inv = event.getInventory();

        if (InteractiveChat.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv2);
        }

        InventoryPlaceholderEvent event2 = new InventoryPlaceholderEvent(player, reciever, component, unix, inv2, InventoryPlaceholderType.INVENTORY1_LOWER);
        Bukkit.getPluginManager().callEvent(event2);
        inv2 = event2.getInventory();

        Inventory finalRef = inv;
        InteractiveChat.plugin.getScheduler().runAsync((task) -> {
            ItemStack skull = SkinUtils.getSkull(player.getUniqueId());
            ItemMeta meta = skull.getItemMeta();
            String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.invSkullName));
            meta.setDisplayName(name);
            skull.setItemMeta(meta);
            finalRef.setItem(10, skull);
        });

        InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY1_UPPER, sha1, inv);
        InteractiveChatAPI.addInventoryToItemShareList(SharedType.INVENTORY1_LOWER, sha1, inv2);

        if (InteractiveChat.bungeecordMode) {
            if (player.isLocal()) {
                try {
                    BungeeMessageSender.forwardInventory(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, inv2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
