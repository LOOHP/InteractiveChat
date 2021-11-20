package com.loohp.interactivechat.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import com.loohp.interactivechat.objectholders.LegacyIdKey;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Codec.Decoder;
import net.kyori.adventure.util.Codec.Encoder;

public class InteractiveChatComponentSerializer {
	
	private static final InteractiveChatBungeecordAPILegacyComponentSerializer BUNGEECORD_CHAT_LEGACY;
	private static final GsonComponentSerializer GSON_SERIALIZER;
	private static final GsonComponentSerializer GSON_SERIALIZER_LEGACY;
	private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER;
	//private static final LegacyComponentSerializer LEGACY_SERIALIZER_SPECIAL_HEX;
	
	private static final Pattern LEGACY_ID_PATTERN = Pattern.compile("^interactivechat:legacy_hover/id_([0-9]*)/damage_([0-9]*)$");
	
	private static final LegacyHoverEventSerializer LEGACY_HOVER_SERIALIZER;

	static {
		LEGACY_HOVER_SERIALIZER = new InteractiveChatLegacyHoverEventSerializer();		
		BUNGEECORD_CHAT_LEGACY = new InteractiveChatBungeecordAPILegacyComponentSerializer();		
		GSON_SERIALIZER = GsonComponentSerializer.builder().legacyHoverEventSerializer(LEGACY_HOVER_SERIALIZER).build();
		GSON_SERIALIZER_LEGACY = GsonComponentSerializer.builder().downsampleColors().emitLegacyHoverEvent().legacyHoverEventSerializer(LEGACY_HOVER_SERIALIZER).build();
		PLAIN_TEXT_SERIALIZER = new InteractiveChatPlainTextComponentSerializer();
		//LEGACY_SERIALIZER_SPECIAL_HEX = new InteractiveChatLegacyComponentSerializer();
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
	
	public static PlainTextComponentSerializer plainText() {
		return PLAIN_TEXT_SERIALIZER;
	}
	/*
	public static LegacyComponentSerializer legacySpecialHex() {
		return LEGACY_SERIALIZER_SPECIAL_HEX;
	}
	*/
	public static Key legacyIdToInteractiveChatKey(byte id, short damage) {
		return Key.key("interactivechat", "legacy_hover/id_" + id + "/damage_" + damage);
	}
	
	public static LegacyIdKey interactiveChatKeyToLegacyId(Key key) {
		Matcher matcher = LEGACY_ID_PATTERN.matcher(key.asString());
		if (matcher.find()) {
			try {
				byte id = Byte.parseByte(matcher.group(1));
				short damage = Short.parseShort(matcher.group(2));
				return new LegacyIdKey(id, damage);
			} catch (NumberFormatException e) {}
		}
		return null;
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

	public static class InteractiveChatLegacyHoverEventSerializer implements LegacyHoverEventSerializer {

		private InteractiveChatLegacyHoverEventSerializer() {

		}

		@Override
		public HoverEvent.@NonNull ShowItem deserializeShowItem(@NonNull Component input) throws IOException {
			String snbt = PlainTextComponentSerializer.plainText().serialize(input);
			CompoundBinaryTag item;
			try {
				item = TagStringIO.get().asCompound(snbt);
			} catch (Exception e) {
				return ShowItem.of(Key.key("minecraft:stone"), 1);
			}
			
			boolean isTagEmpty = false;
			CompoundBinaryTag tag = (CompoundBinaryTag) item.get("tag");
			if (tag == null) {
				isTagEmpty = true;
				tag = CompoundBinaryTag.empty();
			}

			Key key;
			String idIfString = item.getString("id", "");
			if (idIfString.isEmpty()) {
				byte idAsByte = item.getByte("id", (byte) 1);
				short damage = item.getShort("Damage", (short) 0);
				tag = tag.putInt("Damage", damage);
				isTagEmpty = false;
				key = legacyIdToInteractiveChatKey(idAsByte, damage);
			} else {
				key = Key.key(idIfString);
			}

			byte count = item.getByte("Count", (byte) 1);
			return ShowItem.of(key, count, isTagEmpty ? null : BinaryTagHolder.of(TagStringIO.get().asString(tag)));
		}

		@Override
		public HoverEvent.@NonNull ShowEntity deserializeShowEntity(@NonNull Component input, Decoder<Component, String, ? extends RuntimeException> componentDecoder) throws IOException {
			String snbt = PlainTextComponentSerializer.plainText().serialize(input);
			CompoundBinaryTag item = TagStringIO.get().asCompound(snbt);

			Component name;
			try {
				name = componentDecoder.decode(item.getString("name"));
			} catch (Exception e) {
				name = Component.text(item.getString("name"));
			}

			return ShowEntity.of(Key.key(item.getString("type").toLowerCase()), UUID.fromString(item.getString("id")), name);
		}

		@Override
		public @NonNull Component serializeShowItem(HoverEvent.@NonNull ShowItem input) throws IOException {
			CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().putByte("Count", (byte) input.count());

			LegacyIdKey legacyId = interactiveChatKeyToLegacyId(input.item());
			if (legacyId != null) {
				builder.putByte("id", legacyId.getId());
				builder.putShort("Damage", legacyId.getDamage());
			} else {
				builder.putString("id", input.item().asString());
			}

			BinaryTagHolder nbt = input.nbt();
			try {
				if (nbt != null) {
					builder.put("tag", TagStringIO.get().asCompound(nbt.string()));
				}

				return Component.text(TagStringIO.get().asString(builder.build()));
			} catch (Throwable e) {
				try {
					String nbtAsString = "";
					if (nbt != null) {
						nbtAsString = nbt.string();
						builder.put("tag", StringBinaryTag.of("{Tag}"));
					}
	
					return Component.text(TagStringIO.get().asString(builder.build()).replace("\"{Tag}\"", nbtAsString));
				} catch (Throwable e1) {
					e.printStackTrace();
					return Component.empty();
				}
			}
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
	
	public static class InteractiveChatPlainTextComponentSerializer implements PlainTextComponentSerializer {

		@Override
		public @NotNull Builder toBuilder() {
			throw new UnsupportedOperationException("The InteractiveChatLegacyComponentSerializer cannot be turned into a builder");
		}

		@Override
		public void serialize(@NotNull StringBuilder sb, @NotNull Component component) {
			component = ComponentFlattening.flatten(component);
			for (Component children : component.children()) {
				if (children instanceof TranslatableComponent) {
					TranslatableComponent translatable = (TranslatableComponent) children;
					sb.append(translatable.key());
					if (!translatable.args().isEmpty()) {
						sb.append("(" + translatable.args().stream().map(each -> {
							StringBuilder csb = new StringBuilder();
							serialize(csb, each);
							return csb.toString();
						}).collect(Collectors.joining(";")) + ")");
					}
				} else {
					sb.append(PlainTextComponentSerializer.plainText().serialize(children));
				}
			}
		}
		
	}
	/*
	public static class InteractiveChatLegacyComponentSerializer implements LegacyComponentSerializer {
		
		private static final LegacyComponentSerializer DESERIALIZER = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().hexColors().hexCharacter(SECTION_CHAR).character(SECTION_CHAR).build();
		private static final LegacyComponentSerializer SERILAIZER = LegacyComponentSerializer.legacySection();
		private static final Map<Pattern, Function<MatchResult, String>> LIST_OF_COLOR_PATTERNS = new LinkedHashMap<>();
		
		static {
			LIST_OF_COLOR_PATTERNS.put(Pattern.compile(ChatColorUtils.COLOR_CHAR + "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])"), result -> {
				return ChatColorUtils.COLOR_CHAR + "x" + ChatColorUtils.COLOR_CHAR + result.group(1) + ChatColorUtils.COLOR_CHAR + result.group(2) + ChatColorUtils.COLOR_CHAR + result.group(3) + ChatColorUtils.COLOR_CHAR + result.group(4) + ChatColorUtils.COLOR_CHAR + result.group(5) + ChatColorUtils.COLOR_CHAR + result.group(6);
			});
		}

		@Override
		public @NotNull Builder toBuilder() {
			throw new UnsupportedOperationException("The InteractiveChatLegacyComponentSerializer cannot be turned into a builder");
		}

		@Override
		public @NotNull TextComponent deserialize(@NotNull String input) {
			for (Entry<Pattern, Function<MatchResult, String>> entry : LIST_OF_COLOR_PATTERNS.entrySet()) {
				Matcher matcher = entry.getKey().matcher(input);
				StringBuffer sb = new StringBuffer();
				while (matcher.find()) {
					matcher.appendReplacement(sb, entry.getValue().apply(matcher));
				}
				matcher.appendTail(sb);
				input = sb.toString();
			}
			return DESERIALIZER.deserialize(input);
		}

		@Override
		public @NotNull String serialize(@NotNull Component component) {
			return SERILAIZER.serialize(component);
		}
		
	}
	*/
}
