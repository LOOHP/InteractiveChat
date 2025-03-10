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

package com.loohp.interactivechat.listeners.packet.protocollib;

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
import com.loohp.interactivechat.platform.protocollib.ProtocolLibPlatform;
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

import static com.loohp.interactivechat.listeners.packet.OutTabCompletePacketHandler.*;

public class PLibOutTabCompletePacket {

    public static void tabCompleteListener() {
        ProtocolLibPlatform.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params()
                .optionAsync()
                .plugin(InteractiveChat.plugin)
                .listenerPriority(ListenerPriority.HIGHEST)
                .types(PacketType.Play.Server.TAB_COMPLETE)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (shouldProcessPacket(event)) {
                    processPacket(event);
                }
            }
        });
    }

    private static boolean shouldProcessPacket(PacketEvent event) {
        return !event.isFiltered() && !event.isCancelled() && event.getPacketType().equals(PacketType.Play.Server.TAB_COMPLETE) && !event.isPlayerTemporary();
    }

    private static void processPacket(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player tabCompleter = event.getPlayer();
        ValuePairs<Integer, ?> pair = NMS.getInstance().readCommandSuggestionPacket(packet);
        int id = pair.getFirst();
        Suggestions suggestions = (Suggestions) pair.getSecond();
        StringRange range = suggestions.getRange();

        List<Suggestion> newMatches = new ArrayList<>();
        for (Suggestion suggestion : suggestions.getList()) {
            newMatches.add(processSuggestion(suggestion, range, tabCompleter));
        }

        event.setPacket(NMS.getInstance().createCommandSuggestionPacket(id, new Suggestions(range, newMatches)));
    }

    private static Suggestion processSuggestion(Suggestion suggestion, StringRange range, Player tabCompleter) {
        String text = suggestion.getText();
        int pos = text.indexOf("\0");
        if (pos < 0) {
            return processTextSuggestion(suggestion, range, tabCompleter, text);
        } else {
            return processTooltipSuggestion(suggestion, range, text, pos);
        }
    }

    private static Suggestion processTextSuggestion(Suggestion suggestion, StringRange range, Player tabCompleter, String text) {
        if (InteractiveChat.useTooltipOnTab) {
            ICPlayer icplayer = findICPlayer(text);
            if (icplayer != null) {
                Component component = createComponent(icplayer, tabCompleter);
                String json = serializeComponent(component);
                return new Suggestion(range, text, (Message) WrappedChatComponent.fromJson(json).getHandle());
            }
        }
        return suggestion;
    }

    private static String serializeComponent(Component component) {
        return InteractiveChat.version.isLegacyRGB() ? InteractiveChatComponentSerializer.legacyGson().serialize(component) : InteractiveChatComponentSerializer.gson().serialize(component);
    }

    private static Suggestion processTooltipSuggestion(Suggestion suggestion, StringRange range, String text, int pos) {
        String tooltip = text.substring(pos + 1);
        text = text.substring(0, pos);
        return new Suggestion(range, text, (Message) WrappedChatComponent.fromJson(tooltip).getHandle());
    }
}
