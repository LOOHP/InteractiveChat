/*
 * This file is part of InteractiveChat.
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

package com.loohp.interactivechat.objectholders;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.cache.Cache;
import com.loohp.interactivechat.InteractiveChat;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ICMaterial {

    private static Field cachedRegexField;
    private static Method formatMethod;

    static {
        try {
            cachedRegexField = XMaterial.class.getDeclaredField("CACHED_REGEX");
            formatMethod = XMaterial.class.getDeclaredMethod("format", String.class);
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static ICMaterial of(Material material) {
        XMaterial xMaterial = null;
        try {
            xMaterial = XMaterial.matchXMaterial(material);
        } catch (Throwable ignore) {
        }
        return new ICMaterial(xMaterial, material);
    }

    public static ICMaterial of(XMaterial xMaterial) {
        return new ICMaterial(xMaterial, xMaterial.parseMaterial());
    }

    public static ICMaterial from(String name) {
        XMaterial xMaterial = XMaterial.matchXMaterial(name).orElse(null);
        if (xMaterial != null) {
            return of(xMaterial);
        }
        Material material = Material.getMaterial(name);
        if (material != null) {
            return of(material);
        }
        return null;
    }

    public static ICMaterial from(ItemStack itemStack) {
        try {
            XMaterial xMaterial = matchXMaterial(itemStack);
            return of(xMaterial);
        } catch (Throwable ignore) {
        }
        return of(itemStack.getType());
    }

    @SuppressWarnings("deprecation")
    private static XMaterial matchXMaterial(ItemStack itemstack) {
        if (itemstack == null) {
            return null;
        }
        if (InteractiveChat.version.isLegacy()) {
            try {
                return XMaterial.matchXMaterial(itemstack);
            } catch (Throwable e) {
                ItemStack dataResetItemStack = itemstack.clone();
                dataResetItemStack.setDurability((short) 0);
                return XMaterial.matchXMaterial(dataResetItemStack);
            }
        } else {
            try {
                return XMaterial.matchXMaterial(itemstack);
            } catch (Throwable e) {
                ItemStack dataResetItemStack = itemstack.clone();
                if (NBTEditor.getInt(dataResetItemStack, "Damage") != 0) {
                    dataResetItemStack = NBTEditor.set(dataResetItemStack, 0, "Damage");
                }
                return XMaterial.matchXMaterial(dataResetItemStack);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Cache<String, Pattern> getCachedRegex() {
        try {
            cachedRegexField.setAccessible(true);
            return (Cache<String, Pattern>) cachedRegexField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String format(String name) {
        try {
            formatMethod.setAccessible(true);
            formatMethod.invoke(null, name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private final XMaterial xMaterial;
    private final Material material;

    private ICMaterial(XMaterial xMaterial, Material material) {
        if (xMaterial == null && material == null) {
            throw new RuntimeException("Both XMaterial and Material is null");
        }
        this.xMaterial = xMaterial;
        this.material = material;
    }

    public String name() {
        if (xMaterial != null) {
            return xMaterial.name();
        }
        return material.name();
    }

    @Override
    public String toString() {
        return name();
    }

    public ItemStack parseItem() {
        if (xMaterial != null) {
            return xMaterial.parseItem();
        }
        return new ItemStack(material);
    }

    public Material parseMaterial() {
        if (xMaterial != null) {
            return xMaterial.parseMaterial();
        }
        return material;
    }

    public XMaterial parseXMaterial() {
        return xMaterial;
    }

    public boolean isOneOf(Collection<String> materials) {
        if (xMaterial != null) {
            return xMaterial.isOneOf(materials);
        }
        String name = name();
        for (String comp : materials) {
            String checker = comp.toUpperCase(Locale.ENGLISH);
            if (checker.startsWith("CONTAINS:")) {
                comp = format(checker.substring(9));
                if (name.contains(comp)) return true;
                continue;
            }
            if (checker.startsWith("REGEX:")) {
                comp = comp.substring(6);
                Pattern pattern = getCachedRegex().getIfPresent(comp);
                if (pattern == null) {
                    try {
                        pattern = Pattern.compile(comp);
                        getCachedRegex().put(comp, pattern);
                    } catch (PatternSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                if (pattern != null && pattern.matcher(name).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMaterial(XMaterial xMaterial) {
        return Objects.equals(this.xMaterial, xMaterial);
    }

    public boolean isMaterial(Material material) {
        if (xMaterial != null && xMaterial.getData() != 0) {
            try {
                return xMaterial.equals(XMaterial.matchXMaterial(material));
            } catch (Throwable e) {
                return false;
            }
        }
        return Objects.equals(this.material, material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ICMaterial that = (ICMaterial) o;
        return xMaterial == that.xMaterial && material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xMaterial, material);
    }
}
