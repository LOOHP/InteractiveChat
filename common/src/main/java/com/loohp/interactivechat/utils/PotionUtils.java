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

import com.loohp.interactivechat.InteractiveChat;
import org.bukkit.potion.PotionType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PotionUtils {

    private static final Map<PotionType, String> potionMapping = new EnumMap<>(PotionType.class);

    static {
        if (!InteractiveChat.version.isLegacy()) {
            potionMapping.put(PotionType.WATER, "water");
            potionMapping.put(PotionType.MUNDANE, "mundane");
            potionMapping.put(PotionType.THICK, "thick");
            potionMapping.put(PotionType.AWKWARD, "awkward");
            potionMapping.put(PotionType.NIGHT_VISION, "night_vision");
            potionMapping.put(PotionType.INVISIBILITY, "invisibility");
            potionMapping.put(PotionType.JUMP, "leaping");
            potionMapping.put(PotionType.FIRE_RESISTANCE, "fire_resistance");
            potionMapping.put(PotionType.SPEED, "swiftness");
            potionMapping.put(PotionType.SLOWNESS, "slowness");
            potionMapping.put(PotionType.TURTLE_MASTER, "turtle_master");
            potionMapping.put(PotionType.WATER_BREATHING, "water_breathing");
            potionMapping.put(PotionType.INSTANT_HEAL, "healing");
            potionMapping.put(PotionType.INSTANT_DAMAGE, "harming");
            potionMapping.put(PotionType.POISON, "poison");
            potionMapping.put(PotionType.REGEN, "regeneration");
            potionMapping.put(PotionType.STRENGTH, "strength");
            potionMapping.put(PotionType.WEAKNESS, "weakness");
            potionMapping.put(PotionType.LUCK, "luck");
            potionMapping.put(PotionType.SLOW_FALLING, "slow_falling");
            potionMapping.put(PotionType.UNCRAFTABLE, "empty");
        }
    }

    public static String getVanillaPotionName(PotionType type) {
        if (potionMapping.containsKey(type)) {
            return potionMapping.get(type);
        }
        return potionMapping.get(PotionType.UNCRAFTABLE);
    }

}
