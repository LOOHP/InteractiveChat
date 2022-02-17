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

package com.loohp.interactivechat.utils;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.inventory.ItemStack;

public class XMaterialUtils {

    @SuppressWarnings("deprecation")
    public static XMaterial matchXMaterial(ItemStack itemstack) {
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

    public static XMaterial matchXMaterial(String name) {
        return XMaterial.matchXMaterial(name).orElse(null);
    }

}
