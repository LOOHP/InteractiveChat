package com.loohp.interactivechat.Utils;

import net.md_5.bungee.api.ChatColor;

public class JsonUtils{
	
    private static final StringBuilder JSON_BUILDER = new StringBuilder("{\"text\":\"\",\"extra\":[");

    private static final int RETAIN = "{\"text\":\"\",\"extra\":[".length();

    public static String toJSON(String message)
    {
        if(message == null || message.isEmpty())
            return null;
        if(JSON_BUILDER.length() > RETAIN)
            JSON_BUILDER.delete(RETAIN, JSON_BUILDER.length());
        String[] parts = message.split(Character.toString(ChatColor.COLOR_CHAR));
        boolean first = true;
        String colour = null;
        String format = null;
        boolean ignoreFirst = !parts[0].isEmpty() && ChatColor.getByChar(parts[0].charAt(0)) != null;
        for(String part : parts)
        {
            // If it starts with a colour, just ignore the empty String
            // before it
            if(part.isEmpty())
            {
                continue;
            }
            
            String newStyle = null;
            if(!ignoreFirst)
            {
                newStyle = getStyle(part.charAt(0));
            }
            else
            {
                ignoreFirst = false;    
            }
            
            if(newStyle != null)
            {
                part = part.substring(1);
                if(newStyle.startsWith("\"c"))
                    colour = newStyle;
                else
                    format = newStyle;
            }
            if(!part.isEmpty())
            {
                if(first)
                    first = false;
                else
                {
                    JSON_BUILDER.append(",");
                }
                JSON_BUILDER.append("{");
                if(colour != null)
                {
                    JSON_BUILDER.append(colour);
                    colour = null;
                }
                if(format != null)
                {
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

    private static String getStyle(char colour)
    {
        if(STYLE.length() > 0)
            STYLE.delete(0, STYLE.length());
        switch(colour)
        {
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
}