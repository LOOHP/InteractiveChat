package com.loohp.interactivechat.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.md_5.bungee.api.ChatColor;

public class JsonUtils {
	
    private static final StringBuilder JSON_BUILDER = new StringBuilder("{\"text\":\"\",\"extra\":[");

    private static final int RETAIN = "{\"text\":\"\",\"extra\":[".length();

    public static String toJSON(String message) {
        if (message == null || message.isEmpty())
            return null;
        if (JSON_BUILDER.length() > RETAIN)
            JSON_BUILDER.delete(RETAIN, JSON_BUILDER.length());
        String[] parts = message.split(Character.toString(ChatColor.COLOR_CHAR));
        boolean first = true;
        String colour = null;
        String format = null;
        boolean ignoreFirst = !parts[0].isEmpty() && ChatColor.getByChar(parts[0].charAt(0)) != null;
        for (String part : parts) {
            // If it starts with a colour, just ignore the empty String
            // before it
            if(part.isEmpty()) {
                continue;
            }
            
            String newStyle = null;
            if (!ignoreFirst) {
                newStyle = getStyle(part.charAt(0));
            } else {
                ignoreFirst = false;    
            }
            
            if (newStyle != null) {
                part = part.substring(1);
                if(newStyle.startsWith("\"c"))
                    colour = newStyle;
                else
                    format = newStyle;
            }
            if (!part.isEmpty()) {
                if (first) {
                    first = false;
                } else {
                    JSON_BUILDER.append(",");
                }
                JSON_BUILDER.append("{");
                if (colour != null) {
                    JSON_BUILDER.append(colour);
                    colour = null;
                }
                if (format != null) {
                    JSON_BUILDER.append(format);
                    format = null;
                }
                JSON_BUILDER.append(String.format("text:\"%s\"", part));
                JSON_BUILDER.append("}");
            }
        }
        return JSON_BUILDER.append("]}").toString();
    }

    private static final StringBuilder STYLE = new StringBuilder();

    @SuppressWarnings("deprecation")
	private static String getStyle(char colour) {
        if(STYLE.length() > 0)
            STYLE.delete(0, STYLE.length());
        switch(colour) {
            case 'k':
                return "\"obfuscated\": true,";
            case 'l':
                return "\"bold\": true,";
            case 'm':
                return "\"strikethrough\": true,";
            case 'n':
                return "\"underlined\": true,";
            case 'o':
                return "\"italic\": true,";
            case 'r':
                return "\"reset\": true,";
            default:
                break;
        }
        ChatColor cc = ChatColor.getByChar(colour);
        if(cc == null)
            return null;
        return STYLE.append("\"color\":\"").append(cc.name().toLowerCase()).append("\",").toString();
    }
    
    public static boolean containsKey(String json, String key) {
    	try {
			Object jsonObj = new JSONParser().parse(json);
			if (jsonObj instanceof JSONObject) {
				return containsKey((JSONObject) jsonObj, key);
			} else if (jsonObj instanceof JSONArray) {
				return containsKey((JSONArray) jsonObj, key);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return false;
    }
    
    private static boolean containsKey(JSONObject json, String key) {
    	boolean contains = false;
    	for (Object obj : json.keySet()) {
    		if (obj instanceof String) {
    			if (((String) obj).equals(key)) {
    				return true;
    			}
    		}
    		Object jsonObj = json.get(obj);
    		if (jsonObj instanceof JSONObject) {
    			contains = containsKey((JSONObject) jsonObj, key);
			} else if (jsonObj instanceof JSONArray) {
				contains = containsKey((JSONArray) jsonObj, key);
			}
    		if (contains) {
    			return true;
    		}
    	}
    	return contains;
    }
    
    private static boolean containsKey(JSONArray json, String key) {
    	boolean contains = false;
    	for (Object jsonObj : json) {
    		if (jsonObj instanceof JSONObject) {
    			contains = containsKey((JSONObject) jsonObj, key);
			} else if (jsonObj instanceof JSONArray) {
				contains = containsKey((JSONArray) jsonObj, key);
			}
    		if (contains) {
    			return true;
    		}
    	}
    	return contains;
    }
    
    @SuppressWarnings("unchecked")
	public static String toString(JSONObject json) {
    	JSONObject toSave = json;
        
    	TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
    	treeMap.putAll(toSave);
    	
    	Gson g = new GsonBuilder().create();
        return g.toJson(treeMap);
    }
    
    @SuppressWarnings("unchecked")
	public static boolean saveToFilePretty(JSONObject json, File file) {
        try {
        	JSONObject toSave = json;
        
        	TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        	treeMap.putAll(toSave);
        	
        	Gson g = new GsonBuilder().setPrettyPrinting().create();
            String prettyJsonString = g.toJson(treeMap);
            
            PrintWriter clear = new PrintWriter(file);
            clear.print("");
            clear.close();
            
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(prettyJsonString);
            writer.flush();
            writer.close();

            return true;
        } catch (Exception e) {
        	e.printStackTrace();
        	return false;
        }
    }
}