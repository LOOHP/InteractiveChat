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

import org.bukkit.inventory.Inventory;

public class InternalOfflinePlayerInfo {

    private final int selectedSlot;
    private final boolean rightHanded;
    private final int xpLevel;
    private final Inventory inventory;
    private final Inventory enderchest;

    public InternalOfflinePlayerInfo(int selectedSlot, boolean rightHanded, int xpLevel, Inventory inventory, Inventory enderchest) {
        this.selectedSlot = selectedSlot;
        this.rightHanded = rightHanded;
        this.xpLevel = xpLevel;
        this.inventory = inventory;
        this.enderchest = enderchest;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public boolean isRightHanded() {
        return rightHanded;
    }

    public int getXpLevel() {
        return xpLevel;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Inventory getEnderchest() {
        return enderchest;
    }
}
