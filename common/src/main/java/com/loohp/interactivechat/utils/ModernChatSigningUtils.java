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
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModernChatSigningUtils {

    public static void detectRateSpam(Player player, String message) {
        NMS.getInstance().modernChatSigningDetectRateSpam(player, message);
    }

    public static int getChatMessageType(Object chatMessageTypeB) {
        return NMS.getInstance().modernChatSigningGetChatMessageType(chatMessageTypeB);
    }

    public static Object getPlayerChatMessage(String message) {
        return NMS.getInstance().modernChatSigningGetPlayerChatMessage(message);
    }

    public static Object getPlayerChatMessage(String message, Component component) {
        return NMS.getInstance().modernChatSigningGetPlayerChatMessage(message, component);
    }

    public static Optional<?> getUnsignedContent(Object playerChatMessage) {
        return NMS.getInstance().modernChatSigningGetUnsignedContent(playerChatMessage);
    }

    public static Object getSignedContent(Object playerChatMessage) {
        return NMS.getInstance().modernChatSigningGetSignedContent(playerChatMessage);
    }

    public static boolean hasWithResult() {
        return NMS.getInstance().modernChatSigningHasWithResult();
    }

    public static Object withResult(Object playerChatMessage, Object result) {
        return NMS.getInstance().modernChatSigningWithResult(playerChatMessage, result);
    }

    public static Object withUnsignedContent(Object playerChatMessage, Object unsignedContent) {
        return NMS.getInstance().modernChatSigningWithUnsignedContent(playerChatMessage, unsignedContent);
    }

    public static boolean isArgumentSignatureClass(Object instance) {
        return NMS.getInstance().modernChatSigningIsArgumentSignatureClass(instance);
    }

    public static List<?> getArgumentSignatureEntries(Object argumentSignatures) {
        return NMS.getInstance().modernChatSigningGetArgumentSignatureEntries(argumentSignatures);
    }

    public static String getSignedMessageBodyAContent(Object signedMessageBodyA) {
        return NMS.getInstance().modernChatSigningGetSignedMessageBodyAContent(signedMessageBodyA);
    }

    public static boolean isChatMessageIllegal(String s) {
        return NMS.getInstance().modernChatSigningIsChatMessageIllegal(s);
    }

    public static CompletableFuture<?> getChatDecorator(Player player, Component message) {
        return NMS.getInstance().modernChatSigningGetChatDecorator(player, message);
    }

}
