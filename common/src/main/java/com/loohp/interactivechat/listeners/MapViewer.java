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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.utils.FilledMapUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapViewer implements Listener {

    public static final Map<Player, ItemStack> MAP_VIEWERS = new ConcurrentHashMap<>();

    @SuppressWarnings("deprecation")
    public static void showMap(Player player, ItemStack item) {
        if (!FilledMapUtils.isFilledMap(item)) {
            throw new IllegalArgumentException("ItemStack is not a filled map");
        }

        try {
            int mapId = FilledMapUtils.getMapId(item);
            MapView mapView = FilledMapUtils.getMapView(item);

            NMS.getInstance().sendFakeMainHandSlot(player, item);

            MAP_VIEWERS.put(player, item);

            InteractiveChat.plugin.getScheduler().runTimer((outer) -> {
                ItemStack itemStack = MAP_VIEWERS.get(player);
                if (itemStack != null && itemStack.equals(item)) {
                    if (!player.getInventory().containsAtLeast(itemStack, 1)) {
                        byte[] colors = FilledMapUtils.getColors(mapView, player);
                        List<MapCursor> mapCursors = FilledMapUtils.getCursors(mapView, player);
                        Iterator<MapCursor> itr = mapCursors.iterator();
                        while (itr.hasNext()) {
                            MapCursor mapCursor = itr.next();
                            int type = mapCursor.getRawType();
                            if (type == 0 || type == 6 || type == 7) {
                                itr.remove();
                            }
                        }
                        NMS.getInstance().sendFakeMapUpdate(player, mapId, mapCursors, colors);
                    }
                } else {
                    outer.cancel();
                }
            }, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            InteractiveChat.plugin.getScheduler().runLater((task) -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
                }
            }, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                if (event.getClick().equals(ClickType.SWAP_OFFHAND) && event.getClickedInventory().equals(player.getInventory()) && event.getSlot() == player.getInventory().getHeldItemSlot()) {
                    event.setCancelled(true);
                }
                NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventory(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        int slot = event.getSlot();

        if (removed) {
            if (player.getInventory().equals(event.getClickedInventory()) && slot >= 9) {
                ItemStack item = player.getInventory().getItem(slot);
                InteractiveChat.plugin.getScheduler().runLater((task) -> player.getInventory().setItem(slot, item), 1);
            } else {
                event.setCursor(null);
            }
        }

        if (removed) {
            NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlotChange(PlayerItemHeldEvent event) {
        int lastSlot = event.getPreviousSlot();
        int slot = event.getNewSlot();
        if (event.getNewSlot() == lastSlot) {
            return;
        }

        Player player = event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setHeldItemSlot(lastSlot);
            NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
            InteractiveChat.plugin.getScheduler().runLater((task) -> player.getInventory().setHeldItemSlot(slot), 1);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            InteractiveChat.plugin.getScheduler().runLater((task) -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
                }
            }, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                NMS.getInstance().sendFakeMainHandSlot(player, player.getInventory().getItemInHand());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MAP_VIEWERS.remove(event.getPlayer());
    }

}
