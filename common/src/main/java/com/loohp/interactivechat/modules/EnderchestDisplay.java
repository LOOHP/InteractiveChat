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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent;
import com.loohp.interactivechat.api.events.InventoryPlaceholderEvent.InventoryPlaceholderType;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CompassUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EnderchestDisplay {

    public static Component process(Component component, Optional<ICPlayer> optplayer, Player receiver, boolean preview, long unix) throws Exception {
        String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
        if (InteractiveChat.enderPlaceholder.getKeyword().matcher(plain).find()) {
            String regex = InteractiveChat.enderPlaceholder.getKeyword().pattern();
            if (optplayer.isPresent()) {
                ICPlayer player = optplayer.get();
                if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.enderchest", true, 5)) {

                    Component replaceText = InteractiveChat.enderReplaceText;
                    String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, InteractiveChat.enderTitle));
                    String sha1 = HashUtils.createSha1(title, player.getEnderChest());

                    if (!preview && !InteractiveChat.enderDisplay.containsKey(sha1)) {
                        layout(player, sha1, title, receiver, component, unix);
                    }

                    Component componentText = PlaceholderParser.parse(player, replaceText);

                    List<String> hoverList = ConfigManager.getConfig().getStringList("ItemDisplay.EnderChest.HoverMessage");
                    String hoverText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, String.join("\n", hoverList)));

                    String command = "/interactivechat viewender " + sha1;

                    Component enderComponent = componentText;
                    enderComponent = enderComponent.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText)));
                    enderComponent = enderComponent.clickEvent(ClickEvent.runCommand(command));
                    component = ComponentReplacing.replace(component, regex, true, enderComponent);
                }
            } else {
                Component message;
                if (InteractiveChat.playerNotFoundReplaceEnable) {
                    message = InteractiveChat.playerNotFoundReplaceText.replaceText(TextReplacementConfig.builder().matchLiteral("{Placeholder}").replacement(InteractiveChat.enderName).build());
                } else {
                    message = Component.text(InteractiveChat.enderName);
                }
                if (InteractiveChat.playerNotFoundHoverEnable) {
                    message = message.hoverEvent(HoverEvent.showText(InteractiveChat.playerNotFoundHoverText.replaceText(TextReplacementConfig.builder().matchLiteral("{Placeholder}").replacement(InteractiveChat.enderName).build())));
                }
                if (InteractiveChat.playerNotFoundClickEnable) {
                    String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholder}", InteractiveChat.enderName));
                    ClickEvent.Action<?> clickEventAction = ClickEvent.Action.NAMES.value(CustomPlaceholder.ClickEventAction.of(InteractiveChat.playerNotFoundClickAction).getId());
                    ClickEvent.Payload.Text payload = ClickEvent.Payload.string(clickValue);
                    if (clickEventAction != null && clickEventAction.supports(payload)) {
                        //noinspection unchecked
                        message = message.clickEvent(ClickEvent.clickEvent((ClickEvent.Action<ClickEvent.Payload.Text>) clickEventAction, payload));
                    }
                }
                component = ComponentReplacing.replace(component, regex, true, message);
            }

            return component;
        } else {
            return component;
        }
    }

    public static void layout(ICPlayer player, String sha1, String title, Player receiver, Component component, long unix) throws Exception {
        int size = player.getEnderChest().getSize();
        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(size), title);
        for (int j = 0; j < size; j++) {
            if (player.getEnderChest().getItem(j) != null) {
                if (!player.getEnderChest().getItem(j).getType().equals(Material.AIR)) {
                    inv.setItem(j, player.getEnderChest().getItem(j).clone());
                }
            }
        }

        if (InteractiveChat.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        InventoryPlaceholderEvent event = new InventoryPlaceholderEvent(player, receiver, component, unix, inv, InventoryPlaceholderType.ENDERCHEST);
        Bukkit.getPluginManager().callEvent(event);
        inv = event.getInventory();

        InteractiveChatAPI.addInventoryToItemShareList(SharedType.ENDERCHEST, sha1, inv);

        if (InteractiveChat.bungeecordMode) {
            if (player.isLocal()) {
                try {
                    BungeeMessageSender.forwardEnderchest(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), null, inv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
