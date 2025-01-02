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

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICMaterial;
import com.loohp.interactivechat.objectholders.ValuePairs;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.MCVersion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryEvents implements Listener {

    private static final Set<InventoryClickEvent> CANCELLED_INVENTORY = ConcurrentHashMap.newKeySet();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().getType().equals(InventoryType.CREATIVE)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String hash = InteractiveChat.viewingInv1.get(player.getUniqueId());
        if (hash != null) {
            Inventory fakeInv = InteractiveChat.inventoryDisplay1Lower.get(hash);
            if (fakeInv == null) {
                InteractiveChat.plugin.getScheduler().runNextTick((task) -> player.closeInventory());
            } else {
                InteractiveChat.plugin.getScheduler().runNextTick((task) -> InventoryUtils.sendFakePlayerInventory(player, fakeInv, true, false));
            }
        }
        if (event.getView().getTopInventory() == null) {
            return;
        }
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof ICInventoryHolder) {
            event.setCancelled(true);
            CANCELLED_INVENTORY.add(event);
        }
        if (InteractiveChat.containerDisplay.containsKey(topInventory) || InteractiveChat.upperSharedInventory.contains(topInventory)) {
            if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                ItemStack item = event.getCurrentItem();
                inventoryAction(item, player, topInventory);
            } else if (InteractiveChat.viewingInv1.containsKey(player.getUniqueId())) {
                ItemStack item;
                if (event.getClickedInventory().equals(topInventory)) {
                    item = event.getCurrentItem();
                } else {
                    int rawSlot = event.getRawSlot();
                    int slot;
                    if (rawSlot < 81) {
                        slot = rawSlot - 45;
                    } else {
                        slot = rawSlot - 81;
                    }
                    Inventory bottomInventory = InteractiveChat.inventoryDisplay1Lower.get(hash);
                    if (bottomInventory != null) {
                        item = bottomInventory.getItem(slot);
                    } else {
                        item = null;
                    }
                }
                inventoryAction(item, player, topInventory);
            }
        }
    }

    private void inventoryAction(ItemStack item, Player player, Inventory topInventory) {
        if (item != null) {
            ICMaterial icMaterial = ICMaterial.from(item);
            if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_14)) {
                if (icMaterial.isMaterial(XMaterial.WRITTEN_BOOK)) {
                    player.openBook(item.clone());
                } else if (icMaterial.isMaterial(XMaterial.WRITABLE_BOOK)) {
                    ItemStack book = XMaterial.WRITTEN_BOOK.parseItem();
                    if (book != null && book.getItemMeta() instanceof BookMeta) {
                        BookMeta ori = (BookMeta) item.getItemMeta();
                        BookMeta dis = (BookMeta) book.getItemMeta();
                        List<BaseComponent[]> pages = new ArrayList<>(ori.spigot().getPages());
                        if (pages.isEmpty()) {
                            dis.setPages(" ");
                        } else {
                            dis.spigot().setPages(pages);
                        }
                        dis.setTitle("Temp Book");
                        dis.setAuthor("InteractiveChat");
                        book.setItemMeta(dis);
                        InteractiveChat.viewingInv1.remove(player.getUniqueId());
                        player.openBook(book);
                    }
                }
            }
            if (!InteractiveChat.containerDisplay.containsKey(topInventory) && item.getItemMeta() instanceof BlockStateMeta) {
                BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
                if (bsm instanceof InventoryHolder) {
                    Inventory container = ((InventoryHolder) bsm).getInventory();
                    if ((container.getSize() % 9) == 0) {
                        Inventory displayInventory = Bukkit.createInventory(ICInventoryHolder.INSTANCE, container.getSize() + 9, InteractiveChat.containerViewTitle);
                        ItemStack empty = InteractiveChat.itemFrame1.clone();
                        if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
                            empty = InteractiveChat.itemFrame2.clone();
                        }
                        if (empty.getItemMeta() != null) {
                            ItemMeta emptyMeta = empty.getItemMeta();
                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                            empty.setItemMeta(emptyMeta);
                        }
                        for (int j = 0; j < 9; j++) {
                            displayInventory.setItem(j, empty);
                        }
                        displayInventory.setItem(4, item);
                        for (int i = 0; i < container.getSize(); i++) {
                            ItemStack containerItem = container.getItem(i);
                            displayInventory.setItem(i + 9, containerItem == null ? null : containerItem.clone());
                        }

                        InteractiveChat.plugin.getScheduler().runAtEntityLater(player, (task) -> {
                            ValuePairs<Inventory, String> opened;
                            String hash = InteractiveChat.viewingInv1.remove(player.getUniqueId());
                            if (hash != null) {
                                InventoryUtils.restorePlayerInventory(player);
                                opened = new ValuePairs<>(topInventory, hash);
                            } else {
                                opened = new ValuePairs<>(topInventory, null);
                            }
                            InteractiveChat.containerDisplay.put(displayInventory, opened);
                            player.openInventory(displayInventory);
                        }, 2);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClickHighest(InventoryClickEvent event) {
        if (CANCELLED_INVENTORY.remove(event)) {
            event.setCancelled(true);
            InteractiveChat.plugin.getScheduler().runAtEntityLater(event.getWhoClicked(), (task) -> ((Player) event.getWhoClicked()).updateInventory(), 5);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (InteractiveChat.viewingInv1.remove(player.getUniqueId()) != null) {
            InventoryUtils.restorePlayerInventory(player);
        }
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory != null) {
            ValuePairs<Inventory, String> opened = InteractiveChat.containerDisplay.remove(topInventory);
            if (opened != null) {
                InteractiveChat.plugin.getScheduler().runAtEntityLater(player, (task) -> {
                    player.openInventory(opened.getFirst());
                    String hash = opened.getSecond();
                    if (hash != null) {
                        Inventory lowerInventory = InteractiveChat.inventoryDisplay1Lower.get(hash);
                        if (lowerInventory != null) {
                            InventoryUtils.sendFakePlayerInventory(player, lowerInventory, true, false);
                            InteractiveChat.viewingInv1.put(player.getUniqueId(), hash);
                        }
                    }
                }, 2);
            }
        }
    }

}
