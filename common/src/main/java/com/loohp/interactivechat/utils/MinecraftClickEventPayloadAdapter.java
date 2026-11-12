/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;

import java.io.IOException;
import java.util.Map;

final class MinecraftClickEventPayloadAdapter implements TypeAdapterFactory {

    private static final TagStringIO SNBT = TagStringIO.builder().acceptHeterogeneousLists(true).emitHeterogeneousLists(true).build();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() != Style.class) {
            return null;
        }
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        TypeAdapter<JsonElement> json = gson.getAdapter(JsonElement.class);
        return new TypeAdapter<T>() {
            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement style = json.read(in);
                JsonObject clickEvent = customClickEvent(style);
                if (clickEvent != null) {
                    // Adventure reads custom payloads as SNBT strings, while Minecraft reads NBT through JSON.
                    clickEvent.addProperty("payload", SNBT.asString(toTag(clickEvent.get("payload"))));
                }
                return delegate.fromJsonTree(style);
            }

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                ClickEvent<?> event = value == null ? null : ((Style) value).clickEvent();
                if (event == null || event.action() != ClickEvent.Action.CUSTOM) {
                    delegate.write(out, value);
                    return;
                }
                JsonElement style = delegate.toJsonTree(value);
                JsonObject clickEvent = customClickEvent(style);
                if (clickEvent != null) {
                    clickEvent.add("payload", tagToJson(SNBT.asTag(clickEvent.get("payload").getAsString()), gson));
                }
                json.write(out, style);
            }
        };
    }

    private static JsonObject customClickEvent(JsonElement style) {
        if (style.isJsonObject()) {
            JsonElement clickEvent = style.getAsJsonObject().get("click_event");
            if (clickEvent != null && clickEvent.isJsonObject()) {
                JsonObject event = clickEvent.getAsJsonObject();
                if (new JsonPrimitive("custom").equals(event.get("action")) && event.has("payload") && !event.get("payload").isJsonNull()) {
                    return event;
                }
            }
        }
        return null;
    }

    private static BinaryTag toTag(JsonElement json) {
        if (json.isJsonObject()) {
            CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                compound.put(entry.getKey(), toTag(entry.getValue()));
            }
            return compound.build();
        }
        if (json.isJsonArray()) {
            ListBinaryTag.Builder<BinaryTag> list = ListBinaryTag.heterogeneousListBinaryTag();
            for (JsonElement element : json.getAsJsonArray()) {
                list.add(toTag(element));
            }
            return list.build();
        }
        if (json.isJsonNull()) {
            throw new JsonParseException("Unexpected null in custom click event payload");
        }
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isString()) {
            return StringBinaryTag.stringBinaryTag(primitive.getAsString());
        }
        if (primitive.isBoolean()) {
            return ByteBinaryTag.byteBinaryTag((byte) (primitive.getAsBoolean() ? 1 : 0));
        }
        try {
            long number = primitive.getAsBigDecimal().longValueExact();
            return (int) number == number ? IntBinaryTag.intBinaryTag((int) number) : LongBinaryTag.longBinaryTag(number);
        } catch (ArithmeticException e) {
            return DoubleBinaryTag.doubleBinaryTag(primitive.getAsDouble());
        }
    }

    private static JsonElement tagToJson(BinaryTag tag, Gson gson) {
        if (tag instanceof CompoundBinaryTag) {
            JsonObject compound = new JsonObject();
            for (Map.Entry<String, ? extends BinaryTag> entry : (CompoundBinaryTag) tag) {
                compound.add(entry.getKey(), tagToJson(entry.getValue(), gson));
            }
            return compound;
        }
        if (tag instanceof ListBinaryTag) {
            JsonArray list = new JsonArray();
            for (BinaryTag element : ((ListBinaryTag) tag).unwrapHeterogeneity()) {
                list.add(tagToJson(element, gson));
            }
            return list;
        }
        if (tag instanceof StringBinaryTag) {
            return new JsonPrimitive(((StringBinaryTag) tag).value());
        }
        if (tag instanceof NumberBinaryTag) {
            return new JsonPrimitive(((NumberBinaryTag) tag).numberValue());
        }
        if (tag instanceof ByteArrayBinaryTag) {
            return gson.toJsonTree(((ByteArrayBinaryTag) tag).value());
        }
        if (tag instanceof IntArrayBinaryTag) {
            return gson.toJsonTree(((IntArrayBinaryTag) tag).value());
        }
        if (tag instanceof LongArrayBinaryTag) {
            return gson.toJsonTree(((LongArrayBinaryTag) tag).value());
        }
        throw new JsonParseException("Unsupported custom click event payload tag: " + tag.type());
    }
}
