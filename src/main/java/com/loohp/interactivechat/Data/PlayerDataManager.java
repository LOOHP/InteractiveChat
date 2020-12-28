package com.loohp.interactivechat.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.interactivechat.InteractiveChat;

public class PlayerDataManager implements Listener {
	
	public static final String DATA_FOLDER_NAME = "player_data";
	public static final String FILE_EXTENSION = ".yml";

	private InteractiveChat plugin;
	private File dataFolder;
	private Map<UUID, PlayerData> data = new HashMap<>();
	
	public PlayerDataManager(InteractiveChat plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, this.plugin);
		dataFolder = new File(plugin.getDataFolder(), DATA_FOLDER_NAME);
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			onJoin(new PlayerJoinEvent(player, ""));
		}
	}
	
	public void mergeOnline(UUID uuid, String data) {
		PlayerData pd = this.data.get(uuid);
		FileConfiguration config = pd.getConfig();
		FileConfiguration toMerge = new YamlConfiguration();
		try {
			toMerge.loadFromString(data);
			
			for (Entry<String, Object> entry : toMerge.getValues(true).entrySet()) {
				config.set(entry.getKey(), entry.getValue());
			}
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		
		pd.saveConfig();
	}
	
	public void mergeOffline(UUID uuid, String data) {
		File file = new File(dataFolder, uuid.toString() + FILE_EXTENSION);
		if (!file.exists()) {
			try {
				PrintWriter pw = new PrintWriter(file);
				pw.println();
				pw.flush();
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		FileConfiguration toMerge = new YamlConfiguration();
		try {
			toMerge.loadFromString(data);
			
			for (Entry<String, Object> entry : toMerge.getValues(true).entrySet()) {
				config.set(entry.getKey(), entry.getValue());
			}
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		
		try {
			config.save(new File(dataFolder, uuid.toString() + FILE_EXTENSION));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PlayerData getPlayerData(Player player) {
		return getPlayerData(player.getUniqueId());
	}
	
	public PlayerData getPlayerData(UUID uuid) {
		return data.get(uuid);
	}
	
	//===============
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID playerUUID = player.getUniqueId();
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			File file = new File(dataFolder, playerUUID.toString() + FILE_EXTENSION);
			if (!file.exists()) {
				try {
					PrintWriter pw = new PrintWriter(file);
					pw.println();
					pw.flush();
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			PlayerData pd = new PlayerData(file, config);
			pd.setPlayerName(player.getName());
			pd.saveConfig();
			
			Bukkit.getScheduler().runTask(plugin, () -> data.put(playerUUID, pd));
		});
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		PlayerData pd = data.remove(event.getPlayer().getUniqueId());
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> pd.saveConfig());
	}
	
	//=============
	
	public static class PlayerData {
		
		public static final String PLAYERNAME = "PlayerName";
		public static final String DISABLE_MENTION = "Preferences.DisableMention";
		
		private FileConfiguration config;
		private File file;
		
		protected PlayerData(File file, FileConfiguration config) {
			this.file = file;
			this.config = config;
		}
		
		public FileConfiguration getConfig() {
			return config;
		}
		
		public void reloadConfig() {
			createIfNotExist();
			config = YamlConfiguration.loadConfiguration(file);
		}
		
		public void saveConfig() {
			createIfNotExist();
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private void createIfNotExist() {
			if (!file.exists()) {
				try {
					PrintWriter pw = new PrintWriter(file);
					pw.println();
					pw.flush();
					pw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public <T> T get(String path, T def, Class<T> clazz) {
			return (T) config.get(path, def);
		}
		
		public <T> void set(String path, T value) {
			config.set(path, value);
		}
		
		public String getPlayerName() {
			return config.getString(PLAYERNAME, "");
		}
		
		public void setPlayerName(String value) {
			config.set(PLAYERNAME, value);
		}
		
		public boolean isMentionDisabled() {
			return config.getBoolean(DISABLE_MENTION, false);
		}
		
		public void setMentionDisabled(boolean value) {
			config.set(DISABLE_MENTION, value);
		}
	}
	
}
