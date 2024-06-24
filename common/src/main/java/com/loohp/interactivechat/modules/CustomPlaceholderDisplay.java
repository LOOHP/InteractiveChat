/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ClickEventAction;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.CustomStringUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

public class CustomPlaceholderDisplay {

    public static Component process(Component component, Optional<ICPlayer> optplayer, Player reciever, Collection<ICPlaceholder> placeholderList, long unix) {
        for (ICPlaceholder icplaceholder : placeholderList) {
            if (icplaceholder.isBuildIn()) {
                continue;
            }
            CustomPlaceholder cp = (CustomPlaceholder) icplaceholder;

            ICPlayer parseplayer = cp.getParsePlayer().equals(ParsePlayer.SENDER) ? optplayer.orElse(null) : ICPlayerFactory.getICPlayer(reciever);

            if (InteractiveChat.useCustomPlaceholderPermissions && optplayer.isPresent()) {
                ICPlayer sender = optplayer.get();
                if (!PlayerUtils.hasPermission(sender.getUniqueId(), cp.getPermission(), true, 5)) {
                    continue;
                }
            }

            Pattern placeholder = cp.getKeyword();
            if (cp.getParseKeyword() && parseplayer != null) {
                placeholder = Pattern.compile(PlaceholderParser.parse(parseplayer, placeholder.pattern()));
            }
            long cooldown = cp.getCooldown();
            boolean hoverEnabled = cp.getHover().isEnabled();
            String hoverText = cp.getHover().getText();
            boolean clickEnabled = cp.getClick().isEnabled();
            ClickEventAction clickAction = cp.getClick().getAction();
            String clickValue = cp.getClick().getValue();
            boolean replaceEnabled = cp.getReplace().isEnabled();
            String replaceText = cp.getReplace().getReplaceText();

            component = processCustomPlaceholder(parseplayer, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
        }

        if (InteractiveChat.t && WebData.getInstance() != null) {
            for (CustomPlaceholder cp : WebData.getInstance().getSpecialPlaceholders()) {
                ICPlayer parseplayer = (cp.getParsePlayer().equals(ParsePlayer.SENDER) && optplayer.isPresent()) ? optplayer.get() : ICPlayerFactory.getICPlayer(reciever);
                Pattern placeholder = cp.getKeyword();
                if (cp.getParseKeyword()) {
                    placeholder = Pattern.compile(PlaceholderParser.parse(parseplayer, placeholder.pattern()));
                }
                long cooldown = cp.getCooldown();
                boolean hoverEnabled = cp.getHover().isEnabled();
                String hoverText = cp.getHover().getText();
                boolean clickEnabled = cp.getClick().isEnabled();
                ClickEventAction clickAction = cp.getClick().getAction();
                String clickValue = cp.getClick().getValue();
                boolean replaceEnabled = cp.getReplace().isEnabled();
                String replaceText = cp.getReplace().getReplaceText();

                component = processCustomPlaceholder(parseplayer, placeholder, cooldown, hoverEnabled, hoverText, clickEnabled, clickAction, clickValue, replaceEnabled, replaceText, component, optplayer, unix);
            }
        }

        return component;
    }

    public static Component processCustomPlaceholder(ICPlayer player, Pattern placeholder, long cooldown, boolean hoverEnabled, String hoverText, boolean clickEnabled, ClickEventAction clickAction, String clickValue, boolean replaceEnabled, String replaceText, Component component, Optional<ICPlayer> optplayer, long unix) {
        String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
        if (placeholder.matcher(plain).find()) {
            String regex = placeholder.pattern();
            return ComponentReplacing.replace(component, regex, true, (result, matchedComponents) -> {
                if (player == null) {
                    return Component.empty().children(matchedComponents);
                } else {
                    Component replaceComponent;
                    if (replaceEnabled) {
                        String replace = CustomStringUtils.applyReplacementRegex(replaceText, result, 1);
                        replace = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, replace));
                        replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replace);
                    } else {
                        replaceComponent = Component.empty().children(matchedComponents);
                    }
                    if (hoverEnabled) {
                        replaceComponent = replaceComponent.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, CustomStringUtils.applyReplacementRegex(hoverText, result, 1))))));
                    }
                    if (clickEnabled) {
                        String clicktext = PlaceholderParser.parse(player, CustomStringUtils.applyReplacementRegex(clickValue, result, 1));
                        replaceComponent = replaceComponent.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(clickAction.name()), clicktext));
                    }
                    return replaceComponent;
                }
            });
        } else {
            return component;
        }
    }

}
