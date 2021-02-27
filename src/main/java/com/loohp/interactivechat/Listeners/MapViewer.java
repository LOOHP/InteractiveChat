package com.loohp.interactivechat.Listeners;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.Utils.FilledMapUtils;
import com.loohp.interactivechat.Utils.MCVersion;

public class MapViewer implements Listener {
	
	private static Class<?> nmsMapIconClass;
	
	private static Method bukkitBukkitClassGetMapShortMethod;
	private static Method bukkitMapViewClassGetIdMethod;
	
	private static Class<?> nmsItemWorldMapClass;
	private static Constructor<?> nmsItemWorldMapClassContructor;
	private static Class<?> nmsWorldClass;
	private static Class<?> nmsItemStackClass;
	private static Method nmsItemWorldMapClassGetSavedMapMethod;
	private static Class<?> craftItemStackClass;
	private static Method craftItemStackClassAsNMSCopyMethod;
	private static Class<?> craftWorldClass;
	private static Method craftWorldClassGetHandleMethod;
	private static Class<?> nmsWorldMapClass;
	private static Field nmsWorldMapClassColorsField;
	private static Field nmsWorldMapClassDecorationsField;
	private static Method nmsMapIconClassGetTypeMethod;
	private static boolean nmsMapIconClassGetTypeMethodReturnsByte;
	
	private static Object nmsItemWorldMapInstance;
	
	static {
		try {
			try {
				bukkitBukkitClassGetMapShortMethod = Bukkit.class.getMethod("getMap", short.class);
			} catch (NoSuchMethodException e1) {
				bukkitBukkitClassGetMapShortMethod = null;
			}
			try {
				bukkitMapViewClassGetIdMethod = MapView.class.getMethod("getId");
			} catch (NoSuchMethodException e1) {
				bukkitMapViewClassGetIdMethod = null;
			}
			
			nmsItemWorldMapClass = getNMSClass("net.minecraft.server.", "ItemWorldMap");
			try {
				if (InteractiveChat.version.isLegacy()) {
					nmsItemWorldMapClassContructor = nmsItemWorldMapClass.getDeclaredConstructor();
					nmsItemWorldMapClassContructor.setAccessible(true);
					nmsItemWorldMapInstance = nmsItemWorldMapClassContructor.newInstance();
					nmsItemWorldMapClassContructor.setAccessible(false);
				} else {
					nmsItemWorldMapClassContructor = null;
					nmsItemWorldMapInstance = null;
				}
			} catch (NoSuchMethodException e1) {
				nmsItemWorldMapClassContructor = null;
				nmsItemWorldMapInstance = null;
			}
			
			nmsWorldClass = getNMSClass("net.minecraft.server.", "World");
			nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");
			nmsItemWorldMapClassGetSavedMapMethod = nmsItemWorldMapClass.getMethod("getSavedMap", nmsItemStackClass, nmsWorldClass);
			craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
			craftItemStackClassAsNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
			craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");
			craftWorldClassGetHandleMethod = craftWorldClass.getMethod("getHandle");
			nmsWorldMapClass = getNMSClass("net.minecraft.server.", "WorldMap");
			nmsWorldMapClassColorsField = nmsWorldMapClass.getField("colors");
			nmsMapIconClass = getNMSClass("net.minecraft.server.", "MapIcon");
			nmsWorldMapClassDecorationsField = nmsWorldMapClass.getField("decorations");
			nmsMapIconClassGetTypeMethod = nmsMapIconClass.getMethod("getType");
			nmsMapIconClassGetTypeMethodReturnsByte = nmsMapIconClassGetTypeMethod.getReturnType().equals(byte.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
	
	public static final Map<Player, ItemStack> MAP_VIEWERS = new ConcurrentHashMap<>();
	
	@SuppressWarnings("deprecation")
	public static void showMap(Player player, ItemStack item) {
		if (!FilledMapUtils.isFilledMap(item)) {
			throw new IllegalArgumentException("ItemStack is not a filled map");
		}
		
		try {
			MapMeta map = (MapMeta) item.getItemMeta();
			int mapId;
			MapView mapView;
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13_1)) {
				mapView = map.getMapView();
				mapId = mapView.getId();
			} else if (InteractiveChat.version.equals(MCVersion.V1_13)) {
				mapId = (short) bukkitMapViewClassGetIdMethod.invoke(map);
				mapView = (MapView) bukkitBukkitClassGetMapShortMethod.invoke(null, bukkitMapViewClassGetIdMethod.invoke(map));
			} else {
				mapId = item.getDurability();
				mapView = (MapView) bukkitBukkitClassGetMapShortMethod.invoke(null, mapId);
			}
			Object nmsItemStackObject = craftItemStackClassAsNMSCopyMethod.invoke(null, item);
			Object nmsWorldServerObject = craftWorldClassGetHandleMethod.invoke(craftWorldClass.cast(mapView.getWorld()));
			Object worldMapObject = nmsItemWorldMapClassGetSavedMapMethod.invoke(nmsItemWorldMapInstance, nmsItemStackObject, nmsWorldServerObject);
			
			PacketContainer packet1;
			if (InteractiveChat.version.isOld()) {
				packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
				packet1.getIntegers().write(0, 0);
				packet1.getIntegers().write(1, player.getInventory().getHeldItemSlot() + 36);
				packet1.getItemModifier().write(0, item);
			} else {
				packet1 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
				packet1.getIntegers().write(0, player.getEntityId());
				if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16)) {
					List<Pair<ItemSlot, ItemStack>> list = new ArrayList<>();
					list.add(new Pair<ItemSlot, ItemStack>(ItemSlot.MAINHAND, item));
					packet1.getSlotStackPairLists().write(0, list);
				} else {
					packet1.getItemSlots().write(0, ItemSlot.MAINHAND);
					packet1.getItemModifier().write(0, item);
				}
			}
			
			PacketContainer packet2 = InteractiveChat.protocolManager.createPacket(PacketType.Play.Server.MAP);
			int mapIconFieldPos = 2;
			packet2.getIntegers().write(0, (int) mapId);
			packet2.getBytes().write(0, (byte) 0);
			if (!InteractiveChat.version.isOld()) {
				packet2.getBooleans().write(0, false);
				mapIconFieldPos++;
			}
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_14)) {
				packet2.getBooleans().write(1, false);
				mapIconFieldPos++;
			}
			packet2.getIntegers().write(1, 0);
			packet2.getIntegers().write(2, 0);
			packet2.getIntegers().write(3, 128);
			packet2.getIntegers().write(4, 128);  
			
			MAP_VIEWERS.put(player, item);
			
			InteractiveChat.protocolManager.sendServerPacket(player, packet1);
			
			int mapIconFieldPos0 = mapIconFieldPos;
			new BukkitRunnable() {
				@Override
				public void run() {
					ItemStack itemStack = MAP_VIEWERS.get(player);
					if (itemStack != null && itemStack.equals(item)) {
						if (!player.getInventory().containsAtLeast(itemStack, 1)) {
							try {
								byte[] colors = (byte[]) nmsWorldMapClassColorsField.get(worldMapObject);
								
								List<?> nmsMapIconsList = new ArrayList<>(((Map<?, ?>) nmsWorldMapClassDecorationsField.get(worldMapObject)).values());
								Iterator<?> itr = nmsMapIconsList.iterator();
								while (itr.hasNext()) {
									Object nmsMapIconObject = itr.next();
									int type;
									if (nmsMapIconClassGetTypeMethodReturnsByte) {
										type = (byte) nmsMapIconClassGetTypeMethod.invoke(nmsMapIconObject);
									} else {
										type = ((Enum<?>) nmsMapIconClassGetTypeMethod.invoke(nmsMapIconObject)).ordinal();
									}
									if (type == 0 || type == 6 || type == 7) {
										itr.remove();
									}
								}
								Object nmsMapIconsArray = Array.newInstance(nmsMapIconClass, nmsMapIconsList.size());
								for (int i = 0; i < nmsMapIconsList.size(); i++) {
									Object nmsMapIconObject = nmsMapIconsList.get(i);
									Array.set(nmsMapIconsArray, i, nmsMapIconObject);
								}
								
								packet2.getByteArrays().write(0, colors);
								packet2.getModifier().write(mapIconFieldPos0, nmsMapIconsArray);
								InteractiveChat.protocolManager.sendServerPacket(player, packet2);
							} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					} else {
						this.cancel();
					}
				}
			}.runTaskTimer(InteractiveChat.plugin, 0, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventory(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		boolean removed = MAP_VIEWERS.remove(player) != null;
		
		if (removed) {
			player.getInventory().setItemInHand(player.getInventory().getItemInHand());
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventory(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (player.getGameMode().equals(GameMode.CREATIVE)) {
			Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
				boolean removed = MAP_VIEWERS.remove(player) != null;
				
				if (removed) {
					player.getInventory().setItemInHand(player.getInventory().getItemInHand());
				}
			}, 1);
		} else {
			boolean removed = MAP_VIEWERS.remove(player) != null;
		
			if (removed) {
				player.getInventory().setItemInHand(player.getInventory().getItemInHand());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventory(InventoryCreativeEvent event) {
		Player player = (Player) event.getWhoClicked();
		boolean removed = MAP_VIEWERS.remove(player) != null;
		
		int slot = event.getSlot();
		
		if (removed) {
			if (player.getInventory().equals(event.getClickedInventory()) && slot >= 9) {
				ItemStack item = player.getInventory().getItem(slot);
				Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> player.getInventory().setItem(slot, item), 1);
			} else {
				event.setCursor(null);
			}
		}
		
		if (removed) {
			player.getInventory().setItemInHand(player.getInventory().getItemInHand());
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSlotChange(PlayerItemHeldEvent event) {
		if (event.getNewSlot() == event.getPreviousSlot()) {
			return;
		}
		
		Player player = event.getPlayer();
		boolean removed = MAP_VIEWERS.remove(player) != null;
		
		if (removed) {
			player.getInventory().setItemInHand(player.getInventory().getItemInHand());
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.PHYSICAL)) {
			return;
		}
		Player player = event.getPlayer();
		
		if (player.getGameMode().equals(GameMode.CREATIVE)) {
			Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
				boolean removed = MAP_VIEWERS.remove(player) != null;
				
				if (removed) {
					player.getInventory().setItemInHand(player.getInventory().getItemInHand());
				}
			}, 1);
		} else {
			boolean removed = MAP_VIEWERS.remove(player) != null;
			
			if (removed) {
				player.getInventory().setItemInHand(player.getInventory().getItemInHand());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {
		Entity entity = event.getDamager();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			boolean removed = MAP_VIEWERS.remove(player) != null;
			
			if (removed) {
				player.getInventory().setItemInHand(player.getInventory().getItemInHand());
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		MAP_VIEWERS.remove(event.getPlayer());
	}

}
