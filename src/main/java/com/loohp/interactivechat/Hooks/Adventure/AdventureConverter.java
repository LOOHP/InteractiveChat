package com.loohp.interactivechat.Hooks.Adventure;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;

import com.comphenix.protocol.reflect.cloning.BukkitCloner;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class AdventureConverter {
	
	@SuppressWarnings("unchecked")
	public static void inject() {
		try {
			Class<BukkitCloner> clazz = BukkitCloner.class;
			
			Field cloners = clazz.getDeclaredField("CLONERS");
			cloners.setAccessible(true);
			Map<Class<?>, Function<Object, Object>> clonersMap = (Map<Class<?>, Function<Object, Object>>) cloners.get(null);
			cloners.setAccessible(false);
			for (Class<?> c : clonersMap.keySet()) {
				if (c.equals(AdventureConverter.getComponentClass())) {
					return;
				}
			}
			
			Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] Adventure Component ProtocolLibrary Converter not found, injecting our own...");
			
			Method fromManualMethod = clazz.getDeclaredMethod("fromManual", Supplier.class, Function.class);
			Supplier<Class<?>> supplier = new Supplier<Class<?>>() {
				@Override
				public Class<?> get() {
					return AdventureConverter.getComponentClass();
				}
			};
			Function<Object, Object> function = new Function<Object, Object>() {
				@Override
				public Object apply(Object source) {
					return AdventureConverter.clone(source);
				}
			};
			fromManualMethod.setAccessible(true);
			fromManualMethod.invoke(null, supplier, function);
			fromManualMethod.setAccessible(false);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	private AdventureConverter() {
		
	}

	public static Class<?> getComponentClass() {
		return Component.class;
	}

	public static Component clone(Object component) {
		GsonComponentSerializer gson = GsonComponentSerializer.gson();
		return gson.deserialize(gson.serialize((Component) component));
	}
}
