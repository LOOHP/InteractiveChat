package com.loohp.interactivechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.interactivechat.Debug.Debug;
import com.loohp.interactivechat.Hooks.EssentialsHook;
import com.loohp.interactivechat.Listeners.ChatPackets;
import com.loohp.interactivechat.Listeners.Events;
import com.loohp.interactivechat.Metrics.Metrics;
import com.loohp.interactivechat.ObjectHolders.CommandPlaceholderGroup;
import com.loohp.interactivechat.Utils.MaterialUtils;
import com.loohp.interactivechat.Utils.RarityUtils;

public class InteractiveChat extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static String version = "";
	
	public static ProtocolManager protocolManager;
	
	public static String space0 = "\u200B";
	public static String space1 = "\u200A";
	
	public static boolean ess3 = false;
	public static boolean cm = false;
	
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
	
	public static Material itemFrame1;
	public static Material itemFrame2;
	
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
	public static HashMap<String, CommandPlaceholderGroup> commandPlaceholderMatch = new HashMap<String, CommandPlaceholderGroup>();
	
	public static boolean UpdaterEnabled = true;
	public static int UpdaterTaskID = -1;
	
	public static HashMap<Player, String> essenNick = new HashMap<Player, String>();

	@Override
	public void onEnable() {	
		plugin = (Plugin)getServer().getPluginManager().getPlugin("InteractiveChat");
		
		getServer().getPluginManager().registerEvents(new Debug(), this);
		
		int pluginId = 6747;

		Metrics metrics = new Metrics(this, pluginId);
		
		plugin.getConfig().options().copyDefaults(true);
		ConfigManager.saveConfig();
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		String packageName = getServer().getClass().getPackage().getName();

        if (packageName.contains("1_15_R1")) {
            version = "1.15";
        } else if (packageName.contains("1_14_R1")) {
            version = "1.14";
        } else if (packageName.contains("1_13_R2")) {
            version = "1.13.1";
        } else if (packageName.contains("1_13_R1")) {
            version = "1.13";
        } else if (packageName.contains("1_12_R1")) {
            version = "legacy1.12";
        } else if (packageName.contains("1_11_R1")) {
            version = "legacy1.11";
        } else if (packageName.contains("1_10_R1")) {
            version = "legacy1.10";
        } else if (packageName.contains("1_9_R2")) {
            version = "legacy1.9.4";
        } else if (packageName.contains("1_9_R1")) {
            version = "legacy1.9";
        } else if (packageName.contains("1_8_R3")) {
            version = "OLDlegacy1.8.4";
        } else if (packageName.contains("1_8_R2")) {
            version = "OLDlegacy1.8.3";
        } else if (packageName.contains("1_8_R1")) {
            version = "OLDlegacy1.8";
	    } else {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    }

	    getCommand("interactivechat").setExecutor(new Commands());
	    
	    ConfigManager.loadConfig();
	    
	    getServer().getPluginManager().registerEvents(new Events(), this);
	    ChatPackets.chatMessageListener();
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Interactive Chat has hooked into Essentials!");
			ess3 = true;
			getServer().getPluginManager().registerEvents(new EssentialsHook(), this);
			EssentialsHook.setup();
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("ChatManager") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Interactive Chat has hooked into ChatManager!");
			cm = true;
		}
		
	    MaterialUtils.setupLang();
	    RarityUtils.setupRarity();
	    
	    metrics.addCustomChart(new Metrics.SingleLineChart("total_placeholders", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return InteractiveChat.placeholderList.size();
            }
        }));
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "InteractiveChat has been Enabled!");
	    
	    for (Player player : Bukkit.getOnlinePlayers()) {
			InteractiveChat.mentionCooldown.put(player, (System.currentTimeMillis() - 3000));
		}
	}

	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "InteractiveChat has been Disabled!");
	}
}