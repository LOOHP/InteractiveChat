package com.loohp.interactivechat.utils;

import org.bukkit.Bukkit;

public class NMSUtils {
	
	public static Class<?> getNMSClass(String path, String... paths) throws ClassNotFoundException {	
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        ClassNotFoundException error = null;
        try {
    		return Class.forName(path.replace("%s", version));
    	} catch (ClassNotFoundException e) {
    		error = e;
    	}
        for (String classpath : paths) {
        	try {
        		return Class.forName(classpath.replace("%s", version));
        	} catch (ClassNotFoundException e) {
        		error = e;
        	}
        }
        throw error;
    }

}
