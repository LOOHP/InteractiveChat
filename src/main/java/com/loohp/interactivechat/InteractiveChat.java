package com.loohp.interactivechat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.loohp.interactivechat.BungeeMessaging.BungeeMessageListener;
import com.loohp.interactivechat.BungeeMessaging.BungeeMessageSender;
import com.loohp.interactivechat.BungeeMessaging.ServerPingListener;
import com.loohp.interactivechat.Data.Database;
import com.loohp.interactivechat.Data.PlayerDataManager;
import com.loohp.interactivechat.Debug.Debug;
import com.loohp.interactivechat.Hooks.DiscordSRV.DiscordSRVEvents;
import com.loohp.interactivechat.Hooks.Dynmap.DynmapListener;
import com.loohp.interactivechat.Hooks.Essentials.EssentialsNicknames;
import com.loohp.interactivechat.Hooks.VentureChat.PacketListener;
import com.loohp.interactivechat.Listeners.ClientSettingPacket;
import com.loohp.interactivechat.Listeners.Events;
import com.loohp.interactivechat.Listeners.InChatPacket;
import com.loohp.interactivechat.Listeners.MapViewer;
import com.loohp.interactivechat.Listeners.OutChatPacket;
import com.loohp.interactivechat.Listeners.OutTabCompletePacket;
import com.loohp.interactivechat.Metrics.Charts;
import com.loohp.interactivechat.Metrics.Metrics;
import com.loohp.interactivechat.Modules.ProcessExternalMessage;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderInfo;
import com.loohp.interactivechat.ObjectHolders.CompatibilityListener;
import com.loohp.interactivechat.ObjectHolders.ICPlaceholder;
import com.loohp.interactivechat.ObjectHolders.ICPlayer;
import com.loohp.interactivechat.ObjectHolders.LogFilter;
import com.loohp.interactivechat.ObjectHolders.MentionPair;
import com.loohp.interactivechat.ObjectHolders.SenderPlaceholderInfo;
import com.loohp.interactivechat.ObjectHolders.SharedDisplayTimeoutInfo;
import com.loohp.interactivechat.PlaceholderAPI.Placeholders;
import com.loohp.interactivechat.Updater.Updater;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.PlaceholderParser;
import com.loohp.interactivechat.Utils.PlayerUtils;
import com.loohp.interactivechat.Utils.PotionUtils;
import com.loohp.interactivechat.Utils.RarityUtils;

import github.scarsz.discordsrv.DiscordSRV;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.milkbowl.vault.permission.Permission;

public class InteractiveChat extends JavaPlugin {
	
	public static final int BSTATS_PLUGIN_ID = 6747;
	
	public static InteractiveChat plugin = null;
	
	public static String exactMinecraftVersion;
	public static MCVersion version;
	
	public static ProtocolManager protocolManager;
	
	public static String space0 = "\u200B";
	public static String space1 = "\u200A";
	public static String nullString = null;
	
	public static String language = "en_us";
	
	public static Boolean essentialsHook = false;
	public static Boolean chatManagerHook = false;
	public static Boolean vanishHook = false;
	public static Boolean cmiHook = false;
	public static Boolean ventureChatHook = false;
	public static Boolean discordSrvHook = false;
	public static Boolean dynmapHook = false;
	
	public static Permission perms = null;
	
	public static boolean t = true;
	
	public static boolean useItem = true;
	public static boolean useInventory = true;
	public static boolean useEnder = true;
	
	public static boolean itemMapPreview = true;
	
	public static boolean itemCaseSensitive = false;
	public static boolean invCaseSensitive = false;
	public static boolean enderCaseSensitive = false;
	
	public static String itemPlaceholder = "[item]";
	public static String invPlaceholder = "[inv]";
	public static String enderPlaceholder = "[ender]";
	
	public static String itemReplaceText = "&f[&f{Item} &bx{Amount}&f]";
	public static String invReplaceText = "&f[&b%player_name%'s Inventory&f]";
	public static String enderReplaceText = "&f[&d%player_name%'s Ender Chest&f]";
	
	public static String itemTitle = "%player_name%'s Item";
	public static String invTitle = "%player_name%'s Inventory";
	public static String enderTitle = "%player_name%'s Ender Chest";
	
	public static String containerViewTitle = "Container Contents";
	
	public static boolean usePlayerName = true;
	public static boolean usePlayerNameHoverEnable = true;
	public static String usePlayerNameHoverText = "";
	public static boolean usePlayerNameClickEnable = true;
	public static String usePlayerNameClickAction = "SUGGEST_COMMAND";
	public static String usePlayerNameClickValue = "";
	public static boolean usePlayerNameCaseSensitive = true;
	public static boolean usePlayerNameOnTranslatables = true;
	
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
	public static String clickableCommandsPrefix = "[";
	public static String clickableCommandsSuffix = "]";
	public static ClickEvent.Action clickableCommandsAction = ClickEvent.Action.SUGGEST_COMMAND;
	public static String clickableCommandsFormat = "";
	public static String clickableCommandsHoverText = null;
	public static boolean clickableCommandsEnforceColors = true;
	
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
	
	public static Map<Long, Set<String>> cooldownbypass = new ConcurrentHashMap<>();
	
	public static long universalCooldown = -1;
	
	public static Map<UUID, Map<String, Long>> placeholderCooldowns = new HashMap<>();
	public static Map<UUID, Long> universalCooldowns = new ConcurrentHashMap<>();
	
	public static List<ICPlaceholder> placeholderList = new ArrayList<>();
	public static int maxPlacholders = -1;
	public static String limitReachMessage = "&cPlease do now use excessive amount of placeholders in one message!";
	
	public static Map<Player, Long> mentionCooldown = new ConcurrentHashMap<>();	
	public static Map<UUID, MentionPair> mentionPair = new ConcurrentHashMap<>();
	public static String mentionPrefix = "@";
	public static String mentionHightlight = "&e{MentionedPlayer}";
	public static String mentionHover = "&e{MentionedPlayer}";
	public static long mentionDuration = 2;
	public static String mentionEnable = "";
	public static String mentionDisable = "";
	
	public static List<String> commandList = new ArrayList<String>();
	public static Map<String, CommandPlaceholderInfo> commandPlaceholderMatch = new ConcurrentHashMap<>();
	
	public static Set<String> messageToIgnore = new HashSet<>();
	
	public static Map<Plugin, Function<UUID, List<String>>> pluginNicknames = new ConcurrentHashMap<>();
	
	public static boolean filterUselessColorCodes = true;
	
	public static Map<String, String> aliasesMapping = new ConcurrentHashMap<>();
	
	public static boolean updaterEnabled = true;
	public static boolean cancelledMessage = true;
	
	public static boolean legacyChatAPI = false;
	public static boolean useCustomPlaceholderPermissions = false;
	
	public static boolean sendOriginalIfTooLong = false;
	
	public static Optional<Character> chatAltColorCode = Optional.empty();
	
	public static AtomicLong messagesCounter = new AtomicLong(0);
	
	public static Boolean bungeecordMode = false;
	public static BiMap<UUID, ICPlayer> remotePlayers = Maps.synchronizedBiMap(HashBiMap.create());
	public static Map<String, List<ICPlaceholder>> remotePlaceholderList = new HashMap<>();
	public static int remoteDelay = 500;
	public static boolean queueRemoteUpdate = false;
	
	public static ItemStack unknownReplaceItem;
	
	public static Map<String, CompatibilityListener> compatibilityListeners = new LinkedHashMap<>();
	
	public static boolean useAccurateSenderFinder = true;
	public static Map<String, SenderPlaceholderInfo> senderPlaceholderMatch = new ConcurrentHashMap<>();
	
	public static PlayerDataManager playerDataManager;
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
        File configFile = new File(getDataFolder(), "config.yml"); 
        if (!configFile.exists()) {
            try (InputStream in = this.getClassLoader().getResourceAsStream("config_default.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                getLogger().severe("[InteractiveChat] Unable to copy config.yml");
            }
        }
		
        plugin.getConfig().options().header("For information on what each option does. Please refer to https://github.com/LOOHP/InteractiveChat/blob/master/src/main/resources/config_default.yml");
		plugin.getConfig().options().copyDefaults(true);
		ConfigManager.saveConfig();
		
		File storageFile = new File(getDataFolder(), "storage.yml"); 
        if (!storageFile.exists()) {
            try (InputStream in = this.getClassLoader().getResourceAsStream("storage.yml")) {
                Files.copy(in, storageFile.toPath());
            } catch (IOException e) {
                getLogger().severe("[InteractiveChat] Unable to copy storage.yml");
            }
    	}
		
		if (getConfig().contains("Settings.BlockMessagesLongerThan30000RegardlessOfVersion")) {
			getConfig().set("Settings.BlockMessagesLongerThan30000RegardlessOfVersion", null);
			saveConfig();
		}
		
		protocolManager = ProtocolLibrary.getProtocolManager();

	    getCommand("interactivechat").setExecutor(new Commands());
	    
	    bungeecordMode = getConfig().getBoolean("Settings.Bungeecord");
	    
		if (bungeecordMode) {
			getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] Registering Plugin Messaging Channels for bungeecord...");
			getServer().getMessenger().registerOutgoingPluginChannel(this, "interchat:main");
		    getServer().getMessenger().registerIncomingPluginChannel(this, "interchat:main", new BungeeMessageListener(this));
		    getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
		    ServerPingListener.listen();
		    
		    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
		    	for (Player player : Bukkit.getOnlinePlayers()) {
		    		PlaceholderParser.parse(new ICPlayer(player), usePlayerNameHoverText);
		    		PlaceholderParser.parse(new ICPlayer(player), usePlayerNameClickValue);
		    	}
		    }, 0, 100);
		}
	    
	    ConfigManager.loadConfig();
	    
	    FileConfiguration storage = ConfigManager.getStorageConfig();
		database = new Database(false, getDataFolder(), storage.getString("StorageType"), storage.getString("MYSQL.Host"), storage.getString("MYSQL.Database"), storage.getString("MYSQL.Username"), storage.getString("MYSQL.Password"), storage.getInt("MYSQL.Port"));
		database.setup();
	    
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
			EssentialsNicknames.setup();
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("ChatManager") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ChatManager!");
			chatManagerHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("VentureChat") != null) {
	    	protocolManager.getPacketListeners().forEach(each -> {
	    		if (each.getPlugin().getName().equals("VentureChat")) {
	    			ListeningWhitelist whitelist = each.getSendingWhitelist();
	    			if (whitelist.getTypes().contains(PacketType.Play.Server.CHAT)) {
	    				if (whitelist.getPriority().equals(ListenerPriority.MONITOR)) {
	    					protocolManager.removePacketListener(each);
	    					protocolManager.addPacketListener(new PacketListener());
	    					return;
	    				}
	    			}
	    		}
	    	});
	    	getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into VentureChat!");
	    	ventureChatHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into DiscordSRV!");
	    	DiscordSRV.api.subscribe(new DiscordSRVEvents());
			discordSrvHook = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Dynmap") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractiveChat] InteractiveChat has injected into Dynmap!");
	    	DynmapListener._init_();
			dynmapHook = true;
		}
		
	    RarityUtils.setupRarity();
	    PotionUtils.setupPotions();
	    
	    Charts.setup(metrics);
	    
	    if (updaterEnabled) {
	    	getServer().getPluginManager().registerEvents(new Updater(), this);
	    }
	    
	    ClientSettingPacket.clientSettingsListener();
	    
	    playerDataManager = new PlayerDataManager(this, database);
	    
	    if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new Placeholders().register();
		}
	    
	    try {
			TextComponent test = new TextComponent("Legacy Bungeecord Chat API Test");
			test.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new Text("Test Hover Text")));
			test.getHoverEvent().getContents();
			legacyChatAPI = false;
		} catch (Throwable e) {
			legacyChatAPI = true;
			getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] Legacy Bungeecord Chat API detected, using legacy methods...");
		}
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] InteractiveChat has been Enabled!");
	    
	    for (Player player : Bukkit.getOnlinePlayers()) {
			InteractiveChat.mentionCooldown.put(player, (System.currentTimeMillis() - 3000));
		}
	    
	    Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
	    	if (queueRemoteUpdate && Bukkit.getOnlinePlayers().size() > 0) {
	    		try {
					if (BungeeMessageSender.resetAndForwardPlaceholderList(InteractiveChat.placeholderList) && BungeeMessageSender.resetAndForwardAliasMapping(InteractiveChat.aliasesMapping)) {
						queueRemoteUpdate = false;
					}
				} catch (IOException e) {
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
	    
	    displayTimeout();
	}

	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] InteractiveChat has been Disabled!");
	}
	
	private void displayTimeout() {
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
}
