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

package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.ValuePairs;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.ComponentStyling;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class OutTabCompletePacket {

    private static AtomicReference<Map<String, UUID>> playernames = new AtomicReference<>(new HashMap<>());

    public static void tabCompleteListener() {
        InteractiveChat.plugin.getScheduler().runTimerAsync((outer) -> {
            if (InteractiveChat.useTooltipOnTab) {
                Map<String, UUID> playernames = new HashMap<>();
                for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                    playernames.put(ChatColorUtils.stripColor(player.getName()), player.getUniqueId());
                    if (!player.getName().equals(player.getDisplayName())) {
                        playernames.put(ChatColorUtils.stripColor(player.getDisplayName()), player.getUniqueId());
                    }
                    List<String> names = InteractiveChatAPI.getNicknames(player.getUniqueId());
                    for (String name : names) {
                        playernames.put(ChatColorUtils.stripColor(name), player.getUniqueId());
                    }
                }
                InteractiveChat.plugin.getScheduler().runNextTick((inner) -> OutTabCompletePacket.playernames.set(playernames));
            }
        }, 0, 100);

        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().optionAsync().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.HIGHEST).types(PacketType.Play.Server.TAB_COMPLETE)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Server.TAB_COMPLETE) || event.isPlayerTemporary()) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                Player tabCompleter = event.getPlayer();
                ValuePairs<Integer, ?> pair = NMS.getInstance().readCommandSuggestionPacket(packet);
                int id = pair.getFirst();
                Suggestions suggestions = (Suggestions) pair.getSecond();
                StringRange range = suggestions.getRange();

                List<Suggestion> matches = suggestions.getList();
                List<Suggestion> newMatches = new ArrayList<>();
                for (Suggestion suggestion : matches) {
                    String text = suggestion.getText();
                    int pos = text.indexOf("\0");
                    if (pos < 0) {
                        if (InteractiveChat.useTooltipOnTab) {
                            ICPlayer icplayer = null;
                            for (Entry<String, UUID> entry : playernames.get().entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(text)) {
                                    icplayer = ICPlayerFactory.getICPlayer(entry.getValue());
                                    if (icplayer == null) {
                                        newMatches.add(suggestion);
                                        continue;
                                    }
                                    break;
                                }
                            }
                            if (icplayer != null) {
                                Component component = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(icplayer, InteractiveChat.tabTooltip)));
                                if (!PlayerUtils.canChatColor(tabCompleter)) {
                                    component = ComponentStyling.stripColor(component);
                                }
                                String json = InteractiveChat.version.isLegacyRGB() ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
                                newMatches.add(new Suggestion(range, text, (Message) WrappedChatComponent.fromJson(json).getHandle()));
                            } else {
                                newMatches.add(suggestion);
                            }
                        } else {
                            newMatches.add(suggestion);
                        }
                    } else {
                        String tooltip = text.substring(pos + 1);
                        text = text.substring(0, pos);
                        newMatches.add(new Suggestion(range, text, (Message) WrappedChatComponent.fromJson(tooltip).getHandle()));
                    }
                }

                event.setPacket(NMS.getInstance().createCommandSuggestionPacket(id, new Suggestions(range, newMatches)));
            }
        });
    }

}
