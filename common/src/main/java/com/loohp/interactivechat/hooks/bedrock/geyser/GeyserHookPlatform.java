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

package com.loohp.interactivechat.hooks.bedrock.geyser;

import com.loohp.interactivechat.hooks.bedrock.BedrockHookPlatform;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.geysermc.geyser.api.GeyserApi;

import java.util.UUID;

public class GeyserHookPlatform implements BedrockHookPlatform {

    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return GeyserApi.api().isBedrockPlayer(uuid);
    }

    @Override
    public boolean isBedrockPlayerFromProxy(UUID uuid) {
        return false;
    }

    @Override
    public boolean sendForm(UUID uuid, FormBuilder<?, ?, ?> formBuilder) {
        return GeyserApi.api().sendForm(uuid, formBuilder);
    }

}
