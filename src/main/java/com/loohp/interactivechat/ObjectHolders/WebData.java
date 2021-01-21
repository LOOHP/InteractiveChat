package com.loohp.interactivechat.ObjectHolders;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderClickEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderHoverEvent;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.CustomPlaceholderReplaceText;
import com.loohp.interactivechat.ObjectHolders.CustomPlaceholder.ParsePlayer;
import com.loohp.interactivechat.Utils.HTTPRequestUtils;

import net.md_5.bungee.api.chat.ClickEvent.Action;

@SuppressWarnings("unchecked")
public class WebData {
	
	public static final String URL = "https://api.loohpjames.com/spigot/plugins/interactivechat";
	
	private static JSONObject def = new JSONObject();
	
	static {
		JSONArray array = new JSONArray();
		def.put("special-placeholders", array);

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
	}
	
	private JSONObject json;
	private List<CustomPlaceholder> specialPlaceholders;
	
	public WebData() {
		reload();
	}
	
	public void reload() {
		json = HTTPRequestUtils.getJSONResponse(URL);
		if (json == null) {
			json = def;
		}
		
		specialPlaceholders = new ArrayList<>();
		for (Object obj : (JSONArray) json.get("special-placeholders")) {
			JSONObject each = (JSONObject) obj;
			specialPlaceholders.add(new CustomPlaceholder(-1, ParsePlayer.valueOf((String) each.get("parseplayer")), (String) each.get("placeholder"), new ArrayList<>(), true, (boolean) each.get("casesensitive"), (long) each.get("cooldown"), new CustomPlaceholderHoverEvent((boolean) each.get("hoverEnabled"), (String) each.get("hoverText")), new CustomPlaceholderClickEvent((boolean) each.get("clickEnabled"), Action.valueOf((String) each.get("clickAction")), (String) each.get("clickValue")), new CustomPlaceholderReplaceText((boolean) each.get("replaceEnabled"), (String) each.get("replaceText"))));
		}
	}
	
	public JSONObject getJSON() {
		JSONObject json = HTTPRequestUtils.getJSONResponse(URL);
		return json == null ? def : json;
	}
	
	public List<CustomPlaceholder> getSpecialPlaceholders() {
		return specialPlaceholders;
	}

}
