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

package com.loohp.interactivechat.utils;

import com.loohp.interactivechat.nms.NMS;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;

public class NBTParsingUtils {

    public static Tag<?> fromSNBT(String snbt) throws IOException {
        try {
            boolean isList = snbt.trim().startsWith("[");
            if (isList) {
                snbt = "{List:" + snbt + "}";
            }
            NamedTag namedTag = NMS.getInstance().fromSNBT(snbt);
            if (namedTag == null) {
                return null;
            }
            if (isList) {
                return ((CompoundTag) namedTag.getTag()).getListTag("List");
            }
            return namedTag.getTag();
        } catch (Exception e) {
            throw new IOException("Unable to parse SNBT: " + snbt, e);
        }
    }

}
