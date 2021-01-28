package com.loohp.interactivechat.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class LanguageUtils {
	
	private static Class<?> craftItemStackClass;
	private static Class<?> nmsItemStackClass;
	private static Method asNMSCopyMethod;
	private static Method getRawItemTypeNameMethod;
	
	static {
		if (InteractiveChat.version.isLegacy()) {
			try {
				craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
				nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");
				asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
				getRawItemTypeNameMethod = nmsItemStackClass.getMethod("a");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {	
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
	
	public static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	public static final String RESOURCES_URL = "http://resources.download.minecraft.net/";
	
	private static Map<String, Map<String, String>> translations = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static void loadTranslations() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] Loading languages...");
		Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
			try {
				File langFolder = new File(InteractiveChat.plugin.getDataFolder(), "lang");
				langFolder.mkdirs();
				File langFileFolder = new File(langFolder, "languages");
				langFileFolder.mkdirs();
				File hashFile = new File(langFolder, "hashes.json");
		    	if (!hashFile.exists()) {
		    	    PrintWriter pw = new PrintWriter(hashFile, "UTF-8");
		    	    pw.print("{");
		    	    pw.print("}");
		    	    pw.flush();
		    	    pw.close();
		    	}
		    	InputStreamReader hashStream = new InputStreamReader(new FileInputStream(hashFile), StandardCharsets.UTF_8);
		    	JSONObject data = (JSONObject) new JSONParser().parse(hashStream);
		    	hashStream.close();
				
				JSONObject manifest = HTTPRequestUtils.getJSONResponse(VERSION_MANIFEST_URL);
				if (manifest == null) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to fetch version_manifest from " + VERSION_MANIFEST_URL);
				} else {
					String mcVersion = InteractiveChat.exactMinecraftVersion;
					Object urlObj = ((JSONArray) manifest.get("versions")).stream().filter(each -> ((JSONObject) each).get("id").toString().equalsIgnoreCase(mcVersion)).map(each -> ((JSONObject) each).get("url").toString()).findFirst().orElse(null);
					if (urlObj == null) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to find " + mcVersion + " from version_manifest");
					} else {
						JSONObject versionData = HTTPRequestUtils.getJSONResponse(urlObj.toString());
						if (versionData == null) {
							Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to fetch version data from " + urlObj.toString());
						} else {
							String clientUrl = ((JSONObject) ((JSONObject) versionData.get("downloads")).get("client")).get("url").toString();
							File tempFolder = new File(langFolder, "temp");
							if (tempFolder.exists()) {
								FileUtils.removeFolderRecursively(tempFolder);
							}
							tempFolder.mkdirs();
							File jarFile = new File(tempFolder, "client.jar");
							HTTPRequestUtils.download(jarFile, clientUrl);
							ZipUtils.extract(new ZipInputStream(new FileInputStream(jarFile)), tempFolder);
							File enUsFile = new File(tempFolder, "assets/minecraft/lang").listFiles()[0];
							String enUsFileHash = HashUtils.createSha1String(enUsFile);
							String enUsExtension = enUsFile.getName().substring(enUsFile.getName().indexOf(".") + 1);
							if (data.containsKey("en_us")) {
								JSONObject values = (JSONObject) data.get("en_us");
								File fileToSave = new File(langFileFolder, "en_us" + "." + enUsExtension);
								if (!values.get("hash").toString().equals(enUsFileHash) || !fileToSave.exists()) {
									values.put("hash", enUsFileHash);
									if (fileToSave.exists()) {
										fileToSave.delete();
									}
									FileUtils.copy(enUsFile, fileToSave);
								}
							} else {
								JSONObject values = new JSONObject();
								values.put("hash", enUsFileHash);
								File fileToSave = new File(langFileFolder, "en_us" + "." + enUsExtension);
								if (fileToSave.exists()) {
									fileToSave.delete();
								}
								FileUtils.copy(enUsFile, fileToSave);
								data.put("en_us", values);											
							}
							FileUtils.removeFolderRecursively(tempFolder);
							
							String indexUrl = ((JSONObject) versionData.get("assetIndex")).get("url").toString();
							JSONObject assets = HTTPRequestUtils.getJSONResponse(indexUrl);
							if (assets == null) {
								Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to fetch assets data from " + indexUrl);
							} else {
								JSONObject objects = (JSONObject) assets.get("objects");
								for (Object obj : objects.keySet()) {
									if (obj.toString().startsWith("minecraft/lang/")) {
										String lang = obj.toString().substring(obj.toString().lastIndexOf("/") + 1, obj.toString().indexOf("."));
										String extension = obj.toString().substring(obj.toString().indexOf(".") + 1);
										String hash = ((JSONObject) objects.get(obj.toString())).get("hash").toString();
										String fileUrl = RESOURCES_URL + hash.substring(0, 2) + "/" + hash;
										if (data.containsKey(lang)) {
											JSONObject values = (JSONObject) data.get(lang);
											File fileToSave = new File(langFileFolder, lang + "." + extension);
											if (!values.get("hash").toString().equals(hash) || !fileToSave.exists()) {
												values.put("hash", hash);
												if (fileToSave.exists()) {
													fileToSave.delete();
												}
												if (!HTTPRequestUtils.download(fileToSave, fileUrl)) {
													Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to download " + obj.toString() + " from " + fileUrl);
												}
											}
										} else {
											JSONObject values = new JSONObject();
											values.put("hash", hash);
											File fileToSave = new File(langFileFolder, lang + "." + extension);
											if (fileToSave.exists()) {
												fileToSave.delete();
											}
											if (!HTTPRequestUtils.download(fileToSave, fileUrl)) {
												Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to download " + obj.toString() + " from " + fileUrl);
											}
											data.put(lang, values);											
										}
									}
								}
							}
						}
					}
				}
				JsonUtils.saveToFilePretty(data, hashFile);
				
				for (File file : langFileFolder.listFiles()) {
					try {
						if (file.getName().endsWith(".json")) {
							InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
							JSONObject json = (JSONObject) new JSONParser().parse(reader);
							reader.close();
							Map<String, String> mapping = new HashMap<>();
							for (Object obj : json.keySet()) {
								try {
									String key = (String) obj;
									mapping.put(key, (String) json.get(key));
								} catch (Exception e) {}
							}
							translations.put(file.getName().substring(0, file.getName().lastIndexOf(".")), mapping);
						} else if (file.getName().endsWith(".lang")) {
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
							Map<String, String> mapping = new HashMap<>();
							br.lines().forEach(line -> {
								if (line.contains("=")) {
									mapping.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=") + 1));
								}
							});
							br.close();
							translations.put(file.getName().substring(0, file.getName().lastIndexOf(".")), mapping);
						}
					} catch (Exception e) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to load " + file.getName());
						e.printStackTrace();
					}
				}
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] Loaded all " + translations.size() + 1 + " languages!");
			} catch (Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to setup languages");
				e.printStackTrace();
			}
		});
	}
	
	public static String getTranslationKey(ItemStack itemStack) {
		try {
			if (InteractiveChat.version.isLegacy()) {
				return getLegacyTranslationKey(itemStack);
			} else {
				return getModernTranslationKey(itemStack);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static String getModernTranslationKey(ItemStack itemStack) {
		Material material = itemStack.getType();
		String path = "";
		
		if (material.isBlock()) {
			path = "block." + material.getKey().getNamespace() + "." + material.getKey().getKey();
		} else {
			path = "item." + material.getKey().getNamespace() + "." + material.getKey().getKey();
		}
		
		if (itemStack.getType().equals(Material.POTION) || itemStack.getType().equals(Material.SPLASH_POTION) || itemStack.getType().equals(Material.LINGERING_POTION)) {
			PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
			String namespace = PotionUtils.getVanillaPotionName(meta.getBasePotionData().getType());
			path += ".effect." + namespace;
		}
		
		if (itemStack.getType().equals(Material.PLAYER_HEAD)) {
			String owner = NBTUtils.getString(itemStack, "SkullOwner", "Name");
			if (owner != null) {
				path += ".named";
			}
		}
	
		return path;
	}
	
	private static String getLegacyTranslationKey(ItemStack itemStack) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object nmsItemStackObject = asNMSCopyMethod.invoke(null, itemStack);
		String path = getRawItemTypeNameMethod.invoke(nmsItemStackObject).toString() + ".name";
		if (XMaterial.matchXMaterial(itemStack).equals(XMaterial.PLAYER_HEAD)) {
			String owner = NBTUtils.getString(itemStack, "SkullOwner", "Name");
			if (owner != null) {
				path = "item.skull.player.name";
			}
		}
		return path;
	}
	
	public static Set<String> getSupportedLanguages() {
		return Collections.unmodifiableSet(translations.keySet());
	}
	
	public static String getTranslation(String translationKey, String language) {
		try {
			if (InteractiveChat.version.isLegacy() && translationKey.equals("item.skull.player.name")) {
				return "%s's Head";
			}
			Map<String, String> mapping = translations.get(language);
			return mapping == null ? new TranslatableComponent(translationKey).toPlainText() : mapping.getOrDefault(translationKey, translationKey);
		} catch (Exception e) {
			return translationKey;
		}
	}

}
