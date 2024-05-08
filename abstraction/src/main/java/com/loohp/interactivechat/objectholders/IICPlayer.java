/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General License for more details.
 *
 * You should have received a copy of the GNU General License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.objectholders;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Set;

public interface IICPlayer extends IOfflineICPlayer {

    boolean isLocal();

    boolean isRemote();

    boolean isValid();

    Player getLocalPlayer();

    String getRemoteServer();

    void setRemoteServer(String server);

    String getServer();

    String getDisplayName();

    boolean isVanished();

    void setRemoteVanished(boolean remoteVanished);

    void setRemoteRightHanded(boolean rightHanded);

    void setRemoteSelectedSlot(int selectedSlot);

    void setRemoteExperienceLevel(int experienceLevel);

    void setRemoteInventory(Inventory inventory);

    void setRemoteEnderChest(Inventory enderchest);

    Set<String> getNicknames();

    Set<String> getRemoteNicknames();

    void setRemoteNicknames(Set<String> remoteNicknames);

    Map<String, String> getRemotePlaceholdersMapping();

}
