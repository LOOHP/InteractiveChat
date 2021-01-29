package com.loohp.interactivechat.ObjectHolders;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.Utils.HTTPRequestUtils;

import net.md_5.bungee.api.chat.ClickEvent.Action;

public class WebData {
	
	public static final String URL = "https://api.loohpjames.com/spigot/plugins/interactivechat";
	private static final WebData INSTANCE = new WebData();
	
	public static WebData getInstance() {
		return INSTANCE;
	}
	
	private JSONObject json;
	private List<CustomPlaceholder> specialPlaceholders;
	
	@SuppressWarnings("unchecked")
	private WebData() {
		specialPlaceholders = new ArrayList<>();
		json = new JSONObject();
		JSONArray array = new JSONArray();
		json.put("special-placeholders", array);

		JSONObject nana = new JSONObject();
		nana.put("parseplayer", "VIEWER");
		nana.put("casesensitive", false);
		nana.put("placeholder", "[NARS~]");
		nana.put("cooldown", 0);
		nana.put("hoverEnabled", true);
		nana.put("hoverText", "񘿾Stay Wild.\\n     �7~N (IC Author's Adorable)");
		nana.put("clickEnabled", true);
		nana.put("clickAction", "OPEN_URL");
		nana.put("clickValue", "https://www.instagram.com/narliar/");
		nana.put("replaceEnabled", true);
		nana.put("replaceText", "[�6narliar]");
		array.add(nana);
		
		reload();
		run();
	}
	
	private void run() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChat.plugin, () -> {
			reload();
		}, 18000, 18000);
	}
	
	public void reload() {
		JSONObject newJson = HTTPRequestUtils.getJSONResponse(URL);
		if (newJson != null) {
			json = newJson;
		}
		
		List<CustomPlaceholder> specialPlaceholders = new ArrayList<>();
		for (Object obj : (JSONArray) json.get("special-placeholders")) {
			JSONObject each = (JSONObject) obj;
			specialPlaceholders.add(new CustomPlaceholder(-1, ParsePlayer.valueOf((String) each.get("parseplayer")), (String) each.get("placeholder"), new ArrayList<>(), true, (boolean) each.get("casesensitive"), Long.parseLong(each.get("cooldown").toString()), new CustomPlaceholderHoverEvent((boolean) each.get("hoverEnabled"), (String) each.get("hoverText")), new CustomPlaceholderClickEvent((boolean) each.get("clickEnabled"), Action.valueOf((String) each.get("clickAction")), (String) each.get("clickValue")), new CustomPlaceholderReplaceText((boolean) each.get("replaceEnabled"), (String) each.get("replaceText")), ""));
		}
		
		this.specialPlaceholders = specialPlaceholders;
	}
	
	public List<CustomPlaceholder> getSpecialPlaceholders() {
		return specialPlaceholders;
	}

}
