package com.loohp.interactivechat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageListener;
import com.loohp.interactivechat.bungeemessaging.BungeeMessageSender;
import com.loohp.interactivechat.bungeemessaging.ServerPingListener;
import com.loohp.interactivechat.config.ConfigManager;
import com.loohp.interactivechat.data.Database;
import com.loohp.interactivechat.data.PlayerDataManager;
import com.loohp.interactivechat.debug.Debug;
import com.loohp.interactivechat.hooks.discordsrv.DiscordSRVEvents;
import com.loohp.interactivechat.hooks.dynmap.DynmapListener;
import com.loohp.interactivechat.hooks.essentials.EssentialsDiscord;
import com.loohp.interactivechat.hooks.essentials.EssentialsNicknames;
import com.loohp.interactivechat.hooks.luckperms.LuckPermsEvents;
import com.loohp.interactivechat.hooks.venturechat.VentureChatInjection;
import com.loohp.interactivechat.listeners.ClientSettingPacket;
import com.loohp.interactivechat.listeners.Events;
import com.loohp.interactivechat.listeners.InChatPacket;
import com.loohp.interactivechat.listeners.MapViewer;
import com.loohp.interactivechat.listeners.OutChatPacket;
import com.loohp.interactivechat.listeners.OutTabCompletePacket;
import com.loohp.interactivechat.metrics.Charts;
import com.loohp.interactivechat.metrics.Metrics;
import com.loohp.interactivechat.modules.PlayernameDisplay;
import com.loohp.interactivechat.modules.ProcessExternalMessage;
import com.loohp.interactivechat.objectholders.CompatibilityListener;
import com.loohp.interactivechat.objectholders.ICPlaceholder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.objectholders.LogFilter;
import com.loohp.interactivechat.objectholders.MentionPair;
import com.loohp.interactivechat.objectholders.PlaceholderCooldownManager;
import com.loohp.interactivechat.objectholders.SharedDisplayTimeoutInfo;
import com.loohp.interactivechat.placeholderapi.Placeholders;
import com.loohp.interactivechat.updater.Updater;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechat.utils.PotionUtils;
import com.loohp.interactivechat.utils.RarityUtils;

import github.scarsz.discordsrv.DiscordSRV;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;

@SuppressWarnings("deprecation")
public class InteractiveChat extends JavaPlugin {
	
	public static final int BSTATS_PLUGIN_ID = 6747;
	
	public static Optional<Character> chatAltColorCode = Optional.empty();
	
	public static final Function<String, String> COLOR_CHAR_ESCAPE = str -> str.replaceAll("(?i)(?<!\\\\)\\\\u00A7", InteractiveChat.chatAltColorCode.orElse(' ') + "");
	public static final Function<String, String> COLOR_CHAR_UNESCAPE = str -> str.replaceAll("(?i)(?<!\\\\)\\\\u00A7", ChatColor.COLOR_CHAR + "");
	
	public static InteractiveChat plugin = null;
	
	public static String exactMinecraftVersion;
	public static MCVersion version;
	
	public static ProtocolManager protocolManager;
	
	public static String language = "en_us";
	
	public static Boolean essentialsHook = false;
	public static Boolean essentialsDiscordHook = false;
	public static Boolean chatManagerHook = false;
	public static Boolean vanishHook = false;
	public static Boolean cmiHook = false;
	public static Boolean ventureChatHook = false;
	public static Boolean discordSrvHook = false;
	public static Boolean dynmapHook = false;
	public static Boolean viaVersionHook = false;
	public static Boolean procotcolSupportHook = false;
	public static Boolean ecoHook = false;
	public static Boolean luckPermsHook = false;
	public static Boolean mysqlPDBHook = false;
	
	public static Permission perms = null;
	
	public static boolean t = true;
	
	public static boolean useItem = true;
	public static boolean useInventory = true;
	public static boolean useEnder = true;
	
	public static boolean itemMapPreview = true;
	
	public static Pattern itemPlaceholder = null;
	public static Pattern invPlaceholder = null;
	public static Pattern enderPlaceholder = null;
	
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
	
	public static Map<String, UUID> messages = new ConcurrentHashMap<>();
	public static Map<String, Long> keyTime = new ConcurrentHashMap<>();
	public static Map<String, ICPlayer> keyPlayer = new ConcurrentHashMap<>();
	
	public static long itemDisplayTimeout = 0;
	
	public static int invDisplayLayout = 0;
	
	public static Queue<SharedDisplayTimeoutInfo> itemDisplayTimeouts = new ConcurrentLinkedQueue<>();
	
	public static BiMap<String, Inventory> itemDisplay = Maps.synchronizedBiMap(HashBiMap.create());
	public static BiMap<String, Inventory> inventoryDisplay = Maps.synchronizedBiMap(HashBiMap.create());
	public static BiMap<String, Inventory> inventoryDisplay1Upper = Maps.synchronizedBiMap(HashBiMap.create());
	public static BiMap<String, Inventory> inventoryDisplay1Lower = Maps.synchronizedBiMap(HashBiMap.create());
	public static BiMap<String, Inventory> enderDisplay = Maps.synchronizedBiMap(HashBiMap.create());
	public static Map<String, ItemStack> mapDisplay = new ConcurrentHashMap<>();
	
	public static Set<Inventory> containerDisplay = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	public static Map<UUID, String> viewingInv1 = new ConcurrentHashMap<>();
	
	public static long universalCooldown = 0;
	
	public static Map<UUID, ICPlaceholder> placeholderList = new LinkedHashMap<>();
	public static int maxPlaceholders = -1;
	public static String limitReachMessage = "&cPlease do now use excessive amount of placeholders in one message!";
	
	public static List<MentionPair> mentionPair = Collections.synchronizedList(new ArrayList<>());
	public static String mentionPrefix = "@";
	public static String mentionHightlight = "&e{MentionedPlayer}";
	public static String mentionHover = "&e{MentionedPlayer}";
	public static long mentionDuration = 2;
	public static String mentionEnable = "";
	public static String mentionDisable = "";
	
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
	
	public static List<CompatibilityListener> compatibilityListeners = new ArrayList<>();
	public static Map<EventPriority, Set<RegisteredListener>> isolatedAsyncListeners = new EnumMap<>(EventPriority.class);
	public static Map<EventPriority, Set<RegisteredListener>> isolatedSyncListeners = new EnumMap<>(EventPriority.class);
	public static Map<EventPriority, Set<RegisteredListener>> superVanishPremiumVanishListeners = new EnumMap<>(EventPriority.class);
	
	public static boolean useAccurateSenderFinder = true;
	
	public static boolean useBukkitDisplayName = true;
	public static boolean useEssentialsNicknames = true;
	
	public static boolean rgbTags = true;
	public static boolean fontTags = true;
	
	public static int itemTagMaxLength = 32767;
	public static int packetStringMaxLength = 32767;
	
	public static BungeeMessageListener bungeeMessageListener;
	public static PlayerDataManager playerDataManager;
	public static PlaceholderCooldownManager placeholderCooldownManager;
	public static Database database;
	
	public ProcessExternalMessage externalProcessor;

	@Override
	public void onEnable() {	
		plugin = this;
		
		externalProcessor = new ProcessExternalMessage();
		
		getServer().getPluginManager().registerEvents(new Debug(), this);

		Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
		
		exactMinecraftVersion = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(") + 5, Bukkit.getVersion().indexOf(")"));
		version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());

        if (!version.isSupported()) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] This version of minecraft is unsupported! (" + version.toString() + ")");
	    }
        
        if (!getDataFolder().exists()) {
        	getDataFolder().mkdirs();
        }
        try {
			ConfigManager.setup();
		} catch (IOException | InvalidConfigurationException e) {
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
		}
	    
	    FileConfiguration storage = ConfigManager.getStorageConfig();
		database = new Database(false, getDataFolder(), storage.getString("StorageType"), storage.getString("MYSQL.Host"), storage.getString("MYSQL.Database"), storage.getString("MYSQL.Username"), storage.getString("MYSQL.Password"), storage.getInt("MYSQL.Port"));
		database.setup();
		
		placeholderCooldownManager = new PlaceholderCooldownManager();
	    
	    getServer().getPluginManager().registerEvents(new Events(), this);
	    getServer().getPluginManager().registerEvents(new PlayerUtils(), this);
	    getServer().getPluginManager().registerEvents(new OutChatPacket(), this);
	    getServer().getPluginManager().registerEvents(new MapViewer(), this);
	    OutChatPacket.chatMessageListener();
	    InChatPacket.chatMessageListener();
	    if (!version.isLegacy()) {
	    	OutTabCompletePacket.tabCompleteListener();
	    }
	    
	    RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        
        if (getServer().getPluginManager().getPlugin("SuperVanish") != null || getServer().getPluginManager().getPlugin("PremiumVanish") != null) {
        	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into SuperVanish/PremiumVanish!");
			vanishHook = true;
		}
		if (getServer().getPluginManager().getPlugin("CMI") != null) {
			getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into CMI!");
			cmiHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into Essentials!");
			essentialsHook = true;
			getServer().getPluginManager().registerEvents(new EssentialsNicknames(), this);
			EssentialsNicknames._init_();
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("EssentialsDiscord") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into EssentialsDiscord!");
			essentialsDiscordHook = true;
			getServer().getPluginManager().registerEvents(new EssentialsDiscord(), this);
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("ChatManager") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ChatManager!");
			chatManagerHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into DiscordSRV!");
	    	DiscordSRV.api.subscribe(new DiscordSRVEvents());
			discordSrvHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("ViaVersion") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ViaVersion!");
			viaVersionHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("ProtocolSupport") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ProtocolSupport!");
			procotcolSupportHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("eco") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into eco (core)!");
			ecoHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("LuckPerms") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into LuckPerms!");
	    	new LuckPermsEvents(this);
			luckPermsHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("MysqlPlayerDataBridge") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into MysqlPlayerDataBridge!");
			mysqlPDBHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("VentureChat") != null) {
	    	VentureChatInjection._init_();
	    	getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into VentureChat!");
	    	ventureChatHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("dynmap") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into Dynmap!");
	    	DynmapListener._init_();
			dynmapHook = true;
		}
		
	    RarityUtils.setupRarity();
	    PotionUtils.setupPotions();
	    PlayernameDisplay.setup();
	    
	    Charts.setup(metrics);
	    
	    if (updaterEnabled) {
	    	getServer().getPluginManager().registerEvents(new Updater(), this);
	    }
	    
	    ClientSettingPacket.clientSettingsListener();
	    
	    playerDataManager = new PlayerDataManager(this, database);
	    
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
		for (Player player : Bukkit.getOnlinePlayers()) {
			Inventory topInventory = player.getOpenInventory().getTopInventory();
			if (InteractiveChat.containerDisplay.contains(topInventory) || InteractiveChat.itemDisplay.inverse().containsKey(topInventory) || InteractiveChat.inventoryDisplay.inverse().containsKey(topInventory) || InteractiveChat.inventoryDisplay1Upper.inverse().containsKey(topInventory) || InteractiveChat.enderDisplay.inverse().containsKey(topInventory)) {
				player.closeInventory();
				if (InteractiveChat.viewingInv1.remove(player.getUniqueId()) != null) {
					InventoryUtils.restorePlayerInventory(player);
				}
			}
		}
		restoreIsolatedChatListeners();
		try {
			OutChatPacket.getAsyncChatSendingExecutor().close();
		} catch (Exception e) {}
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] InteractiveChat has been Disabled!");
	}
	
	private void gc() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			List<SharedDisplayTimeoutInfo> remove = new ArrayList<>();
			long now = System.currentTimeMillis();
			for (SharedDisplayTimeoutInfo entry : itemDisplayTimeouts) {
				long timeout = entry.getTimeout();
				if (now > timeout) {
					itemDisplayTimeouts.remove(entry);
					remove.add(entry);
				}
			}
			if (!remove.isEmpty()) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					for (SharedDisplayTimeoutInfo entry : remove) {
						switch (entry.getType()) {
						case 0:
							itemDisplay.remove(entry.getHash());
							break;
						case 1:
							inventoryDisplay.remove(entry.getHash());
							break;
						case 2:
							inventoryDisplay1Upper.remove(entry.getHash());
							break;
						case 3:
							inventoryDisplay1Lower.remove(entry.getHash());
							break;
						case 4:
							enderDisplay.remove(entry.getHash());
							break;
						case 5:
							mapDisplay.remove(entry.getHash());
							break;
						}
					}
				});
			}
		}, 0, 1200);
	}
	
	/**
	 * <b>Do not invoke unless you know what you are doing!!!</b>
	 */
	@Deprecated
	public static void restoreIsolatedChatListeners() {
		HandlerList handlerList = AsyncPlayerChatEvent.getHandlerList();
		for (EventPriority priority : EventPriority.values()) {
			Set<RegisteredListener> isolatedListeners = InteractiveChat.isolatedAsyncListeners.get(priority);
			if (isolatedListeners != null) {
				for (RegisteredListener registration : isolatedListeners) {
					handlerList.register(registration);
				}
			}
			Set<RegisteredListener> vanishListeners = InteractiveChat.superVanishPremiumVanishListeners.get(priority);
			if (vanishListeners != null) {
				for (RegisteredListener registration : vanishListeners) {
					handlerList.register(registration);
				}
			}
		}
		InteractiveChat.isolatedAsyncListeners.clear();
		InteractiveChat.superVanishPremiumVanishListeners.clear();
		
		HandlerList syncHandlerList = PlayerChatEvent.getHandlerList();
		for (EventPriority priority : EventPriority.values()) {
			Set<RegisteredListener> isolatedListeners = InteractiveChat.isolatedSyncListeners.get(priority);
			if (isolatedListeners != null) {
				for (RegisteredListener registration : isolatedListeners) {
					syncHandlerList.register(registration);
				}
			}
		}
		InteractiveChat.isolatedSyncListeners.clear();
	}
	
	public static void sendMessage(CommandSender sender, Component component) {
		if (InteractiveChat.version.isLegacyRGB()) {
			sender.spigot().sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.legacyGson().serialize(component)));
		} else {
			sender.spigot().sendMessage(ComponentSerializer.parse(InteractiveChatComponentSerializer.gson().serialize(component)));
		}
	}
	
}
