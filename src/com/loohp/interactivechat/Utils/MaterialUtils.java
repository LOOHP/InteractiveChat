package com.loohp.interactivechat.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loohp.interactivechat.InteractiveChat;

public class MaterialUtils {
	
	private static File file;
	private static JSONObject json;
    private static JSONParser parser = new JSONParser();
    private static HashMap<String, Object> defaults = new HashMap<String, Object>();
	
	public static String getMinecraftName(ItemStack item) {
		
		if (file == null) {
			return LegacyMaterialUtils.getLegacyItemName(item);
		}
		
		Material material = item.getType();
		String path = "";
		
		if (material.isBlock()) {
			path = new StringBuilder().append("block.").append(material.getKey().getNamespace()).append('.').append(material.getKey().getKey()).toString();
		} else {
			path = new StringBuilder().append("item.").append(material.getKey().getNamespace()).append('.').append(material.getKey().getKey()).toString();
		}
		
		if (item.getType().equals(Material.POTION) || item.getType().equals(Material.SPLASH_POTION) || item.getType().equals(Material.LINGERING_POTION)) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			String namespace = PotionUtils.getVanillaPotionName(meta.getBasePotionData().getType());
			path = new StringBuilder().append(path).append(".effect.").append(namespace).toString();
		}
	
		String name = json.containsKey(path) ? json.get(path).toString() : (defaults.containsKey(path) ? defaults.get(path).toString() : path);
		return name;
	}
	
	public static void reloadLang() {		
		if (file == null) {
			LegacyMaterialUtils.reloadLegacyLang();
			return;
		}
		try {
			json = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}	
	}
	
	public static void setupLang() {		
		String langVersion = "";
		
		if (InteractiveChat.version.equals("1.15")) {
			langVersion = "V1_15";
	    } else if (InteractiveChat.version.equals("1.14")) {
	    	langVersion = "V1_14";
	    } else if (InteractiveChat.version.equals("1.13.1") || InteractiveChat.version.equals("1.13")) {
	    	langVersion = "V1_13";
	    } else {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "JSON custom language files are not supported on this version");
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "InteractiveChat will use legacy item names method instead!");
	    	file = null;
	    	LegacyMaterialUtils.setupLegacyLang();
	    	return;
	    }
		
		if (!InteractiveChat.plugin.getDataFolder().exists()) {
			InteractiveChat.plugin.getDataFolder().mkdir();
		}
		
		File langFolder = new File(InteractiveChat.plugin.getDataFolder().getAbsolutePath() + "/lang");
		if (!langFolder.exists()) {
			langFolder.mkdir();
		}
		
        String resourceName = "/lang/" + langVersion + ".json";
        
        File langFile = new File(langFolder.getAbsolutePath() + "/" + langVersion + ".json");
        if (!langFile.exists()) {
        	try (InputStream in = InteractiveChat.plugin.getClass().getResourceAsStream(resourceName)) {
                Files.copy(in, langFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  
        
        file = langFile;
        reloadLang();
        
	    PotionUtils.setupPotions();
	    RarityUtils.setupRarity();
	}
	
}
