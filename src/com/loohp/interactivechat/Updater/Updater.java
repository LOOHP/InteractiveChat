package com.loohp.interactivechat.Updater;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Updater {
	
	public static void updaterInterval() {
		InteractiveChat.UpdaterTaskID = new BukkitRunnable() {
			public void run() {
				int minute = LocalDateTime.now().getMinute();
				if (minute == 0 || minute == 30) {
					String version = Updater.checkUpdate();
					if (!version.equals("latest")) {
						Updater.sendUpdateMessage(version);
					}
				}
			}
		}.runTaskTimerAsynchronously(InteractiveChat.plugin, 500, 1190).getTaskId();
	}
	
	public static void sendUpdateMessage(String version) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("interactivechat.update")) {
				player.sendMessage(ChatColor.YELLOW + "[InteractiveChat] A new version is available on SpigotMC: " + version);
				TextComponent url = new TextComponent(ChatColor.GOLD + "https://www.spigotmc.org/resources/75804");
				url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.AQUA + "Click me!").create()));
				url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/75870"));
				player.spigot().sendMessage(url);
			}
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractiveChat] A new version is available on SpigotMC: " + version);
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Download: https://www.spigotmc.org/resources/75870");
	}

    public static String checkUpdate() {
        try {
            String localPluginVersion = InteractiveChat.plugin.getDescription().getVersion();
            String spigotPluginVersion = readStringFromURL("https://api.spigotmc.org/legacy/update.php?resource=75870");
            Version current = new Version(localPluginVersion);
            Version spigotmc = new Version(spigotPluginVersion);
            if (!spigotPluginVersion.isEmpty() && current.compareTo(spigotmc) < 0) {
                return spigotPluginVersion;
            } else {
            	return "latest";
            }
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Failed to check for an update on SpigotMC.. It could be an internet issue or SpigotMC is down. If you want disable the update checker, you can disable in config.yml, but we still highly-recommend you to keep your plugin up to date!");
        }
        return "error";
    }
    
    public static String readStringFromURL(String requestURL) throws IOException
    {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
    
}