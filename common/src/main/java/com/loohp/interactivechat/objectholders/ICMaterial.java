/*
 * This file is part of InteractiveChat4.
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

package com.loohp.interactivechat.objectholders;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import com.loohp.interactivechat.InteractiveChat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Objects;

public class ICMaterial {

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
                if (dataResetItemStack.getDurability() != 0) {
                    dataResetItemStack.setDurability((short) 0);
                }
                return XMaterial.matchXMaterial(dataResetItemStack);
            }
        }
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
        return XTag.anyMatch(name(), XTag.stringMatcher(materials));
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
