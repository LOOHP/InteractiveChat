package com.loohp.interactivechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.interactivechat.Debug.Debug;
import com.loohp.interactivechat.Listeners.DeathMessagePrimeEvents;
import com.loohp.interactivechat.Listeners.Events;
import com.loohp.interactivechat.Listeners.LegacyEvents;
import com.loohp.interactivechat.Metrics.Metrics;
import com.loohp.interactivechat.Utils.MaterialUtils;

public class InteractiveChat extends JavaPlugin {
	
	public static Plugin plugin = null;
	
	public static String version = "";
	
	public static ProtocolManager protocolManager;
	
	public static String space0 = "\u200B";
	public static String space1 = "\u200A";
	
	public static boolean ess3 = false;
	public static boolean dmp = false;
	
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
	
	public static boolean AllowMention = true;
	
	public static String NoPermission = "&cYou do not have permission to use that command!";
	public static String InvExpired = "&cThis inventory view has expired!";
	public static String ReloadPlugin = "&aInteractive Chat has been reloaded!";
	
	public static HashMap<Player, String> lastMessage = new HashMap<Player, String>();
	public static HashMap<Player, Long> time = new HashMap<Player, Long>();
	public static HashMap<String, Player> messageKey = new HashMap<String, Player>();
	public static HashMap<String, UUID> messageKeyUUID = new HashMap<String, UUID>();
	
	public static HashMap<Long, Inventory> itemDisplay = new HashMap<Long, Inventory>();
	public static HashMap<Long, Inventory> inventoryDisplay = new HashMap<Long, Inventory>();
	public static HashMap<Long, Inventory> enderDisplay = new HashMap<Long, Inventory>();
	
	public static HashMap<Player, Long> mentionCooldown = new HashMap<Player, Long>();
	
	public static HashMap<Player, HashMap<String, Long>> placeholderCooldowns = new HashMap<Player, HashMap<String, Long>>();
	public static HashMap<Player, Long> universalCooldowns = new HashMap<Player, Long>();
	
	public static HashMap<String, Long> timedRemove = new HashMap<String, Long>();
	
	public static List<String> placeholderList = new ArrayList<String>();
	public static int maxPlacholders = -1;
	public static String limitReachMessage = "&cPlease do now use excessive amount of placeholders in one message!";
	
	public static HashMap<UUID, UUID> mentionPair = new HashMap<UUID, UUID>();
	public static String mentionHightlight = "&e{MentionedPlayer}";
	public static String mentionHover = "&e{MentionedPlayer}";
	
	public static boolean UpdaterEnabled = true;
	public static int UpdaterTaskID = -1;
	
	public static HashMap<String, Long> keyTime = new HashMap<String, Long>(); 

	@Override
	public void onEnable() {	
		plugin = (Plugin)getServer().getPluginManager().getPlugin("InteractiveChat");
		
		getServer().getPluginManager().registerEvents(new Debug(), this);
		
		int pluginId = 6747;

		Metrics metrics = new Metrics(this, pluginId);
		
		InteractiveChat.plugin.getConfig().options().copyDefaults(false);
		
		protocolManager = ProtocolLibrary.getProtocolManager();

	    getCommand("interactivechat").setExecutor(new Commands());
	    
	    ConfigManager.setup();	    
	    
	    removeTimeout();
	    removePlayer();
		
	    if (getServer().getClass().getPackage().getName().contains("1_15_R1") == true) {
	    	version = "1.15";
	    } else if (getServer().getClass().getPackage().getName().contains("1_14_R1") == true) {
	    	version = "1.14";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R2") == true) {
	    	version = "1.13.1";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R1") == true) {
	    	version = "1.13";
	    } else if (getServer().getClass().getPackage().getName().contains("1_12_R1") == true) {
	    	version = "legacy1.12";
	    } else if (getServer().getClass().getPackage().getName().contains("1_11_R1") == true) {
	    	version = "legacy1.11";
	    } else if (getServer().getClass().getPackage().getName().contains("1_10_R1") == true) {
	    	version = "legacy1.10";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R2") == true) {
	    	version = "legacy1.9.4";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R1") == true) {
	    	version = "legacy1.9";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R3") == true) {
	    	version = "OLDlegacy1.8.4";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R1") == true) {
	    	version = "OLDlegacy1.8";
	    } else {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    	plugin.getPluginLoader().disablePlugin(this);
	    }
	    
	    if (InteractiveChat.version.contains("legacy")) {
	    	getServer().getPluginManager().registerEvents(new LegacyEvents(), this);
	    	LegacyEvents.chatMessageListener();
	    } else {
	    	getServer().getPluginManager().registerEvents(new Events(), this);
	 	    Events.chatMessageListener();
	    }
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Interactive Chat has hooked into Essentials!");
			ess3 = true;
		}
	    
	    if (Bukkit.getServer().getPluginManager().getPlugin("DeathMessagesPrime") != null) {
	    	getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Interactive Chat has hooked into DeathMessagesPrime!");
	    	getServer().getPluginManager().registerEvents(new DeathMessagePrimeEvents(), this);
	    	dmp = true;
		}
		
	    MaterialUtils.setupLang();
	    
	    metrics.addCustomChart(new Metrics.SingleLineChart("total_placeholders", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return InteractiveChat.placeholderList.size();
            }
        }));
	    
	    //getServer().getMessenger().registerOutgoingPluginChannel(this, "interactivechat:channel");
	    //getServer().getMessenger().registerIncomingPluginChannel(this, "interactivechat:channel", new BungeeMessageListener(this));
	    
	    getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "InteractiveChat has been Enabled!");
	    
	    for (Player player : Bukkit.getOnlinePlayers()) {
			InteractiveChat.mentionCooldown.put(player, (System.currentTimeMillis() - 3000));
		}
	}

	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "InteractiveChat has been Disabled!");
	}
	
	public void removePlayer() {
		new BukkitRunnable() {
			public void run() {
				long unix = System.currentTimeMillis();
				List<String> removeList = new ArrayList<String>();
				
				List<String> UUIDlist = new ArrayList<String>();
				for (Player player : Bukkit.getOnlinePlayers()) {
					UUIDlist.add(player.getUniqueId().toString());
				}
				
				for (Entry<String, Long> entry : InteractiveChat.timedRemove.entrySet()) {
					if (entry.getValue() < unix) {
						if (UUIDlist.contains(entry.getKey())) {
							InteractiveChat.time.remove(Bukkit.getPlayer(UUID.fromString(entry.getKey())));
						} else {
							InteractiveChat.messageKey.remove(entry.getKey());
							InteractiveChat.messageKeyUUID.remove(entry.getKey());
						}
						removeList.add(entry.getKey());
					}
				}
				for (String key : removeList) {
					InteractiveChat.timedRemove.remove(key);
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 1);
	}
	
	public void removeTimeout() {	
		new BukkitRunnable() {		
			public void run() {
				List<Long> removelist = new ArrayList<Long>();
				for (Entry<Long, Inventory> entry : InteractiveChat.inventoryDisplay.entrySet()) {
					long unix = System.currentTimeMillis();
					if ((unix - entry.getKey()) > 300000) {
						removelist.add(entry.getKey());
					}
				}
				
				for (Long key : removelist) {
					InteractiveChat.inventoryDisplay.remove(key);
				}
				
				removelist.clear();
				for (Entry<Long, Inventory> entry : InteractiveChat.enderDisplay.entrySet()) {
					long unix = System.currentTimeMillis();
					if ((unix - entry.getKey()) > 300000) {
						removelist.add(entry.getKey());
					}
				}
				
				for (Long key : removelist) {
					InteractiveChat.enderDisplay.remove(key);
				}
				
				removelist.clear();
				for (Entry<Long, Inventory> entry : InteractiveChat.itemDisplay.entrySet()) {
					long unix = System.currentTimeMillis();
					if ((unix - entry.getKey()) > 300000) {
						removelist.add(entry.getKey());
					}
				}
				
				for (Long key : removelist) {
					InteractiveChat.itemDisplay.remove(key);
				}
				
				List<String> removelist2 = new ArrayList<String>();
				for (Entry<String, Long> entry : InteractiveChat.keyTime.entrySet()) {
					long unix = System.currentTimeMillis();
					if ((unix - entry.getValue()) > 3000) {
						removelist2.add(entry.getKey());
					}
				}
				
				for (String key : removelist2) {
					InteractiveChat.keyTime.remove(key);
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 180);
	}
}