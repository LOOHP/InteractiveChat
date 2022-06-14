/*
 * This file is part of InteractiveChat4.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NBTParsingUtils {

    private static Class<?> nmsMojangsonParserClass;
    private static Method parseMojangsonMethod;
    private static Class<?> nmsNbtTagCompoundClass;
    private static Class<?> nmsNBTCompressedStreamToolsClass;
    private static Method streamNBTCompoundMethod;

    static {
        try {
            nmsMojangsonParserClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MojangsonParser", "net.minecraft.nbt.MojangsonParser");
            parseMojangsonMethod = NMSUtils.reflectiveLookup(Method.class, () -> {
                return nmsMojangsonParserClass.getMethod("parse", String.class);
            }, () -> {
                return nmsMojangsonParserClass.getMethod("a", String.class);
            });
            nmsNbtTagCompoundClass = NMSUtils.getNMSClass("net.minecraft.server.%s.NBTTagCompound", "net.minecraft.nbt.NBTTagCompound");
            nmsNBTCompressedStreamToolsClass = NMSUtils.getNMSClass("net.minecraft.server.%s.NBTCompressedStreamTools", "net.minecraft.nbt.NBTCompressedStreamTools");
            streamNBTCompoundMethod = nmsNBTCompressedStreamToolsClass.getMethod("a", nmsNbtTagCompoundClass, DataOutput.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Tag<?> fromSNBT(String snbt) throws IOException {
        try {
            boolean isList = snbt.trim().startsWith("[");
            if (isList) {
                snbt = "{List:" + snbt + "}";
            }
            Object nbt = parseMojangsonMethod.invoke(null, snbt);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            streamNBTCompoundMethod.invoke(null, nbt, new DataOutputStream(out));
            NamedTag namedTag = new NBTDeserializer(false).fromBytes(out.toByteArray());
            if (namedTag == null) {
                return null;
            }
            if (isList) {
                return ((CompoundTag) namedTag.getTag()).getListTag("List");
            }
            return namedTag.getTag();
        } catch (InvocationTargetException | IllegalAccessException | IOException e) {
            throw new IOException("Unable to parse SNBT: " + snbt, e);
        }
    }

}
