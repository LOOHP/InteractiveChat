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

package com.loohp.interactivechat.listeners.packet.listeners;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PostPacketComponentProcessEvent;
import com.loohp.interactivechat.api.events.PreChatPacketSendEvent;
import com.loohp.interactivechat.api.events.PrePacketComponentProcessEvent;
import com.loohp.interactivechat.hooks.bedrock.BedrockHook;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.objectholders.CustomPlaceholder;
import com.loohp.interactivechat.objectholders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.WebData;
import com.loohp.interactivechat.registry.Registry;
import com.loohp.interactivechat.utils.ComponentConversionCache;
import com.loohp.interactivechat.utils.ComponentModernizing;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.RegexCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

/**
 * decides whether OutMessagePacket.processPacket could change an outgoing chat/system message for a
 * receiver. When it cannot, the packet is sent as it is and the whole accessor -> modernize -> modules -> writer
 * round trip (most of InteractiveChat's allocation on a busy server) is skipped.
 *
 * Every check mirrors the trigger of one processPacket step; each step is a no-op (apart from flattening and
 * compacting the component tree, which does not change what the client shows) when its trigger is absent. Any
 * doubt answers "may change", so the full path runs. The caller already requires that no sender was determined
 * from the packet and that the packet type's writer only writes the component back (see
 * MessagePacketHandler.isFastPathEligible).
 */
public class UnchangedMessageFastPath {

    /** Settings.FastPathUnchangedMessages, set by ConfigManager.loadConfig (kept here, not in InteractiveChat). */
    public static volatile boolean enabled = true;
    /** Settings.FastPathStatsLogMinutes: every N minutes log why messages took the full path, 0 = off. */
    public static volatile int statsLogMinutes = 0;

    private static final ConcurrentHashMap<String, LongAdder> STATS = new ConcurrentHashMap<>();

    // The receiver-independent result per component instance. ComponentConversionCache hands every receiver of a
    // broadcast the same instance, so the scan runs once per broadcast. Entries are only trusted for a second, since
    // online names, Discord attachments and the config can change.
    private static final int REASON_CACHE_MAX = 1024;
    private static final long REASON_CACHE_MILLIS = 1000;
    private static final String NO_REASON = "";
    private static final ConcurrentHashMap<ComponentConversionCache.IdentityKey, CachedReason> REASONS = new ConcurrentHashMap<>();
    private static final LongAdder REASON_HITS = new LongAdder();
    private static final LongAdder REASON_MISSES = new LongAdder();
    private static final AtomicLong NEXT_STATS_LOG = new AtomicLong(0);

    // ProcessCommands / ProcessAccurateSender (PATTERN_0/1) and Registry.ID_PATTERN all need one of these literals.
    private static final String COMMAND_ID_TAG = "<cmd=";
    private static final String CHAT_ID_TAG = "<chat=";
    // Registry.MENTION_TAG_CONVERTER reverse pattern ("<IC^%s>" -> "<IC\^(.*?)>") needs the part before %s.
    private static final String MENTION_TAG_PREFIX;
    // ComponentFont.FONT_TAG_PATTERN / FONT_TAG_ESCAPE need "[font=" (case-insensitive).
    private static final String FONT_TAG = "[font=";
    private static final char SECTION_SIGN = (char) 0xA7;

    // InteractiveChatDiscordSrvAddon listeners on the packet events and what they react to.
    private static final String DISCORDSRV_ADDON_INBOUND_LISTENER = "com.loohp.interactivechatdiscordsrvaddon.listeners.InboundToGameEvents";
    private static final String DISCORDSRV_ADDON_COMMANDS_LISTENER = "com.loohp.interactivechatdiscordsrvaddon.listeners.DiscordCommands";
    private static final String DISCORDSRV_ADDON_SHARE_TAG = "<DiscordShare=";

    static {
        String style = Registry.MENTION_TAG_CONVERTER.getRawTagStyle();
        int index = style.indexOf("%s");
        MENTION_TAG_PREFIX = index < 0 ? style : style.substring(0, index);
    }

    private static volatile String commandsFormat = null;
    private static volatile String commandsLiteral = null;

    private static volatile Class<?> attachmentListenerClass = null;
    private static volatile Map<?, ?> attachmentData = null;

    public static boolean isUnchanged(Component component, Player receiver) {
        if (!enabled) {
            return false;
        }
        String reason = fullPathReason(component, receiver);
        count(reason == null ? "fast path" : reason);
        return reason == null;
    }

    /**
     * Counts one message for the stats log. OutMessagePacket also counts the messages that never reach the check.
     */
    public static void count(String reason) {
        int minutes = statsLogMinutes;
        if (minutes <= 0) {
            return;
        }
        STATS.computeIfAbsent(reason, k -> new LongAdder()).increment();
        long now = System.currentTimeMillis();
        long next = NEXT_STATS_LOG.get();
        if (next == 0) {
            NEXT_STATS_LOG.compareAndSet(0, now + minutes * 60000L);
        } else if (now >= next && NEXT_STATS_LOG.compareAndSet(next, now + minutes * 60000L)) {
            logStats(minutes);
        }
    }

    private static void logStats(int minutes) {
        List<Map.Entry<String, Long>> entries = new ArrayList<>();
        long total = 0;
        for (Map.Entry<String, LongAdder> entry : STATS.entrySet()) {
            long value = entry.getValue().sumThenReset();
            if (value > 0) {
                entries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), value));
                total += value;
            }
        }
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        StringBuilder sb = new StringBuilder("[InteractiveChat] Messages in the last ").append(minutes).append(" min: ").append(total);
        for (Map.Entry<String, Long> entry : entries) {
            sb.append(" | ").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        sb.append(" | ").append(ComponentConversionCache.statsAndReset());
        long hits = REASON_HITS.sumThenReset();
        long misses = REASON_MISSES.sumThenReset();
        sb.append(" | check cache hits: ").append(hits).append("/").append(hits + misses);
        InteractiveChat.plugin.getLogger().info(sb.toString());
    }

    /**
     * @return null when processing could not change the message for this receiver, otherwise a short reason.
     */
    private static String fullPathReason(Component component, Player receiver) {
        // ComponentStyling.stripColor for receivers with chat colors disabled.
        if (!PlayerUtils.canChatColor(receiver)) {
            return "receiver: chat colors off";
        }
        // BedrockHook records messages for Bedrock receivers' recent messages form.
        if (hasBedrockHookListener() && BedrockHook.isBedrockPlayer(receiver.getUniqueId())) {
            return "BedrockHook (Bedrock receiver)";
        }

        ComponentConversionCache.IdentityKey key = new ComponentConversionCache.IdentityKey(component);
        long now = System.currentTimeMillis();
        CachedReason cached = REASONS.get(key);
        if (cached != null && now - cached.time < REASON_CACHE_MILLIS) {
            REASON_HITS.increment();
            return cached.reason == NO_REASON ? null : cached.reason;
        }
        REASON_MISSES.increment();
        String reason = componentReason(component);
        if (REASONS.size() >= REASON_CACHE_MAX) {
            REASONS.clear();
        }
        REASONS.put(key, new CachedReason(reason == null ? NO_REASON : reason, now));
        return reason;
    }

    /**
     * The checks that do not depend on the receiver.
     */
    private static String componentReason(Component component) {

        // Steps that cannot be predicted cheaply, or that depend on a sender/remote state: never fast path.
        // Bungeecord mode: remote senders, delayed reprocessing and placeholder forwarding.
        // !UseAccurateSenderParser: SenderFinder can find a sender in any message (and removes entries from
        // InteractiveChat.messages as a side effect).
        if (InteractiveChat.bungeecordMode || !InteractiveChat.useAccurateSenderFinder) {
            return "config: bungeecord or !UseAccurateSenderParser";
        }
        // TritonHook.parseLanguageChat and CraftEngineFontCompatibility can touch any message. PlayernameDisplay is
        // checked against the plain text below.
        if (InteractiveChat.tritonHook) {
            return "config: triton";
        }
        if (InteractiveChat.craftEngineFontCompatibility && !InteractiveChat.craftEngineFontCompatibilityFonts.isEmpty()) {
            return "config: craftengine fonts";
        }
        // One walk: IC plain text (same string as InteractiveChatComponentSerializer.plainText(), which every module
        // matches its keyword against), whether any text has a section sign, whether any part shows an item.
        ScanResult scan = new ScanResult();
        scan(component, scan);

        // HoverableItemDisplay adds a click event to every component with a show_item hover.
        if (scan.showItem && InteractiveChat.translateHoverableItems && InteractiveChat.itemGUI) {
            return "show_item hover";
        }

        String plain;
        if (scan.sectionSign) {
            // ComponentModernizing turns legacy codes inside text into styles, so the modules see the text without
            // them ("[it" + section sign + "aem]" is matched as "[item]"). Rare, so just do what the full path does.
            plain = InteractiveChatComponentSerializer.plainText().serialize(ComponentModernizing.modernize(component));
        } else {
            plain = scan.plain.toString();
        }

        if (plain.contains(COMMAND_ID_TAG) || plain.contains(CHAT_ID_TAG) || plain.contains(MENTION_TAG_PREFIX)) {
            return "id or mention tag";
        }

        String listener = listenersBlockingFastPath(plain);
        if (listener != null) {
            return "listener " + listener;
        }

        // PlayernameDisplay only changes a message whose adventure plain text contains an online player's name,
        // display name or nickname. Check that exact text, plus the scan text to be careful.
        if (InteractiveChat.usePlayerName && (PlayernameDisplay.anyNameIn(plain) || PlayernameDisplay.anyNameIn(PlainTextComponentSerializer.plainText().serialize(component)))) {
            return "player name";
        }

        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16) && InteractiveChat.fontTags && containsIgnoreCase(plain, FONT_TAG)) {
            return "font tag";
        }

        if (InteractiveChat.useInventory && InteractiveChat.invPlaceholder.getKeyword().matcher(plain).find()) {
            return "inv keyword";
        }
        if (InteractiveChat.useEnder && InteractiveChat.enderPlaceholder.getKeyword().matcher(plain).find()) {
            return "ender keyword";
        }
        if (InteractiveChat.useItem && InteractiveChat.itemPlaceholder.getKeyword().matcher(plain).find()) {
            return "item keyword";
        }

        // CustomPlaceholderDisplay: with no sender, SENDER placeholders use the keyword as it is; VIEWER placeholders
        // with ParseKeyword run the keyword through PlaceholderParser for the receiver and recompile it (without the
        // original flags). That is only predictable when there is nothing to parse (no % placeholder, no & code).
        for (ICPlaceholder icplaceholder : InteractiveChat.placeholderList.values()) {
            if (icplaceholder.isBuildIn()) {
                continue;
            }
            CustomPlaceholder cp = (CustomPlaceholder) icplaceholder;
            Pattern keyword = cp.getKeyword();
            if (keyword.matcher(plain).find()) {
                return "placeholder " + cp.getName();
            }
            if (cp.getParseKeyword() && cp.getParsePlayer().equals(ParsePlayer.VIEWER)) {
                if (!isUnaffectedByPlaceholderParsing(keyword.pattern()) || RegexCache.get(keyword.pattern()).matcher(plain).find()) {
                    return "viewer placeholder " + cp.getName();
                }
            }
        }
        if (InteractiveChat.t && WebData.getInstance() != null) {
            // Special placeholders always parse their keyword for the receiver when there is no sender.
            for (CustomPlaceholder cp : WebData.getInstance().getSpecialPlaceholders()) {
                Pattern keyword = cp.getKeyword();
                if (!isUnaffectedByPlaceholderParsing(keyword.pattern()) || keyword.matcher(plain).find() || RegexCache.get(keyword.pattern()).matcher(plain).find()) {
                    return "special placeholder";
                }
            }
        }

        // CommandsDisplay: the pattern is the escaped Commands.Format with "{Command}" replaced by "(/...)", matched
        // case-insensitively, so a match needs the literal text before {Command} followed by "/".
        if (InteractiveChat.clickableCommands) {
            String literal = getCommandsLiteral();
            if (literal == null || containsIgnoreCase(plain, literal)) {
                return "clickable command";
            }
        }

        return null;
    }

    /**
     * Listeners of the three packet events can change or record anything, so any listener not known here disables
     * the fast path. Known ones only act on something detectable:
     * - InteractiveChatDiscordSrvAddon InboundToGameEvents (PrePacketComponentProcessEvent): replaces the URLs of
     *   recent Discord attachments, kept in its static DATA map until they time out -> full path while it is not empty.
     * - InteractiveChatDiscordSrvAddon DiscordCommands (PostPacketComponentProcessEvent): replaces "<DiscordShare=uuid>"
     *   keys broadcast by its share commands -> full path if the text contains "<DiscordShare=".
     * - InteractiveChat's own BedrockHook (PreChatPacketSendEvent): records messages for Bedrock receivers' recent
     *   messages form -> full path for Bedrock receivers.
     */
    private static String listenersBlockingFastPath(String plain) {
        for (RegisteredListener registeredListener : PrePacketComponentProcessEvent.getHandlerList().getRegisteredListeners()) {
            Listener listener = registeredListener.getListener();
            if (!listener.getClass().getName().equals(DISCORDSRV_ADDON_INBOUND_LISTENER) || !isAttachmentDataEmpty(listener.getClass())) {
                return listener.getClass().getName();
            }
        }
        for (RegisteredListener registeredListener : PostPacketComponentProcessEvent.getHandlerList().getRegisteredListeners()) {
            Listener listener = registeredListener.getListener();
            if (!listener.getClass().getName().equals(DISCORDSRV_ADDON_COMMANDS_LISTENER) || plain.contains(DISCORDSRV_ADDON_SHARE_TAG)) {
                return listener.getClass().getName();
            }
        }
        for (RegisteredListener registeredListener : PreChatPacketSendEvent.getHandlerList().getRegisteredListeners()) {
            Listener listener = registeredListener.getListener();
            if (!(listener instanceof BedrockHook)) {
                return listener.getClass().getName();
            }
        }
        return null;
    }

    private static boolean hasBedrockHookListener() {
        for (RegisteredListener registeredListener : PreChatPacketSendEvent.getHandlerList().getRegisteredListeners()) {
            if (registeredListener.getListener() instanceof BedrockHook) {
                return true;
            }
        }
        return false;
    }

    private static final class CachedReason {

        private final String reason;
        private final long time;

        private CachedReason(String reason, long time) {
            this.reason = reason;
            this.time = time;
        }

    }

    private static boolean isAttachmentDataEmpty(Class<?> listenerClass) {
        Map<?, ?> data = attachmentData;
        if (attachmentListenerClass != listenerClass) {
            // Looked up once per class (a reload of the addon brings a new class).
            data = null;
            try {
                Field field = listenerClass.getField("DATA");
                Object value = field.get(null);
                if (value instanceof Map) {
                    data = (Map<?, ?>) value;
                }
            } catch (Throwable ignored) {
            }
            attachmentData = data;
            attachmentListenerClass = listenerClass;
        }
        return data != null && data.isEmpty();
    }

    private static String getCommandsLiteral() {
        String format = InteractiveChat.clickableCommandsFormat;
        if (format == null) {
            return null;
        }
        if (!format.equals(commandsFormat)) {
            int index = format.indexOf("{Command}");
            commandsLiteral = index < 0 ? null : format.substring(0, index) + "/";
            commandsFormat = format;
        }
        return commandsLiteral;
    }

    /**
     * PlaceholderParser.parse only replaces %placeholders% (PlaceholderAPI) and translates &amp; color codes.
     */
    private static boolean isUnaffectedByPlaceholderParsing(String regex) {
        return regex.indexOf('%') < 0 && regex.indexOf('&') < 0;
    }

    private static boolean containsIgnoreCase(String text, String literal) {
        int max = text.length() - literal.length();
        for (int i = 0; i <= max; i++) {
            if (text.regionMatches(true, i, literal, 0, literal.length())) {
                return true;
            }
        }
        return false;
    }

    private static final class ScanResult {
        private final StringBuilder plain = new StringBuilder();
        private boolean sectionSign = false;
        private boolean showItem = false;
    }

    /**
     * Builds the same string as InteractiveChatPlainTextComponentSerializer (flatten, then per component: translatable
     * as "key(arg;arg)", everything else through the plain text serializer without its children) without creating the
     * flattened copy of the tree.
     */
    private static void scan(Component component, ScanResult result) {
        HoverEvent<?> hoverEvent = component.hoverEvent();
        if (hoverEvent != null && hoverEvent.action().equals(HoverEvent.Action.SHOW_ITEM)) {
            result.showItem = true;
        }
        if (component instanceof TextComponent) {
            String content = ((TextComponent) component).content();
            if (content.indexOf(SECTION_SIGN) >= 0) {
                result.sectionSign = true;
            }
            result.plain.append(content);
        } else if (component instanceof TranslatableComponent) {
            TranslatableComponent translatable = (TranslatableComponent) component;
            result.plain.append(translatable.key());
            // Not part of IC's plain text, but adventure's plain serializer (used by PlayernameDisplay) can show it.
            // Extra text only makes every check here more careful.
            if (translatable.fallback() != null) {
                result.plain.append('\n').append(translatable.fallback());
            }
            List<? extends ComponentLike> arguments = translatable.arguments();
            if (!arguments.isEmpty()) {
                result.plain.append("(");
                for (int i = 0; i < arguments.size(); i++) {
                    if (i > 0) {
                        result.plain.append(";");
                    }
                    scan(arguments.get(i).asComponent(), result);
                }
                result.plain.append(")");
            }
        } else {
            result.plain.append(PlainTextComponentSerializer.plainText().serialize(component.children(Collections.emptyList())));
        }
        for (Component child : component.children()) {
            scan(child, result);
        }
    }

}
