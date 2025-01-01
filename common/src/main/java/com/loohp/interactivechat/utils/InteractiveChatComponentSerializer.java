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

package com.loohp.interactivechat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.loohp.interactivechat.objectholders.LegacyIdKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Codec.Decoder;
import net.kyori.adventure.util.Codec.Encoder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("PatternValidation")
public class InteractiveChatComponentSerializer {

    private static final InteractiveChatBungeecordAPILegacyComponentSerializer BUNGEECORD_CHAT_LEGACY;
    private static final GsonComponentSerializer GSON_SERIALIZER;
    private static final GsonComponentSerializer GSON_SERIALIZER_LEGACY;
    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER;
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER;

    private static final Pattern LEGACY_ID_PATTERN = Pattern.compile("^interactivechat:legacy_hover/id_(.*?)/damage_([0-9]*)$");

    private static final LegacyHoverEventSerializer LEGACY_HOVER_SERIALIZER;

    static {
        LEGACY_HOVER_SERIALIZER = new InteractiveChatLegacyHoverEventSerializer();
        BUNGEECORD_CHAT_LEGACY = new InteractiveChatBungeecordAPILegacyComponentSerializer();
        GSON_SERIALIZER = new InteractiveChatGsonComponentSerializer(
                GsonComponentSerializer.builder()
                        .legacyHoverEventSerializer(LEGACY_HOVER_SERIALIZER)
                        .editOptions(builder -> builder
                                .value(JSONOptions.SHOW_ITEM_HOVER_DATA_MODE, JSONOptions.ShowItemHoverDataMode.EMIT_EITHER)
                                .value(JSONOptions.EMIT_RGB, true)
                                .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.BOTH)
                                .value(JSONOptions.EMIT_DEFAULT_ITEM_HOVER_QUANTITY, true)
                                .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, isProxyEnvironment() || isBukkitAboveV1_20_3())
                                .build()
                        )
                        .build()
        );
        GSON_SERIALIZER_LEGACY = new InteractiveChatGsonComponentSerializer(
                GsonComponentSerializer.builder()
                        .legacyHoverEventSerializer(LEGACY_HOVER_SERIALIZER)
                        .editOptions(builder -> builder
                                .value(JSONOptions.SHOW_ITEM_HOVER_DATA_MODE, JSONOptions.ShowItemHoverDataMode.EMIT_EITHER)
                                .value(JSONOptions.EMIT_RGB, false)
                                .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.BOTH)
                                .value(JSONOptions.EMIT_DEFAULT_ITEM_HOVER_QUANTITY, true)
                                .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, false)
                                .build()
                        )
                        .build()
        );
        PLAIN_TEXT_SERIALIZER = new InteractiveChatPlainTextComponentSerializer();
        LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection().toBuilder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    }

    private static boolean isProxyEnvironment() {
        try {
            Class.forName("org.bukkit.plugin.java.JavaPlugin");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean isBukkitAboveV1_20_3() {
       try {
           return com.loohp.interactivechat.InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_20_3);
       } catch (Throwable e) {
           return true;
       }
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

    public static LegacyComponentSerializer legacySection() {
        return LEGACY_COMPONENT_SERIALIZER;
    }

    public static Key legacyIdToInteractiveChatKey(byte id, short damage) {
        return Key.key("interactivechat", "legacy_hover/id_" + id + "/damage_" + damage);
    }

    public static Key legacyIdToInteractiveChatKey(String id, short damage) {
        return Key.key("interactivechat", "legacy_hover/id_" + id.replace(":", "-") + "/damage_" + damage);
    }

    public static LegacyIdKey interactiveChatKeyToLegacyId(Key key) {
        Matcher matcher = LEGACY_ID_PATTERN.matcher(key.asString());
        if (matcher.find()) {
            try {
                byte id = Byte.parseByte(matcher.group(1));
                short damage = Short.parseShort(matcher.group(2));
                return new LegacyIdKey(id, damage);
            } catch (NumberFormatException e) {
                String id = matcher.group(1).replace("-", ":");
                short damage = Short.parseShort(matcher.group(2));
                return new LegacyIdKey(id, damage);
            }
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
                LanguageUtils.TranslationResult translationResult = LanguageUtils.getTranslation(trans.key(), language);
                Component translated = Component.text(translationResult.getResultOrFallback(trans.fallback())).style(trans.style()).children(trans.children()).clickEvent(trans.clickEvent()).hoverEvent(trans.hoverEvent());
                for (TranslationArgument with : trans.arguments()) {
                    translated = translated.replaceText(TextReplacementConfig.builder().match("%+[0-9]*\\$?[sd]").replacement(with).times(1).build());
                }
                component = translated;
            }
            List<Component> children = new ArrayList<>(component.children());
            children.replaceAll(c -> translate(c, language));
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
                return ShowItem.showItem(Key.key("minecraft:stone"), 1);
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
                short damage = item.getShort("Damage", Short.MIN_VALUE);
                if (damage == Short.MIN_VALUE) {
                    key = Key.key(idIfString);
                } else {
                    tag = tag.putInt("Damage", damage);
                    isTagEmpty = false;
                    key = legacyIdToInteractiveChatKey(idIfString, damage);
                }
            }

            byte count = item.getByte("Count", (byte) 1);
            return ShowItem.showItem(key, count, isTagEmpty ? null : BinaryTagHolder.binaryTagHolder(TagStringIO.get().asString(tag)));
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

            return ShowEntity.showEntity(Key.key(item.getString("type").toLowerCase()), UUID.fromString(item.getString("id")), name);
        }

        @Override
        public @NonNull Component serializeShowItem(HoverEvent.@NonNull ShowItem input) {
            CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder().putByte("Count", (byte) input.count());

            LegacyIdKey legacyId = interactiveChatKeyToLegacyId(input.item());
            if (legacyId != null) {
                if (legacyId.hasByteId()) {
                    builder.putByte("id", legacyId.getByteId());
                } else {
                    builder.putString("id", legacyId.getStringId());
                }
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
                        builder.put("tag", StringBinaryTag.stringBinaryTag("{Tag}"));
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
                    if (!translatable.arguments().isEmpty()) {
                        sb.append("(").append(translatable.arguments().stream().map(each -> {
                            StringBuilder csb = new StringBuilder();
                            serialize(csb, each.asComponent());
                            return csb.toString();
                        }).collect(Collectors.joining(";"))).append(")");
                    }
                } else {
                    sb.append(PlainTextComponentSerializer.plainText().serialize(children));
                }
            }
        }

    }

    public static class InteractiveChatGsonComponentSerializer implements GsonComponentSerializer {

        private final GsonComponentSerializer instance;

        private InteractiveChatGsonComponentSerializer(GsonComponentSerializer instance) {
            this.instance = instance;
        }

        @Override
        public @NotNull Gson serializer() {
            return instance.serializer();
        }

        @Override
        public @NotNull UnaryOperator<GsonBuilder> populator() {
            return instance.populator();
        }

        @Override
        public @NotNull Component deserializeFromTree(@NotNull JsonElement input) {
            return instance.deserializeFromTree(input);
        }

        @Override
        public @NotNull JsonElement serializeToTree(@NotNull Component component) {
            return instance.serializeToTree(component);
        }

        @Override
        public @NotNull Component deserialize(@NotNull String input) {
            return instance.deserialize(input);
        }

        @Override
        public @Nullable Component deserializeOr(@Nullable String input, @Nullable Component fallback) {
            return instance.deserializeOr(input, fallback);
        }

        @Override
        public @NotNull String serialize(@NotNull Component component) {
            return instance.serialize(component);
        }

        @Override
        public @NotNull Builder toBuilder() {
            throw new UnsupportedOperationException("The InteractiveChatGsonComponentSerializer cannot be turned into a builder");
        }

    }

}
