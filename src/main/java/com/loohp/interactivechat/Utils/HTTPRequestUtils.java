package com.loohp.interactivechat.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HTTPRequestUtils {
	
	public static JSONObject getJSONResponse(String link) {
		try {	    	
			URL url = new URL(link);
	        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.addRequestProperty("Pragma", "no-cache");
	        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
	        	String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());
	            return (JSONObject) new JSONParser().parse(reply);
	        } else {
	            return null;
	        }
	    } catch (IOException | ParseException e) {
	        return null;
	    }
	}

}
