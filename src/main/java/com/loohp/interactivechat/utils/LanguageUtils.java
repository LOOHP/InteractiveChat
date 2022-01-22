package com.loohp.interactivechat.utils;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.InteractiveChat;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LanguageUtils {

    public static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String RESOURCES_URL = "http://resources.download.minecraft.net/";
    private static Class<?> craftItemStackClass;
    private static Class<?> nmsItemStackClass;
    private static Method asNMSCopyMethod;
    private static Method getRawItemTypeNameMethod;
    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    private static final Map<Plugin, Map<String, Map<String, String>>> pluginTranslations = new ConcurrentHashMap<>();
    private static final AtomicBoolean lock = new AtomicBoolean(false);

    static {
        if (InteractiveChat.version.isLegacy()) {
            try {
                craftItemStackClass = NMSUtils.getNMSClass("org.bukkit.craftbukkit.%s.inventory.CraftItemStack");
                nmsItemStackClass = NMSUtils.getNMSClass("net.minecraft.server.%s.ItemStack", "net.minecraft.world.item.ItemStack");
                asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
                getRawItemTypeNameMethod = nmsItemStackClass.getMethod("a");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadTranslations(String language) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] Loading languages...");
        Bukkit.getScheduler().runTaskAsynchronously(InteractiveChat.plugin, () -> {
            while (lock.get()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
            }
            lock.set(true);
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

                try {
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
                                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to fetch version data from " + urlObj);
                            } else {
                                String clientUrl = ((JSONObject) ((JSONObject) versionData.get("downloads")).get("client")).get("url").toString();
                                try (ZipArchiveInputStream zip = new ZipArchiveInputStream(new ByteArrayInputStream(HTTPRequestUtils.download(clientUrl)), StandardCharsets.UTF_8.toString(), false, true, true)) {
                                    while (true) {
                                        ZipArchiveEntry entry = zip.getNextZipEntry();
                                        if (entry == null) {
                                            break;
                                        }
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        byte[] byteChunk = new byte[4096];
                                        int n;
                                        while ((n = zip.read(byteChunk)) > 0) {
                                            baos.write(byteChunk, 0, n);
                                        }
                                        byte[] currentEntry = baos.toByteArray();

                                        String name = entry.getName().toLowerCase();
                                        if (name.matches("^.*assets/minecraft/lang/en_us.(json|lang)$")) {
                                            String enUsFileHash = HashUtils.createSha1String(new ByteArrayInputStream(currentEntry));
                                            String enUsExtension = name.substring(name.indexOf(".") + 1);
                                            if (data.containsKey("en_us")) {
                                                JSONObject values = (JSONObject) data.get("en_us");
                                                File fileToSave = new File(langFileFolder, "en_us" + "." + enUsExtension);
                                                if (!values.get("hash").toString().equals(enUsFileHash) || !fileToSave.exists()) {
                                                    values.put("hash", enUsFileHash);
                                                    if (fileToSave.exists()) {
                                                        fileToSave.delete();
                                                    }
                                                    FileUtils.copy(new ByteArrayInputStream(currentEntry), fileToSave);
                                                }
                                            } else {
                                                JSONObject values = new JSONObject();
                                                values.put("hash", enUsFileHash);
                                                File fileToSave = new File(langFileFolder, "en_us" + "." + enUsExtension);
                                                if (fileToSave.exists()) {
                                                    fileToSave.delete();
                                                }
                                                FileUtils.copy(new ByteArrayInputStream(currentEntry), fileToSave);
                                                data.put("en_us", values);
                                            }
                                            zip.close();
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                String indexUrl = ((JSONObject) versionData.get("assetIndex")).get("url").toString();
                                JSONObject assets = HTTPRequestUtils.getJSONResponse(indexUrl);
                                if (assets == null) {
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to fetch assets data from " + indexUrl);
                                } else {
                                    JSONObject objects = (JSONObject) assets.get("objects");
                                    for (Object obj : objects.keySet()) {
                                        String key = obj.toString().toLowerCase();
                                        if (key.matches("^minecraft\\/lang\\/" + language + ".(json|lang)$")) {
                                            String lang = key.substring(key.lastIndexOf("/") + 1, key.indexOf("."));
                                            String extension = key.substring(key.indexOf(".") + 1);
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
                                                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to download " + key + " from " + fileUrl);
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
                                                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to download " + key + " from " + fileUrl);
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
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to download latest languages files from Mojang");
                    e.printStackTrace();
                }

                String langRegex = "(en_us|" + language + ")";

                for (File file : langFileFolder.listFiles()) {
                    try {
                        if (file.getName().matches("^" + langRegex + ".json$")) {
                            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                            JSONObject json = (JSONObject) new JSONParser().parse(reader);
                            reader.close();
                            Map<String, String> mapping = new HashMap<>();
                            for (Object obj : json.keySet()) {
                                try {
                                    String key = (String) obj;
                                    mapping.put(key, (String) json.get(key));
                                } catch (Exception e) {
                                }
                            }
                            translations.put(file.getName().substring(0, file.getName().lastIndexOf(".")), mapping);
                        } else if (file.getName().matches("^" + langRegex + ".lang$")) {
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
                if (translations.isEmpty()) {
                    throw new RuntimeException();
                }
                for (Map<String, Map<String, String>> pluginLanguageMapping : pluginTranslations.values()) {
                    for (Entry<String, Map<String, String>> entry : pluginLanguageMapping.entrySet()) {
                        String lang = entry.getKey();
                        Map<String, String> mapping = entry.getValue();
                        Map<String, String> existingMapping = translations.get(lang);
                        if (existingMapping == null) {
                            translations.put(lang, new HashMap<>(mapping));
                        } else {
                            existingMapping.putAll(mapping);
                        }
                    }
                }
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractiveChat] Loaded all " + translations.size() + " languages!");
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractiveChat] Unable to setup languages");
                e.printStackTrace();
            }
            lock.set(false);
        });
    }

    public synchronized static void clearPluginTranslations(Plugin plugin) {
        pluginTranslations.remove(plugin);
    }

    public synchronized static void loadPluginTranslations(Plugin plugin, String language, Map<String, String> mapping) {
        Map<String, String> existingMapping = translations.get(language);
        if (existingMapping == null) {
            translations.put(language, new HashMap<>(mapping));
        } else {
            existingMapping.putAll(mapping);
        }
        Map<String, Map<String, String>> existingPluginMapping = pluginTranslations.get(plugin);
        if (existingPluginMapping == null) {
            existingPluginMapping = new ConcurrentHashMap<>();
            existingPluginMapping.put(language, new HashMap<>(mapping));
            pluginTranslations.put(plugin, existingPluginMapping);
        } else {
            existingPluginMapping.put(language, new HashMap<>(mapping));
        }
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

    @SuppressWarnings("deprecation")
    private static String getModernTranslationKey(ItemStack itemStack) {
        Material material = itemStack.getType();
        String path = "";

        if (material.isBlock()) {
            path = "block." + material.getKey().getNamespace() + "." + material.getKey().getKey();
        } else {
            path = "item." + material.getKey().getNamespace() + "." + material.getKey().getKey();
        }

        if (itemStack.getType().equals(Material.POTION) || itemStack.getType().equals(Material.SPLASH_POTION) || itemStack.getType().equals(Material.LINGERING_POTION) || itemStack.getType().equals(Material.TIPPED_ARROW)) {
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            String namespace = PotionUtils.getVanillaPotionName(meta.getBasePotionData().getType());
            path += ".effect." + namespace;
        }

        if (itemStack.getType().equals(Material.PLAYER_HEAD)) {
            String owner = NBTEditor.getString(itemStack, "SkullOwner", "Name");
            if (owner != null) {
                path += ".named";
            }
        }

        if (itemStack.getType().equals(Material.SHIELD)) {
            if (NBTEditor.contains(itemStack, "BlockEntityTag")) {
                DyeColor color = DyeColor.WHITE;
                if (!(itemStack.getItemMeta() instanceof BannerMeta)) {
                    if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                        BlockStateMeta bmeta = (BlockStateMeta) itemStack.getItemMeta();
                        if (bmeta.hasBlockState()) {
                            Banner bannerBlockMeta = (Banner) bmeta.getBlockState();
                            color = bannerBlockMeta.getBaseColor();
                        }
                    }
                } else {
                    BannerMeta meta = (BannerMeta) itemStack.getItemMeta();
                    color = meta.getBaseColor();
                }

                path += "." + color.name().toLowerCase();
            }
        }

        return path;
    }

    private static String getLegacyTranslationKey(ItemStack itemStack) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (itemStack.getType().equals(Material.AIR)) {
            if (InteractiveChat.version.isOld()) {
                return "Air";
            } else if (InteractiveChat.version.isOlderThan(MCVersion.V1_11)) {
                return "createWorld.customize.flat.air";
            }
        }
        Object nmsItemStackObject = asNMSCopyMethod.invoke(null, itemStack);
        String path = getRawItemTypeNameMethod.invoke(nmsItemStackObject).toString() + ".name";
        XMaterial xMaterial = XMaterialUtils.matchXMaterial(itemStack);
        if (xMaterial.equals(XMaterial.PLAYER_HEAD)) {
            String owner = NBTEditor.getString(itemStack, "SkullOwner", "Name");
            if (owner != null) {
                path = "item.skull.player.name";
            }
        } else if (xMaterial.isOneOf(Arrays.asList("CONTAINS:banner"))) {
            String color = xMaterial.name().replace("_BANNER", "").toLowerCase();
            Matcher matcher = Pattern.compile("_(.)").matcher(color);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
            }
            matcher.appendTail(sb);
            path = "item.banner." + sb + ".name";

        }
        return path;
    }

    public static Set<String> getLoadedLanguages() {
        return Collections.unmodifiableSet(translations.keySet());
    }

    public static String getTranslation(String translationKey, String language) {
        try {
            Map<String, String> mapping = translations.get(language);
            if (language.equals("en_us")) {
                return mapping.getOrDefault(translationKey, translationKey);
            } else {
                return mapping == null ? getTranslation(translationKey, "en_us") : mapping.getOrDefault(translationKey, getTranslation(translationKey, "en_us"));
            }
        } catch (Exception e) {
            return translationKey;
        }
    }

}
