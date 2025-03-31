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

package com.loohp.interactivechat.hooks.bedrock;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PreChatPacketSendEvent;
import com.loohp.interactivechat.api.events.PreExternalResponseSendEvent;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.objectholders.LimitedQueue;
import com.loohp.interactivechat.objectholders.ValuePairs;
import com.loohp.interactivechat.objectholders.ValueTrios;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentFlattening;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BedrockHook implements Listener {

    private static final Map<UUID, List<Component>> CHAT_MESSAGES = new ConcurrentHashMap<>();

    private static BedrockHookPlatform PLATFORM;

    public static void setBedrockHookPlatform(BedrockHookPlatform bedrockHookPlatform) {
        PLATFORM = bedrockHookPlatform;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatPacket(PreChatPacketSendEvent event) {
        if (!event.isCancelled() || event.sendOriginalIfCancelled()) {
            addChatMessage(event.getReciver().getUniqueId(), event.getComponent());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatPacket(PreExternalResponseSendEvent event) {
        addChatMessage(event.getReciever().getUniqueId(), event.getComponent());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CHAT_MESSAGES.remove(event.getPlayer().getUniqueId());
    }

    public static void addChatMessage(UUID uuid, Component component) {
        if (!isBedrockPlayer(uuid)) {
            return;
        }
        CHAT_MESSAGES.computeIfAbsent(uuid, k -> Collections.synchronizedList(new LimitedQueue<>(30))).add(component);
    }

    public static boolean isBedrockPlayer(UUID uuid) {
        return PLATFORM.isBedrockPlayer(uuid);
    }

    public static void sendRecentChatMessagesForm(UUID uuid) {
        SimpleForm.Builder builder = SimpleForm.builder().title(InteractiveChat.bedrockEventsMenuTitle);
        List<Component> list = CHAT_MESSAGES.get(uuid);
        if (list != null) {
            builder.content(InteractiveChat.bedrockEventsMenuContent);
            List<Component> components;
            synchronized (list) {
                components = new ArrayList<>(list.size());
                for (ListIterator<Component> itr = list.listIterator(list.size()); itr.hasPrevious();) {
                    components.add(itr.previous());
                }
            }
            for (Component component : components) {
                builder.button(InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(component, InteractiveChat.language));
            }
            builder.validResultHandler(response -> {
                int index = response.clickedButtonId();
                if (index >= components.size()) {
                    return;
                }
                sendEventsForm(uuid, components.get(index));
            });
        }
        PLATFORM.sendForm(uuid, builder);
    }

    public static void sendEventsForm(UUID uuid, Component message) {
        StringBuilder sb = new StringBuilder(InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(message, InteractiveChat.language));
        for (ValuePairs<Component, Component> pair : extractHoverTexts(message)) {
            sb.append("\n\n").append(InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(pair.getFirst(), InteractiveChat.language)).append("\n")
                    .append(InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(pair.getSecond(), InteractiveChat.language));
        }
        SimpleForm.Builder builder = SimpleForm.builder().title(InteractiveChat.bedrockEventsMenuTitle).content(sb.toString());
        List<ValueTrios<Component, String, ClickEvent.Action>> clicks = extractClickCommands(message);
        for (ValueTrios<Component, String, ClickEvent.Action> trio : clicks) {
            builder.button(InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(trio.getFirst(), InteractiveChat.language));
        }
        builder.validResultHandler(response -> {
            int index = response.clickedButtonId();
            if (index >= clicks.size()) {
                return;
            }
            ValueTrios<Component, String, ClickEvent.Action> trio = clicks.get(index);
            ClickEvent.Action action = trio.getThird();
            String command = trio.getSecond();
            if (action.equals(ClickEvent.Action.SUGGEST_COMMAND)) {
                handleSuggestCommand(uuid, trio.getFirst(), message, command);
            } else {
                handleRunCommand(uuid, command);
            }
        }).closedOrInvalidResultHandler(() -> sendRecentChatMessagesForm(uuid));
        PLATFORM.sendForm(uuid, builder);
    }

    private static void handleSuggestCommand(UUID uuid, Component clickComponent, Component message, String suggestedCommand) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        CustomForm.Builder builder = CustomForm.builder().title(InteractiveChat.bedrockEventsMenuRunSuggested)
                .input(InteractiveChatComponentSerializer.bungeecordApiLegacy().serialize(clickComponent, InteractiveChat.language), suggestedCommand, suggestedCommand)
                .validResultHandler(response -> handleRunCommand(uuid, response.asInput()))
                .closedOrInvalidResultHandler(() -> sendEventsForm(uuid, message));
        PLATFORM.sendForm(uuid, builder);
    }

    private static void handleRunCommand(UUID uuid, String command) {
        if (PLATFORM.isBedrockPlayerFromProxy(uuid) && InteractiveChat.bungeecordMode) {
            try {
                BungeeMessageSender.executeProxyCommand(System.currentTimeMillis(), uuid, command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                InteractiveChat.plugin.getScheduler().runNextTick((task) -> PlayerUtils.dispatchCommandAsPlayer(player, command));
            }
        }
    }

    private static List<ValuePairs<Component, Component>> extractHoverTexts(Component component) {
        List<ValuePairs<Component, Component>> result = new ArrayList<>();
        List<Component> flattened = new ArrayList<>(ComponentFlattening.flatten(component).children());
        for (int i = 0; i < flattened.size(); i++) {
            Component c = flattened.get(i);
            if (c.clickEvent() != null) {
                flattened.set(i, c.clickEvent(null));
            }
        }
        Component optimizeEvents = ComponentCompacting.optimizeEvents(Component.empty().children(flattened));
        for (Component c : optimizeEvents.children()) {
            HoverEvent<?> hoverEvent = c.hoverEvent();
            if (hoverEvent != null && hoverEvent.action().equals(HoverEvent.Action.SHOW_TEXT)) {
                result.add(new ValuePairs<>(c, (Component) hoverEvent.value()));
            }
        }
        return result;
    }

    private static List<ValueTrios<Component, String, ClickEvent.Action>> extractClickCommands(Component component) {
        List<ValueTrios<Component, String, ClickEvent.Action>> result = new ArrayList<>();
        List<Component> flattened = new ArrayList<>(ComponentFlattening.flatten(component).children());
        for (int i = 0; i < flattened.size(); i++) {
            Component c = flattened.get(i);
            if (c.hoverEvent() != null) {
                flattened.set(i, c.hoverEvent(null));
            }
        }
        Component optimizeEvents = ComponentCompacting.optimizeEvents(Component.empty().children(flattened));
        for (Component c : optimizeEvents.children()) {
            ClickEvent clickEvent = c.clickEvent();
            if (clickEvent != null && (clickEvent.action().equals(ClickEvent.Action.RUN_COMMAND) || clickEvent.action().equals(ClickEvent.Action.SUGGEST_COMMAND))) {
                result.add(new ValueTrios<>(c, clickEvent.value(), clickEvent.action()));
            }
        }
        return result;
    }

}
