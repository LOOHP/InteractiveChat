package com.loohp.interactivechat.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;

import com.loohp.interactivechat.InteractiveChat;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class FilledMapUtils {
	
	private static Class<?> nmsMapIconClass;
	private static Constructor<?> nmsMapIconClassConstructor;
	private static Class<?> nmsMapIconTypeClass;
	private static Method nmsMapIconTypeClassGetFromIDMethod;
	
	private static Method bukkitBukkitClassGetMapShortMethod;
	private static Method bukkitMapViewClassGetIdMethod;
	
	private static Class<?> craftMapViewClass;
	private static Class<?> craftPlayerClass;
	private static Method craftMapViewClassRenderMethod;
	private static Class<?> craftRenderDataClass;
	private static Field craftRenderDataClassBufferField;
	private static Field craftRenderDataClassCursorsField;
	
	private static Class<?> nmsWorldMapBClass;
	private static Constructor<?> nmsWorldMapBClassConstructor;
	
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
			
			craftMapViewClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.map.CraftMapView");
			craftPlayerClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftPlayer");
			craftMapViewClassRenderMethod = craftMapViewClass.getMethod("render", craftPlayerClass);
			craftRenderDataClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.map.RenderData");
			craftRenderDataClassBufferField = craftRenderDataClass.getField("buffer");
			craftRenderDataClassCursorsField = craftRenderDataClass.getField("cursors");
			
			nmsMapIconClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MapIcon", "net.minecraft.world.level.saveddata.maps.MapIcon");
			nmsMapIconClassConstructor = nmsMapIconClass.getConstructors()[0];
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_11)) {
				nmsMapIconTypeClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MapIcon$Type", "net.minecraft.world.level.saveddata.maps.MapIcon$Type");
				nmsMapIconTypeClassGetFromIDMethod = nmsMapIconTypeClass.getMethod("a", byte.class);
			}
			
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_17)) {
				nmsWorldMapBClass = NMSUtils.getNMSClass("net.minecraft.server.%s.WorldMap$b", "net.minecraft.world.level.saveddata.maps.WorldMap$b");
				nmsWorldMapBClassConstructor = nmsWorldMapBClass.getConstructor(int.class, int.class, int.class, int.class, byte[].class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isFilledMap(ItemStack itemStack) {
		try {
			return (itemStack != null) && (itemStack.getItemMeta() != null) && (itemStack.getItemMeta() instanceof MapMeta);
		} catch (Exception e) {
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static MapView getMapView(ItemStack itemStack) {
		try {
			MapMeta map = (MapMeta) itemStack.getItemMeta();
			MapView mapView;
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13_1)) {
				mapView = map.getMapView();
			} else if (InteractiveChat.version.equals(MCVersion.V1_13)) {
				mapView = (MapView) bukkitBukkitClassGetMapShortMethod.invoke(null, bukkitMapViewClassGetIdMethod.invoke(map));
			} else {
				mapView = (MapView) bukkitBukkitClassGetMapShortMethod.invoke(null, itemStack.getDurability());
			}
			return mapView;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static int getMapId(ItemStack itemStack) {
		try {
			MapMeta map = (MapMeta) itemStack.getItemMeta();
			int mapId;
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13_1)) {
				mapId = map.getMapView().getId();
			} else if (InteractiveChat.version.equals(MCVersion.V1_13)) {
				mapId = (short) bukkitMapViewClassGetIdMethod.invoke(map);
			} else {
				mapId = itemStack.getDurability();
			}
			return mapId;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public static byte[] getColors(MapView mapView, Player player) {
		try {
			Object renderData = craftMapViewClassRenderMethod.invoke(craftMapViewClass.cast(mapView), craftPlayerClass.cast(player));
			return (byte[]) craftRenderDataClassBufferField.get(renderData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<MapCursor> getCursors(MapView mapView, Player player) {
		try {
			Object renderData = craftMapViewClassRenderMethod.invoke(craftMapViewClass.cast(mapView), craftPlayerClass.cast(player));
			return (ArrayList<MapCursor>) craftRenderDataClassCursorsField.get(renderData);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static List<?> toNMSMapIconList(List<MapCursor> mapCursors) {
		List<Object> nmsMapIconList = new ArrayList<>();
		try {
			if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_13)) {
				for (MapCursor cursor : mapCursors) {
					nmsMapIconList.add(nmsMapIconClassConstructor.newInstance(nmsMapIconTypeClassGetFromIDMethod.invoke(null, cursor.getRawType()), cursor.getX(), cursor.getY(), cursor.getDirection(), ChatComponentType.IChatBaseComponent.convertTo(LegacyComponentSerializer.legacySection().deserializeOrNull(cursor.getCaption()), false)));
				}
			} else if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_11)) {
				for (MapCursor cursor : mapCursors) {
					nmsMapIconList.add(nmsMapIconClassConstructor.newInstance(nmsMapIconTypeClassGetFromIDMethod.invoke(null, cursor.getRawType()), cursor.getX(), cursor.getY(), cursor.getDirection()));
				}
			} else {
				for (MapCursor cursor : mapCursors) {
					nmsMapIconList.add(nmsMapIconClassConstructor.newInstance(cursor.getRawType(), cursor.getX(), cursor.getY(), cursor.getDirection()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nmsMapIconList;
	}
	
	public static Constructor<?> getNMSWorldMapBClassConstructor() {
		return nmsWorldMapBClassConstructor;
	}
	
	public static Class<?> getNMSMapIconClass() {
		return nmsMapIconClass;
	}

}
