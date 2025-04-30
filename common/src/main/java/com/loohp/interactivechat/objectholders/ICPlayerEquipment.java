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

package com.loohp.interactivechat.objectholders;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public class ICPlayerEquipment implements EntityEquipment {

    private final Map<EquipmentSlot, Float> dropChance;
    private final OfflineICPlayer parent;

    public ICPlayerEquipment(OfflineICPlayer parent) {
        this.dropChance = new EnumMap<>(EquipmentSlot.class);
        this.parent = parent;
        resetChances();
    }

    private void resetEquipments() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setItem(slot, null);
        }
    }

    private void resetChances() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            dropChance.put(slot, 100.0F);
        }
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {
        switch (slot) {
            case HAND:
                parent.getInventory().setItem(parent.getSelectedSlot(), item);
                break;
            case OFF_HAND:
                parent.getInventory().setItem(40, item);
                break;
            case FEET:
                parent.getInventory().setItem(36, item);
                break;
            case LEGS:
                parent.getInventory().setItem(37, item);
                break;
            case CHEST:
                parent.getInventory().setItem(38, item);
                break;
            case HEAD:
                parent.getInventory().setItem(39, item);
                break;
        }
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        switch (slot) {
            case HAND:
                return parent.getInventory().getItem(parent.getSelectedSlot());
            case OFF_HAND:
                return parent.getInventory().getItem(40);
            case FEET:
                return parent.getInventory().getItem(36);
            case LEGS:
                return parent.getInventory().getItem(37);
            case CHEST:
                return parent.getInventory().getItem(38);
            case HEAD:
                return parent.getInventory().getItem(39);
        }
        return null;
    }

    @Override
    public ItemStack getItemInMainHand() {
        return getItem(EquipmentSlot.HAND);
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        setItem(EquipmentSlot.HAND, item);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return getItem(EquipmentSlot.OFF_HAND);
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        setItem(EquipmentSlot.OFF_HAND, item);
    }

    @Override
    @Deprecated
    public ItemStack getItemInHand() {
        return getItem(EquipmentSlot.HAND);
    }

    @Override
    @Deprecated
    public void setItemInHand(ItemStack stack) {
        setItem(EquipmentSlot.HAND, stack);
    }

    @Override
    public ItemStack getHelmet() {
        return getItem(EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        setItem(EquipmentSlot.HEAD, helmet);
    }

    @Override
    public ItemStack getChestplate() {
        return getItem(EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        setItem(EquipmentSlot.CHEST, chestplate);
    }

    @Override
    public ItemStack getLeggings() {
        return getItem(EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        setItem(EquipmentSlot.LEGS, leggings);
    }

    @Override
    public ItemStack getBoots() {
        return getItem(EquipmentSlot.FEET);
    }

    @Override
    public void setBoots(ItemStack boots) {
        setItem(EquipmentSlot.FEET, boots);
    }

    @Override
    public ItemStack[] getArmorContents() {
        return new ItemStack[] {getHelmet(), getChestplate(), getLeggings(), getBoots()};
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        setHelmet(items[0]);
        setChestplate(items[1]);
        setLeggings(items[2]);
        setBoots(items[3]);
    }

    @Override
    public void clear() {
        resetEquipments();
    }

    @Override
    @Deprecated
    public float getItemInHandDropChance() {
        return dropChance.get(EquipmentSlot.HAND);
    }

    @Override
    @Deprecated
    public void setItemInHandDropChance(float chance) {
        dropChance.put(EquipmentSlot.HAND, chance);
    }

    @Override
    @Deprecated
    public float getItemInMainHandDropChance() {
        return dropChance.get(EquipmentSlot.HAND);
    }

    @Override
    @Deprecated
    public void setItemInMainHandDropChance(float chance) {
        dropChance.put(EquipmentSlot.HAND, chance);
    }

    @Override
    @Deprecated
    public float getItemInOffHandDropChance() {
        return dropChance.get(EquipmentSlot.OFF_HAND);
    }

    @Override
    @Deprecated
    public void setItemInOffHandDropChance(float chance) {
        dropChance.put(EquipmentSlot.OFF_HAND, chance);
    }

    @Override
    @Deprecated
    public float getHelmetDropChance() {
        return dropChance.get(EquipmentSlot.HEAD);
    }

    @Override
    @Deprecated
    public void setHelmetDropChance(float chance) {
        dropChance.put(EquipmentSlot.HEAD, chance);
    }

    @Override
    @Deprecated
    public float getChestplateDropChance() {
        return dropChance.get(EquipmentSlot.CHEST);
    }

    @Override
    @Deprecated
    public void setChestplateDropChance(float chance) {
        dropChance.put(EquipmentSlot.CHEST, chance);
    }

    @Override
    @Deprecated
    public float getLeggingsDropChance() {
        return dropChance.get(EquipmentSlot.LEGS);
    }

    @Override
    @Deprecated
    public void setLeggingsDropChance(float chance) {
        dropChance.put(EquipmentSlot.LEGS, chance);
    }

    @Override
    @Deprecated
    public float getBootsDropChance() {
        return dropChance.get(EquipmentSlot.FEET);
    }

    @Override
    @Deprecated
    public void setBootsDropChance(float chance) {
        dropChance.put(EquipmentSlot.FEET, chance);
    }

    @Override
    public void setBoots(ItemStack boots, boolean silent) {
        setBoots(boots);
    }

    @Override
    public void setLeggings(ItemStack leggings, boolean silent) {
        setLeggings(leggings);
    }

    @Override
    public void setChestplate(ItemStack chestplate, boolean silent) {
        setChestplate(chestplate);
    }

    @Override
    public void setHelmet(ItemStack helmet, boolean silent) {
        setHelmet(helmet);
    }

    @Override
    public void setItemInMainHand(ItemStack stack, boolean silent) {
        setItemInMainHand(stack);
    }

    @Override
    public void setItemInOffHand(ItemStack stack, boolean silent) {
        setItemInOffHand(stack);
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack stack, boolean silent) {
        setItem(slot, stack);
    }

    @Override
    public Entity getHolder() {
        return null;
    }

}
