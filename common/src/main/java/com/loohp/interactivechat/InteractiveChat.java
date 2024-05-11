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

package com.loohp.interactivechat;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageListener;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.bungeemessaging.ServerPingListener;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.data.Database;
import com.loohp.interactivechat.data.PlayerDataManager;
import com.loohp.interactivechat.debug.Debug;
import com.loohp.interactivechat.hooks.discordsrv.DiscordSRVEvents;
import com.loohp.interactivechat.hooks.dynmap.DynmapListener;
import com.loohp.interactivechat.hooks.eco.EcoHook;
import com.loohp.interactivechat.hooks.essentials.EssentialsDiscord;
import com.loohp.interactivechat.hooks.essentials.EssentialsNicknames;
import com.loohp.interactivechat.hooks.excellentenchants.ExcellentEnchantsHook;
import com.loohp.interactivechat.hooks.floodgate.FloodgateHook;
import com.loohp.interactivechat.hooks.luckperms.LuckPermsEvents;
import com.loohp.interactivechat.hooks.venturechat.VentureChatInjection;
import com.loohp.interactivechat.listeners.ChatEvents;
import com.loohp.interactivechat.listeners.ClientSettingPacket;
import com.loohp.interactivechat.listeners.InventoryEvents;
import com.loohp.interactivechat.listeners.MapViewer;
import com.loohp.interactivechat.listeners.OutMessagePacket;
import com.loohp.interactivechat.listeners.OutTabCompletePacket;
import com.loohp.interactivechat.listeners.PaperChatEvents;
import com.loohp.interactivechat.listeners.PlayerEvents;
import com.loohp.interactivechat.listeners.RedispatchSignedPacket;
import com.loohp.interactivechat.metrics.Charts;
import com.loohp.interactivechat.metrics.Metrics;
import com.loohp.interactivechat.modules.MentionDisplay;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.objectholders.ConcurrentCacheHashMap;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.LogFilter;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.objectholders.ModernChatCompletionTask;
import com.loohp.interactivechat.objectholders.NicknameManager;
import com.loohp.interactivechat.objectholders.PlaceholderCooldownManager;
import com.loohp.interactivechat.objectholders.SignedMessageModificationData;
import com.loohp.interactivechat.objectholders.ValuePairs;
import com.loohp.interactivechat.placeholderapi.Placeholders;
import com.loohp.interactivechat.updater.Updater;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import github.scarsz.discordsrv.DiscordSRV;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InteractiveChat extends JavaPlugin {

    public static final int BSTATS_PLUGIN_ID = 6747;

    public static InteractiveChat plugin = null;

    public static String exactMinecraftVersion;
    public static MCVersion version;

    public static ProtocolManager protocolManager;

    public static String language = "en_us";

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static Optional<Character> chatAltColorCode = Optional.empty();

    public static int extraProxiedPacketProcessingDelay;
    public static boolean pluginMessagePacketVerbose;

    public static Boolean essentialsHook = false;
    public static Boolean essentialsDiscordHook = false;
    public static Boolean cmiHook = false;
    public static Boolean ventureChatHook = false;
    public static Boolean discordSrvHook = false;
    public static Boolean dynmapHook = false;
    public static Boolean viaVersionHook = false;
    public static Boolean protocolSupportHook = false;
    public static Boolean ecoHook = false;
    public static Boolean excellentenchantsHook = false;
    public static Boolean luckPermsHook = false;
    public static Boolean mysqlPDBHook = false;
    public static Boolean chatControlRedHook = false;
    public static Boolean floodgateHook = false;

    public static Permission perms = null;

    public static boolean t = true;

    public static boolean chatListener = true;
    public static boolean titleListener = false;

    public static boolean usePaperModernChatEvent = false;
    public static boolean paperChatEventEditOriginalMessageField = true;

    public static boolean chatPreviewRemoveClickAndHover = false;

    public static EventPriority chatEventPriority = EventPriority.HIGH;
    public static EventPriority commandsEventPriority = EventPriority.HIGH;

    public static boolean useItem = true;
    public static boolean useInventory = true;
    public static boolean useEnder = true;

    public static boolean itemMapPreview = true;

    public static ICPlaceholder itemPlaceholder = null;
    public static ICPlaceholder invPlaceholder = null;
    public static ICPlaceholder enderPlaceholder = null;

    public static String itemName = "";
    public static String invName = "";
    public static String enderName = "";

    public static String itemReplaceText = "&f[&f{Item} &bx{Amount}&f]";
    public static String itemSingularReplaceText = "&f[&f{Item}&f]";
    public static String invReplaceText = "&f[&b%player_name%'s Inventory&f]";
    public static String enderReplaceText = "&f[&d%player_name%'s Ender Chest&f]";

    public static boolean itemAirAllow = true;
    public static String itemAirErrorMessage = "";

    public static String itemTitle = "%player_name%'s Item";
    public static String invTitle = "%player_name%'s Inventory";
    public static String enderTitle = "%player_name%'s Ender Chest";

    public static boolean itemHover = true;
    public static String itemAlternativeHoverMessage = "";
    public static boolean itemGUI = true;
    public static boolean translateHoverableItems = true;
    public static String hoverableItemTitle = "";

    public static String containerViewTitle = "Container Contents";

    public static boolean usePlayerName = true;
    public static boolean usePlayerNameOverrideHover = true;
    public static boolean usePlayerNameOverrideClick = true;
    public static boolean usePlayerNameHoverEnable = true;
    public static String usePlayerNameHoverText = "";
    public static boolean usePlayerNameClickEnable = true;
    public static String usePlayerNameClickAction = "SUGGEST_COMMAND";
    public static String usePlayerNameClickValue = "";
    public static boolean usePlayerNameCaseSensitive = true;

    public static boolean chatTabCompletionsEnabled = true;
    public static boolean useTooltipOnTab = true;
    public static String tabTooltip = "";

    public static boolean playerNotFoundHoverEnable = true;
    public static String playerNotFoundHoverText = "&cUnable to parse placeholder..";
    public static boolean playerNotFoundClickEnable = false;
    public static String playerNotFoundClickAction = "SUGGEST_COMMAND";
    public static String playerNotFoundClickValue = "";
    public static boolean playerNotFoundReplaceEnable = true;
    public static String playerNotFoundReplaceText = "[&cERROR]";

    public static ItemStack itemFrame1;
    public static ItemStack itemFrame2;

    public static ItemStack invFrame1;
    public static ItemStack invFrame2;
    public static String invSkullName = "";

    public static boolean allowMention = true;
    public static boolean disableHere = false;
    public static boolean disableEveryone = false;

    public static boolean clickableCommands = true;
    public static String clickableCommandsFormat = "";
    public static ClickEvent.Action clickableCommandsAction = ClickEvent.Action.SUGGEST_COMMAND;
    public static String clickableCommandsDisplay = "";
    public static String clickableCommandsHoverText = null;

    public static String noPermissionMessage = "&cYou do not have permission to use that command!";
    public static String invExpiredMessage = "&cThis inventory view has expired!";
    public static String reloadPluginMessage = "&aInteractive Chat has been reloaded!";
    public static String noConsoleMessage = "";
    public static String invalidPlayerMessage = "";
    public static String listPlaceholderHeader = "";
    public static String listPlaceholderBody = "";
    public static String notEnoughArgs = "";
    public static String setInvDisplayLayout = "";
    public static String invalidArgs = "";
    public static String placeholderCooldownMessage = "";
    public static String universalCooldownMessage = "";
    public static String bedrockEventsMenuTitle = "";
    public static String bedrockEventsMenuContent = "";
    public static String bedrockEventsMenuRunSuggested = "";

    public static Map<String, UUID> messages = new ConcurrentHashMap<>();
    public static Map<String, Long> keyTime = new ConcurrentHashMap<>();
    public static Map<String, ICPlayer> keyPlayer = new ConcurrentHashMap<>();

    public static int invDisplayLayout = 0;

    public static long itemDisplayTimeout = 300000;
    public static boolean hideLodestoneCompassPos = false;

    public static ConcurrentCacheHashMap<String, Inventory> itemDisplay = new ConcurrentCacheHashMap<>(InteractiveChat.itemDisplayTimeout, 60000);
    public static ConcurrentCacheHashMap<String, Inventory> inventoryDisplay = new ConcurrentCacheHashMap<>(InteractiveChat.itemDisplayTimeout, 60000);
    public static ConcurrentCacheHashMap<String, Inventory> inventoryDisplay1Upper = new ConcurrentCacheHashMap<>(InteractiveChat.itemDisplayTimeout, 60000);
    public static ConcurrentCacheHashMap<String, Inventory> inventoryDisplay1Lower = new ConcurrentCacheHashMap<>(InteractiveChat.itemDisplayTimeout, 60000);
    public static ConcurrentCacheHashMap<String, Inventory> enderDisplay = new ConcurrentCacheHashMap<>(InteractiveChat.itemDisplayTimeout, 60000);
    public static ConcurrentCacheHashMap<String, ItemStack> mapDisplay = new ConcurrentCacheHashMap<>(InteractiveChat.itemDisplayTimeout, 60000);
    public static Set<Inventory> upperSharedInventory = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    public static Set<Inventory> lowerSharedInventory = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static Map<Inventory, ValuePairs<Inventory, String>> containerDisplay = new ConcurrentHashMap<>();

    public static Map<UUID, String> viewingInv1 = new ConcurrentHashMap<>();

    public static long universalCooldown = 0;

    public static Map<UUID, ICPlaceholder> placeholderList = new LinkedHashMap<>();
    public static int maxPlaceholders = -1;
    public static String limitReachMessage = "&cPlease do now use excessive amount of placeholders in one message!";

    public static List<MentionPair> mentionPair = Collections.synchronizedList(new ArrayList<>());
    public static Map<UUID, Map<UUID, Long>> lastNonSilentMentionTime = new ConcurrentHashMap<>();
    public static String mentionPrefix = "@";
    public static String mentionHighlight = "&e{MentionedPlayer}";
    public static String mentionHighlightOthers = "&3{MentionedPlayer}";
    public static String mentionHover = "&e{MentionedPlayer}";
    public static double mentionDuration = 2;
    public static long mentionCooldown = 3000;
    public static String mentionEnable = "";
    public static String mentionDisable = "";
    public static String mentionTitle = "";
    public static String mentionSubtitle = "";
    public static String mentionActionbar = "";
    public static String mentionToast = "";
    public static String mentionBossBarText = "";
    public static String mentionBossBarColorName = "";
    public static String mentionBossBarOverlayName = "";
    public static String mentionSound = "";
    public static int mentionTitleDuration = 0;
    public static int mentionBossBarDuration = 0;
    public static int mentionBossBarRemoveDelay = 0;

    public static List<String> commandList = new ArrayList<>();

    public static Set<String> messageToIgnore = new HashSet<>();

    public static Map<Plugin, Function<UUID, List<String>>> pluginNicknames = new ConcurrentHashMap<>();

    public static boolean filterUselessColorCodes = true;

    public static boolean updaterEnabled = true;
    public static boolean cancelledMessage = true;

    public static boolean useCustomPlaceholderPermissions = false;

    public static boolean sendOriginalIfTooLong = false;

    public static AtomicLong messagesCounter = new AtomicLong(0);

    public static boolean parsePAPIOnMainThread = false;

    public static Boolean bungeecordMode = false;
    public static Map<String, List<ICPlaceholder>> remotePlaceholderList = new HashMap<>();
    public static int remoteDelay = 500;
    public static boolean queueRemoteUpdate = false;

    public static ItemStack unknownReplaceItem;

    public static boolean useAccurateSenderFinder = true;
    public static boolean tagEveryIdentifiableMessage = false;

    public static boolean useBukkitDisplayName = true;
    public static boolean useEssentialsNicknames = true;

    public static boolean rgbTags = true;
    public static boolean fontTags = true;
    public static List<Pattern> additionalRGBFormats = new ArrayList<>();

    public static int itemTagMaxLength = 32767;
    public static int packetStringMaxLength = 32767;

    public static boolean forceUnsignedChatPackets = false;
    public static boolean forceUnsignedChatCommandPackets = false;
    public static boolean hideServerUnsignedStatus = true;
    public static boolean skipDetectSpamRateWhenDispatchingUnsignedPackets = false;

    public static boolean ecoSetLoreOnMainThread = false;

    public static BungeeMessageListener bungeeMessageListener;
    public static PlayerDataManager playerDataManager;
    public static PlaceholderCooldownManager placeholderCooldownManager;
    public static NicknameManager nicknameManager;
    public static Database database;

    public static Map<UUID, List<SignedMessageModificationData>> signedMessageModificationData = new ConcurrentHashMap<>();
    public static Map<Plugin, ValuePairs<Integer, BiFunction<ItemStack, UUID, ItemStack>>> itemStackTransformFunctions = new ConcurrentHashMap<>();

    public static void closeSharedInventoryViews() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (InteractiveChat.containerDisplay.containsKey(topInventory) || InteractiveChat.upperSharedInventory.contains(topInventory)) {
                player.closeInventory();
                if (InteractiveChat.viewingInv1.remove(player.getUniqueId()) != null) {
                    InventoryUtils.restorePlayerInventory(player);
                }
            }
        }
    }

    public static void closeInventoryViews(Inventory inventory) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (topInventory.equals(inventory)) {
                player.closeInventory();
                if (InteractiveChat.viewingInv1.remove(player.getUniqueId()) != null) {
                    InventoryUtils.restorePlayerInventory(player);
                }
            }
        }
    }

    public static void sendMessage(CommandSender sender, Component component) {
        if (InteractiveChat.version.isLegacyRGB()) {
            try {
                sender.spigot().sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.legacyGson().serialize(component)));
            } catch (Throwable e) {
                if (sender instanceof Player) {
                    ((Player) sender).spigot().sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.legacyGson().serialize(component)));
                } else {
                    sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
                }
            }
        } else {
            sender.spigot().sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)));
        }
    }

    public static boolean isPluginEnabled(String name) {
        return isPluginEnabled(name, true);
    }

    public static boolean isPluginEnabled(String name, boolean checkRunning) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && (!checkRunning || plugin.isEnabled());
    }

    public static boolean hasChatSigning() {
        return MinecraftVersion.getCurrentVersion().compareTo(new MinecraftVersion(1, 19, 1)) >= 0;
    }

    public ProcessExternalMessage externalProcessor;

    @Override
    public void onEnable() {
        plugin = this;

        externalProcessor = new ProcessExternalMessage();

        getServer().getPluginManager().registerEvents(new Debug(), this);

        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        exactMinecraftVersion = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(") + 5, Bukkit.getVersion().indexOf(")"));
        version = MCVersion.resolve();

        if (!version.isSupported()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] This version of minecraft is unsupported! (" + version.toString() + ")");
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        try {
            ConfigManager.setup();
        } catch (IOException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        protocolManager = ProtocolLibrary.getProtocolManager();

        getCommand("interactivechat").setExecutor(new Commands());

        bungeecordMode = ConfigManager.getConfig().getBoolean("Settings.Bungeecord");

        if (bungeecordMode) {
            getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] Registering Plugin Messaging Channels for bungeecord...");
            getServer().getMessenger().registerOutgoingPluginChannel(this, "interchat:main");
            getServer().getMessenger().registerIncomingPluginChannel(this, "interchat:main", bungeeMessageListener = new BungeeMessageListener(this));
            getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
            ServerPingListener.listen();

            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                if (parsePAPIOnMainThread) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            PlaceholderParser.parse(ICPlayerFactory.getICPlayer(player), usePlayerNameHoverText);
                            PlaceholderParser.parse(ICPlayerFactory.getICPlayer(player), usePlayerNameClickValue);
                        }
                    });
                } else {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlaceholderParser.parse(ICPlayerFactory.getICPlayer(player), usePlayerNameHoverText);
                        PlaceholderParser.parse(ICPlayerFactory.getICPlayer(player), usePlayerNameClickValue);
                    }
                }
            }, 0, 100);

            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Map<UUID, Boolean> vanishStates = new HashMap<>();
                for (ICPlayer player : ICPlayerFactory.getOnlineICPlayers()) {
                    if (player.isLocal()) {
                        vanishStates.put(player.getUniqueId(), player.isVanished());
                    }
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        BungeeMessageSender.updatePlayersVanished(System.currentTimeMillis(), vanishStates);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }, 0, 40);
        }

        BiConsumer<String, Inventory> inventoryRemovalListener = (hash, inv) -> {
            Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> closeInventoryViews(inv));
        };
        itemDisplay.registerRemovalListener(inventoryRemovalListener);
        inventoryDisplay.registerRemovalListener(inventoryRemovalListener);
        inventoryDisplay1Upper.registerRemovalListener(inventoryRemovalListener);
        inventoryDisplay1Lower.registerRemovalListener(inventoryRemovalListener);
        enderDisplay.registerRemovalListener(inventoryRemovalListener);

        mapDisplay.registerRemovalListener((hash, item) -> {
            Bukkit.getScheduler().runTask(InteractiveChat.plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean removed = MapViewer.MAP_VIEWERS.remove(player, item);
                    if (removed) {
                        //noinspection deprecation
                        player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                    }
                }
            });
        });

        YamlFile storage = ConfigManager.getStorageConfig();
        database = new Database(false, getDataFolder(), storage.getString("StorageType"), storage.getString("MYSQL.Host"), storage.getString("MYSQL.Database"), storage.getString("MYSQL.Username"), storage.getString("MYSQL.Password"), storage.getInt("MYSQL.Port"));
        database.setup();

        placeholderCooldownManager = new PlaceholderCooldownManager();

        getServer().getPluginManager().registerEvents(new ChatEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        getServer().getPluginManager().registerEvents(new InventoryEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerUtils(), this);
        getServer().getPluginManager().registerEvents(new OutMessagePacket(), this);
        getServer().getPluginManager().registerEvents(new MapViewer(), this);
        OutMessagePacket.messageListeners();
        if (version.isNewerOrEqualTo(MCVersion.V1_19)) {
            RedispatchSignedPacket.packetListener();
            if (ModernChatCompletionTask.isSupported()) {
                getServer().getPluginManager().registerEvents(new ModernChatCompletionTask(), this);
            }
        }
        if (!version.isLegacy()) {
            OutTabCompletePacket.tabCompleteListener();
        }
        if (version.isNewerOrEqualTo(MCVersion.V1_17)) {
            try {
                Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
                getServer().getPluginManager().registerEvents(new PaperChatEvents(), this);
            } catch (ClassNotFoundException ignore) {
            }
        }

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();

        if (isPluginEnabled("CMI", false)) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into CMI!");
            cmiHook = true;
        }

        if (isPluginEnabled("Essentials")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into Essentials!");
            essentialsHook = true;
            getServer().getPluginManager().registerEvents(new EssentialsNicknames(), this);
            EssentialsNicknames.init();
        }

        if (isPluginEnabled("EssentialsDiscord")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into EssentialsDiscord!");
            essentialsDiscordHook = true;
            getServer().getPluginManager().registerEvents(new EssentialsDiscord(), this);
        }

        if (isPluginEnabled("DiscordSRV", false)) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into DiscordSRV!");
            DiscordSRV.api.subscribe(new DiscordSRVEvents());
            discordSrvHook = true;
        }

        if (isPluginEnabled("ViaVersion")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ViaVersion!");
            viaVersionHook = true;
        }

        if (isPluginEnabled("ProtocolSupport")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ProtocolSupport!");
            protocolSupportHook = true;
        }

        if (isPluginEnabled("eco")) {
            EcoHook.init();
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into Eco (Core)!");
            ecoHook = true;
        }

        if (isPluginEnabled("ExcellentEnchants")) {
            ExcellentEnchantsHook.init();
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ExcellentEnchants!");
            excellentenchantsHook = true;
        }

        if (isPluginEnabled("LuckPerms")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into LuckPerms!");
            new LuckPermsEvents(this);
            luckPermsHook = true;
        }

        if (isPluginEnabled("MysqlPlayerDataBridge")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into MysqlPlayerDataBridge!");
            mysqlPDBHook = true;
        }

        if (isPluginEnabled("ChatControlRed", false)) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ChatControlRed!");
            chatControlRedHook = true;
        }

        if (isPluginEnabled("floodgate")) {
            getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into Floodgate!");
            getServer().getPluginManager().registerEvents(new FloodgateHook(), this);
            floodgateHook = true;
        }

        if (isPluginEnabled("VentureChat")) {
            getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into VentureChat!");
            VentureChatInjection._init_();
            ventureChatHook = true;
        }

        if (isPluginEnabled("dynmap")) {
            getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into Dynmap!");
            DynmapListener._init_();
            dynmapHook = true;
        }

        PlayernameDisplay.setup();
        MentionDisplay.setup();

        Charts.setup(metrics);

        if (updaterEnabled) {
            getServer().getPluginManager().registerEvents(new Updater(), this);
        }

        ClientSettingPacket.clientSettingsListener();

        playerDataManager = new PlayerDataManager(this, database);
        nicknameManager = new NicknameManager(uuid -> InteractiveChatAPI.getNicknames(uuid), () -> InteractiveChatAPI.getOnlineICPlayers().stream().filter(each -> each.isLocal()).map(each -> each.getUniqueId()).collect(Collectors.toSet()), 5000, (uuid, nicknames) -> {
            if (InteractiveChat.bungeecordMode) {
                Player bukkitPlayer = Bukkit.getPlayer(uuid);
                if (bukkitPlayer != null) {
                    Set<String> nicks = new HashSet<>(nicknames);
                    if (InteractiveChat.useBukkitDisplayName) {
                        nicks.add(bukkitPlayer.getDisplayName());
                    }
                    try {
                        BungeeMessageSender.forwardNicknames(System.currentTimeMillis(), uuid, nicks);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        }

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] InteractiveChat has been Enabled!");

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (queueRemoteUpdate && Bukkit.getOnlinePlayers().size() > 0) {
                try {
                    if (BungeeMessageSender.resetAndForwardPlaceholderList(System.currentTimeMillis(), InteractiveChat.placeholderList.values())) {
                        queueRemoteUpdate = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 100);

        try {
            Logger logger = LogManager.getRootLogger();
            LogFilter filter = new LogFilter();
            Method method = logger.getClass().getMethod("addFilter", Filter.class);
            method.invoke(logger, filter);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] Unable to add filter to logger, safely skipping...");
        }

        gc();
    }

    @Override
    public void onDisable() {
        closeSharedInventoryViews();
        if (nicknameManager != null) {
            nicknameManager.close();
        }
        try {
            OutMessagePacket.getAsyncChatSendingExecutor().close();
        } catch (Exception ignored) {
        }
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] InteractiveChat has been Disabled!");
    }

    private void gc() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            itemDisplay.cleanUp();
            inventoryDisplay.cleanUp();
            inventoryDisplay1Upper.cleanUp();
            inventoryDisplay1Lower.cleanUp();
            enderDisplay.cleanUp();
            mapDisplay.cleanUp();
        }, 0, 1200);
    }

}
