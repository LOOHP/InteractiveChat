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

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.api.InteractiveChatAPI.SharedType;
import com.loohp.interactivechat.api.events.ItemPlaceholderEvent;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.nms.NMS;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import com.loohp.interactivechat.utils.ChatColorUtils;
import com.loohp.interactivechat.utils.CompassUtils;
import com.loohp.interactivechat.utils.ComponentCompacting;
import com.loohp.interactivechat.utils.ComponentFlattening;
import com.loohp.interactivechat.utils.ComponentReplacing;
import com.loohp.interactivechat.utils.FilledMapUtils;
import com.loohp.interactivechat.utils.HashUtils;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.ItemNBTUtils;
import com.loohp.interactivechat.utils.ItemStackUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ItemDisplay {

    @SuppressWarnings("deprecation")
    public static Component process(Component component, Optional<ICPlayer> optplayer, Player receiver, boolean preview, long unix) throws Exception {
        String plain = InteractiveChatComponentSerializer.plainText().serialize(component);
        if (InteractiveChat.itemPlaceholder.getKeyword().matcher(plain).find()) {
            String regex = InteractiveChat.itemPlaceholder.getKeyword().pattern();
            if (InteractiveChat.bungeecordMode && optplayer.isPresent() && optplayer.get().isLocal()) {
                ICPlayer player = optplayer.get();
                ItemStack[] equipment;
                if (InteractiveChat.version.isOld()) {
                    equipment = new ItemStack[] {player.getEquipment().getHelmet(), player.getEquipment().getChestplate(), player.getEquipment().getLeggings(), player.getEquipment().getBoots(), player.getEquipment().getItemInHand()};
                } else {
                    equipment = new ItemStack[] {player.getEquipment().getHelmet(), player.getEquipment().getChestplate(), player.getEquipment().getLeggings(), player.getEquipment().getBoots(), player.getEquipment().getItemInMainHand(), player.getEquipment().getItemInOffHand()};
                }
                try {
                    BungeeMessageSender.forwardEquipment(unix, player.getUniqueId(), player.isRightHanded(), player.getSelectedSlot(), player.getExperienceLevel(), equipment);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (optplayer.isPresent()) {
                ICPlayer player = optplayer.get();
                if (PlayerUtils.hasPermission(player.getUniqueId(), "interactivechat.module.item", true, 5)) {
                    Component alternativeHover = null;
                    if (!InteractiveChat.itemHover && !InteractiveChat.itemAlternativeHoverMessage.isEmpty()) {
                        alternativeHover = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.itemAlternativeHoverMessage);
                    }
                    Component itemComponent = ComponentFlattening.flatten(createItemDisplay(player, receiver, component, unix, InteractiveChat.itemHover, alternativeHover, preview));
                    component = ComponentReplacing.replace(component, regex, true, itemComponent);
                }
            } else {
                Component message;
                if (InteractiveChat.playerNotFoundReplaceEnable) {
                    message = LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundReplaceText.replace("{Placeholder}", InteractiveChat.itemName));
                } else {
                    message = Component.text(InteractiveChat.itemName);
                }
                if (InteractiveChat.playerNotFoundHoverEnable && InteractiveChat.itemHover) {
                    message = message.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(InteractiveChat.playerNotFoundHoverText.replace("{Placeholder}", InteractiveChat.itemName))));
                }
                if (InteractiveChat.playerNotFoundClickEnable) {
                    String clickValue = ChatColorUtils.translateAlternateColorCodes('&', InteractiveChat.playerNotFoundClickValue.replace("{Placeholder}", InteractiveChat.itemName));
                    message = message.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(InteractiveChat.playerNotFoundClickAction), clickValue));
                }
                component = ComponentReplacing.replace(component, regex, true, message);
            }
        }
        return component;
    }

    public static boolean useInventoryView(ItemStack item) {
        try {
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
                if (bsm instanceof InventoryHolder) {
                    Inventory container = ((InventoryHolder) bsm).getInventory();
                    if ((container.getSize() % 9) != 0) {
                        return false;
                    }
                    for (int i = 0; i < container.getSize(); i++) {
                        ItemStack containerItem = container.getItem(i);
                        if (containerItem != null && !containerItem.getType().equals(Material.AIR)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static Component createItemDisplay(ICPlayer player, Player receiver, Component component, long timeSent, boolean preview) throws Exception {
        return createItemDisplay(player, receiver, component, timeSent, true, null, preview);
    }

    public static Component createItemDisplay(ICPlayer player, Player receiver, Component component, long timeSent, boolean showHover, Component alternativeHover, boolean preview) throws Exception {
        ItemStack item = PlayerUtils.getHeldItem(player);

        item = InteractiveChatAPI.transformItemStack(item, receiver.getUniqueId());

        ItemPlaceholderEvent event = new ItemPlaceholderEvent(player, receiver, component, timeSent, item);
        Bukkit.getPluginManager().callEvent(event);
        item = event.getItemStack();

        return createItemDisplay(player, item, InteractiveChat.itemTitle, showHover, alternativeHover, preview);
    }

    public static Component createItemDisplay(OfflineICPlayer player, ItemStack item) throws Exception {
        return createItemDisplay(player, item, InteractiveChat.itemTitle, true, null, false);
    }

    public static Component createItemDisplay(OfflineICPlayer player, ItemStack item, String rawTitle, boolean showHover, Component alternativeHover, boolean preview) throws Exception {
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        if (InteractiveChat.hideLodestoneCompassPos) {
            item = CompassUtils.hideLodestoneCompassPosition(item);
        }

        boolean trimmed = false;
        boolean isAir = item.getType().equals(Material.AIR);
        int itemAmount = isAir && InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5) ? 1 : item.getAmount();
        ItemMeta itemMeta = item.getItemMeta();

        ItemStack originalItem = item.clone();

        String itemJson = ItemNBTUtils.getNMSItemStackJson(item);
        ItemStack trimmedItem = null;
        if (InteractiveChat.sendOriginalIfTooLong && itemJson.length() > InteractiveChat.itemTagMaxLength) {
            trimmedItem = new ItemStack(item.getType());
            trimmedItem.addUnsafeEnchantments(item.getEnchantments());
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                ItemStack nameItem = trimmedItem.clone();
                Component name = NMS.getInstance().getItemStackDisplayName(item);
                NMS.getInstance().setItemStackDisplayName(nameItem, name);
                String newjson = ItemNBTUtils.getNMSItemStackJson(nameItem);
                if (newjson.length() <= InteractiveChat.itemTagMaxLength) {
                    trimmedItem = nameItem;
                }
            }
            if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
                ItemStack loreItem = trimmedItem.clone();
                ItemMeta meta = loreItem.getItemMeta();
                meta.setLore(item.getItemMeta().getLore());
                loreItem.setItemMeta(meta);
                String newjson = ItemNBTUtils.getNMSItemStackJson(loreItem);
                if (newjson.length() <= InteractiveChat.itemTagMaxLength) {
                    trimmedItem = loreItem;
                }
            }
            trimmed = true;
        }

        String amountString = "";
        Component itemDisplayNameComponent = ItemStackUtils.getDisplayName(item);

        amountString = String.valueOf(itemAmount);
        Key key = ItemNBTUtils.getNMSItemStackNamespacedKey(item);
        ShowItem showItem;
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_5)) {
            if (item.getType().equals(Material.AIR)) {
                showHover = false;
            }
            Map<Key, DataComponentValue> dataComponents = ItemNBTUtils.getNMSItemStackDataComponents(trimmedItem == null ? item : trimmedItem);
            showItem = dataComponents.isEmpty() ? ShowItem.showItem(key, itemAmount) : ShowItem.showItem(key, itemAmount, dataComponents);
        } else {
            String tag = ItemNBTUtils.getNMSItemStackTag(trimmedItem == null ? item : trimmedItem);
            showItem = tag == null ? ShowItem.showItem(key, itemAmount) : ShowItem.showItem(key, itemAmount, BinaryTagHolder.binaryTagHolder(tag));
        }
        HoverEvent<ShowItem> hoverEvent = HoverEvent.showItem(showItem);
        String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, rawTitle));
        String sha1 = HashUtils.createSha1(title, item);

        String command = null;
        boolean isMapView = false;

        if (!preview) {
            if (InteractiveChat.itemMapPreview && FilledMapUtils.isFilledMap(item)) {
                isMapView = true;
                if (!InteractiveChat.mapDisplay.containsKey(sha1)) {
                    InteractiveChatAPI.addMapToMapSharedList(sha1, item);
                }
            } else if (!InteractiveChat.itemDisplay.containsKey(sha1)) {
                if (useInventoryView(item)) {
                    Inventory container = ((InventoryHolder) ((BlockStateMeta) item.getItemMeta()).getBlockState()).getInventory();
                    Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, container.getSize() + 9, title);
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
                        inv.setItem(j, empty);
                    }
                    inv.setItem(4, isAir ? null : originalItem);
                    for (int j = 0; j < container.getSize(); j++) {
                        ItemStack shulkerItem = container.getItem(j);
                        if (shulkerItem != null && !shulkerItem.getType().equals(Material.AIR)) {
                            inv.setItem(j + 9, shulkerItem == null ? null : shulkerItem.clone());
                        }
                    }
                    InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
                } else {
                    if (InteractiveChat.version.isOld()) {
                        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 27, title);
                        ItemStack empty = InteractiveChat.itemFrame1.clone();
                        if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
                            empty = InteractiveChat.itemFrame2.clone();
                        }
                        if (empty.getItemMeta() != null) {
                            ItemMeta emptyMeta = empty.getItemMeta();
                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                            empty.setItemMeta(emptyMeta);
                        }
                        for (int j = 0; j < inv.getSize(); j++) {
                            inv.setItem(j, empty);
                        }
                        inv.setItem(13, isAir ? null : originalItem);
                        InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
                    } else {
                        Inventory inv = InventoryUtils.CAN_USE_DROPPER_TYPE ? Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryType.DROPPER, title) : Bukkit.createInventory(ICInventoryHolder.INSTANCE, 27, title);
                        ItemStack empty = InteractiveChat.itemFrame1.clone();
                        if (item.getType().equals(InteractiveChat.itemFrame1.getType())) {
                            empty = InteractiveChat.itemFrame2.clone();
                        }
                        if (empty.getItemMeta() != null) {
                            ItemMeta emptyMeta = empty.getItemMeta();
                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                            empty.setItemMeta(emptyMeta);
                        }
                        for (int j = 0; j < inv.getSize(); j++) {
                            inv.setItem(j, empty);
                        }
                        inv.setItem(inv.getSize() / 2, isAir ? null : originalItem);
                        InteractiveChatAPI.addInventoryToItemShareList(SharedType.ITEM, sha1, inv);
                    }
                }
            }
            command = isMapView ? "/interactivechat viewmap " + sha1 : "/interactivechat viewitem " + sha1;
        }

        if (trimmed && InteractiveChat.cancelledMessage) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] " + ChatColor.RED + "Trimmed an item display's meta data as it's NBT exceeds the maximum characters allowed in the chat [THIS IS NOT A BUG]");
        }

        Component itemDisplayComponent = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, itemAmount == 1 ? InteractiveChat.itemSingularReplaceText : InteractiveChat.itemReplaceText.replace("{Amount}", amountString))));
        itemDisplayComponent = itemDisplayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Item}").replacement(itemDisplayNameComponent).build());
        if (showHover) {
            itemDisplayComponent = itemDisplayComponent.hoverEvent(hoverEvent);
        } else if (alternativeHover != null) {
            itemDisplayComponent = itemDisplayComponent.hoverEvent(HoverEvent.showText(alternativeHover));
        }
        if (command != null && !isAir && (isMapView || (!isMapView && InteractiveChat.itemGUI))) {
            itemDisplayComponent = itemDisplayComponent.clickEvent(ClickEvent.runCommand(command));
        }
        return ComponentCompacting.optimize(itemDisplayComponent);
    }

}
