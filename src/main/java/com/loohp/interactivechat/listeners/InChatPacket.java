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

package com.loohp.interactivechat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.CompatibilityListener;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class InChatPacket {

    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final EventPriority[] EVENT_PRIORITIES = new EventPriority[] {EventPriority.LOWEST, EventPriority.LOW, EventPriority.NORMAL, EventPriority.HIGH, EventPriority.HIGHEST, EventPriority.MONITOR};

    private static Class<?> craftPlayerClass;
    private static Method craftPlayerGetHandleMethod;
    private static Field playerConnectionField;
    private static Class<?> nmsPacketPlayInChatClass;
    private static Method handleChatMethod;

    static {
        try {
            craftPlayerClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftPlayer");
            craftPlayerGetHandleMethod = craftPlayerClass.getMethod("getHandle");
            try {
                playerConnectionField = craftPlayerGetHandleMethod.getReturnType().getField("playerConnection");
            } catch (NoSuchFieldException e) {
                playerConnectionField = craftPlayerGetHandleMethod.getReturnType().getField("b");
            }
            nmsPacketPlayInChatClass = NMSUtils.getNMSClass("net.minecraft.server.%s.PacketPlayInChat", "net.minecraft.network.protocol.game.PacketPlayInChat");
            handleChatMethod = Stream.of(playerConnectionField.getType().getMethods()).filter(each -> each.getParameterCount() == 1 && each.getParameterTypes()[0].equals(nmsPacketPlayInChatClass)).findFirst().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleChatPacket(Player player, PacketContainer packet) throws InvocationTargetException, IllegalAccessException {
        if (!packet.getType().equals(PacketType.Play.Client.CHAT)) {
            throw new IllegalArgumentException("PacketContainer is not of type \"PacketType.Play.Client.CHAT\"");
        }
        Object nmsPlayer = craftPlayerGetHandleMethod.invoke(craftPlayerClass.cast(player));
        Object playerConnectionObject = playerConnectionField.get(nmsPlayer);
        Object packetPlayInChatPacket = packet.getHandle();
        handleChatMethod.invoke(playerConnectionObject, packetPlayInChatPacket);
    }

    public static void chatMessageListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
            if (InteractiveChat.vanishHook) {
                checkSuperVanishAndPremiumVanish();
            }
            checkAsync();
            checkSync();
        }, 100, 200);

        InteractiveChat.protocolManager.addPacketListener(new PacketAdapter(PacketAdapter.params().plugin(InteractiveChat.plugin).listenerPriority(ListenerPriority.MONITOR).types(PacketType.Play.Client.CHAT)) {
            @Override
            public void onPacketSending(PacketEvent event) {
                //do nothing
            }

            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.isFiltered() || event.isCancelled() || !event.getPacketType().equals(PacketType.Play.Client.CHAT) || event.isPlayerTemporary()) {
                    return;
                }

                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                String message0 = packet.getStrings().read(0);

                if (message0 != null && !message0.startsWith("/")) {
                    PacketContainer newPacket = packet.deepClone();
                    event.setReadOnly(false);
                    event.setCancelled(true);
                    event.setReadOnly(true);

                    Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
                        String message = message0.trim();

                        if (message.matches(".*<cmd=" + UUID_REGEX + ">.*") || message.matches(".*<chat=" + UUID_REGEX + ">.*")) {
                            message = message.replaceAll("<cmd=" + UUID_REGEX + ">", "").replaceAll("<chat=" + UUID_REGEX + ">", "").trim();
                        }

                        if (player.isConversing()) {
                            String conversation = message;
                            Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> player.acceptConversationInput(conversation));
                            return;
                        }

                        AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(true, player, message, new HashSet<>());

                        for (EventPriority priority : EVENT_PRIORITIES) {
                            Set<RegisteredListener> isolatedListeners = InteractiveChat.isolatedAsyncListeners.get(priority);
                            if (isolatedListeners != null) {
                                for (RegisteredListener registration : isolatedListeners) {
                                    try {
                                        registration.callEvent(chatEvent);
                                    } catch (AuthorNagException ex) {
                                        Plugin plugin = registration.getPlugin();

                                        if (plugin.isNaggable()) {
                                            plugin.setNaggable(false);

                                            Bukkit.getLogger().log(Level.SEVERE, String.format(
                                                    "Nag author(s): '%s' of '%s' about the following: %s",
                                                    plugin.getDescription().getAuthors(),
                                                    plugin.getDescription().getFullName(),
                                                    ex.getMessage()
                                            ));
                                        }
                                    } catch (Throwable ex) {
                                        Bukkit.getLogger().log(Level.SEVERE, "Could not pass event " + chatEvent.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), ex);
                                    }
                                }
                            }
                        }

                        AtomicBoolean isCancelled = new AtomicBoolean(chatEvent.isCancelled());
                        String message1 = chatEvent.getMessage();

                        if (!InteractiveChat.isolatedSyncListeners.isEmpty()) {
                            AtomicBoolean flag = new AtomicBoolean(false);

                            Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
                                PlayerChatEvent syncChatEvent = new PlayerChatEvent(player, message1);

                                for (EventPriority priority : EVENT_PRIORITIES) {
                                    Set<RegisteredListener> isolatedListeners = InteractiveChat.isolatedSyncListeners.get(priority);
                                    if (isolatedListeners != null) {
                                        for (RegisteredListener registration : isolatedListeners) {
                                            try {
                                                registration.callEvent(syncChatEvent);
                                            } catch (AuthorNagException ex) {
                                                Plugin plugin = registration.getPlugin();

                                                if (plugin.isNaggable()) {
                                                    plugin.setNaggable(false);

                                                    Bukkit.getLogger().log(Level.SEVERE, String.format(
                                                            "Nag author(s): '%s' of '%s' about the following: %s",
                                                            plugin.getDescription().getAuthors(),
                                                            plugin.getDescription().getFullName(),
                                                            ex.getMessage()
                                                    ));
                                                }
                                            } catch (Throwable ex) {
                                                Bukkit.getLogger().log(Level.SEVERE, "Could not pass event " + chatEvent.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), ex);
                                            }
                                        }
                                    }
                                }

                                isCancelled.set(syncChatEvent.isCancelled() || isCancelled.get());
                                flag.set(true);
                            });

                            while (!flag.get()) {
                                try {
                                    TimeUnit.NANOSECONDS.sleep(10000);
                                } catch (InterruptedException ignored) {
                                }
                            }
                        }

                        if (isCancelled.get()) {
                            newPacket.getStrings().write(0, message + Registry.CANCELLED_IDENTIFIER);
                        }

                        try {
                            handleChatPacket(player, newPacket);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private static void checkAsync() {
        if (!InteractiveChat.plugin.isEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
            HandlerList handlerList = AsyncPlayerChatEvent.getHandlerList();
            if (handlerList.getRegisteredListeners().length <= 0) {
                return;
            }
            List<RegisteredListener> listeners = new ArrayList<>(Arrays.asList(handlerList.getRegisteredListeners()));
            List<RegisteredListener> toRemove = new ArrayList<>();

            for (RegisteredListener registration : listeners) {
                if (!registration.getPlugin().isEnabled()) {
                    continue;
                }
                String pluginName = registration.getPlugin().getName();
                if (pluginName.equalsIgnoreCase("InteractiveChat")) {
                    continue;
                }
                CompatibilityListener compatibilityListener = null;
                for (CompatibilityListener listener : InteractiveChat.compatibilityListeners) {
                    if (pluginName.matches(listener.getPluginRegex())) {
                        compatibilityListener = listener;
                        break;
                    }
                }
                if (compatibilityListener == null) {
                    continue;
                }
                if (!registration.getPriority().equals(compatibilityListener.getPriority())) {
                    continue;
                }
                if (!registration.getListener().getClass().getName().matches(compatibilityListener.getClassName())) {
                    continue;
                }

                Set<RegisteredListener> list = InteractiveChat.isolatedAsyncListeners.get(registration.getPriority());
                if (list == null) {
                    list = new LinkedHashSet<>();
                    InteractiveChat.isolatedAsyncListeners.put(registration.getPriority(), list);
                }

                list.add(registration);

                toRemove.add(registration);
            }

            for (RegisteredListener registration : toRemove) {
                handlerList.unregister(registration);
            }
        });
    }

    private static void checkSync() {
        if (!InteractiveChat.plugin.isEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
            HandlerList handlerList = PlayerChatEvent.getHandlerList();
            if (handlerList.getRegisteredListeners().length <= 0) {
                return;
            }
            List<RegisteredListener> listeners = new ArrayList<>(Arrays.asList(handlerList.getRegisteredListeners()));
            List<RegisteredListener> toRemove = new ArrayList<>();

            for (RegisteredListener registration : listeners) {
                if (!registration.getPlugin().isEnabled()) {
                    continue;
                }
                String pluginName = registration.getPlugin().getName();
                if (pluginName.equalsIgnoreCase("InteractiveChat")) {
                    continue;
                }
                CompatibilityListener compatibilityListener = null;
                for (CompatibilityListener listener : InteractiveChat.compatibilityListeners) {
                    if (pluginName.matches(listener.getPluginRegex())) {
                        compatibilityListener = listener;
                        break;
                    }
                }
                if (compatibilityListener == null) {
                    continue;
                }
                if (!registration.getPriority().equals(compatibilityListener.getPriority())) {
                    continue;
                }
                if (!registration.getListener().getClass().getName().matches(compatibilityListener.getClassName())) {
                    continue;
                }

                Set<RegisteredListener> list = InteractiveChat.isolatedSyncListeners.get(registration.getPriority());
                if (list == null) {
                    list = new LinkedHashSet<>();
                    InteractiveChat.isolatedSyncListeners.put(registration.getPriority(), list);
                }

                list.add(registration);

                toRemove.add(registration);
            }

            for (RegisteredListener registration : toRemove) {
                handlerList.unregister(registration);
            }
        });
    }

    private static void checkSuperVanishAndPremiumVanish() {
        if (!InteractiveChat.plugin.isEnabled()) {
            return;
        }
        Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
            HandlerList handlerList = AsyncPlayerChatEvent.getHandlerList();
            if (handlerList.getRegisteredListeners().length <= 0) {
                return;
            }
            List<RegisteredListener> listeners = new ArrayList<>(Arrays.asList(handlerList.getRegisteredListeners()));
            List<RegisteredListener> toRemove = new ArrayList<>();

            for (RegisteredListener registration : listeners) {
                if (!registration.getPlugin().isEnabled()) {
                    continue;
                }
                String pluginName = registration.getPlugin().getName();
                if (!pluginName.equals("PremiumVanish")) {
                    continue;
                }

                Set<RegisteredListener> list = InteractiveChat.superVanishPremiumVanishListeners.get(registration.getPriority());
                if (list == null) {
                    list = new LinkedHashSet<>();
                    InteractiveChat.superVanishPremiumVanishListeners.put(registration.getPriority(), list);
                }

                list.add(registration);

                toRemove.add(registration);
            }

            for (RegisteredListener registration : toRemove) {
                handlerList.unregister(registration);
            }
        });
    }

}
