package com.loohp.interactivechat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.earth2me.essentials.Essentials;
import com.loohp.interactivechat.Debug.Debug;
import com.loohp.interactivechat.Hooks.EssentialsNicknames;
import com.loohp.interactivechat.Listeners.ChatPackets;
import com.loohp.interactivechat.Listeners.ClientSettingPackets;
import com.loohp.interactivechat.Listeners.Events;
import com.loohp.interactivechat.Metrics.Charts;
import com.loohp.interactivechat.Metrics.Metrics;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderInfo;
import com.loohp.interactivechat.Updater.Updater;
import com.loohp.interactivechat.Utils.MCVersion;
import com.loohp.interactivechat.Utils.MaterialUtils;
import com.loohp.interactivechat.Utils.RarityUtils;

public class InteractiveChat extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static MCVersion version;
	
	public static ProtocolManager protocolManager;
	
	public static String space0 = "\u200B";
	public static String space1 = "\u200A";
	
	public static boolean EssentialsHook = false;
	public static boolean ChatManagerHook = false;
	
	public static boolean useItem = true;
	public static boolean useInventory = true;
	public static boolean useEnder = true;
	
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
	
	public static boolean usePlayerName = true;
	public static boolean usePlayerNameHoverEnable = true;
	public static String usePlayerNameHoverText = "";
	public static boolean usePlayerNameClickEnable = true;
	public static String usePlayerNameClickAction = "SUGGEST_COMMAND";
	public static String usePlayerNameClickValue = "";
	public static boolean usePlayerNameCaseSensitive = true;
	
	public static boolean PlayerNotFoundHoverEnable = true;
	public static String PlayerNotFoundHoverText = "&cUnable to parse placeholder..";
	public static boolean PlayerNotFoundClickEnable = false;
	public static String PlayerNotFoundClickAction = "SUGGEST_COMMAND";
	public static String PlayerNotFoundClickValue = "";
	public static boolean PlayerNotFoundReplaceEnable = true;
	public static String PlayerNotFoundReplaceText = "[&cERROR]";
	
	public static ItemStack itemFrame1;
	public static ItemStack itemFrame2;
	
	public static boolean AllowMention = true;
	
	public static String NoPermission = "&cYou do not have permission to use that command!";
	public static String InvExpired = "&cThis inventory view has expired!";
	public static String ReloadPlugin = "&aInteractive Chat has been reloaded!";
	
	public static HashMap<String, UUID> messages = new HashMap<String, UUID>();
	public static HashMap<String, Long> keyTime = new HashMap<String, Long>();
	public static HashMap<String, Player> keyPlayer = new HashMap<String, Player>();
	
	public static HashMap<Long, HashSet<String>> cooldownbypass = new HashMap<Long, HashSet<String>>();
	
	public static HashMap<Long, Inventory> itemDisplay = new HashMap<Long, Inventory>();
	public static HashMap<Long, Inventory> inventoryDisplay = new HashMap<Long, Inventory>();
	public static HashMap<Long, Inventory> enderDisplay = new HashMap<Long, Inventory>();
	
	public static HashMap<Player, HashMap<String, Long>> placeholderCooldowns = new HashMap<Player, HashMap<String, Long>>();
	public static HashMap<Player, Long> universalCooldowns = new HashMap<Player, Long>();
	
	public static List<String> placeholderList = new ArrayList<String>();
	public static int maxPlacholders = -1;
	public static String limitReachMessage = "&cPlease do now use excessive amount of placeholders in one message!";
	
	public static HashMap<Player, Long> mentionCooldown = new HashMap<Player, Long>();	
	public static HashMap<UUID, UUID> mentionPair = new HashMap<UUID, UUID>();
	public static String mentionHightlight = "&e{MentionedPlayer}";
	public static String mentionHover = "&e{MentionedPlayer}";
	public static long mentionDuration = 2;
	
	public static List<String> commandList = new ArrayList<String>();
	public static HashMap<String, CommandPlaceholderInfo> commandPlaceholderMatch = new HashMap<String, CommandPlaceholderInfo>();
	
	public static ConcurrentHashMap<Player, String> essenNick = new ConcurrentHashMap<Player, String>();
	
	public static boolean FilterUselessColorCodes = true;
	
	public static HashMap<String, String> aliasesMapping = new HashMap<String, String>();
	
	public static boolean UpdaterEnabled = true;

	@Override
	public void onEnable() {	
		plugin = (Plugin)getServer().getPluginManager().getPlugin("InteractiveChat");
		
		getServer().getPluginManager().registerEvents(new Debug(), this);
		
		int pluginId = 6747;

		Metrics metrics = new Metrics(this, pluginId);
		
		version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());

        if (!version.isSupported()) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] This version of minecraft is unsupported! (" + version.toString() + ")");
	    }
        
        if (!getDataFolder().exists()) {
        	getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "config.yml"); 
        if (!file.exists()) {
        	if (version.isOld()) {
	            try (InputStream in = this.getClassLoader().getResourceAsStream("config_old.yml")) {
	                Files.copy(in, file.toPath());
	            } catch (IOException e) {
	                getLogger().severe("[InteractiveChat] Unable to copy config.yml");
	            }
        	} else if (version.isLegacy()) {
	            try (InputStream in = this.getClassLoader().getResourceAsStream("config_legacy.yml")) {
	                Files.copy(in, file.toPath());
	            } catch (IOException e) {
	                getLogger().severe("[InteractiveChat] Unable to copy config.yml");
	            }
        	}
        }
		
		plugin.getConfig().options().copyDefaults(true);
		ConfigManager.saveConfig();
		
		protocolManager = ProtocolLibrary.getProtocolManager();

	    getCommand("interactivechat").setExecutor(new Commands());
	    
	    ConfigManager.loadConfig();
	    
	    getServer().getPluginManager().registerEvents(new Events(), this);
	    ChatPackets.chatMessageListener();
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into Essentials!");
			EssentialsHook = true;
			getServer().getPluginManager().registerEvents(new EssentialsNicknames(), this);
			EssentialsNicknames.setup();
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("ChatManager") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractiveChat] InteractiveChat has hooked into ChatManager!");
			ChatManagerHook = true;
		}
		
	    MaterialUtils.setupLang();
	    RarityUtils.setupRarity();
	    
	    Charts.setup(metrics);
	    
	    if (UpdaterEnabled) {
	    	getServer().getPluginManager().registerEvents(new Updater(), this);
	    }
	    
	    ClientSettingPackets.clientSettingsListener();
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] InteractiveChat has been Enabled!");
	    
	    for (Player player : Bukkit.getOnlinePlayers()) {
			InteractiveChat.mentionCooldown.put(player, (System.currentTimeMillis() - 3000));
			
			if (EssentialsHook) {
				Essentials essen = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
				Bukkit.getScheduler().runTaskLater(InteractiveChat.plugin, () -> {
					if (essen.getUser(player.getUniqueId()).getNickname() != null) {
						if (!essen.getUser(player.getUniqueId()).getNickname().equals("")) {
							String essentialsNick = essen.getUser(player.getUniqueId()).getNickname();
							InteractiveChat.essenNick.put(player, essen.getConfig().getString("nickname-prefix") + essentialsNick);
						}
					}
				}, 100);
			}
		}
	}

	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] InteractiveChat has been Disabled!");
	}
}