package com.loohp.interactivechat.utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.util.Codec.Decoder;
import net.kyori.adventure.util.Codec.Encoder;

public class InteractiveChatComponentSerializer {
	
	private static LegacyHoverEventSerializer LEGACY_HOVER_SERIALIZER = null;
	
	private static InteractiveChatBungeecordAPILegacyComponentSerializer BUNGEECORD_CHAT_LEGACY;
	private static GsonComponentSerializer GSON_SERIALIZER;
	private static GsonComponentSerializer GSON_SERIALIZER_LEGACY;

	static {
		try {
			Class<?> paperNative = Class.forName("io.papermc.paper.adventure.NBTLegacyHoverEventSerializer");
			Field field = paperNative.getField("INSTANCE");
			field.setAccessible(true);
			LEGACY_HOVER_SERIALIZER = (LegacyHoverEventSerializer) field.get(null);
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}
		
		if (LEGACY_HOVER_SERIALIZER == null) {
			LEGACY_HOVER_SERIALIZER = new VelocityLegacyHoverEventSerializer();
		}
		
		BUNGEECORD_CHAT_LEGACY = new InteractiveChatBungeecordAPILegacyComponentSerializer();
		
		GSON_SERIALIZER = GsonComponentSerializer.builder().legacyHoverEventSerializer(LEGACY_HOVER_SERIALIZER).build();
		GSON_SERIALIZER_LEGACY = GsonComponentSerializer.builder().downsampleColors().emitLegacyHoverEvent().legacyHoverEventSerializer(LEGACY_HOVER_SERIALIZER).build();
	}
	
	public static InteractiveChatBungeecordAPILegacyComponentSerializer bungeecordApiLegacy() {
		return BUNGEECORD_CHAT_LEGACY;
	}
	
	public static GsonComponentSerializer gson() {
		return GSON_SERIALIZER;
	}
	
	public static GsonComponentSerializer legacyGson() {
		return GSON_SERIALIZER_LEGACY;
	}

	private InteractiveChatComponentSerializer() {

	}

	public static class InteractiveChatBungeecordAPILegacyComponentSerializer implements ComponentSerializer<Component, Component, String> {

		private InteractiveChatBungeecordAPILegacyComponentSerializer() {

		}

		@Override
		public String serialize(Component component) {
			return net.md_5.bungee.api.chat.BaseComponent.toLegacyText(net.md_5.bungee.chat.ComponentSerializer.parse(GSON_SERIALIZER.serialize(component)));
		}

		public String serialize(Component component, String language) {
			return serialize(translate(component, language));
		}

		public Component translate(Component component, String language) {
			if (component instanceof TranslatableComponent) {
				TranslatableComponent trans = (TranslatableComponent) component;
				Component translated = Component.text(LanguageUtils.getTranslation(trans.key(), language)).style(trans.style()).children(trans.children()).clickEvent(trans.clickEvent()).hoverEvent(trans.hoverEvent());
				for (Component with : trans.args()) {
					translated = translated.replaceText(TextReplacementConfig.builder().matchLiteral("%s").replacement(with).times(1).build());
				}
				component = translated;
			}
			List<Component> children = new ArrayList<>(component.children());
			for (int i = 0; i < children.size(); i++) {
				children.set(i, translate(children.get(i), language));
			}
			return component.children(children);
		}

		@Override
		public Component deserialize(String input) {
			return LegacyComponentSerializer.legacySection().deserialize(input);
		}

	}

	/*
	 * Copyright (C) 2018 Velocity Contributors
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
	 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
	 */
	
	/**
	 * An implementation of {@link LegacyHoverEventSerializer} that implements the interface in the
	 * most literal, albeit "incompatible" way possible.
	 */
	public static class VelocityLegacyHoverEventSerializer implements LegacyHoverEventSerializer {

		private VelocityLegacyHoverEventSerializer() {

		}

		private static Key legacyIdToFakeKey(byte id) {
			return Key.key("velocity", "legacy_hover/id_" + id);
		}

		@Override
		public HoverEvent.@NonNull ShowItem deserializeShowItem(@NonNull Component input) throws IOException {
			String snbt = PlainComponentSerializer.plain().serialize(input);
			CompoundBinaryTag item = TagStringIO.get().asCompound(snbt);

			Key key;
			String idIfString = item.getString("id", "");
			if (idIfString.isEmpty()) {
				key = legacyIdToFakeKey(item.getByte("id"));
			} else {
				key = Key.key(idIfString);
			}

			byte count = item.getByte("Count", (byte) 1);
			return ShowItem.of(key, count, BinaryTagHolder.of(snbt));
		}

		@Override
		public HoverEvent.@NonNull ShowEntity deserializeShowEntity(@NonNull Component input, Decoder<Component, String, ? extends RuntimeException> componentDecoder) throws IOException {
			String snbt = PlainComponentSerializer.plain().serialize(input);
			CompoundBinaryTag item = TagStringIO.get().asCompound(snbt);

			Component name;
			try {
				name = componentDecoder.decode(item.getString("name"));
			} catch (Exception e) {
				name = Component.text(item.getString("name"));
			}

			return ShowEntity.of(Key.key(item.getString("type")), UUID.fromString(item.getString("id")), name);
		}

		@Override
		public @NonNull Component serializeShowItem(HoverEvent.@NonNull ShowItem input) throws IOException {
			final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().putByte("Count", (byte) input.count());

			String keyAsString = input.item().asString();
			if (keyAsString.startsWith("velocity:legacy_hover/id_")) {
				builder.putByte("id", Byte.parseByte(keyAsString.substring("velocity:legacy_hover/id_".length())));
			} else {
				builder.putString("id", keyAsString);
			}

			BinaryTagHolder nbt = input.nbt();
			if (nbt != null) {
				builder.put("tag", TagStringIO.get().asCompound(nbt.string()));
			}

			return Component.text(TagStringIO.get().asString(builder.build()));
		}

		@Override
		public @NonNull Component serializeShowEntity(HoverEvent.@NonNull ShowEntity input, Encoder<Component, String, ? extends RuntimeException> componentEncoder) throws IOException {
			CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder().putString("id", input.id().toString()).putString("type", input.type().asString());
			Component name = input.name();
			if (name != null) {
				tag.putString("name", componentEncoder.encode(name));
			}
			return Component.text(TagStringIO.get().asString(tag.build()));
		}
	}

}
