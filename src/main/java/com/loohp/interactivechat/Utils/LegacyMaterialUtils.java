package com.loohp.interactivechat.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.loohp.interactivechat.InteractiveChat;

public class LegacyMaterialUtils {
	
	private static File file;
	private static JSONObject json;
    private static JSONParser parser = new JSONParser();
    private static HashMap<String, String> entry = new HashMap<String, String>();
	
	@SuppressWarnings("deprecation")
	public static String getLegacyItemName(ItemStack item) {
		int id = item.getType().getId();
		int meta = item.getData().getData();
		String idAndMetadata = String.valueOf(id) + ":" + String.valueOf(meta);
		if (entry.containsKey(idAndMetadata)) {
			return entry.get(idAndMetadata);
		} else {
			return entry.get(String.valueOf(id) + ":0");
		}
	}
	
	public static void reloadLegacyLang() {		
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(file));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	    JSONArray each = (JSONArray) obj;

	    for (int i = 0; i < each.size(); i++) {
	    	json = (JSONObject) each.get(i);
	    	if (!json.containsKey("id")) {
	    		continue;
	    	}
	    	String id = json.get("id").toString();
			if (json.containsKey("variations")) {
				JSONArray eachVar = (JSONArray) json.get("variations");
				for (int j = 0; j < eachVar.size(); j++) {
			    	json = (JSONObject) eachVar.get(j);
			    	String meta = json.get("metadata").toString();
					String name = json.get("displayName").toString();
					String idandmeta = id + ":" + meta;
					entry.put(idandmeta, name);
				}					
			} else {
				String name = json.get("displayName").toString();
				String idandmeta = id + ":0";
				entry.put(idandmeta, name);
			}
	    }  
	}
	
	public static void setupLegacyLang() {		
		
		if (!InteractiveChat.plugin.getDataFolder().exists()) {
			InteractiveChat.plugin.getDataFolder().mkdir();
		}
		
		File langFolder = new File(InteractiveChat.plugin.getDataFolder().getAbsolutePath() + "/lang");
		if (!langFolder.exists()) {
			langFolder.mkdir();
		}
		
        String resourceName = "/lang/Legacy.json";
        
        File langFile = new File(langFolder.getAbsolutePath() + "/Legacy.json");
        if (!langFile.exists()) {
        	try (InputStream in = InteractiveChat.plugin.getClass().getResourceAsStream(resourceName)) {
                Files.copy(in, langFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }  
        
        file = langFile;
        reloadLegacyLang();
	}

}
