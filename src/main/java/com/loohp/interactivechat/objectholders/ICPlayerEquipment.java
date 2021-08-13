package com.loohp.interactivechat.objectholders;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ICPlayerEquipment implements EntityEquipment {

	private final Map<EquipmentSlot, Float> dropchance;
	private final Map<EquipmentSlot, ItemStack> equipment;

	public ICPlayerEquipment() {
		dropchance = new HashMap<>();
		equipment = new HashMap<>();
		resetEquipments();
		resetChances();
	}
	
	private void resetEquipments() {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			equipment.put(slot, null);
		}
	}
	
	private void resetChances() {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			dropchance.put(slot, 100.0F);
		}
	}

	@Override
	public void setItem(EquipmentSlot slot, ItemStack item) {
		equipment.put(slot, item);
	}

	@Override
	public ItemStack getItem(EquipmentSlot slot) {
		return equipment.get(slot);
	}

	@Override
	public ItemStack getItemInMainHand() {
		return equipment.get(EquipmentSlot.HAND);
	}

	@Override
	public void setItemInMainHand(ItemStack item) {
		equipment.put(EquipmentSlot.HAND, item);
	}

	@Override
	public ItemStack getItemInOffHand() {
		return equipment.get(EquipmentSlot.OFF_HAND);
	}

	@Override
	public void setItemInOffHand(ItemStack item) {
		equipment.put(EquipmentSlot.OFF_HAND, item);
	}

	@Override
	@Deprecated
	public ItemStack getItemInHand() {
		return equipment.get(EquipmentSlot.HAND);
	}

	@Override
	@Deprecated
	public void setItemInHand(ItemStack stack) {
		equipment.put(EquipmentSlot.HAND, stack);
	}

	@Override
	public ItemStack getHelmet() {
		return equipment.get(EquipmentSlot.HEAD);
	}

	@Override
	public void setHelmet(ItemStack helmet) {
		equipment.put(EquipmentSlot.HEAD, helmet);
	}

	@Override
	public ItemStack getChestplate() {
		return equipment.get(EquipmentSlot.CHEST);
	}

	@Override
	public void setChestplate(ItemStack chestplate) {
		equipment.put(EquipmentSlot.CHEST, chestplate);
	}

	@Override
	public ItemStack getLeggings() {
		return equipment.get(EquipmentSlot.LEGS);
	}

	@Override
	public void setLeggings(ItemStack leggings) {
		equipment.put(EquipmentSlot.LEGS, leggings);
	}

	@Override
	public ItemStack getBoots() {
		return equipment.get(EquipmentSlot.FEET);
	}

	@Override
	public void setBoots(ItemStack boots) {
		equipment.put(EquipmentSlot.FEET, boots);
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
		return dropchance.get(EquipmentSlot.HAND);
	}

	@Override
	@Deprecated
	public void setItemInHandDropChance(float chance) {
		dropchance.put(EquipmentSlot.HAND, chance);
	}

	@Override
	@Deprecated
	public float getItemInMainHandDropChance() {
		return dropchance.get(EquipmentSlot.HAND);
	}

	@Override
	@Deprecated
	public void setItemInMainHandDropChance(float chance) {
		dropchance.put(EquipmentSlot.HAND, chance);
	}

	@Override
	@Deprecated
	public float getItemInOffHandDropChance() {
		return dropchance.get(EquipmentSlot.OFF_HAND);
	}

	@Override
	@Deprecated
	public void setItemInOffHandDropChance(float chance) {
		dropchance.put(EquipmentSlot.OFF_HAND, chance);
	}

	@Override
	@Deprecated
	public float getHelmetDropChance() {
		return dropchance.get(EquipmentSlot.HEAD);
	}

	@Override
	@Deprecated
	public void setHelmetDropChance(float chance) {
		dropchance.put(EquipmentSlot.HEAD, chance);
	}

	@Override
	@Deprecated
	public float getChestplateDropChance() {
		return dropchance.get(EquipmentSlot.CHEST);
	}

	@Override
	@Deprecated
	public void setChestplateDropChance(float chance) {
		dropchance.put(EquipmentSlot.CHEST, chance);
	}

	@Override
	@Deprecated
	public float getLeggingsDropChance() {
		return dropchance.get(EquipmentSlot.LEGS);
	}

	@Override
	@Deprecated
	public void setLeggingsDropChance(float chance) {
		dropchance.put(EquipmentSlot.LEGS, chance);
	}

	@Override
	@Deprecated
	public float getBootsDropChance() {
		return dropchance.get(EquipmentSlot.FEET);
	}

	@Override
	@Deprecated
	public void setBootsDropChance(float chance) {
		dropchance.put(EquipmentSlot.FEET, chance);
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
